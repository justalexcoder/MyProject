import java.util.Arrays;
import java.util.concurrent.CyclicBarrier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class DiningRoom {

	public final int num;

	private Fork[] forks;
	private Philosopher[] philosophers;

	public DiningRoom(int numOfPhilosophers) {
		this.num = numOfPhilosophers;
	}

	public void prepareForksAndTable() {

		forks = Stream.generate(Fork::create).limit(num).toArray(Fork[]::new);
		System.out.println("Стол накрыт, вилки положены: " + Arrays.toString(forks));
	}

	public void preparePhilosophers() {
		if (forks == null) {
			throw new IllegalStateException();
		}

		final CyclicBarrier cyclicBarrier = new CyclicBarrier(num);
		philosophers = IntStream.range(0, num)
				.mapToObj(i -> new Philosopher(cyclicBarrier, forks[i], forks[(i + 1) % num]))
				.toArray(Philosopher[]::new);

		System.out.println("Философы рассажены: " + Arrays.toString(philosophers));
		System.out.println();
	}

	public void startPhilosophers() {

		for (Philosopher philosopher : philosophers) {
			philosopher.start();
		}
	}

	public void stopPhilosophers() {
		for (Philosopher philosopher : philosophers) {
			philosopher.interrupt();
		}
	}

	public void awaitPhilosophersEnd() {
		for (Philosopher philosopher : philosophers) {
			try {
				philosopher.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void timeWait(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException ignored) {
		}
	}

	public void printStatistics() {
		System.out.println();
		System.out.println("* Статистика распределения обедов *");

		int[] counts = Arrays.stream(philosophers).mapToInt(Philosopher::getDeadPhilosopherLunchCount).toArray();
		int totalCount = Arrays.stream(counts).sum();
		if (totalCount == 0) {
			throw new IllegalStateException("totalCount must not be 0");
		}
		double[] ratios = Arrays.stream(counts).mapToDouble(c -> c * 100d / totalCount).toArray();
		String[] ratiosFormatted = Arrays.stream(ratios).mapToObj(r -> String.format("%.2f%%", r))
				.toArray(String[]::new);

		System.out.printf("Всего обедов: %,d%n", totalCount);
		System.out.println("Распределение между философами:");
		System.out.println("- по количеству:\t" + Arrays.toString(counts));
		System.out.println("- в %-м отношении:\t" + Arrays.toString(ratiosFormatted));
		System.out.println();
	}
}
