package pedca.utility;

import java.util.Random;

public abstract class CASimRandom {

	public static Random generator = new Random(Constants.RANDOM_SEED);

	public static double nextDouble() {
		return generator.nextDouble();
	}

	public static int nextInt(int limit) {
		return generator.nextInt(limit);
	}

	public static void reset(long seed) {
		generator = new Random(seed);
	}

}
