/**

 * Copyright 2012 Comcast Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.comcast.cqs.controller;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.AsyncContext;

import org.apache.log4j.Logger;

import com.comcast.cmb.common.controller.CMBControllerServlet;
import com.comcast.cmb.common.util.CMBProperties;
import com.comcast.cmb.common.util.RollingWindowCapture;
import com.comcast.cmb.common.util.RollingWindowCapture.PayLoad;
import com.comcast.cqs.persistence.RedisCachedCassandraPersistence;

/**
 * Implement the monitoring for CQS
 * @author aseem, bwolf
 *
 * Class is thread-safe
 */
public class CQSMonitor implements CQSMonitorMBean {

	private final static CQSMonitor Inst = new CQSMonitor();

	private static Logger logger = Logger.getLogger(CQSMonitor.class);

	private CQSMonitor() {
	}

	public static CQSMonitor getInstance() {
		return Inst;
	}

	public enum CacheType {
		QCache, PayloadCache;
	}

	static class MessageNumberDynamicPayLoad extends PayLoad {

		public final AtomicInteger numMessages;
		public final AtomicInteger numRequested;
		public volatile long timeStamp = System.currentTimeMillis();

		public MessageNumberDynamicPayLoad(int numMessages) {
			this.numMessages = new AtomicInteger(numMessages);
			numRequested = new AtomicInteger(0);
		}

		public MessageNumberDynamicPayLoad(int numMessages, int numRequested) {
			this.numMessages = new AtomicInteger(numMessages);
			this.numRequested = new AtomicInteger(numRequested);
		}

		public void addNumMessage(int num) {
			numMessages.addAndGet(num);
		}

		public void addNumRequested(int num) {
			numRequested.addAndGet(num);
		}
	}

	ConcurrentHashMap<String, RollingWindowCapture<MessageNumberDynamicPayLoad>> numMessagesRetRW = new ConcurrentHashMap<String, RollingWindowCapture<MessageNumberDynamicPayLoad>>();
	ConcurrentHashMap<String, RollingWindowCapture<MessageNumberDynamicPayLoad>> numMessagesRecRW = new ConcurrentHashMap<String, RollingWindowCapture<MessageNumberDynamicPayLoad>>();
	ConcurrentHashMap<String, RollingWindowCapture<MessageNumberDynamicPayLoad>> cacheHitRatioRW = new ConcurrentHashMap<String, RollingWindowCapture<MessageNumberDynamicPayLoad>>();
	ConcurrentHashMap<String, RollingWindowCapture<MessageNumberDynamicPayLoad>> pCacheHitRatioRW = new ConcurrentHashMap<String, RollingWindowCapture<MessageNumberDynamicPayLoad>>();
	ConcurrentHashMap<String, RollingWindowCapture<MessageNumberDynamicPayLoad>> numMessagesRw = new ConcurrentHashMap<String, RollingWindowCapture<MessageNumberDynamicPayLoad>>();
	ConcurrentHashMap<String, RollingWindowCapture<MessageNumberDynamicPayLoad>> numMessagesDeleted = new ConcurrentHashMap<String, RollingWindowCapture<MessageNumberDynamicPayLoad>>();
	ConcurrentHashMap<String, RollingWindowCapture<MessageNumberDynamicPayLoad>> numEmptyRespRW = new ConcurrentHashMap<String, RollingWindowCapture<MessageNumberDynamicPayLoad>>();

	/**
	 * Note should only be called by unit-tests
	 */
	public void clearAllState() {
		numMessagesRetRW.clear();
		numMessagesRecRW.clear();
		cacheHitRatioRW.clear();
		pCacheHitRatioRW.clear();
		numMessagesRw.clear();
		numMessagesDeleted.clear();
	}

	class CountMessagesDynamicVisitor implements RollingWindowCapture.Visitor<MessageNumberDynamicPayLoad> {

		int num = 0;

		public void processNode(MessageNumberDynamicPayLoad n) {
			num += n.numMessages.get();
		}
	}

	private int getNumberOfMessages(String queueUrl,
			ConcurrentHashMap<String, RollingWindowCapture<MessageNumberDynamicPayLoad>> rwAll) {

		CountMessagesDynamicVisitor v = new CountMessagesDynamicVisitor();
		RollingWindowCapture<MessageNumberDynamicPayLoad> rw = rwAll.get(queueUrl);

		if (rw == null) {
			return 0;
		}

		rw.visitAllNodes(v);
		return v.num;
	}

