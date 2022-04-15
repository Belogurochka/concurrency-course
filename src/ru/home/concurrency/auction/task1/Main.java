package ru.home.concurrency.auction.task1;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {

	public static void main(String[] args) {
		Auction auction = new Auction();

		List<CompletableFuture<Boolean>> futures = IntStream.rangeClosed(1, 10)
				.boxed()
				.map(i -> CompletableFuture.supplyAsync(() -> {
					long price = ThreadLocalRandom.current().nextLong(1, 10000);
					Auction.Bid bid = new Auction.Bid(i.longValue(), price);
					return auction.propose(bid);
				}))
				.collect(Collectors.toList());

		CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();

		Auction.Bid latestBid = auction.getLatestBid();
		System.out.printf("Latest bid = %1d%n", latestBid.getId());
	}
}
