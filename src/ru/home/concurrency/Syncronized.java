package ru.home.concurrency;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantLock;

public class Syncronized {

	public static void main(String[] args) throws Exception {
		/*Set<Integer> testSet = new HashSet<>();
		testSet.add(1);
		testSet.add(2);

		Test testClass = new Test(testSet);
		Thread t1 = new Thread(testClass::update, "Thread1");
		t1.start();

		Thread.sleep(1000);

		Thread t2 = new Thread(testClass::clear,"Thread2");
		t2.start();*/

		/*Test2 testClass = new Test2();
		Thread t1 = new Thread(testClass::read, "Thread1");
		t1.start();

		//Thread.sleep(1000);

		Thread t2 = new Thread(()-> testClass.update(11),"Thread2");
		t2.start();*/

		Test3 testClass = new Test3();
		Thread t1 = new Thread(testClass::read, "Thread1");


		//Thread.sleep(1000);

		Thread t2 = new Thread(()-> testClass.update(11),"Thread2");
		t1.start();
		t2.start();

		Thread t3 = new Thread(testClass::read, "Thread3");
		t3.start();

	}

	public static class Test {
		Set<Integer> set;

		public Test(Set<Integer> set) {
			this.set = set;
		}
		private String l;
		private ExecutorService s = Executors.newCachedThreadPool();

		public synchronized void update() {
			synchronized (set) {
				try {
					System.out.println(String.format("Start get update %1s", Thread.currentThread().getName()));
					for (int i = 0; i < 1000000000; ++i) {
						set.add(ThreadLocalRandom.current().nextInt(10));
					}
					System.out.println(String.format("End get update %1s, %2d", Thread.currentThread().getName(), set.size()));
					Thread.currentThread().interrupt();
				} catch (Exception ex) {
					System.out.println(ex.getMessage());
				}
			}
		}

		public void clear() {
			System.out.println(String.format("Start get clear %1s", Thread.currentThread().getName()));
			set.clear();
			System.out.println(String.format("End get clear %1s, %2s", Thread.currentThread().getName(), set.size()));
		}
	}

	public static class Test2 {
		int value = 0;
		private final Object readLock = new Object();
		private final Object writeLock = new Object();

		public int read() {
			synchronized (readLock) {
				System.out.println(String.format("Start get read %1s, %2d", Thread.currentThread().getName(), value));
				return value;
			}
		}

		public void update(int value) {
			synchronized (writeLock) {
				System.out.println(String.format("Start get write %1s, %2d", Thread.currentThread().getName(), value));
				this.value = value;
			}
		}
	}

	public static class Test3 {
		int value = 0;
		private final Object readLock = new Object();
		private final Object writeLock = new Object();

		public int read() {
			synchronized (readLock) {
				System.out.println(String.format("Start get read %1s, %2d", Thread.currentThread().getName(), value));
				return value;
			}
		}

		public void update(int value) {
			synchronized (writeLock) {
				System.out.println(String.format("Start get update %1s", Thread.currentThread().getName()));
				this.value = value;
			}
		}
	}
}
