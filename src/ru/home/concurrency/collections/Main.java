package ru.home.concurrency.collections;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {
	private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss.SSSS");

	public static void main(String[] args) {
		RestaurantSearchService searchService = new RestaurantSearchService();
		List<String> restaurantsNames = List.of("Burger King", "Marcellis", "MacDonald's", "Osteria Mario", "KFC", "Wine&Crab");


		CountDownLatch cdl = new CountDownLatch(1);

		List<CompletableFuture<Void>> futures = IntStream.rangeClosed(1, 50)
				.boxed()
				.map(i -> CompletableFuture.runAsync(() -> {
					int restIdx = ThreadLocalRandom.current().nextInt(0, restaurantsNames.size() - 1);
					System.out.printf("%1s create thread number %2d %n", dateFormatter.format(LocalDateTime.now()), i);
					new CountDownThread(() -> searchService.getByName(restaurantsNames.get(restIdx)), String.format("thread-%1d", i), cdl).start();
				}))
				.collect(Collectors.toList());

		cdl.countDown();

		CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();

		searchService.printStat().forEach(stat -> System.out.printf("%1s%n", stat));
		System.out.printf("total calls = %1d%n", searchService.getTotalCalls());
	}
}
