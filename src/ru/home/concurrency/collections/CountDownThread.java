package ru.home.concurrency.collections;

import java.util.concurrent.CountDownLatch;

public class CountDownThread extends Thread {

	private CountDownLatch cdl;

	public CountDownThread(Runnable target, String name, CountDownLatch cdl) {
		super(target, name);
		this.cdl = cdl;
	}

	@Override
	public void run() {
		try {
			cdl.await();
			super.run();
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}
}
