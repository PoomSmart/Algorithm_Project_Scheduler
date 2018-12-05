
public class Job {

	public int id;
	public int arrival;
	public int length;
	public int deadline;
	public int price;
	public int begin;
	public int finish;
	public int type;

	public boolean dequeue;

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

	public String toString() {
		return String.format("Job %d (type %d, arrival %d, length %d, deadline %d)", id, type, arrival, length,
				deadline);
	}

	@Override
	public int hashCode() {
		return id;
	}

}
