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
package com.comcast.cqs.persistence;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import me.prettyprint.hector.api.exceptions.HTimedOutException;

import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;

import com.comcast.cmb.common.util.CMBProperties;
import com.comcast.cmb.common.util.PersistenceException;
import com.comcast.cmb.common.util.ValueAccumulator.AccumulatorName;
import com.comcast.cqs.controller.CQSControllerServlet;
import com.comcast.cqs.controller.CQSMonitor;
import com.comcast.cqs.controller.CQSMonitor.CacheType;
import com.comcast.cqs.model.CQSMessage;
import com.comcast.cqs.model.CQSQueue;
import com.comcast.cqs.util.CQSConstants;

/**
 * This class uses Redis as cache in front of our Cassandra access.
 * Currently we only cache message-ids and hidden state of messages.
 * The payloads are fetched from underlying persistence layer
 * 
 * The class must exist as a singleton
 * @author aseem, bwolf, vvenkatraman
 * 
 * Class is thread-safe
 */

public class RedisCachedCassandraPersistence implements ICQSMessagePersistence, ICQSMessagePersistenceIdSequence {

	private static final Logger log = Logger.getLogger(RedisCachedCassandraPersistence.class);

	private static RedisCachedCassandraPersistence Inst;

	public static ExecutorService executor;
	public static ExecutorService revisibilityExecutor;

	public final TestInterface testInterface = new TestInterface();

	/**
	 * 
	 * @return Singleton implementation of this object
	 */
	public static RedisCachedCassandraPersistence getInstance() {
		return Inst;
	}

	private static JedisPoolConfig cfg = new JedisPoolConfig();
	private static ShardedJedisPool pool;
	static {
		initializeInstance();
		initializePool();
	}

	/**
	 * Check config for payload cache
	 */
	private static void initializeInstance() {
		CQSMessagePartitionedCassandraPersistence cassandraPersistence = new CQSMessagePartitionedCassandraPersistence();
		if (CMBProperties.getInstance().isRedisPayloadCacheEnabled()) {
			log.info("event=initialize_redis payload_cache_enabled=true");
			RedisPayloadCacheCassandraPersistence redis = new RedisPayloadCacheCassandraPersistence(
					cassandraPersistence);
			Inst = new RedisCachedCassandraPersistence(redis);
			redis.setMessagePersistenceIdSequence(Inst);
		} else {
			log.info("event=initialize_redis payload_cache_enabled=false");
			Inst = new RedisCachedCassandraPersistence(cassandraPersistence);
		}
	}

	private volatile ICQSMessagePersistence persistenceStorage;

	private RedisCachedCassandraPersistence(ICQSMessagePersistence persistenceStorage) {
		this.persistenceStorage = persistenceStorage;
	}

	/**
	 * Initialize the redis connection pool
	 */
	private static void initializePool() {

		cfg.maxActive = CMBProperties.getInstance().getRedisConnectionsMaxActive();
		cfg.maxIdle = -1;
		List<JedisShardInfo> shardInfos = new LinkedList<JedisShardInfo>();
		String serverList = CMBProperties.getInstance().getRedisServerList();

		if (serverList == null) {
			throw new RuntimeException("Redis server list not specified");
		}

		String[] arr = serverList.split(",");

		for (int i = 0; i < arr.length; i++) {

			String[] hostPort = arr[i].trim().split(":");

			if (hostPort.length != 2) {
				throw new RuntimeException("Invalid redis server list: " + serverList);
			}

			JedisShardInfo shardInfo = new JedisShardInfo(hostPort[0].trim(), Integer.parseInt(hostPort[1].trim()),
					4000);
			shardInfos.add(shardInfo);
		}

		pool = new ShardedJedisPool(cfg, shardInfos);
		executor = Executors.newFixedThreadPool(CMBProperties.getInstance().getRedisFillerThreads());
		revisibilityExecutor = Executors.newFixedThreadPool(CMBProperties.getInstance().getRedisRevisibleThreads());

		log.info("event=initialize pools_size=" + shardInfos.size() + " max_active=" + cfg.maxActive);
	}

	/**
	 * Possible states for Q's
	 * State if Unavailable should be set when any code determines the queue is in bad state or un-available.
	 * The checkCacheConsistency will take care of performing the appt actions on that queue
	 */
	public enum QCacheState {
		Filling, //Cache is being filled by a thread 
		OK, //Cache is good for use 
		Unavailable; //Cache is unavailable for a single Q due to inconsistency issues.
	}

	public class TestInterface {
		public void setCassandraPersistence(ICQSMessagePersistence pers) {
			persistenceStorage = pers;
		}

		public ICQSMessagePersistence getCassandraPersistence() {
			return persistenceStorage;
		}

		public QCacheState getCacheState(String q) {
			return RedisCachedCassandraPersistence.this.getCacheState(q);
		}

		public void setCacheState(String queueUrl, QCacheState state, QCacheState oldState, boolean checkOldState)
				throws SetFailedException {
			RedisCachedCassandraPersistence.this.setCacheState(queueUrl, state, oldState, checkOldState);
		}

		public String getMemQueueMessage(String messageId, long ts, int initialDelay) {
			return RedisCachedCassandraPersistence.getMemQueueMessage(messageId, ts, initialDelay);
		}

		public long getMemQueueMessageCreatedTS(String memId) {
			return RedisCachedCassandraPersistence.getMemQueueMessageCreatedTS(memId);
		}

		public int getMemQueueMessageInitialDelay(String memId) {
			return RedisCachedCassandraPersistence.getMemQueueMessageInitialDelay(memId);
		}

		public String getMemQueueMessageMessageId(String memId) {
			return RedisCachedCassandraPersistence.getMemQueueMessageMessageId(memId);
		}

		public void resetTestQueue() {
			ShardedJedis jedis = getResource();
			try {
				jedis.del("testQueue-STATE");
				jedis.del("testQueue-Q");
				jedis.del("testQueue-H");
				jedis.del("testQueue-R");
				jedis.del("testQueue-F");
				jedis.del("testQueue-V");
				jedis.del("testQueue-VR");
				if (persistenceStorage instanceof RedisPayloadCacheCassandraPersistence) {
					((RedisPayloadCacheCassandraPersistence) persistenceStorage).testInterface.initializeQ("testQueue");
				}
			} finally {
				returnResource(jedis);
			}
		}

		public ShardedJedis getResource() {
			return RedisCachedCassandraPersistence.getResource();
		}

		public void returnResource(ShardedJedis jedis) {
			RedisCachedCassandraPersistence.returnResource(jedis, false);
		}

		public boolean checkCacheConsistency(String queueUrl) {
			return RedisCachedCassandraPersistence.this.checkCacheConsistency(queueUrl, false);
		}

		public void scheduleRevisibilityProcessor(String queueUrl) throws SetFailedException {
			revisibilityExecutor.submit(new RevisibleProcessor(queueUrl));
		}
	}

	// Each Queue's state is represented by the HashTable with key <Q>-STATE. These states are in the enum
	//QCacheState.
	static AtomicInteger numRedisConnections = new AtomicInteger(0);

	public int getNumRedisConnections() {
		return numRedisConnections.get();
	}

	public static ShardedJedis getResource() {
		ShardedJedis conn = pool.getResource();
		numRedisConnections.incrementAndGet();
		return conn;
	}

