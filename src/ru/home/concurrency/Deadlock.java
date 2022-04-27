package ru.home.concurrency;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Deadlock {
	private static boolean isDone = false;

	public static void main(String[] args) {

		ExecutorService executorService = Executors.newSingleThreadExecutor();
		executorService.submit(() -> {
			while (!isDone) {
				try {
					Thread.sleep(1000);
					System.out.println("we are here");
				} catch (InterruptedException ex) {
					System.out.println(ex.getMessage());
				}
			}
		});

		executorService.submit(() -> {
			isDone = true;
			System.out.println("we are here2");
		});
	}
}
