package ru.home.concurrency;

import java.util.concurrent.ConcurrentHashMap;

public class VolatileTest {

	public static void main(String[] args) {
		Test testClass = new Test();

		Thread t1 = new Thread(()-> testClass.update(4), "Thread1");
		Thread t2 = new Thread(()-> testClass.update(1),"Thread2");

		t2.start();
		t1.start();
	}


	private static class Test {
		volatile int latestValue = 0;

		private void update(int value) {
			latestValue = latestValue + value;
			System.out.println(latestValue);
		}
	}
}
