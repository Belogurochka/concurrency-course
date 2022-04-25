package ru.home.concurrency.collections;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class RestaurantSearchService {
	private Map<String, AtomicInteger> stat = new ConcurrentHashMap<>();
	private Map<String, Restaurant> restaurants = new ConcurrentHashMap<>();
	private AtomicInteger totalCalls = new AtomicInteger(0);

	public Restaurant getByName(String restaurantName) {
		addToStat(restaurantName);
		totalCalls.getAndIncrement();
		return restaurants.computeIfAbsent(restaurantName, k -> new Restaurant(restaurantName));
	}

	private void addToStat(String restaurantName) {
		stat.computeIfAbsent(restaurantName, k -> new AtomicInteger(0)).getAndIncrement();
	}

	public Set<String> printStat() {
		return stat.entrySet().stream().map(entry -> String.format("%1s - %2d", entry.getKey(), entry.getValue().get())).collect(Collectors.toSet());
	}

	public int getTotalCalls(){
		return totalCalls.get();
	}
}