	public static void returnResource(ShardedJedis jedis, boolean broken) {
		if (numRedisConnections.intValue() < 1) {
			throw new IllegalStateException("Returned more connections than acquired from pool");
		}
		numRedisConnections.decrementAndGet();
		if (broken) {
			pool.returnBrokenResource(jedis);
		} else {
			pool.returnResource(jedis);
		}
	}

	/**
	 * Class represents the race condition where the caller was beaten by someone else
	 * In case of this exception, check the initial state again and retry if necessary.
	 */
	static class SetFailedException extends Exception {
		private static final long serialVersionUID = 1L;
	}

	/**
	 * 
	 * @param queueUrl
	 * @return The Q-STATE value for a queue or null if none exists
	 */
	private QCacheState getCacheState(String queueUrl) {
		long ts1 = System.currentTimeMillis();
		ShardedJedis jedis = getResource();
		boolean brokenJedis = false;
		try {
			String st = jedis.hget(queueUrl + "-STATE", "STATE");
			if (st == null)
				return null;
			return QCacheState.valueOf(st);
		} catch (JedisException e) {
			brokenJedis = true;
			throw e;
		} finally {
			returnResource(jedis, brokenJedis);
			long ts2 = System.currentTimeMillis();
			CQSControllerServlet.valueAccumulator.addToCounter(AccumulatorName.RedisTime, (ts2 - ts1));
		}
	}

	/**
	 * Set the state for a queue or throw exception if someone else beat us to it.
	 * This is an atomic operation
	 * @param queueUrl
	 * @param checkOldState - check the oldState in transaction
	 * @param oldState If checkOldstate is set we check within the WATCH..EXEC scope
	 *  if the state is still the same as the old value. If not, we throw SetfailedException
	 *  This helps do atomic CAS operations 
	 * @param state State to set to. if null, then the state field is deleted
	 * @throws SetFailedException
	 */
	private void setCacheState(String queueUrl, QCacheState state, QCacheState oldState, boolean checkOldState)
			throws SetFailedException {
		long ts1 = System.currentTimeMillis();
		boolean brokenJedis = false;
		ShardedJedis jedis = getResource();
		try {
			Jedis j = jedis.getShard(queueUrl + "-STATE");
			j.watch(queueUrl + "-STATE");
			if (checkOldState) {
				String oldStateStr = j.hget(queueUrl + "-STATE", "STATE");
				if (oldState == null && oldStateStr != null) {
					throw new SetFailedException();
				}
				if (oldState != null) {
					if (oldStateStr == null || QCacheState.valueOf(oldStateStr) != oldState) {
						j.unwatch();
						throw new SetFailedException();
					}
				}
			}
			Transaction tr = j.multi();
			if (state == null) {
				tr.hdel(queueUrl + "-STATE", "STATE");
			} else {
				tr.hset(queueUrl + "-STATE", "STATE", state.name());
			}
			List<Object> resp = tr.exec();
			if (resp == null) {
				throw new SetFailedException();
			}
		} catch (JedisException e) {
			brokenJedis = true;
			throw e;
		} finally {
			returnResource(jedis, brokenJedis);
			long ts2 = System.currentTimeMillis();
			CQSControllerServlet.valueAccumulator.addToCounter(AccumulatorName.RedisTime, (ts2 - ts1));
		}
	}

	/**
	 * Sets the sentinel flag in redis denoting either visibility processor is running or cache filler is
	 * running to prevent multiple from running and auto-expiring older runs.
	 * @param queueUrl
	 * @param visibilityOrCacheFiller
	 */
	private void setCacheFillerProcessing(String queueUrl, int exp) {
		long ts1 = System.currentTimeMillis();
		boolean brokenJedis = false;
		ShardedJedis jedis = getResource();
		String suffix = "-F";
		try {
			if (exp > 0) {
				jedis.set(queueUrl + suffix, "Y");
				jedis.expire(queueUrl + suffix, exp); //expire after exp seconds
			} else {
				jedis.del(queueUrl + suffix);
			}
		} catch (JedisException e) {
			brokenJedis = true;
			throw e;
		} finally {
			returnResource(jedis, brokenJedis);
			long ts2 = System.currentTimeMillis();
			CQSControllerServlet.valueAccumulator.addToCounter(AccumulatorName.RedisTime, (ts2 - ts1));
		}
	}

	private static boolean checkAndSetFlag(String queueUrl, String suffix, int exp) throws SetFailedException {
		if (exp <= 0) {
			throw new IllegalArgumentException("Redis expiration cannot be less than 0");
		}
		long ts1 = System.currentTimeMillis();
		boolean brokenJedis = false;
		ShardedJedis jedis = getResource();
		try {
			if (jedis.exists(queueUrl + suffix)) {
				return false;
			}
			Jedis j = jedis.getShard(queueUrl + suffix);
			j.watch(queueUrl + suffix);
			String val = j.get(queueUrl + suffix);
			if (val != null) {
				j.unwatch();
				return false;
			}
			Transaction tr = j.multi();

			//sentinel expired. kick off new RevisibleProcessor job
			tr.set(queueUrl + suffix, "Y");
			tr.expire(queueUrl + suffix, exp); //expire after exp seconds
			List<Object> resp = tr.exec();
			if (resp == null) {
				throw new SetFailedException();
			}
			return true;
		} catch (JedisException e) {
			brokenJedis = true;
			throw e;
		} finally {
			returnResource(jedis, brokenJedis);
			long ts2 = System.currentTimeMillis();
			CQSControllerServlet.valueAccumulator.addToCounter(AccumulatorName.RedisTime, (ts2 - ts1));
		}
	}

	/**
	 * Check weather a visibility processing was kicked off in the last exp seconds. If not, kick one off
	 * Method is atomic
	 * @param queueUrl
	 * @param exp in seconds
	 * @return true if sentinel was set. false otherwise
	 */
	private boolean checkAndSetVisibilityProcessing(String queueUrl, int exp) throws SetFailedException {
		if (checkAndSetFlag(queueUrl, "-R", exp)) {
			revisibilityExecutor.submit(new RevisibleProcessor(queueUrl));
			return true;
		}
		return false;
	}

	/**
	 * Processes the re-visible set once every exp seconds synchronously
	 * @param queueUrl
	 * @param exp
	 * @return true if Revisible set processing was done. false otherwise
	 */
	private static boolean checkAndProcessRevisibleSet(String queueUrl, int exp) throws SetFailedException {
		if (checkAndSetFlag(queueUrl, "-VR", exp)) {
			//perform re-visible set processing. Get all memIds whose score (visibilityTO) is <= now
			long ts1 = System.currentTimeMillis();
			long ts2 = 0;
			boolean brokenJedis = false;
			ShardedJedis jedis = getResource();
			try {
				//jedis is lame and does not have a constant for "-inf" which Redis supports. So we have to
				//pick an arbitrary old min value.
				Set<String> revisibleSet = jedis.zrangeByScore(queueUrl + "-V",
						System.currentTimeMillis() - (1000 * 3600 * 24 * 14), System.currentTimeMillis());
				for (String revisibleMemId : revisibleSet) {
					jedis.rpush(queueUrl + "-Q", revisibleMemId);
					jedis.zrem(queueUrl + "-V", revisibleMemId);
				}
				ts2 = System.currentTimeMillis();
				if (revisibleSet.size() > 0) {
					log.debug("event=redis_revisibility num_made_revisible=" + revisibleSet.size() + " res_ts="
							+ (ts2 - ts1));
				}
			} catch (JedisException e) {
				brokenJedis = true;
				throw e;
			} finally {
				returnResource(jedis, brokenJedis);
				if (ts2 == 0)
					ts2 = System.currentTimeMillis();
				CQSControllerServlet.valueAccumulator.addToCounter(AccumulatorName.RedisTime, (ts2 - ts1));
			}

			return true;
		}
		return false;
	}

