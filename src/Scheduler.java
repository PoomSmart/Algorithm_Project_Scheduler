import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class Scheduler {

	public static final int n_jobs = Constants.n_jobs;
	public static final int n_job_types = Constants.n_job_types;
	public static final int n_employees = Constants.n_employees;

	public static final int lower = Constants.lower;
	public static final int upper = Constants.upper;

	private static final boolean clear_old_data = false;
	private static final boolean vanilla_operate = true;

	public static enum JobSortingType {
		Price, DeadlineThenPrice, TimeToDeadlineThenPrice, PriceOverTimeToDeadline
	};

	public JobSortingType jobSortingType;

	public ArrayList<Job> jobs; // a list of jobs
	public ArrayList<Employee> es; // a list of employees

	public int current_time; // current time
	public int max_known_deadline; // maximum job deadline known to the scheduler
	public int total_time; // total time used for scheduling
	public int profit; // total profit from scheduled jobs
	public int ideal_profit; // maximum profit possible
	public int jobs_done; // number of scheduled jobs

	private int max_job_types = 0;

	public Scheduler(JobSortingType type) {
		jobs = new ArrayList<Job>();
		es = new ArrayList<Employee>();
		current_time = 0;
		max_known_deadline = 0;
		total_time = 0;
		profit = 0;
		ideal_profit = 0;
		jobs_done = 0;
		jobSortingType = type;
	}

	public void looseCopyFrom(Scheduler s) {
		for (Job j : s.jobs) {
			Job jc = new Job(-j.id);
			jc.type = j.type;
			jc.length = j.length;
			jc.arrival = j.arrival;
			jc.deadline = j.deadline;
			jc.price = j.price;
			jobs.add(jc);
		}
		for (Employee e : s.es) {
			Employee ec = new Employee(-e.id);
			ec.types = e.types;
			ec.type_count = e.type_count;
			es.add(ec);
		}
		ideal_profit = s.ideal_profit;
		max_job_types = s.max_job_types;
		jobSortingType = s.jobSortingType;
	}

	public <T> void saveData(String fileName, List<T> data) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(fileName + ".txt")));
			writer.write(data.size() + "\n");
			for (T o : data) {
				writer.write(o.toString() + "\n");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean readJobs(String fileName) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(fileName + ".txt")));
			String line;
			if (n_jobs == Integer.parseInt(reader.readLine())) {
				while ((line = reader.readLine()) != null) {
					line = line.replaceAll("[A-Za-z:]+", "");
					line = line.replaceAll("\\s+", " ").substring(1);
					String[] tokens = line.split(" ");
					Job job = new Job(Integer.parseInt(tokens[0]));
					job.type = Integer.parseInt(tokens[1]);
					job.arrival = Integer.parseInt(tokens[2]);
					job.length = Integer.parseInt(tokens[3]);
					job.deadline = Integer.parseInt(tokens[4]);
					job.price = Integer.parseInt(tokens[5]);
					ideal_profit += job.price;
					Debugger.println(job.toString(), true);
					jobs.add(job);
				}
				reader.close();
			} else {
				reader.close();
				return false;
			}
			sortJobs();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void generateJobs() {
		if (clear_old_data || !readJobs("jobs")) {
			for (int i = 0; i < n_jobs; ++i) {
				Job job = new Job(i + 1);
				job.type = Randomizer.rand(1, n_job_types);
				job.arrival = Randomizer.rand(lower, upper * 4);
				job.length = Randomizer.rand(lower, upper) / 2;
				job.deadline = job.arrival + job.length + Randomizer.rand(lower, upper);
				job.price = (lower + Randomizer.rand(0, upper / 2)) * 10;
				ideal_profit += job.price;
				Debugger.println(job.toString(), true);
				jobs.add(job);
			}
			saveData("jobs", jobs);
			sortJobs();
		}
	}

	public void sortJobs() {
		Debugger.println("Sort jobs by arrival time");
		Collections.sort(jobs, new Comparator<Job>() {
			@Override
			public int compare(Job j1, Job j2) {
				return j1.arrival - j2.arrival;
			}
		});
	}

	public boolean readEmployees(String fileName) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(fileName + ".txt")));
			if (n_employees == Integer.parseInt(reader.readLine())) {
				String line;
				while ((line = reader.readLine()) != null) {
					line = line.replaceAll("[A-Za-z:]+", "");
					line = line.replaceAll("\\s+", " ").substring(1);
					String[] tokens = line.split(" ");
					Employee e = new Employee(Integer.parseInt(tokens[0]));
					for (int i = 1; i < tokens.length; ++i) {
						e.addType(Integer.parseInt(tokens[i]));
					}
					max_job_types = Math.max(max_job_types, e.countJobTypes());
					Debugger.println(e.toString(), true);
					es.add(e);
				}
				reader.close();
				return true;
			} else {
				reader.close();
				return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void generateEmployees() {
		if (clear_old_data || !readEmployees("employees")) {
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
				max_job_types = Math.max(max_job_types, e.countJobTypes());
				Debugger.println(e.toString(), true);
				es.add(e);
			}
			saveData("employees", es);
		}
	}

	public void ct_println(String s, boolean public_) {
		Debugger.println(String.format("ct %d %s", current_time, s), public_);
	}

	public void ct_println(String s) {
		ct_println(s, false);
	}

	// Complexity: O(n_employees * log(n_employees))
	public Employee bestEmployeeFor(Job job) {
		// priority queue for employee candidates: O(n_employees * log(n_employees))
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
			if (e.capable(job.type) && !e.busy)
				candidates.add(e);
		}
		Debugger.println("Best employee for " + job.toString() + " is " + candidates);
		// the first employee in the queue is the best candidate
		return candidates.peek();
	}

	public int jobCompare(Job j1, Job j2) {
		switch (jobSortingType) {
		case Price:
			return Integer.compare(j2.price, j1.price);
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

	// Estimated complexity: max(n_jobs, max{job_deadline}) * (
	// n_jobs * log^2(n_jobs) +
	// n_employees * log(n_employees) +
	// n_jobs)
	// O(max{nj, jd} * max{nj*log^2(nj), ne*log(ne)})
	public void schedule() {
		current_time = jobs.get(0).arrival; // fast forward to the very first job that arrived
		// priority queue for jobs: O(n_jobs * log(n_jobs))
		PriorityQueue<Job> qjobs = new PriorityQueue<Job>(n_jobs, new Comparator<Job>() {
			@Override
			public int compare(Job j1, Job j2) {
				return jobCompare(j1, j2);
			}

		});
		Job last_job = jobs.get(0); // store the last scheduled job
		max_known_deadline = last_job.deadline;
		qjobs.add(last_job); // the first job is pushed into a priority queue
		ct_println(last_job.toStringShort() + " has arrived");
		int idx = 1;
		boolean everScheduled = false;
		// The algorithm proceeds if there are more jobs to see or the maximum deadline
		// so far is yet to exceed
		// Complexity: O(max(n_jobs, max{job_deadline}))
		while (idx < n_jobs || current_time < max_known_deadline) {
			Debugger.println("ct " + current_time);
			// if at this time there are jobs arrived, push them all
			// Practically, there will not be too many jobs arrived at the same time
			// O(log(n_jobs) * n_jobs * log(n_jobs)) = O(n_jobs * log^2(n_jobs))
			while (idx < n_jobs && current_time == jobs.get(idx).arrival) {
				Job job = jobs.get(idx);
				max_known_deadline = Math.max(max_known_deadline, job.deadline);
				ct_println(job.toStringShort() + " has arrived");
				qjobs.add(job);
				++idx;
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
					job.worker = null; // no worker should be assigned
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
					qjobs.poll(); // we are done with this job
					everScheduled = true;
				}
			}
			// O(n_jobs) logic to ensure jobs that must be dequeued are dequeued
			// should have been improved
			for (int i = 0; i < n_jobs; ++i) {
				Job job = jobs.get(i);
				if (job.dequeue)
					continue;
				if (job.finish == -1 && (current_time > job.deadline || current_time + job.length > job.deadline)) {
					// same as above, but we can't remove the job now / at such specified index on a
					// priority queue
					job.begin = -1; // mark the job as unscheduled
					job.worker = null; // no worker should be assigned
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
		jobs_done = profit = 0;
		for (Job job : jobs) {
			if (job.begin == -1)
				continue; // this job is not scheduled at all, skip
			++jobs_done;
			profit += job.price; // this job will be done, add the profit
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

	public void calculateEmployeeUtilization(String suffix) {
		ArrayList<Integer> workUtilization = new ArrayList<Integer>();
		ArrayList<Color> colors = new ArrayList<Color>();
		for (Employee e : es) {
			workUtilization.add(e.work_count);
			colors.add(new Color(55 + (int) (200 * ((double) e.countJobTypes() / max_job_types)), 0, 0, 255));
		}
		GraphPanel.constructGraph("Employee Utilization" + " " + suffix, workUtilization, colors);
	}

	public void calculateEmployeeUtilization() {
		calculateEmployeeUtilization("");
	}

	public void report() {
		Debugger.println("Total time " + total_time, true);
		Debugger.println(String.format("Total profit %d / %d (%.2f%%)", profit, ideal_profit,
				100 * ((double) profit / ideal_profit)), true);
		Debugger.println(String.format("Total jobs done %d / %d (%.2f%%)", jobs_done, n_jobs,
				100 * ((double) jobs_done / n_jobs)), true);
	}

	public int operate() {
		schedule();
		printJobs();
		visualize();
		report();
		return profit << 16 | jobs_done;
	}

	static class ArbitraryScheduler extends Scheduler {

		public int p;
		public boolean vanilla_find_employee;

		public ArbitraryScheduler(JobSortingType type) {
			super(type);
		}

		@Override
		public Employee bestEmployeeFor(Job job) {
			if (vanilla_find_employee)
				return super.bestEmployeeFor(job);
			ArrayList<Employee> employees = new ArrayList<Employee>();
			for (Employee e : es) {
				if (e.capable(job.type) && !e.busy) {
					employees.add(e);
				}
			}
			Employee candidate = employees.isEmpty() ? null : employees.get(Randomizer.rand(0, employees.size() - 1));
			employees = null;
			return candidate;
		}

		@Override
		public void calculateEmployeeUtilization() {
			calculateEmployeeUtilization(p + "");
		}

		@Override
		public int operate() {
			if (vanilla_operate)
				return super.operate();
			Debugger.enabled = false;
			schedule();
			printJobs();
			visualize();
			Debugger.enabled = true;
			report();
			return profit << 16 | jobs_done;
		}

	}

	public static void main(String[] args) {
		JobSortingType[] jobSortingTypes = { JobSortingType.Price, JobSortingType.DeadlineThenPrice,
				JobSortingType.PriceOverTimeToDeadline, JobSortingType.TimeToDeadlineThenPrice };
		int iterations = 60;
		for (JobSortingType jobSortingType : jobSortingTypes) {
			Scheduler s = new Scheduler(jobSortingType);
			Debugger.enabled = false;
			s.prepare();
			Debugger.enabled = true;
			s.operate();
			// s.calculateEmployeeUtilization();
			ArrayList<Integer> profits = new ArrayList<Integer>();
			for (int i = 1; i <= iterations; ++i) {
				profits.add(s.profit);
			}
			int max_profit = s.profit;
			int max_p = 0;
			ArrayList<Integer> a_profits = new ArrayList<Integer>();
			ArrayList<Integer> average_profits = new ArrayList<Integer>();
			ArbitraryScheduler as, best_as = null;
			double average_a_profit = 0;
			Debugger.enabled = false;
			for (int i = 1; i <= iterations; ++i) {
				as = new ArbitraryScheduler(jobSortingType);
				as.p = i;
				as.jobSortingType = jobSortingType;
				as.vanilla_find_employee = false;
				as.looseCopyFrom(s);
				Debugger.println("Permutation: " + as.p, true);
				int a_profit = as.operate() >> 16;
				average_a_profit += a_profit;
				if (a_profit > max_profit) {
					max_profit = a_profit;
					max_p = as.p;
					best_as = as;
				} else
					as = null;
				a_profits.add(a_profit);
			}
			Debugger.enabled = true;
			if (max_p != 0) {
				System.out.println("Max Permutation: " + max_p);
				System.out.println("Profit: " + max_profit + " versus " + s.profit);
				best_as.visualize();
				best_as.report();
				// best_as.calculateEmployeeUtilization();
			}
			average_a_profit /= iterations;
			int int_average_a_profit = (int) average_a_profit;
			for (int i = 1; i <= iterations; ++i) {
				average_profits.add(int_average_a_profit);
			}
			List<List<Integer>> all_profits = new ArrayList<>();
			all_profits.add(profits);
			all_profits.add(a_profits);
			all_profits.add(average_profits);
			List<Color> all_line_colors = new ArrayList<>();
			all_line_colors.add(Color.BLUE);
			all_line_colors.add(Color.PINK);
			all_line_colors.add(Color.ORANGE);
			GraphPanel._constructGraphs(jobSortingType + " Permutation versus Reality", all_profits, null,
					all_line_colors);
		}
	}

}
