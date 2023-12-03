public class PhilosopherSettings {

	public static long lunchDurationMs = 500L;
	public static int numOfLunches = 3;
	public static long maxThinkingDurationMs = 1000L;

	public static boolean printMessages = true;
	public static boolean enableBalancing = true; // включает балансировку распределения обеденного времени между
													// потоками путём запрета потоку обедать дважды подряд
}
