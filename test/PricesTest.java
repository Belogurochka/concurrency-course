import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import ru.home.concurrency.prices.PriceMin;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PricesTest {

	@Test
	public void pricesTest() {
		CountDownLatch countDownLatch = new CountDownLatch(1);
		PriceMin.PriceAggregator priceAggregator = new PriceMin.PriceAggregator();

		List<CompletableFuture<Void>> futures = IntStream.rangeClosed(1, 500)
				.boxed()
				.map(i -> CompletableFuture.runAsync(() -> getMinPrice(countDownLatch, priceAggregator)))
				.collect(Collectors.toList());

		countDownLatch.countDown();

		CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
				.completeOnTimeout(null, 30, TimeUnit.SECONDS)
				.thenAccept(t -> {
					if (!futures.stream().allMatch(CompletableFuture::isDone)) {
						throw new AssertionFailedError("Not all thread were completed");
					}
				})
				.join();
	}

	private void getMinPrice(CountDownLatch countDownLatch, PriceMin.PriceAggregator priceAggregator) {
		try {
			long start = System.currentTimeMillis();
			countDownLatch.await();

			Double min = priceAggregator.getMinPrice(12L);
			long end = System.currentTimeMillis();

			Assertions.assertFalse(Double.isNaN(min), String.format("No prices were fetched for thread = %s", Thread.currentThread().getName()));
			Assertions.assertTrue((end - start) < 3000, String.format("Time is over for thread = %s", Thread.currentThread().getName()));
		} catch (InterruptedException ex) {
			throw new AssertionFailedError("Thread was interrupted");
		}
	}
}
