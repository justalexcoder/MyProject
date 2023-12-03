import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ThreadLocalRandom;

public class Philosopher extends Thread {

	private static final String NAME_PREFIX_STRING = "Философ ";

	private static int nextId = 1;

	private final CyclicBarrier cyclicBarrier;
	private final int id;
	private final Fork leftFork; // для toString() нужно знать
	private final Fork rightFork; // какая слева, какая справа

	private final Fork firstLock;
	private final Fork secondLock;

	private int lunchCount = 0;

	public int getDeadPhilosopherLunchCount() {
		return lunchCount;
	}

	public Philosopher(CyclicBarrier cyclicBarrier, Fork leftFork, Fork rightFork) {
		super(NAME_PREFIX_STRING + nextId);
		this.id = nextId++;
		this.cyclicBarrier = cyclicBarrier;
		this.leftFork = leftFork;
		this.rightFork = rightFork;
		if (leftFork.getId() < rightFork.getId()) {
			this.firstLock = leftFork;
			this.secondLock = rightFork;
		} else {
			this.firstLock = rightFork;
			this.secondLock = leftFork;
		}
	}

	@Override
	public void run() {

		print("начинает свой жизненный цикл.");

		try {
			cyclicBarrier.await();
		} catch (InterruptedException | BrokenBarrierException e) {
			print("остановлен едва начавшись.");
			return;
		}

		while ((PhilosopherSettings.numOfLunches < 0 || PhilosopherSettings.numOfLunches > lunchCount)
				&& !Thread.interrupted()) {

			if (!takeFork(firstLock)) {
				break;
			}
			if (!takeFork(secondLock)) {
				releaseFork(firstLock);
				break;
			}

			try {
				lunch();
			} catch (InterruptedException e) {
				break;
			} finally {
				releaseFork(secondLock);
				releaseFork(firstLock);
			}

			try {
				think();
			} catch (InterruptedException e) {
				break;
			}
		}

		print("закончил свой жизненный цикл.");
	}

	boolean takeFork(Fork fork) {
	}

	void releaseFork(Fork fork) {
	}

	void lunch() throws InterruptedException {
		print("приступил к еде.");
		if (PhilosopherSettings.lunchDurationMs > 0) {
			Thread.sleep(PhilosopherSettings.lunchDurationMs);
		}
		print("закончил есть.");
		++lunchCount;
	}

	void think() throws InterruptedException {
		print("размышляет.");
		if (PhilosopherSettings.maxThinkingDurationMs > 0) {
			long d = 1 + ThreadLocalRandom.current().nextLong(PhilosopherSettings.maxThinkingDurationMs);
			Thread.sleep(d);
		}
		print("закончил размышлять.");
	}

	private void print(String msg) {
		if (PhilosopherSettings.printMessages) {
			System.out.println(getName() + " " + msg);
		}
	}
}
