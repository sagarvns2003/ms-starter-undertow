package com.vidya;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import javax.servlet.ServletException;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.vidya.channel.MyChannel;
import com.vidya.order.ProcessOrder;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;

public class AppStarter {

	private int port;
	private String host;

	public AppStarter() {
		this.port = 8080;
		this.host = "localhost";
	}

	public static void main(String[] args) throws Exception {

		AppStarter app = new AppStarter();
		app.startServer();
		app.placeOrder();
		app.retreiveOrder();
	}

	public void placeOrder() {

		CompletableFuture.runAsync(() -> {
			int i = 1;
			while (true) {
				try {
					String orderID = "OrderID_" + i;
					MyChannel.orderChannel.put(orderID);
					System.out.println("Order placed with order id: " + orderID);
					// Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				i++;
			}
		});
	}

	public void retreiveOrder() {
		CompletableFuture.runAsync(() -> {
			Stream.generate(() -> {
				try {
					List<String> orderBatch = Lists.newArrayList();
					Queues.drain(MyChannel.orderChannel, orderBatch, 100, Duration.ofMillis(200));
					return orderBatch;
				} catch (Exception e) {
					System.err.print(e.getMessage());
					// Return blank list object
					return Lists.<String>newArrayList();
				}
			}).filter(b -> !b.isEmpty())
			.forEach(ProcessOrder::handle);
		});
	}
	
	
	public void startServer() throws ServletException {
		DeploymentInfo servletBuilder = Servlets.deployment().setClassLoader(AppStarter.class.getClassLoader())
				.setContextPath("/").setDeploymentName("vidya");

		DeploymentManager manager = Servlets.defaultContainer().addDeployment(servletBuilder);
		manager.deploy();

		PathHandler path = Handlers.path(manager.start());

		Undertow server = Undertow.builder().addHttpListener(port, host).setHandler(path).build();
		server.start();

		System.out.println("Server started at... http://" + host + ":" + port);
	}
}