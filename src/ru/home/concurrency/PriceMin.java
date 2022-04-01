package ru.home.concurrency;


import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PriceMin {

	public static void main(String[] args) {
		PriceAggregator priceAggregator = new PriceAggregator();
		long itemId = 12L;

		long start = System.currentTimeMillis();
		Double min = priceAggregator.getMinPrice(itemId);
		long end = System.currentTimeMillis();

		System.out.println(Double.isNaN(min) ? "No prices were fetched" : String.format("Min price = %f", min));
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

		public Double getMinPrice(long itemId) {

			List<CompletableFuture<Double>> futures = shopIds.stream()
					.map(shopId -> CompletableFuture.supplyAsync(() -> priceRetriever.getPrice(itemId, shopId)))
					.collect(Collectors.toList());

			return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
					.completeOnTimeout(null, 2900, TimeUnit.MILLISECONDS)
					.thenApply(t -> futures.stream()
							.filter(CompletableFuture::isDone)
							.mapToDouble(CompletableFuture::join)
							.filter(Objects::nonNull)
							.min()
							.orElse(Double.NaN))
					.join();
		}
	}
}
