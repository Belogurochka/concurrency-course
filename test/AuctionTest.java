import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import ru.home.concurrency.auction.task1.AuctionOptimistic;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AuctionTest {

	@Test
	public void auctionTest() {
		CountDownLatch countDownLatch = new CountDownLatch(1);
		AuctionOptimistic auction = new AuctionOptimistic();

		long maxPrice = 100500;

		List<CompletableFuture<Void>> futures = IntStream.rangeClosed(1, 500)
				.boxed()
				.map(i -> CompletableFuture.runAsync(() -> {
					long price = (i.equals(450)) ? maxPrice : ThreadLocalRandom.current().nextLong(1, 10000);
					proposeAuction(countDownLatch, i, auction, price);
				}))
				.collect(Collectors.toList());

		countDownLatch.countDown();

		CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
				.completeOnTimeout(null, 3000, TimeUnit.SECONDS)
				.thenAccept(t -> {
					if (!futures.stream().allMatch(CompletableFuture::isDone)) {
						throw new AssertionFailedError("Not all thread were completed");
					}
				})
				.join();


		AuctionOptimistic.Bid latestBid = auction.getLatestBid();
		Assertions.assertEquals(maxPrice, latestBid.getPrice(), String.format("Latest bid price is not matches = %d", latestBid.getPrice()));
	}

	private void proposeAuction(CountDownLatch countDownLatch, int i, AuctionOptimistic auction, long price) {
		try {
			countDownLatch.await();

			AuctionOptimistic.Bid bid = new AuctionOptimistic.Bid((long) i, price);
			auction.propose(bid);

		} catch (InterruptedException ex) {
			throw new AssertionFailedError(String.format("Thread with name = %s was interrupted", Thread.currentThread().getName()));
		}
	}
}
