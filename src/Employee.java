
public class Employee {

	public int id;
	public int types;
	public boolean busy;
	public int work_count;
	public int type_count;

	public Employee(int d) {
		id = d;
		types = 0;
		busy = false;
		work_count = 0;
		type_count = 0;
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
		StringBuilder sb = new StringBuilder(String.format("Employee %d is capable of ", id));
		for (int i = 1; i <= Scheduler.n_job_types; ++i) {
			if (capable(i)) {
				sb.append(i + " ");
			}
		}
		return sb.toString();
	}

	@Override
	public int hashCode() {
		return id;
	}
}
