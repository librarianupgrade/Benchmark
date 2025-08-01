/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.distributedlog.lock;

import static org.apache.distributedlog.lock.ZKSessionLock.asyncParseClientID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.bookkeeper.common.concurrent.FutureEventListener;
import org.apache.bookkeeper.common.util.OrderedScheduler;
import org.apache.bookkeeper.stats.NullStatsLogger;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.distributedlog.TestDistributedLogBase;
import org.apache.distributedlog.ZooKeeperClient;
import org.apache.distributedlog.ZooKeeperClientBuilder;
import org.apache.distributedlog.ZooKeeperClientUtils;
import org.apache.distributedlog.exceptions.LockingException;
import org.apache.distributedlog.exceptions.OwnershipAcquireFailedException;
import org.apache.distributedlog.exceptions.UnexpectedException;
import org.apache.distributedlog.util.FailpointUtils;
import org.apache.distributedlog.util.Utils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Distributed Lock Tests.
 */
public class TestDistributedLock extends TestDistributedLogBase {

	private static final Logger logger = LoggerFactory.getLogger(TestDistributedLock.class);

	@Rule
	public TestName runtime = new TestName();

	private static final int sessionTimeoutMs = 2000;

	private ZooKeeperClient zkc;
	private ZooKeeperClient zkc0; // used for checking
	private OrderedScheduler lockStateExecutor;

	@Before
	public void setup() throws Exception {
		zkc = ZooKeeperClientBuilder.newBuilder().name("zkc").uri(createDLMURI("/")).sessionTimeoutMs(sessionTimeoutMs)
				.zkAclId(null).build();
		zkc0 = ZooKeeperClientBuilder.newBuilder().name("zkc0").uri(createDLMURI("/"))
				.sessionTimeoutMs(sessionTimeoutMs).zkAclId(null).build();
		lockStateExecutor = OrderedScheduler.newSchedulerBuilder().name("test-scheduer").numThreads(1).build();
	}

	@After
	public void teardown() throws Exception {
		zkc.close();
		zkc0.close();
		lockStateExecutor.shutdown();
	}

