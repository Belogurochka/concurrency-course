package ru.home.concurrency.hadoop.refactoring;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MountTableRefresherService {
	private Others.RouterStore routerStore = new Others.RouterStore();
	private long cacheUpdateTimeout;

	/**
	 * All router admin clients cached. So no need to create the client again and
	 * again. Router admin address(host:port) is used as key to cache RouterClient
	 * objects.
	 */
	private Others.LoadingCache<String, Others.RouterClient> routerClientsCache;

	/**
	 * Removes expired RouterClient from routerClientsCache.
	 */
	private ScheduledExecutorService clientCacheCleanerScheduler;

	public void serviceInit() {
		long routerClientMaxLiveTime = 15L;
		this.cacheUpdateTimeout = 10L;
		routerClientsCache = new Others.LoadingCache<>();
		routerStore.getCachedRecords().stream().map(Others.RouterState::getAdminAddress)
				.forEach(addr -> routerClientsCache.add(addr, new Others.RouterClient()));

		initClientCacheCleaner(routerClientMaxLiveTime);
	}

	public void serviceStop() {
		clientCacheCleanerScheduler.shutdown();
		// remove and close all admin clients
		routerClientsCache.cleanUp();
	}

	private void initClientCacheCleaner(long routerClientMaxLiveTime) {
		ThreadFactory tf = r -> {
			Thread t = new Thread();
			t.setName("MountTableRefresh_ClientsCacheCleaner");
			t.setDaemon(true);
			return t;
		};

		clientCacheCleanerScheduler = Executors.newSingleThreadScheduledExecutor(tf);
		/*
		 * When cleanUp() method is called, expired RouterClient will be removed and
		 * closed.
		 */
		clientCacheCleanerScheduler.scheduleWithFixedDelay(
				() -> routerClientsCache.cleanUp(), routerClientMaxLiveTime,
				routerClientMaxLiveTime, TimeUnit.MILLISECONDS);
	}

	/**
	 * Refresh mount table cache of this router as well as all other routers.
	 */
	public void refresh() {
		List<MountTableRefresher> refreshTasks = routerStore.getCachedRecords()
				.stream()
				.map(this::changeState)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());

		if (!refreshTasks.isEmpty()) {
			refresh(refreshTasks);
		}
	}

	private void refresh(List<MountTableRefresher> refreshTasks) {
		AtomicInteger successCount = new AtomicInteger(0);
		AtomicInteger failureCount = new AtomicInteger(0);

		/*RefreshTask refreshTask = new RefreshTask(null, refreshTasks, 0, refreshTasks.size(), successCount, failureCount, routerClientsCache);
		CompletableFuture.runAsync(refreshTask::compute)
				.completeOnTimeout(null, cacheUpdateTimeout, TimeUnit.MILLISECONDS)
				.thenAccept(t -> {
					if (!refreshTask.isDone()) {
						System.out.println("Not all router admins updated their cache");
					}
				})
				.join();*/

		List<CompletableFuture<Void>> tasks = refreshTasks.stream().map(task -> CompletableFuture.supplyAsync(task::refresh)
				.completeOnTimeout(false, cacheUpdateTimeout, TimeUnit.MILLISECONDS)
				.thenAccept(t -> {
					if (t) {
						successCount.getAndIncrement();
					} else {
						failureCount.getAndIncrement();
					}
				})).collect(Collectors.toList());

		CompletableFuture.allOf(tasks.toArray(CompletableFuture[]::new))
				.completeOnTimeout(null, cacheUpdateTimeout, TimeUnit.MILLISECONDS)
				.thenAccept(t -> {
					if (!tasks.stream().allMatch(CompletableFuture::isDone)) {
						System.out.println("Not all router admins updated their cache");
					}
				})
				.join();
		System.out.printf("Mount table entries cache refresh successCount=%d,failureCount=%d%n", successCount.get(), failureCount.get());
	}

	private MountTableRefresher changeState(Others.RouterState routerState) {
		String adminAddress = routerState.getAdminAddress();
		if (adminAddress == null || adminAddress.length() == 0) {
			return null;
		}
		if (isLocalAdmin(adminAddress)) {
			return getLocalRefresher(adminAddress);
		} else {
			return new MountTableRefresher(new Others.MountTableManager(adminAddress), adminAddress);
		}
	}

	protected MountTableRefresher getLocalRefresher(String adminAddress) {
		return new MountTableRefresher(new Others.MountTableManager("local"), adminAddress);
	}

	private boolean isLocalAdmin(String adminAddress) {
		return adminAddress.contains("local");
	}

	public static void main(String[] args) throws InterruptedException {
		MountTableRefresherService service = new MountTableRefresherService();
		service.serviceInit();

		service.refresh();

		Thread.sleep(2000);
		System.out.println("done");
		service.serviceStop();
	}
}
