package ru.home.concurrency.collections;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class RestaurantSearchService {
	private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss.SSSS");
	private Map<String, Integer> stat = new ConcurrentHashMap<>();
	private Map<String, Restaurant> restaurants = new ConcurrentHashMap<>();
	private AtomicInteger totalCalls = new AtomicInteger(0);

	public Restaurant getByName(String restaurantName) {
		addToStat(restaurantName);
		totalCalls.getAndIncrement();
		System.out.printf("%1s start thread with name %2s%n", dateFormatter.format(LocalDateTime.now()), Thread.currentThread().getName());
		return restaurants.computeIfAbsent(restaurantName, k -> new Restaurant(restaurantName));
	}

	private void addToStat(String restaurantName) {
		//stat.computeIfAbsent(restaurantName, k->new AtomicInteger(0)).getAndIncrement();

		/*Integer num = stat.putIfAbsent(restaurantName, 0);
		if (num != null) {
			stat.computeIfPresent(restaurantName, (name, nm) -> nm++);
		}*/

		stat.merge(restaurantName, 1, (oldNum, newNum) -> {
			oldNum = oldNum + newNum;
			return oldNum;
		});
	}

	public Set<String> printStat() {
		return stat.entrySet().stream().map(entry -> String.format("%1s - %2d", entry.getKey(), entry.getValue())).collect(Collectors.toSet());
	}

	public int getTotalCalls() {
		return totalCalls.get();
	}
}
