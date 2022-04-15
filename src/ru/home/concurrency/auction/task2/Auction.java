package ru.home.concurrency.auction.task2;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

public class Auction {
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
				Thread currentThread = Thread.currentThread();
				currentThread.sleep(2000);
				System.out.printf("Notification for bid with id=%1d was sent, thread name = %2s%n", bid.id, currentThread.getName());
			} catch (InterruptedException ex) {
				System.out.println(ex.getMessage());
			}
		}
	}

	private final Notifier notifier = new Notifier();

	private final AtomicReference<Bid> latestBid = new AtomicReference<>();
	private final AtomicBoolean isAuctionStopped = new AtomicBoolean(false);

	private final ExecutorService executorService = Executors.newCachedThreadPool();

	private final ReentrantLock lock = new ReentrantLock();

	private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss.SSSS");

	public boolean propose(Bid bid) {
		if (!isAuctionStopped.get()) {
			return tryProposeBid(bid);
		} else {
			System.out.printf("%1s Auction was stopped%n", dateFormatter.format(LocalDateTime.now()));
		}
		return false;
	}

	private boolean tryProposeBid(Bid bid) {
		try {
			if (lock.tryLock(1, TimeUnit.SECONDS)) {
				try {
					if (latestBid.get() != null) {
						if (bid.price > latestBid.get().price) {
							Bid oldLatestBid = latestBid.getAndSet(bid);
							System.out.printf("%1s Bid with id = %2d was proposed, last bid id = %3d, bid price = %4d%n",
									dateFormatter.format(LocalDateTime.now()), bid.getId(), oldLatestBid.getId(), bid.getPrice());
							CompletableFuture.runAsync(() -> notifier.sendOutdatedMessage(oldLatestBid), executorService);
							return true;
						}
					} else {
						latestBid.getAndSet(bid);
						System.out.printf("%1s Bid with id = %2d was proposed first, price = %3d%n",
								dateFormatter.format(LocalDateTime.now()), bid.getId(), bid.getPrice());
						return true;
					}
				} finally {
					lock.unlock();
				}
				System.out.printf("%1s Bid with id = %2d was not proposed, last bid id = %3d, bid price = %4d%n",
						dateFormatter.format(LocalDateTime.now()), bid.getId(), latestBid.get().getId(), bid.getPrice());
			} else {
				System.out.printf("Can't getting lock for bid with id=%1d%n", bid.id);
			}
		} catch (InterruptedException ex) {
			System.out.printf("Exception while trying getting lock=%1s%n", ex.getMessage());
		}
		return false;
	}

	public Bid getLatestBid() {
		return latestBid.get();
	}

	public void stopAuction() {
		isAuctionStopped.set(true);
	}

	public boolean isAuctionStopped() {
		return isAuctionStopped.get();
	}
}