	private static boolean tryCheckAndProcessRevisibleSet(String queueUrl, int exp) {
		try {
			if (checkAndProcessRevisibleSet(queueUrl, exp)) {
				return true;
			} else {
				return false;
			}
		} catch (SetFailedException e) {
			return false;
		}
	}

	private boolean getProcessingState(String queueUrl, boolean visibilityOrCacheFiller) {
		long ts1 = System.currentTimeMillis();
		boolean brokenJedis = false;
		ShardedJedis jedis = getResource();
		String suffix = visibilityOrCacheFiller ? "-R" : "-F";
		try {
			return jedis.exists(queueUrl + suffix);
		} catch (JedisException e) {
			brokenJedis = true;
			throw e;
		} finally {
			returnResource(jedis, brokenJedis);
			long ts2 = System.currentTimeMillis();
			CQSControllerServlet.valueAccumulator.addToCounter(AccumulatorName.RedisTime, (ts2 - ts1));
		}
	}

	//------below are helper methods for encoding Cassandra-message-id into in-memory message-id
	/**
	 * In memory message has format addedTS:initialDely:<messge-id>
	 * example - 1335302314:0:ABCDEFGHIJK
	 * @param messageId The Cassandra-message-id
	 * @return The in-memory message-id
	 */
	private static String getMemQueueMessage(String messageId, long ts, int initialDelay) {
		if (ts <= 0L) {
			throw new IllegalArgumentException("Timestamp cannot be 0 or negative");
		}
		if (initialDelay < 0) {
			throw new IllegalArgumentException("Initial delay cannot be 0 or negative");
		}
		if (messageId.length() == 0) {
			throw new IllegalArgumentException("Messge Id cannot be an empty string");
		}
		StringBuffer sb = new StringBuffer();
		sb.append(ts).append(':').append(initialDelay).append(':').append(messageId);
		return sb.toString();
	}

	/**
	 * 
	 * @param memId
	 * @return The created timestamp encoded in the memId
	 */
	public static long getMemQueueMessageCreatedTS(String memId) {
		String[] arr = memId.split(":");
		if (arr.length < 3) {
			throw new IllegalArgumentException(
					"Bad format for memId. Must be of the form addedTS:initialDely:<messge-id>. Got:" + memId);
		}
		return Long.parseLong(arr[0]);
	}

	/**
	 * 
	 * @param memId
	 * @return The initial delay encoded in the memId
	 */
	private static int getMemQueueMessageInitialDelay(String memId) {
		String[] arr = memId.split(":");
		if (arr.length < 3) {
			throw new IllegalArgumentException(
					"Bad format for memId. Must be of the form addedTS:initialDely:<messge-id>. Got:" + memId);
		}
		return Integer.parseInt(arr[1]);
	}

	/**
	 * @param memId
	 * @return The message-id encoded in memId
	 */
	static String getMemQueueMessageMessageId(String memId) {
		String[] arr = memId.split(":");
		if (arr.length < 3) {
			throw new IllegalArgumentException(
					"Bad format for memId. Must be of the form addedTS:initialDely:<messge-id>. Got:" + memId);
		}
		StringBuffer sb = new StringBuffer(arr[2]);
		for (int i = 3; i < arr.length; i++) {
			sb.append(":").append(arr[i]);
		}
		return sb.toString();
	}

	/**
	 * Class clears and then fills the cache for a given queue
	 * When its done, it updates the following cache keys are available:
	 *  <Q>-STATE = OK
	 *  <Q>-Q = The in-memory Q of message-ids
	 *  <Q>-H = The hashtable of hidden message-ids along with the timestamp for when they should become re-visible
	 *  <Q>-R = Existence implies recent processing of revisibility
	 *  <Q>-F = Existence implies currently running CacheFiller
	 *  <Q>-V = The re-visibility set implemented as a sorted set. Items are moved from Hidden set to re-visibility set
	 *    on changeMessageVisibility. Assumed users cannot delete from re-visibility set. We synchronously go through this
	 *    set every so often as part of receivemessage
	 *  <Q>-VR = the sentinel flag the absence of which implies its time to process the re-visible set.
	 *  <Q>-A-<messageId> = The attributes for a message in a queue. Note; This requires that the messageId remain the same
	 *    throughout the life-time of a message.
	 */
	private class CacheFiller implements Runnable {
		final String queueUrl;

		public CacheFiller(String queueUrl) {
			this.queueUrl = queueUrl;
		}

		@Override
		public void run() {
			CQSControllerServlet.valueAccumulator.initializeAllCounters();
			//clear all existing in-memQueue and hidden set
			boolean brokenJedis = false;
			long ts1 = System.currentTimeMillis();
			ShardedJedis jedis = getResource();
			try {
				jedis.del(queueUrl + "-Q");
				jedis.del(queueUrl + "-H");
				jedis.del(queueUrl + "-R");

				String previousReceiptHandle = null;
				List<CQSMessage> messages = persistenceStorage.peekQueue(queueUrl, null, null, 1000);
				int totalCached = 0;
				while (messages.size() != 0) {
					for (CQSMessage message : messages) {
						addMessageToCache(queueUrl, message, jedis);
						totalCached++;
					}
					previousReceiptHandle = messages.get(messages.size() - 1).getMessageId();
					messages = persistenceStorage.peekQueue(queueUrl, previousReceiptHandle, null, 1000);
				}
				setCacheState(queueUrl, QCacheState.OK, null, false);
				setCacheFillerProcessing(queueUrl, 0);
				long ts3 = System.currentTimeMillis();
				log.debug("event=filled_cache num_cached=" + totalCached + " res_ms=" + (ts3 - ts1) + " redis_ms="
						+ CQSControllerServlet.valueAccumulator.getCounter(AccumulatorName.RedisTime));
			} catch (Exception e) {
				if (e instanceof JedisException) {
					brokenJedis = true;
				}
				log.error("event=cache_filler", e);
				trySettingCacheState(queueUrl, QCacheState.Unavailable);
			} finally {
				returnResource(jedis, brokenJedis);
				CQSControllerServlet.valueAccumulator.deleteAllCounters();
			}
		}
	}

	/**
	 * Class goes through all the hidden messages for a specific queue
	 * and makes re-visible anything that was not deleted and has visibilityTO expired
	 * Note: this is a low priority thread.
	 */
	private class RevisibleProcessor implements Runnable {
		private Logger log = Logger.getLogger(RevisibleProcessor.class);
		private final String queueUrl;

		public RevisibleProcessor(String queueUrl) {
			this.queueUrl = queueUrl;
		}

		private boolean setAndDeleteCounters = true;

