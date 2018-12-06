
public class Job {

	public int id;
	public int arrival; // the arrival time of the job
	public int length; // the length of the job
	public int deadline; // the deadline of the job
	public int price; // the cost of the job
	public int begin; // the time that the job is scheduled
	public int finish; // the finish time of the job
	public int type; // the type of the job

	public boolean dequeue; // whether the job should be removed from a priority queue

	public Employee worker;

	public Job(int d) {
		id = d;
		begin = -1;
		finish = -1;
		type = 0;
		worker = null;
		dequeue = false;
	}

	public String toStringShort() {
		return String.format("Job %d", id);
	}

	@Override
	public String toString() {
		return String.format("Job %d (type %d, arrival %d, length %d, deadline %d)", id, type, arrival, length,
				deadline);
	}

	@Override
	public int hashCode() {
		return id;
	}

}
