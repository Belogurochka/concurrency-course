package ru.home.concurrency.immutable.order;

import java.util.Collections;
import java.util.List;

public class Order {

	private final Long id;
	private final List<Item> items;
	private PaymentInfo paymentInfo;
	private boolean isPacked;
	private Status status;

	private Order(Long id, List<Item> items, PaymentInfo paymentInfo, boolean isPacked, Status status) {
		this.id = id;
		this.items = items;
		this.paymentInfo = paymentInfo;
		this.isPacked = isPacked;
		this.status = status;
	}

	public Order(Long id, List<Item> items) {
		this.id = id;
		this.items = Collections.unmodifiableList(items);
	}

	public Order withPaymentInfo(PaymentInfo paymentInfo) {
		return new Order(this.id, this.items, paymentInfo, this.isPacked, this.status);
	}

	public Order withPacked(boolean isPacked) {
		return new Order(this.id, this.items, this.paymentInfo, isPacked, this.status);
	}

	public Order withStatus(Status status) {
		return new Order(this.id, this.items, this.paymentInfo, this.isPacked, status);
	}

	public boolean checkStatus() {
		return items != null && !items.isEmpty() && paymentInfo != null && isPacked;
	}

	public Long getId() {
		return id;
	}
}