		@Override
		public void run() {
			if (setAndDeleteCounters) {
				CQSControllerServlet.valueAccumulator.initializeAllCounters();
			}
			long ts0 = System.currentTimeMillis();
			QCacheState state = getCacheState(queueUrl);
			if (state == null || state != QCacheState.OK) {
				log.debug("event=revisibility_check queue_cache_state=" + (state == null ? "null" : state.name())
						+ " action=do_nothing");
				return;
			}
			boolean brokenJedis = false;
			ShardedJedis jedis = null;
			List<String> memIds = new LinkedList<String>();
			try {
				jedis = getResource();
				updateExpire(queueUrl, jedis);
				long ts1 = System.currentTimeMillis();
				Set<String> keys = jedis.hkeys(queueUrl + "-H");
				long ts2 = System.currentTimeMillis();
				CQSControllerServlet.valueAccumulator.addToCounter(AccumulatorName.RedisTime, (ts2 - ts1));
				log.debug("event=revisibility_check queue_url=" + queueUrl + " invisible_set_size=" + keys.size());
				for (String key : keys) {
					long ts3 = System.currentTimeMillis();
					String val = jedis.hget(queueUrl + "-H", key);
					long ts4 = System.currentTimeMillis();
					CQSControllerServlet.valueAccumulator.addToCounter(AccumulatorName.RedisTime, (ts4 - ts3));
					if (val == null) {
						continue;
					}
					long visibilityTo = Long.parseLong(val);
					if (visibilityTo < System.currentTimeMillis()) {
						memIds.add(key);
					}
				}
				//process memIds that should be revisible
				for (String memId : memIds) {
					String messageId = getMemQueueMessageMessageId(memId);
					long origCreatedTS = getMemQueueMessageCreatedTS(memId);
					jedis.rpush(queueUrl + "-Q", getMemQueueMessage(messageId, origCreatedTS, 0)); //TODO: initialDelay is 0
					jedis.hdel(queueUrl + "-H", memId);
				}
				long ts3 = System.currentTimeMillis();
				log.debug("event=revisibility_check numMadeRevisible=" + memIds.size() + " redisTime="
						+ CQSControllerServlet.valueAccumulator.getCounter(AccumulatorName.RedisTime)
						+ " responseTimeMS=" + (ts3 - ts0) + " hiddenSetSize=" + keys.size());
			} catch (Exception e) {
				if (e instanceof JedisException) {
					brokenJedis = true;
				}
				log.error("event=revisibility_check", e);
			} finally {
				if (jedis != null) {
					returnResource(jedis, brokenJedis);
				}
				if (setAndDeleteCounters) {
					CQSControllerServlet.valueAccumulator.deleteAllCounters();
				}
			}
		}

	}

	/**
	 * Check if the queue is in the cache and in ok state. Else kick off initialization
	 * and return false. 
	 * @param queueUrl
	 * @param trueOnFiller returns true if the current state is Filling.
	 * @return true if the cache is good for use. false if it is unavailable
	 */
	public boolean checkCacheConsistency(String queueUrl, boolean trueOnFiller) {
		try {
			//Check if cache's state exists, if not return false and initializeCache
			//if cache's state is not OK, return false
			QCacheState state = getCacheState(queueUrl);
			if (state == null || state == QCacheState.Unavailable) {
				try {
					setCacheFillerProcessing(queueUrl, 20 * 60); //This must be before setCacheState or else a race-condition                    
					setCacheState(queueUrl, QCacheState.Filling, state, true);
					//we successfully set the stae to filling. queueup filling job
					executor.submit(new CacheFiller(queueUrl));
					log.debug("event=initialize_queue_cache cache_state=" + (state == null ? "null" : state.name())
							+ " queue_url=" + queueUrl);
				} catch (SetFailedException e) {
					//someone beat us to it. return false for now and check again next time
					log.debug("event=initialize_queue_cache cache_state=" + (state == null ? "null" : state.name())
							+ " queue_url=" + queueUrl);
					state = getCacheState(queueUrl);
					if (state != null && state == QCacheState.Filling && trueOnFiller) {
						return true; //otherwise continue and return false
					}
				}
				return false;
			} else if (state != QCacheState.OK) { //must be Filling
				//check if its a stale filler.
				if (!getProcessingState(queueUrl, false)) {
					try {
						setCacheState(queueUrl, null, null, false);
						log.debug("event=clear_stale_cache_filler cache_state="
								+ (state == null ? "null" : state.name()) + " queue_url=" + queueUrl);
					} catch (SetFailedException e) {
						log.debug("event=clear_stale_cache_filler cache_state="
								+ (state == null ? "null" : state.name()) + " queue_url=" + queueUrl);
					}
				}
				if (trueOnFiller && state == QCacheState.Filling) {
					return true;
				}
				return false;
			}
			return true;
		} catch (JedisConnectionException e) {
			log.warn("event=check_cache_consistency error_code=redis_unavailable num_connections="
					+ numRedisConnections.get());
			return false;
		}
	}

	/**
	 * Add message to in-memory-queue. creationTS is now
	 * @param queueUrl
	 * @param message
	 */
	private void addMessageToCache(String queueUrl, CQSMessage message, ShardedJedis jedis) {
		long ts1 = System.currentTimeMillis();
		Long newLen = jedis.rpush(queueUrl + "-Q",
				getMemQueueMessage(message.getMessageId(), System.currentTimeMillis(), 0)); //TODO: currently initialDelay is always 0
		if (newLen.longValue() == 0) {
			throw new IllegalStateException("Could not add memId to queue");
		}
		long ts2 = System.currentTimeMillis();
		CQSControllerServlet.valueAccumulator.addToCounter(AccumulatorName.RedisTime, (ts2 - ts1));
	}

	/**
	 * Method tries to set cache state and swallows all exceptions that are thrown
	 * @param queueUrl
	 * @param state
	 */
	private void trySettingCacheState(String queueUrl, QCacheState state) {
		try {
			setCacheState(queueUrl, state, null, false);
		} catch (Exception e) {
			log.error("event=try_setting_cache_state", e);
		}
	}

	/**
	 * Update the TTL for the queue
	 * @param queueUrl
	 * @param seconds
	 * @param jedis
	 */
	private void updateExpire(String queueUrl, ShardedJedis jedis) {
		int seconds = CMBProperties.getInstance().getRedisExpireTTLSec();
		long ts1 = System.currentTimeMillis();
		Long ret = jedis.expire(queueUrl + "-Q", seconds);
		if (ret == 0) {
			log.debug("event=could_not_update_expire queue_url=" + queueUrl);
		}
		ret = jedis.expire(queueUrl + "-H", seconds);
		if (ret == 0) {
			log.debug("event=could_not_update_expire_for_hidden_set queue_url=" + queueUrl);
		}
		ret = jedis.expire(queueUrl + "-STATE", seconds);
		if (ret == 0) {
			throw new IllegalStateException("event=could_not_update_expire_for_state queue_url=" + queueUrl);
		}
		long ts2 = System.currentTimeMillis();
		CQSControllerServlet.valueAccumulator.addToCounter(AccumulatorName.RedisTime, (ts2 - ts1));
	}

