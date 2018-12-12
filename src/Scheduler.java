import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;

public class Scheduler {

	public static final int n_jobs = Constants.n_jobs;
	public static final int n_job_types = Constants.n_job_types;
	public static final int n_employees = Constants.n_employees;

	public static final int lower = Constants.lower;
	public static final int upper = Constants.upper;

	public static enum JobSortingType {
		DeadlineThenPrice, TimeToDeadlineThenPrice, PriceOverTimeToDeadline
	};

	public JobSortingType jobSortingType;

	public ArrayList<Job> jobs; // a list of jobs
	public ArrayList<Employee> es; // a list of employees

	public int max_deadline; // maximum deadline among all jobs
	public int current_time; // current time
	public int total_time; // total time used for scheduling
	public int profit; // total profit from scheduled jobs
	public int ideal_profit; // maximum profit possible
	public int jobs_done; // number of scheduled jobs

	private int max_job_types = 0;

	public Scheduler(JobSortingType type) {
		jobs = new ArrayList<Job>();
		es = new ArrayList<Employee>();
		max_deadline = 0;
		current_time = 0;
		total_time = 0;
		profit = 0;
		ideal_profit = 0;
		jobs_done = 0;
		jobSortingType = type;
	}

	public void generateJobs() {
		for (int i = 0; i < n_jobs; ++i) {
			Job job = new Job(i + 1);
			job.arrival = Randomizer.rand(lower, upper * 4);
			job.length = Randomizer.rand(lower, upper) / 2;
			job.deadline = job.arrival + job.length + Randomizer.rand(lower, upper);
			max_deadline = Math.max(max_deadline, job.deadline);
			job.price = (lower + Randomizer.rand(0, upper / 2)) * 10;
			ideal_profit += job.price;
			job.type = Randomizer.rand(1, n_job_types);
			Debugger.println(job.toString(), true);
			jobs.add(job);
		}
		Debugger.println("Sort jobs by arrival time");
		Collections.sort(jobs, new Comparator<Job>() {
			@Override
			public int compare(Job j1, Job j2) {
				return j1.arrival - j2.arrival;
			}
		});
	}

	public void generateEmployees() {
		for (int i = 0; i < n_employees; ++i) {
			Employee e = new Employee(i + 1);
			for (int j = 1; j <= n_job_types; ++j) {
				if (Randomizer.rand(0, 4) > 1) {
					e.addType(j);
				}
			}
			if (e.types == 0) {
				e.addType(Randomizer.rand(1, n_job_types));
			}
			Debugger.println(e.toString(), true);
			es.add(e);
		}
	}

	public void ct_println(String s, boolean public_) {
		Debugger.println(String.format("ct %d %s", current_time, s), public_);
	}

	public void ct_println(String s) {
		ct_println(s, false);
	}

	public Employee bestEmployeeFor(Job job) {
		int type = job.type;
		PriorityQueue<Employee> candidates = new PriorityQueue<Employee>(n_employees, new Comparator<Employee>() {
			@Override
			public int compare(Employee e1, Employee e2) {
				// employee with least job types comes first
				return e1.countJobTypes() - e2.countJobTypes();
			}
		});
		ct_println("Find candidates for " + job.toStringShort() + " of type " + job.type);
		for (Employee e : es) {
			// check if this employee can work this job and is not busy
			if (e.capable(type) && !e.busy)
				candidates.add(e);
		}
		// the first employee in the queue is the best candidate
		return candidates.peek();
	}