	private static void createLockPath(ZooKeeper zk, String lockPath) throws Exception {
		zk.create(lockPath, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
	}

	private static List<String> getLockWaiters(ZooKeeperClient zkc, String lockPath) throws Exception {
		List<String> children = zkc.get().getChildren(lockPath, false);
		Collections.sort(children, ZKSessionLock.MEMBER_COMPARATOR);
		return children;
	}

	static class TestLockFactory {
		final String lockPath;
		final String clientId;
		final OrderedScheduler lockStateExecutor;

		public TestLockFactory(String name, ZooKeeperClient defaultZkc, OrderedScheduler lockStateExecutor)
				throws Exception {
			this.lockPath = "/" + name + System.currentTimeMillis();
			this.clientId = name;
			createLockPath(defaultZkc.get(), lockPath);
			this.lockStateExecutor = lockStateExecutor;

		}

		public ZKDistributedLock createLock(int id, ZooKeeperClient zkc) throws Exception {
			SessionLockFactory lockFactory = new ZKSessionLockFactory(zkc, clientId + id, lockStateExecutor, 0,
					Long.MAX_VALUE, sessionTimeoutMs, NullStatsLogger.INSTANCE);
			return new ZKDistributedLock(this.lockStateExecutor, lockFactory, this.lockPath, Long.MAX_VALUE,
					NullStatsLogger.INSTANCE);
		}

		public String getLockPath() {
			return lockPath;
		}
	}

	static class CountDownThrowFailPointAction extends FailpointUtils.AbstractFailPointAction {

		final AtomicInteger successCounter;
		final AtomicInteger failureCounter;

		CountDownThrowFailPointAction(int successCount, int failureCount) {
			this.successCounter = new AtomicInteger(successCount);
			this.failureCounter = new AtomicInteger(failureCount);
		}

		@Override
		public boolean checkFailPoint() throws IOException {
			int successCount = successCounter.getAndDecrement();
			if (successCount > 0) {
				return true;
			}
			int count = failureCounter.getAndDecrement();
			if (count > 0) {
				throw new IOException("counter = " + count);
			}
			return true;
		}
	}

	private SessionLockFactory createLockFactory(String clientId, ZooKeeperClient zkc) {
		return createLockFactory(clientId, zkc, Long.MAX_VALUE, 0);
	}

	private SessionLockFactory createLockFactory(String clientId, ZooKeeperClient zkc, long lockTimeoutMs,
			int recreationTimes) {
		return new ZKSessionLockFactory(zkc, clientId, lockStateExecutor, recreationTimes, lockTimeoutMs,
				sessionTimeoutMs, NullStatsLogger.INSTANCE);
	}

	private static void checkLockAndReacquire(ZKDistributedLock lock, boolean sync) throws Exception {
		lock.checkOwnershipAndReacquire();
		CompletableFuture<ZKDistributedLock> reacquireFuture = lock.getLockReacquireFuture();
		if (null != reacquireFuture && sync) {
			Utils.ioResult(reacquireFuture);
		}
	}

	@Test(timeout = 60000)
	public void testZooKeeperConnectionLossOnLockCreation() throws Exception {
		String lockPath = "/test-zookeeper-connection-loss-on-lock-creation-" + System.currentTimeMillis();
		String clientId = "zookeeper-connection-loss";

		createLockPath(zkc.get(), lockPath);

		FailpointUtils.setFailpoint(FailpointUtils.FailPointName.FP_ZooKeeperConnectionLoss,
				new CountDownThrowFailPointAction(0, Integer.MAX_VALUE));
		SessionLockFactory lockFactory = createLockFactory(clientId, zkc, Long.MAX_VALUE, 0);
		try {
			try {
				ZKDistributedLock lock = new ZKDistributedLock(lockStateExecutor, lockFactory, lockPath, Long.MAX_VALUE,
						NullStatsLogger.INSTANCE);
				Utils.ioResult(lock.asyncAcquire());
				fail("Should fail on creating lock if couldn't establishing connections to zookeeper");
			} catch (IOException ioe) {
				// expected.
			}
		} finally {
			FailpointUtils.removeFailpoint(FailpointUtils.FailPointName.FP_ZooKeeperConnectionLoss);
		}

		FailpointUtils.setFailpoint(FailpointUtils.FailPointName.FP_ZooKeeperConnectionLoss,
				new CountDownThrowFailPointAction(0, Integer.MAX_VALUE));
		lockFactory = createLockFactory(clientId, zkc, Long.MAX_VALUE, 3);
		try {
			try {
				ZKDistributedLock lock = new ZKDistributedLock(lockStateExecutor, lockFactory, lockPath, Long.MAX_VALUE,
						NullStatsLogger.INSTANCE);
				Utils.ioResult(lock.asyncAcquire());
				fail("Should fail on creating lock if couldn't establishing connections to zookeeper after 3 retries");
			} catch (IOException ioe) {
				// expected.
			}
		} finally {
			FailpointUtils.removeFailpoint(FailpointUtils.FailPointName.FP_ZooKeeperConnectionLoss);
		}

		FailpointUtils.setFailpoint(FailpointUtils.FailPointName.FP_ZooKeeperConnectionLoss,
				new CountDownThrowFailPointAction(0, 3));
		lockFactory = createLockFactory(clientId, zkc, Long.MAX_VALUE, 5);
		try {
			ZKDistributedLock lock = new ZKDistributedLock(lockStateExecutor, lockFactory, lockPath, Long.MAX_VALUE,
					NullStatsLogger.INSTANCE);
			Utils.ioResult(lock.asyncAcquire());

			Pair<String, Long> lockId1 = ((ZKSessionLock) lock.getInternalLock()).getLockId();

			List<String> children = getLockWaiters(zkc, lockPath);
			assertEquals(1, children.size());
			assertTrue(lock.haveLock());
			assertEquals(lockId1, Utils.ioResult(asyncParseClientID(zkc0.get(), lockPath, children.get(0))));

			lock.asyncClose();
		} finally {
			FailpointUtils.removeFailpoint(FailpointUtils.FailPointName.FP_ZooKeeperConnectionLoss);
		}
	}

	@Test(timeout = 60000)
	public void testBasicAcquireRelease() throws Exception {
		String lockPath = "/test-basic-acquire-release-" + System.currentTimeMillis();
		String clientId = "basic-acquire-release";

		createLockPath(zkc.get(), lockPath);

		SessionLockFactory lockFactory = createLockFactory(clientId, zkc);
		ZKDistributedLock lock = new ZKDistributedLock(lockStateExecutor, lockFactory, lockPath, Long.MAX_VALUE,
				NullStatsLogger.INSTANCE);
		Utils.ioResult(lock.asyncAcquire());

		Pair<String, Long> lockId1 = ((ZKSessionLock) lock.getInternalLock()).getLockId();

		List<String> children = getLockWaiters(zkc, lockPath);
		assertEquals(1, children.size());
		assertTrue(lock.haveLock());
		assertEquals(lockId1, Utils.ioResult(asyncParseClientID(zkc0.get(), lockPath, children.get(0))));

		Utils.ioResult(lock.asyncClose());

		children = getLockWaiters(zkc, lockPath);
		assertEquals(0, children.size());
		assertFalse(lock.haveLock());

		lock = new ZKDistributedLock(lockStateExecutor, lockFactory, lockPath, Long.MAX_VALUE,
				NullStatsLogger.INSTANCE);
		Utils.ioResult(lock.asyncAcquire());

		Pair<String, Long> lockId2 = ((ZKSessionLock) lock.getInternalLock()).getLockId();

		children = getLockWaiters(zkc, lockPath);
		assertEquals(1, children.size());
		assertTrue(lock.haveLock());
		assertEquals(lockId2, Utils.ioResult(asyncParseClientID(zkc0.get(), lockPath, children.get(0))));

		assertEquals(lockId1, lockId2);

		Utils.ioResult(lock.asyncClose());

		children = getLockWaiters(zkc, lockPath);
		assertEquals(0, children.size());
		assertFalse(lock.haveLock());

		try {
			Utils.ioResult(lock.asyncAcquire());
			fail("Should fail on acquiring a closed lock");
		} catch (UnexpectedException le) {
			// expected.
		}
		children = getLockWaiters(zkc, lockPath);
		assertEquals(0, children.size());
		assertFalse(lock.haveLock());
	}

	@Test(timeout = 60000)
	public void testCheckWriteLockFailureWhenLockIsAcquiredByOthers() throws Exception {
		String lockPath = "/test-check-write-lock-failure-when-lock-is-acquired-by-others-"
				+ System.currentTimeMillis();
		String clientId = "test-check-write-lock-failure";

		createLockPath(zkc.get(), lockPath);

		SessionLockFactory lockFactory0 = createLockFactory(clientId, zkc0);
		ZKDistributedLock lock0 = new ZKDistributedLock(lockStateExecutor, lockFactory0, lockPath, Long.MAX_VALUE,
				NullStatsLogger.INSTANCE);
		Utils.ioResult(lock0.asyncAcquire());

		Pair<String, Long> lockId0_1 = ((ZKSessionLock) lock0.getInternalLock()).getLockId();

		List<String> children = getLockWaiters(zkc, lockPath);
		assertEquals(1, children.size());
		assertTrue(lock0.haveLock());
		assertEquals(lockId0_1, Utils.ioResult(asyncParseClientID(zkc0.get(), lockPath, children.get(0))));

		// expire the session
		ZooKeeperClientUtils.expireSession(zkc0, zkServers, sessionTimeoutMs);

		// reacquire the lock and wait reacquire completed
		checkLockAndReacquire(lock0, true);

		Pair<String, Long> lockId0_2 = ((ZKSessionLock) lock0.getInternalLock()).getLockId();
		assertFalse("New lock should be created under different session", lockId0_1.equals(lockId0_2));

		children = getLockWaiters(zkc, lockPath);
		assertEquals(1, children.size());
		assertTrue(lock0.haveLock());
		assertEquals(lockId0_2, Utils.ioResult(asyncParseClientID(zkc0.get(), lockPath, children.get(0))));

		SessionLockFactory lockFactory = createLockFactory(clientId, zkc);
		final ZKDistributedLock lock1 = new ZKDistributedLock(lockStateExecutor, lockFactory, lockPath, Long.MAX_VALUE,
				NullStatsLogger.INSTANCE);
		final CountDownLatch lockLatch = new CountDownLatch(1);
		Thread lockThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Utils.ioResult(lock1.asyncAcquire());
					lockLatch.countDown();
				} catch (Exception e) {
					logger.error("Failed on locking lock1 : ", e);
				}
			}
		}, "lock-thread");
		lockThread.start();

		// ensure lock1 is waiting for lock0
		do {
			Thread.sleep(1);
			children = getLockWaiters(zkc, lockPath);
		} while (children.size() < 2);

		// expire the session
		ZooKeeperClientUtils.expireSession(zkc0, zkServers, sessionTimeoutMs);

		lockLatch.await();
		lockThread.join();

		try {
			checkLockAndReacquire(lock0, true);
			fail("Should fail on checking write lock since lock is acquired by lock1");
		} catch (LockingException le) {
			// expected
		}

		try {
			checkLockAndReacquire(lock0, false);
			fail("Should fail on checking write lock since lock is acquired by lock1");
		} catch (LockingException le) {
			// expected
		}
	}

	/**
	 * If no lock is acquired between session expired and re-acquisition, check write lock will acquire the lock.
	 * @throws Exception
	 */
	@Test(timeout = 60000)
	public void testLockReacquireSuccessAfterCheckWriteLock() throws Exception {
		testLockReacquireSuccess(true);
	}

	/**
	 * If no lock is acquired between session expired and re-acquisition, check write lock will acquire the lock.
	 * @throws Exception
	 */
	@Test(timeout = 60000)
	public void testLockReacquireSuccessWithoutCheckWriteLock() throws Exception {
		testLockReacquireSuccess(false);
	}

	private void testLockReacquireSuccess(boolean checkOwnershipAndReacquire) throws Exception {
		String lockPath = "/test-lock-re-acquire-success-" + checkOwnershipAndReacquire + "-"
				+ System.currentTimeMillis();
		String clientId = "test-lock-re-acquire";

		createLockPath(zkc.get(), lockPath);

		SessionLockFactory lockFactory0 = createLockFactory(clientId, zkc0);
		ZKDistributedLock lock0 = new ZKDistributedLock(lockStateExecutor, lockFactory0, lockPath, Long.MAX_VALUE,
				NullStatsLogger.INSTANCE);
		Utils.ioResult(lock0.asyncAcquire());

		Pair<String, Long> lockId0_1 = ((ZKSessionLock) lock0.getInternalLock()).getLockId();

		List<String> children = getLockWaiters(zkc, lockPath);
		assertEquals(1, children.size());
		assertTrue(lock0.haveLock());
		assertEquals(lockId0_1, Utils.ioResult(asyncParseClientID(zkc0.get(), lockPath, children.get(0))));

		ZooKeeperClientUtils.expireSession(zkc0, zkServers, sessionTimeoutMs);

		if (checkOwnershipAndReacquire) {
			checkLockAndReacquire(lock0, true);
			checkLockAndReacquire(lock0, false);
		} else {
			// session expire will trigger lock re-acquisition
			CompletableFuture<ZKDistributedLock> asyncLockAcquireFuture;
			do {
				Thread.sleep(1);
				asyncLockAcquireFuture = lock0.getLockReacquireFuture();
			} while (null == asyncLockAcquireFuture && lock0.getReacquireCount() < 1);
			if (null != asyncLockAcquireFuture) {
				Utils.ioResult(asyncLockAcquireFuture);
			}
			checkLockAndReacquire(lock0, false);
		}
		children = getLockWaiters(zkc, lockPath);
		assertEquals(1, children.size());
		assertTrue(lock0.haveLock());
		Pair<String, Long> lock0_2 = ((ZKSessionLock) lock0.getInternalLock()).getLockId();
		assertEquals(lock0_2, Utils.ioResult(asyncParseClientID(zkc.get(), lockPath, children.get(0))));
		assertEquals(clientId, lock0_2.getLeft());
		assertFalse(lockId0_1.equals(lock0_2));

		Utils.ioResult(lock0.asyncClose());

		children = getLockWaiters(zkc, lockPath);
		assertEquals(0, children.size());
	}

	/**
	 * If lock is acquired between session expired and re-acquisition, check write lock will be failed.
	 * @throws Exception
	 */
	@Test(timeout = 60000)
	public void testLockReacquireFailureAfterCheckWriteLock() throws Exception {
		testLockReacquireFailure(true);
	}

	/**
	 * If lock is acquired between session expired and re-acquisition, check write lock will be failed.
	 * @throws Exception
	 */
	@Test(timeout = 60000)
	public void testLockReacquireFailureWithoutCheckWriteLock() throws Exception {
		testLockReacquireFailure(false);
	}

	private void testLockReacquireFailure(boolean checkOwnershipAndReacquire) throws Exception {
		String lockPath = "/test-lock-re-acquire-failure-" + checkOwnershipAndReacquire + "-"
				+ System.currentTimeMillis();
		String clientId = "test-lock-re-acquire";

		createLockPath(zkc.get(), lockPath);

		SessionLockFactory lockFactory0 = createLockFactory(clientId, zkc0);
		ZKDistributedLock lock0 = new ZKDistributedLock(lockStateExecutor, lockFactory0, lockPath, Long.MAX_VALUE,
				NullStatsLogger.INSTANCE);
		Utils.ioResult(lock0.asyncAcquire());

		final CountDownLatch lock1DoneLatch = new CountDownLatch(1);
		SessionLockFactory lockFactory1 = createLockFactory(clientId, zkc);
		final ZKDistributedLock lock1 = new ZKDistributedLock(lockStateExecutor, lockFactory1, lockPath, Long.MAX_VALUE,
				NullStatsLogger.INSTANCE);
		Thread lock1Thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Utils.ioResult(lock1.asyncAcquire());
					lock1DoneLatch.countDown();
				} catch (Exception e) {
					logger.error("Error on acquiring lock1 : ", e);
				}
			}
		}, "lock1-thread");
		lock1Thread.start();

		List<String> children;
		do {
			Thread.sleep(1);
			children = getLockWaiters(zkc, lockPath);
		} while (children.size() < 2);
		assertEquals(2, children.size());
		assertTrue(lock0.haveLock());
		assertFalse(lock1.haveLock());
		assertEquals(((ZKSessionLock) lock0.getInternalLock()).getLockId(),
				Utils.ioResult(asyncParseClientID(zkc0.get(), lockPath, children.get(0))));
		assertEquals(((ZKSessionLock) lock1.getInternalLock()).getLockId(),
				Utils.ioResult(asyncParseClientID(zkc.get(), lockPath, children.get(1))));

		logger.info("Expiring session on lock0");
		ZooKeeperClientUtils.expireSession(zkc0, zkServers, sessionTimeoutMs);
		logger.info("Session on lock0 is expired");
		lock1DoneLatch.await();
		assertFalse(lock0.haveLock());
		assertTrue(lock1.haveLock());

		if (checkOwnershipAndReacquire) {
			try {
				checkLockAndReacquire(lock0, true);
				fail("Should fail check write lock since lock is already held by other people");
			} catch (OwnershipAcquireFailedException oafe) {
				assertEquals(((ZKSessionLock) lock1.getInternalLock()).getLockId().getLeft(), oafe.getCurrentOwner());
			}
			try {
				checkLockAndReacquire(lock0, false);
				fail("Should fail check write lock since lock is already held by other people");
			} catch (OwnershipAcquireFailedException oafe) {
				assertEquals(((ZKSessionLock) lock1.getInternalLock()).getLockId().getLeft(), oafe.getCurrentOwner());
			}
		} else {
			logger.info("Waiting lock0 to attempt acquisition after session expired");
			// session expire will trigger lock re-acquisition
			CompletableFuture<ZKDistributedLock> asyncLockAcquireFuture;
			do {
				Thread.sleep(1);
				asyncLockAcquireFuture = lock0.getLockReacquireFuture();
			} while (null == asyncLockAcquireFuture);

			try {
				Utils.ioResult(asyncLockAcquireFuture);
				fail("Should fail check write lock since lock is already held by other people");
			} catch (OwnershipAcquireFailedException oafe) {
				assertEquals(((ZKSessionLock) lock1.getInternalLock()).getLockId().getLeft(), oafe.getCurrentOwner());
			}
			try {
				checkLockAndReacquire(lock0, false);
				fail("Should fail check write lock since lock is already held by other people");
			} catch (OwnershipAcquireFailedException oafe) {
				assertEquals(((ZKSessionLock) lock1.getInternalLock()).getLockId().getLeft(), oafe.getCurrentOwner());
			}
		}
		children = getLockWaiters(zkc, lockPath);
		assertEquals(1, children.size());
		assertFalse(lock0.haveLock());
		assertTrue(lock1.haveLock());
		assertEquals(((ZKSessionLock) lock1.getInternalLock()).getLockId(),
				Utils.ioResult(asyncParseClientID(zkc.get(), lockPath, children.get(0))));

		Utils.ioResult(lock0.asyncClose());
		Utils.ioResult(lock1.asyncClose());

		children = getLockWaiters(zkc, lockPath);
		assertEquals(0, children.size());
	}

	@Test(timeout = 60000)
	public void testLockReacquire() throws Exception {
		String lockPath = "/reacquirePath";
		Utils.zkCreateFullPathOptimistic(zkc, lockPath, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
				CreateMode.PERSISTENT);
		String clientId = "lockHolder";
		SessionLockFactory lockFactory = createLockFactory(clientId, zkc, conf.getLockTimeoutMilliSeconds(), 0);
		ZKDistributedLock lock = new ZKDistributedLock(lockStateExecutor, lockFactory, lockPath,
				conf.getLockTimeoutMilliSeconds(), NullStatsLogger.INSTANCE);
		Utils.ioResult(lock.asyncAcquire());

		// try and cleanup the underlying lock
		lock.getInternalLock().unlock();

		// This should reacquire the lock
		checkLockAndReacquire(lock, true);

		assertEquals(true, lock.haveLock());
		assertEquals(true, lock.getInternalLock().isLockHeld());

		lockFactory = createLockFactory(clientId + "_2", zkc, conf.getLockTimeoutMilliSeconds(), 0);
		ZKDistributedLock lock2 = new ZKDistributedLock(lockStateExecutor, lockFactory, lockPath, 0,
				NullStatsLogger.INSTANCE);

		boolean exceptionEncountered = false;
		try {
			Utils.ioResult(lock2.asyncAcquire());
		} catch (OwnershipAcquireFailedException exc) {
			assertEquals(clientId, exc.getCurrentOwner());
			exceptionEncountered = true;
		}
		assertTrue(exceptionEncountered);
		Utils.ioResult(lock.asyncClose());
		Utils.ioResult(lock2.asyncClose());
	}

	@Test(timeout = 60000)
	public void testLockReacquireMultiple() throws Exception {
		String lockPath = "/reacquirePathMultiple";
		Utils.zkCreateFullPathOptimistic(zkc, lockPath, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
				CreateMode.PERSISTENT);
		String clientId = "lockHolder";
		SessionLockFactory factory = createLockFactory(clientId, zkc, conf.getLockTimeoutMilliSeconds(), 0);
		ZKDistributedLock lock = new ZKDistributedLock(lockStateExecutor, factory, lockPath,
				conf.getLockTimeoutMilliSeconds(), NullStatsLogger.INSTANCE);
		Utils.ioResult(lock.asyncAcquire());

		// try and cleanup the underlying lock
		lock.getInternalLock().unlock();

		// This should reacquire the lock
		checkLockAndReacquire(lock, true);

		assertEquals(true, lock.haveLock());
		assertEquals(true, lock.getInternalLock().isLockHeld());

		factory = createLockFactory(clientId + "_2", zkc, 0, 0);
		ZKDistributedLock lock2 = new ZKDistributedLock(lockStateExecutor, factory, lockPath, 0,
				NullStatsLogger.INSTANCE);

		boolean exceptionEncountered = false;
		try {
			Utils.ioResult(lock2.asyncAcquire());
		} catch (OwnershipAcquireFailedException exc) {
			assertEquals(clientId, exc.getCurrentOwner());
			exceptionEncountered = true;
		}
		assertTrue(exceptionEncountered);
		Utils.ioResult(lock2.asyncClose());

		Utils.ioResult(lock.asyncClose());
		assertEquals(false, lock.haveLock());
		assertEquals(false, lock.getInternalLock().isLockHeld());

		factory = createLockFactory(clientId + "_3", zkc, 0, 0);
		ZKDistributedLock lock3 = new ZKDistributedLock(lockStateExecutor, factory, lockPath, 0,
				NullStatsLogger.INSTANCE);

		Utils.ioResult(lock3.asyncAcquire());
		assertEquals(true, lock3.haveLock());
		assertEquals(true, lock3.getInternalLock().isLockHeld());
		Utils.ioResult(lock3.asyncClose());
	}

	void assertLatchesSet(CountDownLatch[] latches, int endIndex) {
		for (int i = 0; i < endIndex; i++) {
			assertEquals("latch " + i + " should have been set", 0, latches[i].getCount());
		}
		for (int i = endIndex; i < latches.length; i++) {
			assertEquals("latch " + i + " should not have been set", 1, latches[i].getCount());
		}
	}

	// Assert key lock state (is locked, is internal locked, lock count, etc.) for two dlocks.
	void assertLockState(ZKDistributedLock lock0, boolean owned0, boolean intOwned0, ZKDistributedLock lock1,
			boolean owned1, boolean intOwned1, int waiters, String lockPath) throws Exception {
		assertEquals(owned0, lock0.haveLock());
		assertEquals(intOwned0, lock0.getInternalLock() != null && lock0.getInternalLock().isLockHeld());
		assertEquals(owned1, lock1.haveLock());
		assertEquals(intOwned1, lock1.getInternalLock() != null && lock1.getInternalLock().isLockHeld());
		assertEquals(waiters, getLockWaiters(zkc, lockPath).size());
	}

	@Test(timeout = 60000)
	public void testAsyncAcquireBasics() throws Exception {
		TestLockFactory locks = new TestLockFactory(runtime.getMethodName(), zkc, lockStateExecutor);

		int count = 3;
		ArrayList<CompletableFuture<ZKDistributedLock>> results = new ArrayList<CompletableFuture<ZKDistributedLock>>(
				count);
		ZKDistributedLock[] lockArray = new ZKDistributedLock[count];
		final CountDownLatch[] latches = new CountDownLatch[count];

		// Set up <count> waiters, save async results, count down a latch when lock is acquired in
		// the future.
		for (int i = 0; i < count; i++) {
			latches[i] = new CountDownLatch(1);
			lockArray[i] = locks.createLock(i, zkc);
			final int index = i;
			results.add(lockArray[i].asyncAcquire().whenComplete(new FutureEventListener<ZKDistributedLock>() {
				@Override
				public void onSuccess(ZKDistributedLock lock) {
					latches[index].countDown();
				}

				@Override
				public void onFailure(Throwable cause) {
					fail("unexpected failure " + cause);
				}
			}));
		}

		// Now await ownership and release ownership of locks one by one (in the order they were
		// acquired).
		for (int i = 0; i < count; i++) {
			latches[i].await();
			assertLatchesSet(latches, i + 1);
			Utils.ioResult(results.get(i));
			Utils.ioResult(lockArray[i].asyncClose());
		}
	}

	@Test(timeout = 60000)
	public void testAsyncAcquireSyncThenAsyncOnSameLock() throws Exception {
		TestLockFactory locks = new TestLockFactory(runtime.getMethodName(), zkc, lockStateExecutor);
		final ZKDistributedLock lock0 = locks.createLock(0, zkc);
		final ZKDistributedLock lock1 = locks.createLock(1, zkc0);

		Utils.ioResult(lock0.asyncAcquire());

		// Initial state.
		assertLockState(lock0, true, true, lock1, false, false, 1, locks.getLockPath());

		Thread lock1Thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Utils.ioResult(lock1.asyncAcquire());
				} catch (Exception e) {
					fail("shouldn't fail to acquire");
				}
			}
		}, "lock1-thread");
		lock1Thread.start();

		// Wait for lock count to increase, indicating background acquire has succeeded.
		while (getLockWaiters(zkc, locks.getLockPath()).size() < 2) {
			Thread.sleep(1);
		}
		assertLockState(lock0, true, true, lock1, false, false, 2, locks.getLockPath());

		Utils.ioResult(lock0.asyncClose());
		Utils.ioResult(lock1.getLockAcquireFuture());

		assertLockState(lock0, false, false, lock1, true, true, 1, locks.getLockPath());

		// Release lock1
		Utils.ioResult(lock1.asyncClose());
		assertLockState(lock0, false, false, lock1, false, false, 0, locks.getLockPath());
	}

	@Test(timeout = 60000)
	public void testAsyncAcquireExpireDuringWait() throws Exception {
		TestLockFactory locks = new TestLockFactory(runtime.getMethodName(), zkc, lockStateExecutor);
		final ZKDistributedLock lock0 = locks.createLock(0, zkc);
		final ZKDistributedLock lock1 = locks.createLock(1, zkc0);

		Utils.ioResult(lock0.asyncAcquire());
		CompletableFuture<ZKDistributedLock> result = lock1.asyncAcquire();
		// make sure we place a waiter for lock1
		while (null == lock1.getLockWaiter()) {
			TimeUnit.MILLISECONDS.sleep(20);
		}

		// Expire causes acquire future to be failed and unset.
		ZooKeeperClientUtils.expireSession(zkc0, zkServers, sessionTimeoutMs);
		try {
			Utils.ioResult(result);
			fail("future should have been failed");
		} catch (OwnershipAcquireFailedException ex) {
		}

		assertLockState(lock0, true, true, lock1, false, false, 1, locks.getLockPath());
		lock0.asyncClose();
		lock1.asyncClose();
	}

	@Test(timeout = 60000)
	public void testAsyncAcquireCloseDuringWait() throws Exception {
		TestLockFactory locks = new TestLockFactory(runtime.getMethodName(), zkc, lockStateExecutor);
		final ZKDistributedLock lock0 = locks.createLock(0, zkc);
		final ZKDistributedLock lock1 = locks.createLock(1, zkc0);

		Utils.ioResult(lock0.asyncAcquire());
		CompletableFuture<ZKDistributedLock> result = lock1.asyncAcquire();
		Utils.ioResult(lock1.asyncClose());
		try {
			Utils.ioResult(result);
			fail("future should have been failed");
		} catch (LockClosedException ex) {
		}

		assertLockState(lock0, true, true, lock1, false, false, 1, locks.getLockPath());
		lock0.asyncClose();
	}

	@Test(timeout = 60000)
	public void testAsyncAcquireCloseAfterAcquire() throws Exception {
		TestLockFactory locks = new TestLockFactory(runtime.getMethodName(), zkc, lockStateExecutor);
		final ZKDistributedLock lock0 = locks.createLock(0, zkc);

		CompletableFuture<ZKDistributedLock> result = lock0.asyncAcquire();
		Utils.ioResult(result);
		Utils.ioResult(lock0.asyncClose());

		// Already have this, stays satisfied.
		Utils.ioResult(result);

		// But we no longer have the lock.
		assertEquals(false, lock0.haveLock());
		assertEquals(false, lock0.getInternalLock().isLockHeld());
	}
}
