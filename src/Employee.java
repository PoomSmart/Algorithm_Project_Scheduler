
public class Employee {

	public int id;
	public int types; // representation of job types this employee can handle, one bit per type
	public boolean busy; // whether this employee is busy dealing with a job
	public int work_count; // how many jobs have been done by this employee
	public int type_count; // how many job types this employee can handle
	public String toString;

	public Employee(int d) {
		id = d;
		types = 0;
		busy = false;
		work_count = 0;
		type_count = 0;
		toString = null;
	}

	public boolean capable(int type) {
		return (types & (1 << (type - 1))) != 0;
	}

	public void addType(int type) {
		types |= 1 << (type - 1);
	}

	// sub-linearly count the number of set bits of the given integer
	// example: 00110110 -> 4
	public int countJobTypes() {
		if (type_count != 0)
			return type_count;
		int t = types;
		while (t != 0) {
			t &= t - 1;
			++type_count;
		}
		return type_count;
	}

	public String toStringShort() {
		return String.format("Employee %d", id);
	}

	@Override
	public String toString() {
		if (toString != null)
			return toString;
		StringBuilder sb = new StringBuilder(String.format("Employee %d: ", id));
		for (int i = 1; i <= Scheduler.n_job_types; ++i) {
			if (capable(i)) {
				sb.append(i + " ");
			}
		}
		sb.deleteCharAt(sb.length() - 1);
		return toString = sb.toString();
	}

	@Override
	public int hashCode() {
		return id;
	}
}
