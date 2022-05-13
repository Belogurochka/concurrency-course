package ru.home.concurrency.fork;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

public class ForkJoinPoolTest {
	/*public static void main(String[] args) throws Exception {
		ForkJoinPool forkJoinPool = new ForkJoinPool(4);
		ForkJoinTask<?> task = forkJoinPool.submit(() -> System.out.println(
				IntStream.range(0, 10000000).average().getAsDouble()
		));

		task.invoke();
	}*/

	public static void main(String[] args) throws Exception{
		ExecutorService executor = Executors.newCachedThreadPool();
		Future f = executor.submit(() -> System.out.println(
				IntStream.range(0,10000000).average().getAsDouble()
		));
		f.get();

		Thread.sleep(60000);
	}
}
