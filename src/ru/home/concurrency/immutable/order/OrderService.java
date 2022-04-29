package ru.home.concurrency.immutable.order;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class OrderService {
	private Map<Long, Order> currentOrders = new ConcurrentHashMap<>();
	private AtomicLong nextId = new AtomicLong(0);

	public long createOrder(List<Item> items) {
		return currentOrders.computeIfAbsent(nextId.getAndIncrement(), newId -> new Order(newId, items)).getId();
	}

	public void updatePaymentInfo(long cartId, PaymentInfo paymentInfo) {
		Order order = currentOrders.get(cartId).withPaymentInfo(paymentInfo);
		checkOrderStatus(order, cartId);
	}

	public void setPacked(long cartId) {
		Order order = currentOrders.get(cartId).withPacked(true);
		checkOrderStatus(order, cartId);
	}

	private void checkOrderStatus(Order order, long id) {
		if (order.checkStatus()) {
			deliver(order);
			currentOrders.put(id, order.withStatus(Status.DELIVERED));
		} else {
			currentOrders.put(id, order);
		}
	}

	private void deliver(Order order) {
		System.out.printf("order %d wad delivered%n", order.getId());
	}
}
