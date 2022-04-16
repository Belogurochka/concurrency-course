package ru.home.concurrency.auction.task1;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.StampedLock;

public class AuctionOptimistic {
	public static class Bid {
		private Long id;
		private Long participantId;
		private Long price;

		public Bid(Long id, Long price) {
			this.id = id;
			this.price = price;
		}

		public Long getId() {
			return id;
		}

		public Long getPrice() {
			return price;
		}
	}

	public static class Notifier {
		public void sendOutdatedMessage(Bid bid) {
			try {
				Thread.currentThread().sleep(2000);
				System.out.printf("Notification for bid with id=%1d was sent, thread name = %2s%n", bid.id, Thread.currentThread().getName());
			} catch (InterruptedException ex) {
				System.out.println(ex.getMessage());
			}
		}
	}

	private final Notifier notifier = new Notifier();

	private volatile AtomicReference<Bid> latestBid = new AtomicReference<>();

	private final ExecutorService executorService = Executors.newCachedThreadPool();

	private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss.SSSS");

	public boolean propose(Bid bid) {
		Bid expected, newValue;

		do {
			expected = latestBid.get();
			newValue = expected;

			if (expected != null) {
				if (bid.price > expected.price) {
					newValue = bid;
				}
			} else {
				newValue = bid;
			}
		} while (!latestBid.compareAndSet(expected, newValue));

		if (newValue.getId().equals(bid.getId())) {
			if (expected == null) {
				System.out.printf("%1s Bid with id = %2d was proposed first, price = %3d%n",
						dateFormatter.format(LocalDateTime.now()), bid.getId(), bid.getPrice());
			} else {
				sendMessage(expected);
				System.out.printf("%1s Bid with id = %2d was proposed, last bid id = %3d, bid price = %4d%n",
						dateFormatter.format(LocalDateTime.now()), bid.getId(), expected.getId(), bid.getPrice());
			}
			return true;
		} else {
			System.out.printf("%1s Bid with id = %2d was not proposed, last bid id = %3d, bid price = %4d%n",
					dateFormatter.format(LocalDateTime.now()), bid.getId(), expected.getId(), bid.getPrice());
			return false;
		}
	}

	private void sendMessage(Bid oldLatestBid) {
		CompletableFuture.runAsync(() -> notifier.sendOutdatedMessage(oldLatestBid), executorService);
	}

	public Bid getLatestBid() {
		return latestBid.get();
	}
}
