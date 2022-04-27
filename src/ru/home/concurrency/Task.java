package ru.home.concurrency;

public class Task implements Runnable {
	private static final ThreadLocal<Integer> value = ThreadLocal.withInitial(() -> 0);

	@Override
	public void run() {
		Integer currentValue = value.get();
		value.set(currentValue + 1);
		System.out.println(value.get());
	}
}
