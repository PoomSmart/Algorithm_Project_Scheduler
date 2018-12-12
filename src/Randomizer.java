import java.util.Random;

public class Randomizer {

	private static Random random = new Random();

	public static int rand(int lower, int upper) {
		return lower + random.nextInt(upper - lower + 1);
	}

}
