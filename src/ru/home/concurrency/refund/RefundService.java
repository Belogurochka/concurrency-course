package ru.home.concurrency.refund;

public class RefundService {
	private ShopStatistics stat = new ShopStatistics();

	public synchronized void processRefund(Long count, Long price) {

		synchronized (stat) {
			Long currentCount = stat.getTotalCount();
			Long currentRevenue = stat.getTotalRevenue();
			System.out.println(String.format("Start get refund %1s, count= %2d, revenue =%3d", Thread.currentThread().getName(), currentCount, currentRevenue));
			stat.reset();
			stat.addData(currentCount - count, currentRevenue - price);
			System.out.println(String.format("End get refund %1s, count= %2d, revenue =%3d", Thread.currentThread().getName(), stat.getTotalCount(), stat.getTotalRevenue()));
		}
	}
}