	public void schedule() {
		current_time = jobs.get(0).arrival; // fast forward to the very first job that arrived
		PriorityQueue<Job> qjobs = new PriorityQueue<Job>(n_jobs, new Comparator<Job>() {
			@Override
			public int compare(Job j1, Job j2) {
				switch (jobSortingType) {
				case DeadlineThenPrice:
					if (j1.deadline == j2.deadline)
						return j2.price - j1.price;
					return j1.deadline - j2.deadline;
				case TimeToDeadlineThenPrice:
					int t1 = j1.deadline - current_time;
					int t2 = j2.deadline - current_time;
					if (t1 == t2)
						return j2.price - j1.price;
					return t1 - t2;
				case PriceOverTimeToDeadline:
					int tt1 = j1.deadline - current_time;
					int tt2 = j2.deadline - current_time;
					if (tt1 < 0 && tt2 > 0)
						return 1;
					if (tt1 > 0 && tt2 < 0)
						return -1;
					double r1 = (double) j1.price / (tt1 + 1);
					double r2 = (double) j2.price / (tt2 + 1);
					return Double.compare(r2, r1);
				}
				return 0;
			}

		});
		Job last_job = jobs.get(0); // store the last scheduled job
		qjobs.add(last_job); // the first job is pushed into a priority queue
		ct_println(last_job.toStringShort() + " has arrived");
		int idx = 1;
		boolean everScheduled = false;
		while (current_time < max_deadline) {
			Debugger.println("ct " + current_time);
			// if at this time there are jobs arrived, push them all
			while (idx < n_jobs && current_time == jobs.get(idx).arrival) {
				ct_println(jobs.get(idx).toStringShort() + " has arrived");
				qjobs.add(jobs.get(idx++));
			}
			// if there is any job in the queue to process
			if (!qjobs.isEmpty()) {
				Job job = qjobs.peek(); // retrieve the front-most job
				// the lord says this job must be removed, serve so and process the next job
				if (job.dequeue) {
					qjobs.poll();
					continue;
				}
				// if at this time the job exceeded its deadline, remove it from the queue
				if (job.finish == -1 && (current_time > job.deadline || current_time + job.length > job.deadline)) {
					job.begin = -1; // mark the job as unscheduled
					ct_println(job.toStringShort() + " exceeded the deadline of " + job.deadline);
					qjobs.poll();
					continue;
				}
				// we now find the best employee for the job
				Employee e = bestEmployeeFor(job);
				if (e != null) {
					// if there is no such employee available, don't come in here
					ct_println(job.toStringShort() + " will be handled by " + e.toStringShort());
					e.busy = true; // mark the employee as busy
					++e.work_count; // increment the number of jobs done for the employee
					job.begin = current_time; // mark the job as scheduled
					job.worker = e; // set who is responsible for this job
					profit += job.price; // this job will be done, add the profit
					qjobs.poll(); // we are done with this job
					everScheduled = true;
				}
			}
			// O(n) logic to ensure jobs that must be dequeued are dequeued
			// should have been improved
			for (int i = 0; i < n_jobs; ++i) {
				Job job = jobs.get(i);
				if (job.dequeue)
					continue;
				if (job.finish == -1 && (current_time > job.deadline || current_time + job.length > job.deadline)) {
					// same as above, but we can't remove the job now / at such specified index on a
					// priority queue
					job.begin = -1;
					ct_println(job.toStringShort() + " exceeded the deadline of " + job.deadline);
					job.dequeue = true;
				}
				if (!job.dequeue && job.worker != null && current_time == job.begin + job.length) {
					// if at this time the job is finished
					job.finish = current_time; // mark the finish time of the job
					job.dequeue = true; // the job will be removed
					job.worker.busy = false; // the respective employee will be free
					ct_println(String.format("Free %s from %s", job.worker.toStringShort(), job.toStringShort()));
					if (last_job == null || (last_job != null && job.finish > last_job.finish))
						last_job = job; // this is the most recent job that is scheduled
				}
			}
			++current_time;
		}
		if (!everScheduled)
			last_job = null;
		total_time = last_job != null ? last_job.finish : 0;
	}

	public void printJobs() {
		for (Job job : jobs) {
			Debugger.println(job.toString());
		}
	}

	public void visualize() {
		int i = 0;
		for (Job job : jobs) {
			if (job.begin == -1)
				continue; // this job is not scheduled at all, skip
			++jobs_done;
			StringBuilder lane = new StringBuilder();
			lane.append(String.format("%" + (job.begin + (i > 0 ? 1 : 0)) + "s", " ") + "|");
			lane.append(String.format("%s| %d", Debugger.repeat('-', job.length), job.id));
			if (job.deadline != job.finish)
				lane.append(String.format("%" + (job.deadline - job.finish) + "s", " "));
			lane.append(String.format("D - %s", job.worker.toStringShort()));
			Debugger.println(lane.toString(), true);
			++i;
		}
	}

	public void prepare() {
		generateJobs();
		generateEmployees();
	}

	public void calculateEmployeeUtilization() {
		ArrayList<Integer> workUtilization = new ArrayList<Integer>();
		ArrayList<Color> colors = new ArrayList<Color>();
		for (Employee e : es) {
			workUtilization.add(e.work_count);
			max_job_types = Math.max(max_job_types, e.countJobTypes());
		}
		for (Employee e : es) {
			colors.add(new Color(55 + (int) (200 * ((double) e.countJobTypes() / max_job_types)), 0, 0, 255));
		}
		GraphPanel.constructGraph("Employee Utilization", workUtilization, colors);
	}

	public void report() {
		Debugger.println("Total time " + total_time, true);
		Debugger.println(String.format("Total profit %d / %d (%.2f%%)", profit, ideal_profit,
				100 * ((double) profit / ideal_profit)), true);
		Debugger.println(String.format("Total jobs done %d / %d (%.2f%%)", jobs_done, n_jobs,
				100 * ((double) jobs_done / n_jobs)), true);
	}

	public static void main(String[] args) {
		Scheduler scheduler = new Scheduler(JobSortingType.PriceOverTimeToDeadline);
		scheduler.prepare();
		scheduler.schedule();
		scheduler.printJobs();
		scheduler.visualize();
		scheduler.calculateEmployeeUtilization();
		scheduler.report();
	}

}
