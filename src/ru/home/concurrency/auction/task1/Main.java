package ru.home.concurrency.auction.task1;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {

	public static void main(String[] args) {
		AuctionOptimistic auction = new AuctionOptimistic();

		List<CompletableFuture<Boolean>> futures = IntStream.rangeClosed(1, 10)
				.boxed()
				.map(i -> CompletableFuture.supplyAsync(() -> {
					long price = ThreadLocalRandom.current().nextLong(1, 10000);
					AuctionOptimistic.Bid bid = new AuctionOptimistic.Bid(i.longValue(), price);
					return auction.propose(bid);
				}))
				.collect(Collectors.toList());

		CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();

		AuctionOptimistic.Bid latestBid = auction.getLatestBid();
		System.out.printf("Latest bid = %1d%n", latestBid.getId());
	}
}
