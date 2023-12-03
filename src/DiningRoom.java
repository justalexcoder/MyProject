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



	public void timeWait(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException ignored) {
		}
	}


}
