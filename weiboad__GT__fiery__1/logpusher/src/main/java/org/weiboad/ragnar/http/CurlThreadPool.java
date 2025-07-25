package org.weiboad.ragnar.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CurlThreadPool {

	private Logger log = LoggerFactory.getLogger(CurlThread.class);

	private BlockingQueue<String> sendBizLogQueue;

	private BlockingQueue<String> sendMetaLogQueue;

	private String host;

	private Integer maxthread;

	private ArrayList<CurlThread> threadList = new ArrayList<>();

	public CurlThreadPool(String host, BlockingQueue<String> sendBizLogQueue, BlockingQueue<String> sendMetaLogQueue,
			Integer maxthread) {
		this.host = host;
		this.sendBizLogQueue = sendBizLogQueue;
		this.sendMetaLogQueue = sendMetaLogQueue;
		this.maxthread = maxthread;
	}

	public void start() {
		for (Integer threadIndex = 0; threadIndex < maxthread; threadIndex++) {
			CurlThread curlThread = new CurlThread(host, sendBizLogQueue, sendMetaLogQueue);
			curlThread.start();
			threadList.add(curlThread);
		}
	}

}