	@Override
	/**
	 * @return the method will always return the memid generated by this layer which encodes the id provided
	 * by underlying persistence layer.
	 * Note: method updates the expire time of the Queue attributes in cache
	 */
	public String sendMessage(CQSQueue queue, CQSMessage message)
			throws PersistenceException, IOException, InterruptedException, NoSuchAlgorithmException {
		//first persist to Cassandra and get message-id to put into cache        
		String messageId = persistenceStorage.sendMessage(queue, message);
		if (messageId == null) {
			throw new IllegalStateException("Could not get id from underlying storage");
		}
		boolean cacheAvailable = checkCacheConsistency(queue.getRelativeUrl(), true); //set in cache even if its filling
		String memId = getMemQueueMessage(messageId, System.currentTimeMillis(), 0); //TODO: currently initialDelay is 0
		boolean brokenJedis = false;
		ShardedJedis jedis = null;
		long ts1 = System.currentTimeMillis();
		try {
			if (cacheAvailable) {
				int delaySeconds = 0;
				if (queue.getDelaySeconds() > 0) {
					delaySeconds = queue.getDelaySeconds();
				}
				if (message.getAttributes().containsKey(CQSConstants.DELAY_SECONDS)) {
					delaySeconds = Integer.parseInt(message.getAttributes().get(CQSConstants.DELAY_SECONDS));
				}
				jedis = getResource();
				if (delaySeconds > 0) {
					jedis.hset(queue.getRelativeUrl() + "-H", memId,
							Long.toString(System.currentTimeMillis() + (delaySeconds * 1000)));
				} else {
					jedis.rpush(queue.getRelativeUrl() + "-Q", memId);
				}
				log.debug("event=send_message cache_available=true msg_id= " + messageId + " queue_url="
						+ queue.getAbsoluteUrl());
			} else {
				log.debug("event=send_message cache_available=false msg_id= " + messageId + " queue_url="
						+ queue.getAbsoluteUrl());
			}
		} catch (JedisConnectionException e) {
			log.warn("event=send_message error_code=redis_unavailable num_connections=" + numRedisConnections.get());
			brokenJedis = true;
			trySettingCacheState(queue.getRelativeUrl(), QCacheState.Unavailable);
		} finally {
			if (jedis != null) {
				returnResource(jedis, brokenJedis);
			}
			long ts2 = System.currentTimeMillis();
			CQSControllerServlet.valueAccumulator.addToCounter(AccumulatorName.RedisTime, (ts2 - ts1));
		}
		return memId;
	}

	@Override
	public Map<String, String> sendMessageBatch(CQSQueue queue, List<CQSMessage> messages)
			throws PersistenceException, IOException, InterruptedException, NoSuchAlgorithmException {

		persistenceStorage.sendMessageBatch(queue, messages);

		Map<String, String> memIds = new HashMap<String, String>();
		boolean cacheAvailable = checkCacheConsistency(queue.getRelativeUrl(), true);//set in cache even if its filling
		ShardedJedis jedis = null;
		if (cacheAvailable) {
			try {
				jedis = getResource();
			} catch (JedisConnectionException e) {
				cacheAvailable = false;
				trySettingCacheState(queue.getRelativeUrl(), QCacheState.Unavailable);
			}
		}
		//add messages in the same order as messages list
		boolean brokenJedis = false;
		try {
			for (CQSMessage message : messages) {
				int delaySeconds = 0;
				if (queue.getDelaySeconds() > 0) {
					delaySeconds = queue.getDelaySeconds();
				}
				if (message.getAttributes().containsKey(CQSConstants.DELAY_SECONDS)) {
					delaySeconds = Integer.parseInt(message.getAttributes().get(CQSConstants.DELAY_SECONDS));
				}
				String clientId = message.getSuppliedMessageId();
				String messageId = message.getMessageId();
				String memId = getMemQueueMessage(messageId, System.currentTimeMillis() + delaySeconds * 1000, 0);
				if (cacheAvailable) {
					try {
						if (delaySeconds > 0) {
							jedis.hset(queue.getRelativeUrl() + "-H", memId,
									Long.toString(System.currentTimeMillis() + (delaySeconds * 1000)));
						} else {
							jedis.rpush(queue.getRelativeUrl() + "-Q", memId);
						}
					} catch (JedisConnectionException e) {
						trySettingCacheState(queue.getRelativeUrl(), QCacheState.Unavailable);
					}
					log.debug("event=send_message_batch cache_available=true msg_id= " + messageId + " queue_url="
							+ queue.getAbsoluteUrl());
				} else {
					log.debug("event=send_message_batch cache_available=false msg_id= " + messageId + " queue_url="
							+ queue.getAbsoluteUrl());
				}
				memIds.put(clientId, memId);
			}
		} catch (JedisException e) {
			brokenJedis = true;
			throw e;
		} finally {
			if (jedis != null) {
				returnResource(jedis, brokenJedis);
			}
		}
		return memIds;
	}

	@Override
	public void deleteMessage(String queueUrl, String receiptHandle) throws PersistenceException {
		//receiptHandle is memId
		String messageId = getMemQueueMessageMessageId(receiptHandle);
		boolean cacheAvailable = checkCacheConsistency(queueUrl, false);
		if (cacheAvailable) {
			ShardedJedis jedis = null;
			boolean brokenJedis = false;
			try {
				jedis = getResource();
				long ts1 = System.currentTimeMillis();
				long numDeleted = jedis.hdel(queueUrl + "-H", receiptHandle);
				if (numDeleted != 1) {
					log.warn("event=delete_message error_code=could_not_delelete_hidden_set queue_url=" + queueUrl
							+ " mem_id=" + receiptHandle);
				}
				if (jedis.del(queueUrl + "-A-" + receiptHandle) == 0) {
					log.warn("event=delete_message error_code=could_not_delete_attributes queue_url=" + queueUrl
							+ " mem_id=" + receiptHandle);
				}
				long ts2 = System.currentTimeMillis();
				CQSControllerServlet.valueAccumulator.addToCounter(AccumulatorName.RedisTime, (ts2 - ts1));
			} catch (JedisConnectionException e) {
				log.warn("event=delete_message error_code=redis_unavailable num_connections="
						+ numRedisConnections.get());
				brokenJedis = true;
				cacheAvailable = false;
				trySettingCacheState(queueUrl, QCacheState.Unavailable);
			} finally {
				if (jedis != null) {
					returnResource(jedis, brokenJedis);
				}
			}
		}
		//delete from underlying persistence layer
		persistenceStorage.deleteMessage(queueUrl, messageId);
	}

