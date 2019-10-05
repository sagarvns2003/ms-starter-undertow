package com.vidya.order;

import java.util.List;

public class ProcessOrder {

	public static void handle(final List<String> orderBatch) {
		System.out.println("Order batch size: " + orderBatch.size() + ", received to process.");

		// do with your orders
	}
}