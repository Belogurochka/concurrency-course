import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import ru.home.concurrency.collections.CountDownThread;
import ru.home.concurrency.collections.RestaurantSearchService;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CollectionsTest {

	@Test
	public void RestaurantServiceTest() {
		CountDownLatch countDownLatch = new CountDownLatch(1);
		RestaurantSearchService searchService = new RestaurantSearchService();

		List<String> restaurantsNames = List.of("Burger King", "Marcellis", "MacDonald's", "Osteria Mario", "KFC", "Wine&Crab");

		List<CompletableFuture<Void>> futures = IntStream.rangeClosed(1, 500)
				.boxed()
				.map(i -> CompletableFuture.runAsync(() -> {
					int restIdx = ThreadLocalRandom.current().nextInt(0, restaurantsNames.size() - 1);
					new CountDownThread(() -> searchService.getByName(restaurantsNames.get(restIdx)), String.format("thread-%1d", i), countDownLatch).start();
				}))
				.collect(Collectors.toList());

		countDownLatch.countDown();

		CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
				.completeOnTimeout(null, 3, TimeUnit.SECONDS)
				.thenAccept(t -> {
					if (!futures.stream().allMatch(CompletableFuture::isDone)) {
						throw new AssertionFailedError("Not all thread were completed");
					}
				})
				.join();

		Assertions.assertNotNull(searchService.printStat());
		Assertions.assertTrue(searchService.printStat().size() > 0);
		Assertions.assertTrue(searchService.getTotalCalls() > 0);
	}
}