	/**
	 * 
	 * @param queue
	 * @param message
	 * @return true if message is expired, false otherwise
	 */
	private boolean isMessageExpired(CQSQueue queue, String memId) {
		if (getMemQueueMessageCreatedTS(memId) + (queue.getMsgRetentionPeriod() * 1000) < System.currentTimeMillis()) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public List<CQSMessage> receiveMessage(CQSQueue queue, Map<String, String> receiveAttributes)
			throws PersistenceException, IOException, NoSuchAlgorithmException, InterruptedException {

		int maxNumberOfMessages = 1;
		int visibilityTO = queue.getVisibilityTO();
		if (receiveAttributes != null && receiveAttributes.size() > 0) {
			if (receiveAttributes.containsKey(CQSConstants.MAX_NUMBER_OF_MESSAGES)) {
				maxNumberOfMessages = Integer.parseInt(receiveAttributes.get(CQSConstants.MAX_NUMBER_OF_MESSAGES));
			}
			if (receiveAttributes.containsKey(CQSConstants.VISIBILITY_TIMEOUT)) {
				visibilityTO = Integer.parseInt(receiveAttributes.get(CQSConstants.VISIBILITY_TIMEOUT));
			}
		}

		boolean cacheAvailable = checkCacheConsistency(queue.getRelativeUrl(), false);
		List<CQSMessage> ret = new LinkedList<CQSMessage>();
		if (cacheAvailable) { //get the messageIds from in-memQ
			ShardedJedis jedis = null;
			boolean brokenJedis = false;
			try {
				jedis = getResource();
				//process revisible-set before getting from queue
				tryCheckAndProcessRevisibleSet(queue.getRelativeUrl(),
						CMBProperties.getInstance().getRedisRevisibleSetFrequencySec());

				try {
					if (checkAndSetVisibilityProcessing(queue.getRelativeUrl(),
							CMBProperties.getInstance().getRedisRevisibleFrequencySec())) {
						log.debug("event=scheduled_revisibility_processor");
					}
				} catch (SetFailedException e) {
				}

				boolean done = false;
				while (!done) {
					boolean emptyQueue = false;
					HashMap<String, String> messageIdToMemId = new HashMap<String, String>();
					List<String> messageIds = new LinkedList<String>();
					for (int i = 0; i < maxNumberOfMessages && !emptyQueue; i++) {
						long ts1 = System.currentTimeMillis();
						String memId;
						if (visibilityTO > 0) {
							memId = jedis.lpop(queue.getRelativeUrl() + "-Q");
						} else {
							memId = jedis.lindex(queue.getRelativeUrl() + "-Q", i);
						}
						long ts2 = System.currentTimeMillis();
						CQSControllerServlet.valueAccumulator.addToCounter(AccumulatorName.RedisTime, (ts2 - ts1));
						if (memId == null || memId.equals("nil")) { //done
							emptyQueue = true;
						} else {
							String messageId = getMemQueueMessageMessageId(memId);
							messageIds.add(messageId);
							messageIdToMemId.put(messageId, memId);
						}
					}
					if (messageIds.size() == 0) {
						CQSMonitor.getInstance().registerEmptyResp(queue.getRelativeUrl(), 1);
						return Collections.emptyList();
					}
					//by here messageIds have Underlying layer's messageids. Get messages

					//get messages from underlying layer. The total returned may not be what was
					//in mem cache since master-master replication to Cassandra could mean other
					//colo deleted the message form underlying storage. 
					log.debug("event=found_msg_ids_in_redis num_mem_ids=" + messageIds.size());
					try {
						Map<String, CQSMessage> persisMap = persistenceStorage.getMessages(queue.getRelativeUrl(),
								messageIds);
						for (Entry<String, CQSMessage> messageIdToMessage : persisMap.entrySet()) {
							String memId = messageIdToMemId.get(messageIdToMessage.getKey());
							if (memId == null) {
								throw new IllegalStateException(
										"Underlying storage layer returned a message that was not requested");
							}
							CQSMessage message = messageIdToMessage.getValue();
							if (message == null) {
								log.warn("event=message_is_null msg_id=" + messageIdToMessage.getKey());
								continue;
							}

							//check if message returned is expired. This will only happen if payload coming from payload-cache. Cassandra
							//expires old messages just fine. If expired, skip over and continue. 
							if (isMessageExpired(queue, memId)) {
								try {
									log.info("event=message_expired message_id=" + memId);
									persistenceStorage.deleteMessage(queue.getRelativeUrl(), message.getMessageId());
								} catch (Exception e) {
									//its fine to ignore this exception since message must have been auto-deleted from Cassandra
									//but we need to do this to clean up payloadcache
									log.debug("event=message_already_deleted_in_cassandra msg_id="
											+ message.getMessageId());
								}
								continue;
							}

							//hide message if visibilityTO is greater than 0
							if (visibilityTO > 0) {
								long ts1 = System.currentTimeMillis();
								jedis.hset(queue.getRelativeUrl() + "-H", memId,
										Long.toString(System.currentTimeMillis() + (visibilityTO * 1000)));
								long ts2 = System.currentTimeMillis();
								CQSControllerServlet.valueAccumulator.addToCounter(AccumulatorName.RedisTime,
										(ts2 - ts1));
							}

							message.setMessageId(memId);
							message.setReceiptHandle(memId);

							//get message-attributes and populate in message
							Map<String, String> msgAttrs = (message.getAttributes() != null) ? message.getAttributes()
									: new HashMap<String, String>();
							List<String> attrs = jedis.hmget(queue.getRelativeUrl() + "-A-" + memId,
									CQSConstants.APPROXIMATE_FIRST_RECEIVE_TIMESTAMP,
									CQSConstants.APPROXIMATE_RECEIVE_COUNT);
							if (attrs.get(0) == null) {
								String firstRecvTS = Long.toString(System.currentTimeMillis());
								jedis.hset(queue.getRelativeUrl() + "-A-" + memId,
										CQSConstants.APPROXIMATE_FIRST_RECEIVE_TIMESTAMP, firstRecvTS);
								msgAttrs.put(CQSConstants.APPROXIMATE_FIRST_RECEIVE_TIMESTAMP, firstRecvTS);
							} else {
								msgAttrs.put(CQSConstants.APPROXIMATE_FIRST_RECEIVE_TIMESTAMP, attrs.get(0));
							}
							int recvCount = 1;
							if (attrs.get(1) != null) {
								recvCount = Integer.parseInt(attrs.get(1)) + 1;
							}
							jedis.hset(queue.getRelativeUrl() + "-A-" + memId, CQSConstants.APPROXIMATE_RECEIVE_COUNT,
									Integer.toString(recvCount));

							jedis.expire(queue.getRelativeUrl() + "-A-" + memId, 3600 * 24 * 14); //14 days expiration if not deleted
							msgAttrs.put(CQSConstants.APPROXIMATE_RECEIVE_COUNT, Integer.toString(recvCount));
							message.setAttributes(msgAttrs);

							ret.add(message);
						}
						if (ret.size() > 0) { //There may be cases where the underlying persistent message has two memIds while
							//cache filling. In such cases trying to retrieve message for the second memId may result in no message
							//returned. We should skip over those memIds and continue till we find at least one valid memId
							done = true;
						} else {
							for (String messageId : messageIds) {
								log.debug("event=bad_mem_id_found msg_id=" + messageId + " action=skip_message");
							}
						}
					} catch (HTimedOutException e1) { //If Hector timedout, push messages back
						log.error("event=hector_timeout num_messages=" + messageIds.size()
								+ " action=pushing_messages_back_to_redis");
						if (visibilityTO > 0) {
							for (String messageId : messageIds) {
								String memId = messageIdToMemId.get(messageId);
								jedis.lpush(queue.getRelativeUrl() + "-Q", memId);
							}
						}
						throw e1;
					}
				}
			} catch (JedisConnectionException e) {
				log.warn("event=receive_message error_code=redis_unavailable num_connections="
						+ numRedisConnections.get());
				brokenJedis = true;
				trySettingCacheState(queue.getRelativeUrl(), QCacheState.Unavailable);
				cacheAvailable = false;
			} finally {
				if (jedis != null) {
					returnResource(jedis, brokenJedis);
				}
			}
			CQSMonitor.getInstance().registerCacheHit(queue.getRelativeUrl(), ret.size(), ret.size(), CacheType.QCache); //all ids from cache
			log.debug("event=messages_found cache=available num_messages=" + ret.size());
		} else { //get form underlying layer
			List<CQSMessage> messages = persistenceStorage.peekQueueRandom(queue.getRelativeUrl(), maxNumberOfMessages);
			for (CQSMessage msg : messages) {
				String memId = getMemQueueMessage(msg.getMessageId(), System.currentTimeMillis(), 0); //TODO: initialDelay is 0
				msg.setMessageId(memId);
				msg.setReceiptHandle(memId);
				Map<String, String> msgAttrs = (msg.getAttributes() != null) ? msg.getAttributes()
						: new HashMap<String, String>();
				msgAttrs.put(CQSConstants.APPROXIMATE_RECEIVE_COUNT, "1");
				msgAttrs.put(CQSConstants.APPROXIMATE_FIRST_RECEIVE_TIMESTAMP,
						Long.toString(System.currentTimeMillis()));
				msg.setAttributes(msgAttrs);
				ret.add(msg);
			}
			//Note: In this case there is no message hiding.            
			CQSMonitor.getInstance().registerCacheHit(queue.getRelativeUrl(), 0, ret.size(), CacheType.QCache); //all ids missed cache
			log.debug("event=messages_found cache=unavailable num_messages=" + ret.size());
		}

		if (ret.size() == 0) {
			CQSMonitor.getInstance().registerEmptyResp(queue.getRelativeUrl(), 1);
		}

		return ret;
	}

	@Override
	/**
	 * Assumed only hidden messages or revisible set messages can hve there visibility changed
	 * method moves the memId from hidden set -> re-visibility set
	 * If the new visibilityTO is 0 we shortcut and make the method immediately visible
	 * @return true if visibility was changed. false otherwise
	 */
	public boolean changeMessageVisibility(CQSQueue queue, String receiptHandle, int visibilityTO)
			throws PersistenceException, IOException, NoSuchAlgorithmException, InterruptedException {

		boolean cacheAvailable = checkCacheConsistency(queue.getRelativeUrl(), false);
		if (cacheAvailable) {
			ShardedJedis jedis = null;
			boolean brokenJedis = false;
			try {
				jedis = getResource();
				long ts1 = System.currentTimeMillis();
				String currentVisibilityTO = jedis.hget(queue.getRelativeUrl() + "-H", receiptHandle);
				if (currentVisibilityTO == null) {
					//doesn't exist in Hidden set. Check in the RevisibilitySet
					Long rank = jedis.zrank(queue.getRelativeUrl() + "-V", receiptHandle);
					if (rank == null) {
						log.error(
								"event=change_message_visibility error_code=message_not_found_in_hidden_set_or_revisibility_set receipt_handle="
										+ receiptHandle);
						throw new RuntimeException(
								"Could not find hidden message with receipt handle " + receiptHandle);
					}
				} else {
					jedis.hdel(queue.getRelativeUrl() + "-H", receiptHandle);
				}

				if (visibilityTO == 0) { //make immediately visible
					jedis.rpush(queue.getRelativeUrl() + "-Q", receiptHandle);
					return true;
				}

				//extend from now to provided visibilityTO
				double newVisibilityTO = System.currentTimeMillis() + (visibilityTO * 1000);
				jedis.zadd(queue.getRelativeUrl() + "-V", newVisibilityTO, receiptHandle); //insert or update already existing

				long ts2 = System.currentTimeMillis();
				CQSControllerServlet.valueAccumulator.addToCounter(AccumulatorName.RedisTime, (ts2 - ts1));
				return true;
			} catch (JedisConnectionException e) {
				log.warn("event=change_message_visibility reason=redis_unavailable num_connections="
						+ numRedisConnections.get());
				brokenJedis = true;
				cacheAvailable = false;
				return false;
			} finally {
				if (jedis != null) {
					returnResource(jedis, brokenJedis);
				}
			}
		} else {
			return false;
		}
	}

	@Override
	/**
	 * Note: If cache is unavailable, we will return different id for a message than when the cache is available
	 * So we will have duplicates in that case.
	 * Note: we currently don't respect nextReceiptHandle only previousReceiptHandle and length
	 */
	public List<CQSMessage> peekQueue(String queueUrl, String previousReceiptHandle, String nextReceiptHandle,
			int length) throws PersistenceException, IOException, NoSuchAlgorithmException {
		boolean cacheAvailable = checkCacheConsistency(queueUrl, false);
		if (cacheAvailable) {
			List<String> memIdsRet = getIdsFromHead(queueUrl, previousReceiptHandle, length);
			//by here memIdsRet should have memIds to return messages for
			Map<String, CQSMessage> ret = getMessages(queueUrl, memIdsRet);
			//return list in the same order as memIdsRet
			List<CQSMessage> messages = new LinkedList<CQSMessage>();
			for (String memId : memIdsRet) {
				CQSMessage msg = ret.get(memId);
				if (msg != null) {
					messages.add(msg);
				}
			}
			log.debug("event=peek_queue cache=available queue_url=" + queueUrl + " num_messages=" + messages.size());
			return messages;
		} else { //get from underlying storage
			if (previousReceiptHandle != null) { //strip out this layer's id
				previousReceiptHandle = getMemQueueMessageMessageId(previousReceiptHandle);
			}
			if (nextReceiptHandle != null) {
				nextReceiptHandle = getMemQueueMessageMessageId(nextReceiptHandle);
			}
			List<CQSMessage> messages = persistenceStorage.peekQueue(queueUrl, previousReceiptHandle, nextReceiptHandle,
					length);
			for (CQSMessage message : messages) {
				String memId = getMemQueueMessage(message.getMessageId(), System.currentTimeMillis(), 0); //TODO: initialDelay is 0
				message.setMessageId(memId);
				message.setReceiptHandle(memId);
			}
			log.debug("event=peek_queue cache=unavailable queue_url=" + queueUrl + " num_messages=" + messages.size());
			return messages;
		}
	}

	@Override
	public void clearQueue(String queueUrl)
			throws PersistenceException, NoSuchAlgorithmException, UnsupportedEncodingException {
		boolean cacheAvailable = checkCacheConsistency(queueUrl, true);
		if (cacheAvailable) {
			boolean brokenJedis = false;
			ShardedJedis jedis = null;
			try {
				long ts1 = System.currentTimeMillis();
				jedis = getResource();
				Long num = jedis.del(queueUrl + "-STATE");
				log.debug("num removed=" + num);
				num = jedis.del(queueUrl + "-Q");
				log.debug("num removed=" + num);
				num = jedis.del(queueUrl + "-H");
				log.debug("num removed=" + num);
				num = jedis.del(queueUrl + "-R");
				log.debug("num removed=" + num);
				num = jedis.del(queueUrl + "-F");
				log.debug("num removed=" + num);
				num = jedis.del(queueUrl + "-V");
				log.debug("num removed=" + num);
				num = jedis.del(queueUrl + "-VR");
				log.debug("num removed=" + num);
				long ts2 = System.currentTimeMillis();
				CQSControllerServlet.valueAccumulator.addToCounter(AccumulatorName.RedisTime, (ts2 - ts1));
				log.debug("event=cleared_queue queue_url=" + queueUrl);
			} catch (JedisConnectionException e) {
				log.warn("event=clear_queue error_code=redis_unavailable num_connections=" + numRedisConnections.get());
				brokenJedis = true;
				trySettingCacheState(queueUrl, QCacheState.Unavailable);
				cacheAvailable = false;
			} finally {
				if (jedis != null) {
					returnResource(jedis, brokenJedis);
				}
			}
		}
		//clear queue from underlying layer
		persistenceStorage.clearQueue(queueUrl);
	}

	public Map<String, CQSMessage> getMessages(String queueUrl, List<String> ids)
			throws PersistenceException, NoSuchAlgorithmException, UnsupportedEncodingException {
		//parse the ids generated by this layer to get underlying ids and get from underlying layer
		List<String> messageIds = new LinkedList<String>();
		HashMap<String, String> messageIdToMemId = new HashMap<String, String>();
		for (String id : ids) {
			String messageId = getMemQueueMessageMessageId(id);
			messageIds.add(messageId);
			messageIdToMemId.put(messageId, id);
		}
		Map<String, CQSMessage> persisMap = persistenceStorage.getMessages(queueUrl, messageIds);
		Map<String, CQSMessage> ret = new HashMap<String, CQSMessage>();
		for (Entry<String, CQSMessage> idToMessage : persisMap.entrySet()) {
			CQSMessage msg = idToMessage.getValue();
			if (msg == null)
				continue;
			String memId = messageIdToMemId.get(idToMessage.getKey());
			msg.setMessageId(memId);
			msg.setReceiptHandle(memId);
			ret.put(memId, msg);
		}
		return ret;
	}

	@Override
	public List<String> getIdsFromHead(String queueUrl, int num) {
		return getIdsFromHead(queueUrl, null, num);
	}

	public List<String> getIdsFromHead(String queueUrl, String previousReceiptHandle, int num) {
		boolean cacheAvailable = checkCacheConsistency(queueUrl, true);
		if (!cacheAvailable)
			return Collections.emptyList();

		List<String> memIdsRet = new LinkedList<String>();
		boolean brokenJedis = false;
		ShardedJedis jedis = null;
		try {
			jedis = getResource();
			long llen = jedis.llen(queueUrl + "-Q");
			if (llen == 0L) {
				return Collections.emptyList();
			}
			int retCount = 0;
			long i = 0L;
			boolean includeSet = (previousReceiptHandle == null) ? true : false;
			while (retCount < num && i < llen) {
				List<String> memIds = jedis.lrange(queueUrl + "-Q", i, i + num - 1);
				if (memIds.size() == 0)
					break; //done
				i += num; //next time, exlude the last one in this set
				for (String memId : memIds) {
					if (!includeSet) {
						if (memId.equals(previousReceiptHandle)) {
							includeSet = true;
							//skip over this one since it was the last el of the last call
						}
					} else {
						memIdsRet.add(memId);
						retCount++;
					}
				}
			}
			//by here memIdsRet should have memIds to return messages for
		} catch (JedisException e) {
			brokenJedis = true;
			throw e;
		} finally {
			if (jedis != null) {
				returnResource(jedis, brokenJedis);
			}
		}
		return memIdsRet;
	}

	/**
	 * 
	 * @param queueUrl
	 * @param processRevisibilitySet if true, run re-visibility processing.
	 * @return number of mem-ids in Redis Queue
	 */
	public long getQueueMessageCount(String queueUrl, boolean processRevisibilitySet) {

		boolean cacheAvailable = checkCacheConsistency(queueUrl, true);

		if (!cacheAvailable) {
			throw new IllegalStateException("Redis cache not available");
		}

		if (processRevisibilitySet) {
			tryCheckAndProcessRevisibleSet(queueUrl, CMBProperties.getInstance().getRedisRevisibleSetFrequencySec());
		}
		ShardedJedis jedis = null;
		boolean brokenJedis = false;
		try {
			jedis = getResource();
			return jedis.llen(queueUrl + "-Q");
		} catch (JedisException e) {
			brokenJedis = true;
			throw e;
		} finally {
			if (jedis != null) {
				returnResource(jedis, brokenJedis);
			}
		}
	}

	@Override
	public long getQueueMessageCount(String queueUrl) {
		return getQueueMessageCount(queueUrl, false);
	}

	@Override
	public List<CQSMessage> peekQueueRandom(String queueUrl, int length)
			throws PersistenceException, IOException, NoSuchAlgorithmException {
		return persistenceStorage.peekQueueRandom(queueUrl, length);
	}

	/**
	 * Get all redis shard infos
	 * @return list of hash maps, one for each shard
	 */
	public static List<Map<String, String>> getInfo() {

		boolean brokenJedis = false;
		ShardedJedis jedis = getResource();
		List<Map<String, String>> shardInfos = new ArrayList<Map<String, String>>();

		try {

			Collection<Jedis> shards = jedis.getAllShards();

			for (Jedis shard : shards) {

				String info = shard.info();
				String lines[] = info.split("\n");
				Map<String, String> entries = new HashMap<String, String>();

				for (String line : lines) {

					if (line != null && line.length() > 0) {

						String keyValue[] = line.split(":");

						if (keyValue.length == 2) {
							entries.put(keyValue[0], keyValue[1]);
						}
					}

				}

				shardInfos.add(entries);
			}

			return shardInfos;

		} catch (JedisException ex) {

			brokenJedis = true;
			throw ex;

		} finally {
			returnResource(jedis, brokenJedis);
		}
	}

	/**
	 * Get number of redis shards
	 * @return number of redis shards
	 */
	public static int getNumberOfShards() {

		boolean brokenJedis = false;
		ShardedJedis jedis = getResource();

		try {
			return jedis.getAllShards().size();
		} catch (JedisException ex) {
			brokenJedis = true;
			throw ex;
		} finally {
			returnResource(jedis, brokenJedis);
		}
	}

	/**
	 * Clear cache across all shards. Useful for data center fail-over scenarios.
	 */
	public static void flushAll() {

		boolean brokenJedis = false;
		ShardedJedis jedis = getResource();

		try {

			Collection<Jedis> shards = jedis.getAllShards();

			for (Jedis shard : shards) {
				shard.flushAll();
			}

		} catch (JedisException ex) {

			brokenJedis = true;
			throw ex;

		} finally {
			returnResource(jedis, brokenJedis);
		}
	}

	public static boolean isAlive() {

		boolean atLeastOneShardIsUp = false;
		boolean brokenJedis = false;
		ShardedJedis jedis = getResource();

		try {

			Collection<Jedis> shards = jedis.getAllShards();

			for (Jedis shard : shards) {
				try {
					shard.set("test", "test");
					atLeastOneShardIsUp |= shard.get("test").equals("test");
				} catch (JedisException ex) {
					brokenJedis = true;
				}
			}

		} catch (Exception ex) {
			brokenJedis = true;
		} finally {
			returnResource(jedis, brokenJedis);
		}

		return atLeastOneShardIsUp;
	}
}
