package ru.home.concurrency.hadoop.refactoring;

import java.util.List;
import java.util.concurrent.CountedCompleter;
import java.util.concurrent.atomic.AtomicInteger;

public class RefreshTask extends CountedCompleter {
	private static final int THRESHOLD = 10;
	private final List<MountTableRefresher> refreshList;
	private final int start, end;
	private final AtomicInteger successCount;
	private final AtomicInteger failureCount;
	private final Others.LoadingCache<String, Others.RouterClient> routerClientsCache;

	public RefreshTask(CountedCompleter parentTask, List<MountTableRefresher> refreshList, int start, int end,
					   AtomicInteger successCount, AtomicInteger failureCount, Others.LoadingCache<String, Others.RouterClient> routerClientsCache) {
		super(parentTask);
		this.refreshList = refreshList;
		this.start = start;
		this.end = end;
		this.successCount = successCount;
		this.failureCount = failureCount;
		this.routerClientsCache = routerClientsCache;
	}

	@Override
	public void compute() {
		if (end - start <= THRESHOLD) {
			refresh(refreshList, start, end);
		} else {
			int middle = start + (end - start) / 2;
			addToPendingCount(2);

			RefreshTask subTask1 = new RefreshTask(this, refreshList, start, middle, successCount, failureCount, routerClientsCache);
			RefreshTask subTask2 = new RefreshTask(this, refreshList, middle, end, successCount, failureCount, routerClientsCache);

			invokeAll(subTask1, subTask2);
		}

		tryComplete();
	}

	private void refresh(List<MountTableRefresher> refreshList, int start, int end) {
		for (int i = start; i < end; i++) {
			MountTableRefresher task = refreshList.get(i);
			task.refresh();
			if (task.isSuccess()) {
				successCount.getAndIncrement();
			} else {
				failureCount.getAndIncrement();
				removeFromCache(task.getAdminAddress());
			}
			System.out.printf("Refresh %d%n", i);
		}
	}

	private void removeFromCache(String adminAddress) {
		routerClientsCache.invalidate(adminAddress);
	}
}
