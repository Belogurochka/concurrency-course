package ru.home.concurrency;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class PriceMin {

	public static void main(String[] args) {
		PriceAggregator priceAggregator = new PriceAggregator();
		long itemId = 12L;

		long start = System.currentTimeMillis();
		double min = priceAggregator.getMinPrice(itemId);
		long end = System.currentTimeMillis();

		System.out.println(min != Double.MAX_VALUE ? String.format("Min price = %f", min) : "No prices were fetched");
		System.out.println((end - start) < 3000); // should be true
	}

	public static class PriceRetriever {

		public double getPrice(long itemId, long shopId) {
			// имитация долгого HTTP-запроса
			int delay = ThreadLocalRandom.current().nextInt(10);
			try {
				Thread.sleep(delay * 1000);
			} catch (InterruptedException e) {
				System.out.println(e.getMessage());
			}
			return ThreadLocalRandom.current().nextDouble(1000);
		}
	}

	public static class PriceAggregator {

		private PriceRetriever priceRetriever = new PriceRetriever();
		private Set<Long> shopIds = Set.of(10L, 45L, 66L, 345L, 234L, 333L, 67L, 123L, 768L, 831L);

		public double getMinPrice(long itemId) {
			AtomicReference<Double> result = new AtomicReference<>(Double.MAX_VALUE);
			List<CompletableFuture<Double>> futures = new ArrayList<>();

			shopIds.forEach(shop -> futures.add(CompletableFuture.supplyAsync(() -> priceRetriever.getPrice(itemId, shop))));

			try {
				CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
						.completeOnTimeout(null, 2900, TimeUnit.MILLISECONDS)
						.thenAccept(t ->
								futures.forEach(future -> {
									try {
										if (future.isDone()) {
											result.set(Math.min(result.get(), future.get()));
										}
									} catch (Exception ex) {
										System.out.printf("Exception while getting price = %1s%n", ex.getMessage());
									}
								})).get();
			} catch (Exception ex) {
				System.out.printf("Exception while getting min price = %1s%n", ex.getMessage());
			}
			return result.get();
		}
	}
}
