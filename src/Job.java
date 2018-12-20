
public class Job {

	public int id;
	public int arrival; // the arrival time of the job
	public int length; // the length of the job
	public int deadline; // the deadline of the job
	public int price; // the cost of the job
	public int begin; // the time that the job is scheduled
	public int finish; // the finish time of the job
	public int type; // the type of the job
	public String toString;

	public boolean dequeue; // whether the job should be removed from a priority queue

	public Employee worker;

	public Job(int d) {
		id = d;
		arrival = -1;
		begin = -1;
		finish = -1;
		deadline = -1;
		price = 0;
		type = 0;
		worker = null;
		dequeue = false;
		toString = null;
	}

	public String toStringShort() {
		return String.format("Job %d", id);
	}

	@Override
	public String toString() {
		if (toString != null)
			return toString;
		return toString = String.format("Job %d (type %d, arrival %d, length %d, deadline %d, price %d)", id, type, arrival, length,
				deadline, price);
	}

	@Override
	public int hashCode() {
		return id;
	}

}
