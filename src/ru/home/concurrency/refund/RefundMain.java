package ru.home.concurrency.refund;

public class RefundMain {
	public static void main(String[] args) throws Exception {
		RefundService rs = new RefundService();

		Thread t1 = new Thread(()-> rs.processRefund(1L,2L), "Thread1");
		Thread t2 = new Thread(()-> rs.processRefund(3L,4L), "Thread2");

		t1.start();
		t2.start();
	}
}
