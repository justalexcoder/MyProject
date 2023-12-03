import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Fork extends ReentrantLock {

	private static int nextId = 1;

	private final Condition condition;
	private final int id;
	private int lastHolderId;

	protected Fork() {
		super(true);
		id = nextId++;
		lastHolderId = -1;
		condition = this.newCondition();
	}

	public int getId() {
		return id;
	}

	public static Fork create() {
		return new Fork();
	}

	@Override
	public String toString() {
		return "Вилка[" + id + "]";
	}

	public boolean lockByPhilosopher(int philosopherId) {

		super.lock();
		try {
			while (lastHolderId == philosopherId) {
				condition.await();
			}

		} catch (InterruptedException e) {
			super.unlock();
			return false;
		}

		lastHolderId = philosopherId;
		return true;
	}

	public void unlockByPhilosopher(int philosopherId) {
		assert philosopherId == lastHolderId;
		condition.signalAll();
		super.unlock();

		// if (philosopherId == lastHolderId) {
		// try {
		// condition.signalAll(); // сигналим о том, что маркер последнего
		// // держателя ресурса (вилки) lastHolderId изменился,
		// // как можно ближе к освобождению ресурса
		// } finally {
		// super.unlock();
		// }
		// }
	}
}
