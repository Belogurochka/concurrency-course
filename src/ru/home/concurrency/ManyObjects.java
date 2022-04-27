package ru.home.concurrency;

import java.time.Duration;

public class ManyObjects {
	public static void main(String[] args) {

		long start = System.currentTimeMillis();

		final int size = 50_000_000;

		Object[] objects = new Object[size];
		for (int i = 0; i < size; ++i) {
			objects[i] = new Object();
		}

		long end = System.currentTimeMillis();

		System.out.println(end - start); //1225
	}
}
