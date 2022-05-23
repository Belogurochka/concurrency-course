import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import ru.home.concurrency.prices.PriceMin;

import java.util.concurrent.CountDownLatch;

public class PricesTest {

	@Test
	public void pricesTest() {
		CountDownLatch countDownLatch = new CountDownLatch(1);
		PriceMin.PriceAggregator priceAggregator = new PriceMin.PriceAggregator();

		for (int i = 0; i < 500; i++) {
			new Thread(() -> getMinPrice(countDownLatch, priceAggregator)).start();
		}
		countDownLatch.countDown();
	}

	private void getMinPrice(CountDownLatch countDownLatch, PriceMin.PriceAggregator priceAggregator) {
		try {
			countDownLatch.await();

			long start = System.currentTimeMillis();
			Double min = priceAggregator.getMinPrice(12L);
			long end = System.currentTimeMillis();

			Assertions.assertFalse(Double.isNaN(min), String.format("No prices were fetched for thread = %s", Thread.currentThread().getName()));
			Assertions.assertTrue((end - start) < 3000, String.format("Time is over for thread = %s", Thread.currentThread().getName()));
		} catch (InterruptedException ex) {
			throw new AssertionFailedError("Thread was interrupted");
		}
	}
}
