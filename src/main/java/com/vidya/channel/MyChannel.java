package com.vidya.channel;

import java.util.concurrent.BlockingQueue;

import com.google.common.collect.Queues;

public class MyChannel {
	public static volatile BlockingQueue<String> orderChannel = Queues.newArrayBlockingQueue(10000);

}