	private void addNumberOfMessages(String queueUrl, int numMessages,
			ConcurrentHashMap<String, RollingWindowCapture<MessageNumberDynamicPayLoad>> rwAll) {

		RollingWindowCapture<MessageNumberDynamicPayLoad> rw = rwAll.get(queueUrl);

		if (rw == null) {

			rw = new RollingWindowCapture<MessageNumberDynamicPayLoad>(
					CMBProperties.getInstance().getRollingWindowTimeSec(), 1000);
			RollingWindowCapture<MessageNumberDynamicPayLoad> prev = rwAll.putIfAbsent(queueUrl, rw);

			if (prev != null) {
				rw = prev;
			}
		}

		//check if the head of queue was created less than 60 seconds back. If so, just add count to that bucket

		MessageNumberDynamicPayLoad currentHead = rw.getLatestPayload();

		if (currentHead == null || System.currentTimeMillis() - currentHead.timeStamp > 60000L) {
			rw.addNow(new MessageNumberDynamicPayLoad(numMessages));
		} else {
			currentHead.numMessages.addAndGet(numMessages);
		}
	}

	@Override
	public int getNumberOfMessagesReturned(String queueUrl) {
		return getNumberOfMessages(queueUrl, numMessagesRetRW);
	}

	/**
	 * @param numMessages The number of messages returned by the server as a result of the ReceiveMessage() call.
	 */
	public void addNumberOfMessagesReturned(String queueUrl, int numMessages) {
		addNumberOfMessages(queueUrl, numMessages, numMessagesRetRW);
		addNumberOfMessages(queueUrl, numMessages * -1, numMessagesRw); //should delete from num
		addNumberOfMessages(queueUrl, numMessages, numMessagesDeleted);
	}

	@Override
	public int getNumberOfMessagesReceived(String queueUrl) {
		return getNumberOfMessages(queueUrl, numMessagesRecRW);
	}

	/**
	 * @param numMessages The number of messages returned by the server as a result of the SendMessage() call.
	 */
	public void addNumberOfMessagesReceived(String queueUrl, int numMessages) {
		addNumberOfMessages(queueUrl, numMessages, numMessagesRecRW);
		addNumberOfMessages(queueUrl, numMessages, numMessagesRw);
	}

	@Override
	public int getNumberOpenRedisConnections() {
		return RedisCachedCassandraPersistence.getInstance().getNumRedisConnections();
	}

	/**
	 * Add in buckets of 1 minute. Rolling window would get rid of an entire bucket
	 * @param queueUrl
	 * @param numHit
	 * @param numRequested
	 * @param qOrPayload
	 */
	public void registerCacheHit(String queueUrl, int numHit, int numRequested, CacheType cacheType) {

		ConcurrentHashMap<String, RollingWindowCapture<MessageNumberDynamicPayLoad>> hm = cacheType == CacheType.QCache
				? cacheHitRatioRW
				: pCacheHitRatioRW;
		RollingWindowCapture<MessageNumberDynamicPayLoad> rw = hm.get(queueUrl);

		if (rw == null) {

			rw = new RollingWindowCapture<MessageNumberDynamicPayLoad>(
					CMBProperties.getInstance().getRollingWindowTimeSec(), 1000);
			RollingWindowCapture<MessageNumberDynamicPayLoad> prev = hm.putIfAbsent(queueUrl, rw);

			if (prev != null) {
				rw = prev;
			}
		}

		//check if the head of queue was created less than 60 seconds back. If so, just add count to that bucket

		MessageNumberDynamicPayLoad currentHead = rw.getLatestPayload();

		if (currentHead == null || System.currentTimeMillis() - currentHead.timeStamp > 60000L) {
			rw.addNow(new MessageNumberDynamicPayLoad(numHit, numRequested));
		} else {
			currentHead.numMessages.addAndGet(numHit);
			currentHead.numRequested.addAndGet(numRequested);
		}
	}

	private class PercentageVisitor implements RollingWindowCapture.Visitor<MessageNumberDynamicPayLoad> {

		int hitCount = 0;
		int totalCount = 0;

		public void processNode(MessageNumberDynamicPayLoad n) {
			hitCount += n.numMessages.get();
			totalCount += n.numRequested.get();
		}
	}

	public int getCacheHitPercent(String queueUrl, CacheType cacheType) {

		ConcurrentHashMap<String, RollingWindowCapture<MessageNumberDynamicPayLoad>> hm = cacheType == CacheType.QCache
				? cacheHitRatioRW
				: pCacheHitRatioRW;
		PercentageVisitor v = new PercentageVisitor();
		RollingWindowCapture<MessageNumberDynamicPayLoad> rw = hm.get(queueUrl);

		if (rw == null) {
			return 0;
		}

		rw.visitAllNodes(v);

		if (v.totalCount == 0) {
			return 0;
		}

		return (int) (((float) v.hitCount / (float) v.totalCount) * 100);
	}

	@Override
	public int getReceiveMessageCacheHitPercent(String queueUrl) {
		return getCacheHitPercent(queueUrl, CacheType.QCache);
	}

	@Override
	public int getGetMessagePayloadCacheHitPercent(String queueUrl) {
		return getCacheHitPercent(queueUrl, CacheType.PayloadCache);
	}

