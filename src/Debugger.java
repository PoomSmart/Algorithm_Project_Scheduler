
public class Debugger {

	private static final boolean debug = false;

	public static void println() {
		println(false);
	}

	public static void println(boolean public_) {
		println(null, public_);
	}

	public static void println(String s) {
		println(s, false);
	}

	public static void println(String s, boolean public_) {
		if (public_ || debug)
			if (s != null)
				System.out.println(s);
			else
				System.out.println();
	}

	public static void print(String s) {
		print(s, false);
	}

	public static void print(String s, boolean public_) {
		if (public_ || debug)
			System.out.print(s);
	}

	public static String repeat(char c, int n) {
		StringBuilder sb = new StringBuilder();
		while (n-- != 0)
			sb.append(c);
		return sb.toString();
	}

}
