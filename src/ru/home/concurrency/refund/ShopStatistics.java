package ru.home.concurrency.refund;

public class ShopStatistics {
	private volatile  Long totalCount = 10L;
	private Long totalRevenue = 10L;

	public synchronized void addData(Long count, Long price) {
		totalCount += count;
		totalRevenue += (price * count);
	}

	public synchronized Long getTotalCount() {
		return totalCount;
	}

	public synchronized Long getTotalRevenue() {
		return totalRevenue;
	}

	public synchronized void reset() {
		totalCount = 0L;
		totalRevenue = 0L;
	}
}