	@Override
	public int getNumberOfMessagesInQueue(String queueUrl) {
		return getNumberOfMessages(queueUrl, numMessagesRw);
	}

	@Override
	public int getNumberOfMessagesDeleted(String queueUrl) {
		return getNumberOfMessages(queueUrl, numMessagesDeleted);
	}

	@Override
	public int getNumberOfMessagesMarkedInvisible(String queueUrl) {
		return getNumberOfMessagesReturned(queueUrl);
	}

	@Override
	public int getNumEmptyResponses(String queueUrl) {
		return getNumberOfMessages(queueUrl, numEmptyRespRW);
	}

	public void registerEmptyResp(String queueUrl, int num) {
		addNumberOfMessages(queueUrl, num, numEmptyRespRW);
	}

	@Override
	public int getNumberOfMessages(String queueUrl) {
		RedisCachedCassandraPersistence redisP = RedisCachedCassandraPersistence.getInstance();
		return (int) redisP.getQueueMessageCount(queueUrl, false);
	}

	@Override
	public Long getOldestMessageCreatedTSMS(String queueUrl) {

		RedisCachedCassandraPersistence redisP = RedisCachedCassandraPersistence.getInstance();
		List<String> ids = redisP.getIdsFromHead(queueUrl, 1);

		if (ids.size() == 0) {
			return null;
		}

		return RedisCachedCassandraPersistence.getMemQueueMessageCreatedTS(ids.get(0));
	}

	@Override
	public long getNumberOfActivelyPendingLongPollReceives() {

		if (CQSLongPollReceiver.contextQueues == null) {
			return 0;
		}

		long count = 0;

		for (String queueArn : CQSLongPollReceiver.contextQueues.keySet()) {

			Iterator<AsyncContext> iter = CQSLongPollReceiver.contextQueues.get(queueArn).iterator();

			while (iter.hasNext()) {

				AsyncContext asyncContext = iter.next();

				if (asyncContext.getRequest() instanceof CQSHttpServletRequest) {

					CQSHttpServletRequest request = ((CQSHttpServletRequest) (asyncContext.getRequest()));

					if (request.isActive() && System.currentTimeMillis()
							- request.getRequestReceivedTimestamp() <= request.getWaitTime()) {
						count++;
					}
				}
			}
		}

		return count;
	}

	@Override
	public long getNumberOfLongPollReceives() {

		if (CQSLongPollReceiver.contextQueues == null) {
			return 0;
		}

		long count = 0;

		for (String queueArn : CQSLongPollReceiver.contextQueues.keySet()) {
			count += CQSLongPollReceiver.contextQueues.get(queueArn).size();
		}

		return count;
	}

	@Override
	public long getNumberOfDeadLongPollReceives() {

		if (CQSLongPollReceiver.contextQueues == null) {
			return 0;
		}

		long count = 0;

		for (String queueArn : CQSLongPollReceiver.contextQueues.keySet()) {

			Iterator<AsyncContext> iter = CQSLongPollReceiver.contextQueues.get(queueArn).iterator();

			while (iter.hasNext()) {

				AsyncContext asyncContext = iter.next();

				if (asyncContext.getRequest() instanceof CQSHttpServletRequest) {

					CQSHttpServletRequest request = ((CQSHttpServletRequest) (asyncContext.getRequest()));

					if (!request.isActive() || System.currentTimeMillis()
							- request.getRequestReceivedTimestamp() > request.getWaitTime()) {
						count++;
					}
				}
			}
		}

		return count;
	}

	@Override
	public long getNumberOfLongPollReceivesForQueue(String queueArn) {

		if (CQSLongPollReceiver.contextQueues == null) {
			return 0;
		}

		return CQSLongPollReceiver.contextQueues.get(queueArn).size();
	}

	@Override
	public int getNumberOfRedisShards() {
		return RedisCachedCassandraPersistence.getNumberOfShards();
	}

	@Override
	public List<Map<String, String>> getRedisShardInfos() {
		return RedisCachedCassandraPersistence.getInfo();
	}

	@Override
	public void flushRedis() {
		RedisCachedCassandraPersistence.flushAll();
	}

	@Override
	public Map<String, AtomicLong> getCallStats() {
		return CMBControllerServlet.callStats;
	}

	@Override
	public Map<String, AtomicLong> getCallFailureStats() {
		return CMBControllerServlet.callFailureStats;
	}

	@Override
	public void resetCallStats() {
		CMBControllerServlet.callStats = new ConcurrentHashMap<String, AtomicLong>();
		CMBControllerServlet.callFailureStats = new ConcurrentHashMap<String, AtomicLong>();
	}

	@Override
	public String getCassandraClusterName() {
		return CMBProperties.getInstance().getClusterName();
	}

	@Override
	public String getCassandraNodes() {
		return CMBProperties.getInstance().getClusterUrl();
	}
}
