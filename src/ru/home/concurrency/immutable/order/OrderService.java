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
		currentOrders.compute(cartId, (id, order) -> {
			Order paymentOrder = order.withPaymentInfo(paymentInfo);
			if (paymentOrder.checkStatus()) {
				deliver(paymentOrder);
				return paymentOrder.withStatus(Status.DELIVERED);
			} else {
				return paymentOrder;
			}
		});
	}

	public void setPacked(long cartId) {
		currentOrders.compute(cartId, (id, order) -> {
			Order packedOrder = order.withPacked(true);
			if (packedOrder.checkStatus()) {
				deliver(packedOrder);
				return packedOrder.withStatus(Status.DELIVERED);
			} else {
				return packedOrder;
			}
		});
	}

	private void deliver(Order order) {
		System.out.printf("order %d wad delivered%n", order.getId());
	}
}
