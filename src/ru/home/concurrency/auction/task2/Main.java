package ru.home.concurrency.auction.task2;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {

	public static void main(String[] args) {
		Auction auction = new Auction();

		List<CompletableFuture<Void>> futures = IntStream.rangeClosed(1, 10)
				.boxed()
				.map(i -> {
					if (i.equals(8)) {
						return CompletableFuture.runAsync(auction::stopAuction);
					} else {
						return CompletableFuture.runAsync(() -> proposeBid(auction, i));
					}
				})
				.collect(Collectors.toList());

		CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();

		Auction.Bid latestBid = auction.getLatestBid();
		System.out.printf("Latest bid = %1d%n", latestBid.getId());
	}

	private static void proposeBid(Auction auction, Integer i) {
		long price = ThreadLocalRandom.current().nextLong(1, 10000);

		if (!auction.isAuctionStopped()) {
			Auction.Bid bid = new Auction.Bid(i.longValue(), price);
			auction.propose(bid);
		}
	}
}
