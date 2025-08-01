/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.bookkeeper.replication;

import static org.apache.bookkeeper.replication.ReplicationStats.NUM_ENTRIES_UNABLE_TO_READ_FOR_REPLICATION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URI;
import java.util.Enumeration;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Cleanup;
import org.apache.bookkeeper.client.BKException;
import org.apache.bookkeeper.client.BookKeeper;
import org.apache.bookkeeper.client.ClientUtil;
import org.apache.bookkeeper.client.LedgerEntry;
import org.apache.bookkeeper.client.LedgerHandle;
import org.apache.bookkeeper.common.util.OrderedScheduler;
import org.apache.bookkeeper.conf.ServerConfiguration;
import org.apache.bookkeeper.meta.AbstractZkLedgerManager;
import org.apache.bookkeeper.meta.LedgerManager;
import org.apache.bookkeeper.meta.LedgerManagerFactory;
import org.apache.bookkeeper.meta.LedgerUnderreplicationManager;
import org.apache.bookkeeper.meta.MetadataBookieDriver;
import org.apache.bookkeeper.meta.MetadataClientDriver;
import org.apache.bookkeeper.meta.MetadataDrivers;
import org.apache.bookkeeper.meta.ZkLedgerUnderreplicationManager;
import org.apache.bookkeeper.meta.zk.ZKMetadataDriverBase;
import org.apache.bookkeeper.net.BookieId;
import org.apache.bookkeeper.replication.ReplicationException.CompatibilityException;
import org.apache.bookkeeper.stats.Counter;
import org.apache.bookkeeper.stats.NullStatsLogger;
import org.apache.bookkeeper.stats.StatsLogger;
import org.apache.bookkeeper.test.BookKeeperClusterTestCase;
import org.apache.bookkeeper.test.TestStatsProvider;
import org.apache.bookkeeper.test.TestStatsProvider.TestStatsLogger;
import org.apache.bookkeeper.util.BookKeeperConstants;
import org.apache.bookkeeper.zookeeper.BoundExponentialBackoffRetryPolicy;
import org.apache.bookkeeper.zookeeper.ZooKeeperClient;
import org.apache.bookkeeper.zookeeper.ZooKeeperWatcherBase;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.zookeeper.AsyncCallback.StatCallback;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooKeeper.States;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test the ReplicationWroker, where it has to replicate the fragments from
 * failed Bookies to given target Bookie.
 */
public class TestReplicationWorker extends BookKeeperClusterTestCase {

	private static final byte[] TESTPASSWD = "testpasswd".getBytes();
	private static final Logger LOG = LoggerFactory.getLogger(TestReplicationWorker.class);
	private String basePath = "";
	private String baseLockPath = "";
	private MetadataBookieDriver driver;
	private LedgerManagerFactory mFactory;
	private LedgerUnderreplicationManager underReplicationManager;
	private LedgerManager ledgerManager;
	private static byte[] data = "TestReplicationWorker".getBytes();
	private OrderedScheduler scheduler;
	private String zkLedgersRootPath;

	public TestReplicationWorker() {
		this("org.apache.bookkeeper.meta.HierarchicalLedgerManagerFactory");
	}

	TestReplicationWorker(String ledgerManagerFactory) {
		super(3, 300);
		LOG.info("Running test case using ledger manager : " + ledgerManagerFactory);
		// set ledger manager name
		baseConf.setLedgerManagerFactoryClassName(ledgerManagerFactory);
		baseClientConf.setLedgerManagerFactoryClassName(ledgerManagerFactory);
		baseConf.setRereplicationEntryBatchSize(3);
		baseConf.setZkTimeout(7000);
		baseConf.setZkRetryBackoffMaxMs(500);
		baseConf.setZkRetryBackoffStartMs(10);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();

		zkLedgersRootPath = ZKMetadataDriverBase.resolveZkLedgersRootPath(baseClientConf);
		basePath = zkLedgersRootPath + '/' + BookKeeperConstants.UNDER_REPLICATION_NODE
				+ BookKeeperConstants.DEFAULT_ZK_LEDGERS_ROOT_PATH;
		baseLockPath = zkLedgersRootPath + '/' + BookKeeperConstants.UNDER_REPLICATION_NODE + "/locks";

		this.scheduler = OrderedScheduler.newSchedulerBuilder().name("test-scheduler").numThreads(1).build();

		this.driver = MetadataDrivers.getBookieDriver(URI.create(baseConf.getMetadataServiceUri()));
		this.driver.initialize(baseConf, NullStatsLogger.INSTANCE);
		// initialize urReplicationManager
		mFactory = driver.getLedgerManagerFactory();
		ledgerManager = mFactory.newLedgerManager();
		underReplicationManager = mFactory.newLedgerUnderreplicationManager();
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		if (null != underReplicationManager) {
			underReplicationManager.close();
			underReplicationManager = null;
		}
		if (null != driver) {
			driver.close();
		}
	}

	/**
	 * Tests that replication worker should replicate the failed bookie
	 * fragments to target bookie given to the worker.
	 */
	@Test
	public void testRWShouldReplicateFragmentsToTargetBookie() throws Exception {
		LedgerHandle lh = bkc.createLedger(3, 3, BookKeeper.DigestType.CRC32, TESTPASSWD);

		for (int i = 0; i < 10; i++) {
			lh.addEntry(data);
		}
		BookieId replicaToKill = lh.getLedgerMetadata().getAllEnsembles().get(0L).get(0);

		LOG.info("Killing Bookie : {}", replicaToKill);
		killBookie(replicaToKill);

		BookieId newBkAddr = startNewBookieAndReturnBookieId();
		LOG.info("New Bookie addr : {}", newBkAddr);

		for (int i = 0; i < 10; i++) {
			lh.addEntry(data);
		}

		ReplicationWorker rw = new ReplicationWorker(baseConf);

		rw.start();
		try {

			underReplicationManager.markLedgerUnderreplicated(lh.getId(), replicaToKill.toString());

			while (ReplicationTestUtil.isLedgerInUnderReplication(zkc, lh.getId(), basePath)) {
				Thread.sleep(100);
			}

			killAllBookies(lh, newBkAddr);

			// Should be able to read the entries from 0-9
			verifyRecoveredLedgers(lh, 0, 9);
		} finally {
			rw.shutdown();
		}
	}

	/**
	 * Tests that replication worker should retry for replication until enough
	 * bookies available for replication.
	 */
	@Test
	public void testRWShouldRetryUntilThereAreEnoughBksAvailableForReplication() throws Exception {
		LedgerHandle lh = bkc.createLedger(1, 1, BookKeeper.DigestType.CRC32, TESTPASSWD);

		for (int i = 0; i < 10; i++) {
			lh.addEntry(data);
		}
		lh.close();
		BookieId replicaToKill = lh.getLedgerMetadata().getAllEnsembles().get(0L).get(0);
		LOG.info("Killing Bookie : {}", replicaToKill);
		ServerConfiguration killedBookieConfig = killBookie(replicaToKill);

		BookieId newBkAddr = startNewBookieAndReturnBookieId();
		LOG.info("New Bookie addr :" + newBkAddr);

		killAllBookies(lh, newBkAddr);
		ReplicationWorker rw = new ReplicationWorker(baseConf);

		rw.start();
		try {
			underReplicationManager.markLedgerUnderreplicated(lh.getId(), replicaToKill.toString());
			int counter = 30;
			while (counter-- > 0) {
				assertTrue("Expecting that replication should not complete",
						ReplicationTestUtil.isLedgerInUnderReplication(zkc, lh.getId(), basePath));
				Thread.sleep(100);
			}
			// restart killed bookie
			startAndAddBookie(killedBookieConfig);
			while (ReplicationTestUtil.isLedgerInUnderReplication(zkc, lh.getId(), basePath)) {
				Thread.sleep(100);
			}
			// Should be able to read the entries from 0-9
			verifyRecoveredLedgers(lh, 0, 9);
		} finally {
			rw.shutdown();
		}
	}

	/**
	 * Tests that replication worker1 should take one fragment replication and
	 * other replication worker also should compete for the replication.
	 */
	@Test
	public void test2RWsShouldCompeteForReplicationOf2FragmentsAndCompleteReplication() throws Exception {
		LedgerHandle lh = bkc.createLedger(2, 2, BookKeeper.DigestType.CRC32, TESTPASSWD);

		for (int i = 0; i < 10; i++) {
			lh.addEntry(data);
		}
		lh.close();
		BookieId replicaToKill = lh.getLedgerMetadata().getAllEnsembles().get(0L).get(0);
		LOG.info("Killing Bookie : {}", replicaToKill);
		ServerConfiguration killedBookieConfig = killBookie(replicaToKill);

		killAllBookies(lh, null);
		// Starte RW1
		BookieId newBkAddr1 = startNewBookieAndReturnBookieId();
		LOG.info("New Bookie addr : {}", newBkAddr1);
		ReplicationWorker rw1 = new ReplicationWorker(baseConf);

		// Starte RW2
		BookieId newBkAddr2 = startNewBookieAndReturnBookieId();
		LOG.info("New Bookie addr : {}", newBkAddr2);
		ReplicationWorker rw2 = new ReplicationWorker(baseConf);
		rw1.start();
		rw2.start();

		try {
			underReplicationManager.markLedgerUnderreplicated(lh.getId(), replicaToKill.toString());
			int counter = 10;
			while (counter-- > 0) {
				assertTrue("Expecting that replication should not complete",
						ReplicationTestUtil.isLedgerInUnderReplication(zkc, lh.getId(), basePath));
				Thread.sleep(100);
			}
			// restart killed bookie
			startAndAddBookie(killedBookieConfig);
			while (ReplicationTestUtil.isLedgerInUnderReplication(zkc, lh.getId(), basePath)) {
				Thread.sleep(100);
			}
			// Should be able to read the entries from 0-9
			verifyRecoveredLedgers(lh, 0, 9);
		} finally {
			rw1.shutdown();
			rw2.shutdown();
		}
	}

	/**
	 * Tests that Replication worker should clean the leadger under replication
	 * node of the ledger already deleted.
	 */
	@Test
	public void testRWShouldCleanTheLedgerFromUnderReplicationIfLedgerAlreadyDeleted() throws Exception {
		LedgerHandle lh = bkc.createLedger(2, 2, BookKeeper.DigestType.CRC32, TESTPASSWD);

		for (int i = 0; i < 10; i++) {
			lh.addEntry(data);
		}
		lh.close();
		BookieId replicaToKill = lh.getLedgerMetadata().getAllEnsembles().get(0L).get(0);
		LOG.info("Killing Bookie : {}", replicaToKill);
		killBookie(replicaToKill);

		BookieId newBkAddr = startNewBookieAndReturnBookieId();
		LOG.info("New Bookie addr : {}", newBkAddr);
		ReplicationWorker rw = new ReplicationWorker(baseConf);
		rw.start();

		try {
			bkc.deleteLedger(lh.getId()); // Deleting the ledger
			// Also mark ledger as in UnderReplication
			underReplicationManager.markLedgerUnderreplicated(lh.getId(), replicaToKill.toString());
			while (ReplicationTestUtil.isLedgerInUnderReplication(zkc, lh.getId(), basePath)) {
				Thread.sleep(100);
			}
		} finally {
			rw.shutdown();
		}

	}

	@Test
	public void testMultipleLedgerReplicationWithReplicationWorker() throws Exception {
		// Ledger1
		LedgerHandle lh1 = bkc.createLedger(3, 3, BookKeeper.DigestType.CRC32, TESTPASSWD);

		for (int i = 0; i < 10; i++) {
			lh1.addEntry(data);
		}
		BookieId replicaToKillFromFirstLedger = lh1.getLedgerMetadata().getAllEnsembles().get(0L).get(0);

		LOG.info("Killing Bookie : {}", replicaToKillFromFirstLedger);

		// Ledger2
		LedgerHandle lh2 = bkc.createLedger(3, 3, BookKeeper.DigestType.CRC32, TESTPASSWD);

		for (int i = 0; i < 10; i++) {
			lh2.addEntry(data);
		}
		BookieId replicaToKillFromSecondLedger = lh2.getLedgerMetadata().getAllEnsembles().get(0L).get(0);

		LOG.info("Killing Bookie : {}", replicaToKillFromSecondLedger);

		// Kill ledger1
		killBookie(replicaToKillFromFirstLedger);
		lh1.close();
		// Kill ledger2
		killBookie(replicaToKillFromFirstLedger);
		lh2.close();

		BookieId newBkAddr = startNewBookieAndReturnBookieId();
		LOG.info("New Bookie addr : {}", newBkAddr);

		ReplicationWorker rw = new ReplicationWorker(baseConf);

		rw.start();
		try {

			// Mark ledger1 and 2 as underreplicated
			underReplicationManager.markLedgerUnderreplicated(lh1.getId(), replicaToKillFromFirstLedger.toString());
			underReplicationManager.markLedgerUnderreplicated(lh2.getId(), replicaToKillFromSecondLedger.toString());

			while (ReplicationTestUtil.isLedgerInUnderReplication(zkc, lh1.getId(), basePath)) {
				Thread.sleep(100);
			}

			while (ReplicationTestUtil.isLedgerInUnderReplication(zkc, lh2.getId(), basePath)) {
				Thread.sleep(100);
			}

			killAllBookies(lh1, newBkAddr);

			// Should be able to read the entries from 0-9
			verifyRecoveredLedgers(lh1, 0, 9);
			verifyRecoveredLedgers(lh2, 0, 9);
		} finally {
			rw.shutdown();
		}

	}

	/**
	 * Tests that ReplicationWorker should fence the ledger and release ledger
	 * lock after timeout. Then replication should happen normally.
	 */
	@Test
	public void testRWShouldReplicateTheLedgersAfterTimeoutIfLastFragmentIsUR() throws Exception {
		LedgerHandle lh = bkc.createLedger(3, 3, BookKeeper.DigestType.CRC32, TESTPASSWD);

		for (int i = 0; i < 10; i++) {
			lh.addEntry(data);
		}
		BookieId replicaToKill = lh.getLedgerMetadata().getAllEnsembles().get(0L).get(0);

		LOG.info("Killing Bookie : {}", replicaToKill);
		killBookie(replicaToKill);

		BookieId newBkAddr = startNewBookieAndReturnBookieId();
		LOG.info("New Bookie addr : {}", newBkAddr);

		// set to 3s instead of default 30s
		baseConf.setOpenLedgerRereplicationGracePeriod("3000");
		ReplicationWorker rw = new ReplicationWorker(baseConf);

		@Cleanup
		MetadataClientDriver clientDriver = MetadataDrivers
				.getClientDriver(URI.create(baseClientConf.getMetadataServiceUri()));
		clientDriver.initialize(baseClientConf, scheduler, NullStatsLogger.INSTANCE, Optional.empty());

		LedgerManagerFactory mFactory = clientDriver.getLedgerManagerFactory();

		LedgerUnderreplicationManager underReplicationManager = mFactory.newLedgerUnderreplicationManager();
		rw.start();
		try {

			underReplicationManager.markLedgerUnderreplicated(lh.getId(), replicaToKill.toString());
			while (ReplicationTestUtil.isLedgerInUnderReplication(zkc, lh.getId(), basePath)) {
				Thread.sleep(100);
			}
			killAllBookies(lh, newBkAddr);
			// Should be able to read the entries from 0-9
			verifyRecoveredLedgers(lh, 0, 9);
			lh = bkc.openLedgerNoRecovery(lh.getId(), BookKeeper.DigestType.CRC32, TESTPASSWD);
			assertFalse("Ledger must have been closed by RW", ClientUtil.isLedgerOpen(lh));
		} finally {
			rw.shutdown();
			underReplicationManager.close();
		}

	}

	@Test
	public void testBookiesNotAvailableScenarioForReplicationWorker() throws Exception {
		int ensembleSize = 3;
		LedgerHandle lh = bkc.createLedger(ensembleSize, ensembleSize, BookKeeper.DigestType.CRC32, TESTPASSWD);

		int numOfEntries = 7;
		for (int i = 0; i < numOfEntries; i++) {
			lh.addEntry(data);
		}
		lh.close();

		BookieId[] bookiesKilled = new BookieId[ensembleSize];
		ServerConfiguration[] killedBookiesConfig = new ServerConfiguration[ensembleSize];

		// kill all bookies
		for (int i = 0; i < ensembleSize; i++) {
			bookiesKilled[i] = lh.getLedgerMetadata().getAllEnsembles().get(0L).get(i);
			killedBookiesConfig[i] = getBkConf(bookiesKilled[i]);
			LOG.info("Killing Bookie : {}", bookiesKilled[i]);
			killBookie(bookiesKilled[i]);
		}

		// start new bookiesToKill number of bookies
		for (int i = 0; i < ensembleSize; i++) {
			BookieId newBkAddr = startNewBookieAndReturnBookieId();
		}

		// create couple of replicationworkers
		ServerConfiguration newRWConf = new ServerConfiguration(baseConf);
		newRWConf.setLockReleaseOfFailedLedgerGracePeriod("64");
		ReplicationWorker rw1 = new ReplicationWorker(newRWConf);
		ReplicationWorker rw2 = new ReplicationWorker(newRWConf);

		@Cleanup
		MetadataClientDriver clientDriver = MetadataDrivers
				.getClientDriver(URI.create(baseClientConf.getMetadataServiceUri()));
		clientDriver.initialize(baseClientConf, scheduler, NullStatsLogger.INSTANCE, Optional.empty());

		LedgerManagerFactory mFactory = clientDriver.getLedgerManagerFactory();

		LedgerUnderreplicationManager underReplicationManager = mFactory.newLedgerUnderreplicationManager();
		try {
			//mark ledger underreplicated
			for (int i = 0; i < bookiesKilled.length; i++) {
				underReplicationManager.markLedgerUnderreplicated(lh.getId(), bookiesKilled[i].toString());
			}
			while (!ReplicationTestUtil.isLedgerInUnderReplication(zkc, lh.getId(), basePath)) {
				Thread.sleep(100);
			}
			rw1.start();
			rw2.start();

			AtomicBoolean isBookieRestarted = new AtomicBoolean(false);

			(new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(3000);
						isBookieRestarted.set(true);
						/*
						 * after sleeping for 3000 msecs, restart one of the
						 * bookie, so that replication can succeed.
						 */
						startBookie(killedBookiesConfig[0]);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			})).start();

			int rw1PrevFailedAttemptsCount = 0;
			int rw2PrevFailedAttemptsCount = 0;
			while (!isBookieRestarted.get()) {
				/*
				 * since all the bookies containing the ledger entries are down
				 * replication wouldnt have succeeded.
				 */
				assertTrue("Ledger: " + lh.getId() + " should be underreplicated",
						ReplicationTestUtil.isLedgerInUnderReplication(zkc, lh.getId(), basePath));

				// the number of failed attempts should have increased.
				int rw1CurFailedAttemptsCount = rw1.replicationFailedLedgers.get(lh.getId()).get();
				assertTrue(
						"The current number of failed attempts: " + rw1CurFailedAttemptsCount
								+ " should be greater than or equal to previous value: " + rw1PrevFailedAttemptsCount,
						rw1CurFailedAttemptsCount >= rw1PrevFailedAttemptsCount);
				rw1PrevFailedAttemptsCount = rw1CurFailedAttemptsCount;

				int rw2CurFailedAttemptsCount = rw2.replicationFailedLedgers.get(lh.getId()).get();
				assertTrue(
						"The current number of failed attempts: " + rw2CurFailedAttemptsCount
								+ " should be greater than or equal to previous value: " + rw2PrevFailedAttemptsCount,
						rw2CurFailedAttemptsCount >= rw2PrevFailedAttemptsCount);
				rw2PrevFailedAttemptsCount = rw2CurFailedAttemptsCount;

				Thread.sleep(50);
			}

			/**
			 * since one of the killed bookie is restarted, replicationworker
			 * should succeed in replicating this under replicated ledger and it
			 * shouldn't be under replicated anymore.
			 */
			int timeToWaitForReplicationToComplete = 20000;
			int timeWaited = 0;
			while (ReplicationTestUtil.isLedgerInUnderReplication(zkc, lh.getId(), basePath)) {
				Thread.sleep(100);
				timeWaited += 100;
				if (timeWaited == timeToWaitForReplicationToComplete) {
					fail("Ledger should be replicated by now");
				}
			}

			rw1PrevFailedAttemptsCount = rw1.replicationFailedLedgers.get(lh.getId()).get();
			rw2PrevFailedAttemptsCount = rw2.replicationFailedLedgers.get(lh.getId()).get();
			Thread.sleep(2000);
			// now since the ledger is replicated, number of failed attempts
			// counter shouldn't be increased even after sleeping for sometime.
			assertEquals("rw1 failedattempts", rw1PrevFailedAttemptsCount,
					rw1.replicationFailedLedgers.get(lh.getId()).get());
			assertEquals("rw2 failed attempts ", rw2PrevFailedAttemptsCount,
					rw2.replicationFailedLedgers.get(lh.getId()).get());

			/*
			 * Since these entries are eventually available, and replication has
			 * eventually succeeded, in one of the RW
			 * unableToReadEntriesForReplication should be 0.
			 */
			int rw1UnableToReadEntriesForReplication = rw1.unableToReadEntriesForReplication.get(lh.getId()).size();
			int rw2UnableToReadEntriesForReplication = rw2.unableToReadEntriesForReplication.get(lh.getId()).size();
			assertTrue(
					"unableToReadEntriesForReplication in RW1: " + rw1UnableToReadEntriesForReplication + " in RW2: "
							+ rw2UnableToReadEntriesForReplication,
					(rw1UnableToReadEntriesForReplication == 0) || (rw2UnableToReadEntriesForReplication == 0));
		} finally {
			rw1.shutdown();
			rw2.shutdown();
			underReplicationManager.close();
		}
	}

	class InjectedReplicationWorker extends ReplicationWorker {
		CopyOnWriteArrayList<Long> delayReplicationPeriods;

		public InjectedReplicationWorker(ServerConfiguration conf, StatsLogger statsLogger,
				CopyOnWriteArrayList<Long> delayReplicationPeriods) throws CompatibilityException,
				ReplicationException.UnavailableException, InterruptedException, IOException {
			super(conf, statsLogger);
			this.delayReplicationPeriods = delayReplicationPeriods;
		}

		@Override
		void scheduleTaskWithDelay(TimerTask timerTask, long delayPeriod) {
			delayReplicationPeriods.add(delayPeriod);
			super.scheduleTaskWithDelay(timerTask, delayPeriod);
		}
	}

	@Test
	public void testDeferLedgerLockReleaseForReplicationWorker() throws Exception {
		int ensembleSize = 3;
		LedgerHandle lh = bkc.createLedger(ensembleSize, ensembleSize, BookKeeper.DigestType.CRC32, TESTPASSWD);
		int numOfEntries = 7;
		for (int i = 0; i < numOfEntries; i++) {
			lh.addEntry(data);
		}
		lh.close();

		BookieId[] bookiesKilled = new BookieId[ensembleSize];
		ServerConfiguration[] killedBookiesConfig = new ServerConfiguration[ensembleSize];

		// kill all bookies
		for (int i = 0; i < ensembleSize; i++) {
			bookiesKilled[i] = lh.getLedgerMetadata().getAllEnsembles().get(0L).get(i);
			killedBookiesConfig[i] = getBkConf(bookiesKilled[i]);
			LOG.info("Killing Bookie : {}", bookiesKilled[i]);
			killBookie(bookiesKilled[i]);
		}

		// start new bookiesToKill number of bookies
		for (int i = 0; i < ensembleSize; i++) {
			startNewBookieAndReturnBookieId();
		}

		// create couple of replicationworkers
		long lockReleaseOfFailedLedgerGracePeriod = 64L;
		long baseBackoffForLockReleaseOfFailedLedger = lockReleaseOfFailedLedgerGracePeriod
				/ (int) Math.pow(2, ReplicationWorker.NUM_OF_EXPONENTIAL_BACKOFF_RETRIALS);
		ServerConfiguration newRWConf = new ServerConfiguration(baseConf);
		newRWConf.setLockReleaseOfFailedLedgerGracePeriod(Long.toString(lockReleaseOfFailedLedgerGracePeriod));
		newRWConf.setRereplicationEntryBatchSize(1000);
		CopyOnWriteArrayList<Long> rw1DelayReplicationPeriods = new CopyOnWriteArrayList<Long>();
		CopyOnWriteArrayList<Long> rw2DelayReplicationPeriods = new CopyOnWriteArrayList<Long>();
		TestStatsProvider statsProvider = new TestStatsProvider();
		TestStatsLogger statsLogger1 = statsProvider.getStatsLogger("rw1");
		TestStatsLogger statsLogger2 = statsProvider.getStatsLogger("rw2");
		ReplicationWorker rw1 = new InjectedReplicationWorker(newRWConf, statsLogger1, rw1DelayReplicationPeriods);
		ReplicationWorker rw2 = new InjectedReplicationWorker(newRWConf, statsLogger2, rw2DelayReplicationPeriods);

		Counter numEntriesUnableToReadForReplication1 = statsLogger1
				.getCounter(NUM_ENTRIES_UNABLE_TO_READ_FOR_REPLICATION);
		Counter numEntriesUnableToReadForReplication2 = statsLogger2
				.getCounter(NUM_ENTRIES_UNABLE_TO_READ_FOR_REPLICATION);
		@Cleanup
		MetadataClientDriver clientDriver = MetadataDrivers
				.getClientDriver(URI.create(baseClientConf.getMetadataServiceUri()));
		clientDriver.initialize(baseClientConf, scheduler, NullStatsLogger.INSTANCE, Optional.empty());

		LedgerManagerFactory mFactory = clientDriver.getLedgerManagerFactory();

		LedgerUnderreplicationManager underReplicationManager = mFactory.newLedgerUnderreplicationManager();
		try {
			// mark ledger underreplicated
			for (int i = 0; i < bookiesKilled.length; i++) {
				underReplicationManager.markLedgerUnderreplicated(lh.getId(), bookiesKilled[i].toString());
			}
			while (!ReplicationTestUtil.isLedgerInUnderReplication(zkc, lh.getId(), basePath)) {
				Thread.sleep(100);
			}
			rw1.start();
			rw2.start();

			// wait for RWs to complete 'numOfAttemptsToWaitFor' failed attempts
			int numOfAttemptsToWaitFor = 10;
			while ((rw1.replicationFailedLedgers.get(lh.getId()).get() < numOfAttemptsToWaitFor)
					|| rw2.replicationFailedLedgers.get(lh.getId()).get() < numOfAttemptsToWaitFor) {
				Thread.sleep(500);
			}

			/*
			 * since all the bookies containing the ledger entries are down
			 * replication wouldn't have succeeded.
			 */
			assertTrue("Ledger: " + lh.getId() + " should be underreplicated",
					ReplicationTestUtil.isLedgerInUnderReplication(zkc, lh.getId(), basePath));

			/*
			 * since RW failed 'numOfAttemptsToWaitFor' number of times, we
			 * should have atleast (numOfAttemptsToWaitFor - 1)
			 * delayReplicationPeriods and their value should be
			 * (lockReleaseOfFailedLedgerGracePeriod/16) , 2 * previous value,..
			 * with max : lockReleaseOfFailedLedgerGracePeriod
			 */
			for (int i = 0; i < ((numOfAttemptsToWaitFor - 1)); i++) {
				long expectedDelayValue = Math.min(lockReleaseOfFailedLedgerGracePeriod,
						baseBackoffForLockReleaseOfFailedLedger * (1 << i));
				assertEquals("RW1 delayperiod", (Long) expectedDelayValue, rw1DelayReplicationPeriods.get(i));
				assertEquals("RW2 delayperiod", (Long) expectedDelayValue, rw2DelayReplicationPeriods.get(i));
			}

			/*
			 * RW wont try to replicate until and unless RW succeed in reading
			 * those failed entries before proceeding with replication of under
			 * replicated fragment, so the numEntriesUnableToReadForReplication
			 * should be just 'numOfEntries', though RW failed to replicate
			 * multiple times.
			 */
			assertEquals("numEntriesUnableToReadForReplication for RW1", Long.valueOf((long) numOfEntries),
					numEntriesUnableToReadForReplication1.get());
			assertEquals("numEntriesUnableToReadForReplication for RW2", Long.valueOf((long) numOfEntries),
					numEntriesUnableToReadForReplication2.get());

			/*
			 * Since these entries are unavailable,
			 * unableToReadEntriesForReplication should be of size numOfEntries.
			 */
			assertEquals("RW1 unabletoreadentries", numOfEntries,
					rw1.unableToReadEntriesForReplication.get(lh.getId()).size());
			assertEquals("RW2 unabletoreadentries", numOfEntries,
					rw2.unableToReadEntriesForReplication.get(lh.getId()).size());
		} finally {
			rw1.shutdown();
			rw2.shutdown();
			underReplicationManager.close();
		}
	}

	/**
	 * Tests that ReplicationWorker should not have identified for postponing
	 * the replication if ledger is in open state and lastFragment is not in
	 * underReplication state. Note that RW should not fence such ledgers.
	 */
	@Test
	public void testRWShouldReplicateTheLedgersAfterTimeoutIfLastFragmentIsNotUR() throws Exception {
		LedgerHandle lh = bkc.createLedger(3, 3, BookKeeper.DigestType.CRC32, TESTPASSWD);

		for (int i = 0; i < 10; i++) {
			lh.addEntry(data);
		}
		BookieId replicaToKill = lh.getLedgerMetadata().getAllEnsembles().get(0L).get(0);

		LOG.info("Killing Bookie : {}", replicaToKill);
		killBookie(replicaToKill);

		BookieId newBkAddr = startNewBookieAndReturnBookieId();
		LOG.info("New Bookie addr : {}", newBkAddr);

		// Reform ensemble...Making sure that last fragment is not in
		// under-replication
		for (int i = 0; i < 10; i++) {
			lh.addEntry(data);
		}

		ReplicationWorker rw = new ReplicationWorker(baseConf);

		baseClientConf.setMetadataServiceUri(zkUtil.getMetadataServiceUri());

		@Cleanup
		MetadataClientDriver driver = MetadataDrivers
				.getClientDriver(URI.create(baseClientConf.getMetadataServiceUri()));
		driver.initialize(baseClientConf, scheduler, NullStatsLogger.INSTANCE, Optional.empty());

		LedgerManagerFactory mFactory = driver.getLedgerManagerFactory();

		LedgerUnderreplicationManager underReplicationManager = mFactory.newLedgerUnderreplicationManager();

		rw.start();
		try {

			underReplicationManager.markLedgerUnderreplicated(lh.getId(), replicaToKill.toString());
			while (ReplicationTestUtil.isLedgerInUnderReplication(zkc, lh.getId(), basePath)) {
				Thread.sleep(100);
			}

			killAllBookies(lh, newBkAddr);

			// Should be able to read the entries from 0-9
			verifyRecoveredLedgers(lh, 0, 9);
			lh = bkc.openLedgerNoRecovery(lh.getId(), BookKeeper.DigestType.CRC32, TESTPASSWD);

			// Ledger should be still in open state
			assertTrue("Ledger must have been closed by RW", ClientUtil.isLedgerOpen(lh));
		} finally {
			rw.shutdown();
			underReplicationManager.close();
		}

	}

	/**
	 * Test that the replication worker will not shutdown on a simple ZK disconnection.
	 */
	@Test
	public void testRWZKConnectionLost() throws Exception {
		try (ZooKeeperClient zk = ZooKeeperClient.newBuilder().connectString(zkUtil.getZooKeeperConnectString())
				.sessionTimeoutMs(10000).build()) {

			ReplicationWorker rw = new ReplicationWorker(baseConf);
			rw.start();
			for (int i = 0; i < 10; i++) {
				if (rw.isRunning()) {
					break;
				}
				Thread.sleep(1000);
			}
			assertTrue("Replication worker should be running", rw.isRunning());

			stopZKCluster();
			// ZK is down for shorter period than reconnect timeout
			Thread.sleep(1000);
			startZKCluster();

			assertTrue("Replication worker should not shutdown", rw.isRunning());
		}
	}

	/**
	 * Test that the replication worker shuts down on non-recoverable ZK connection loss.
	 */
	@Test
	public void testRWZKConnectionLostOnNonRecoverableZkError() throws Exception {
		for (int j = 0; j < 3; j++) {
			LedgerHandle lh = bkc.createLedger(1, 1, 1, BookKeeper.DigestType.CRC32, TESTPASSWD, null);
			final long createdLedgerId = lh.getId();
			for (int i = 0; i < 10; i++) {
				lh.addEntry(data);
			}
			lh.close();
		}

		killBookie(2);
		killBookie(1);
		startNewBookie();
		startNewBookie();

		servers.get(0).getConfiguration().setRwRereplicateBackoffMs(100);
		servers.get(0).startAutoRecovery();

		Auditor auditor = getAuditor(10, TimeUnit.SECONDS);
		ReplicationWorker rw = servers.get(0).getReplicationWorker();

		ZkLedgerUnderreplicationManager ledgerUnderreplicationManager = (ZkLedgerUnderreplicationManager) FieldUtils
				.readField(auditor, "ledgerUnderreplicationManager", true);

		ZooKeeper zkc = (ZooKeeper) FieldUtils.readField(ledgerUnderreplicationManager, "zkc", true);
		auditor.submitAuditTask().get();

		assertTrue(zkc.getState().isConnected());
		zkc.close();
		assertFalse(zkc.getState().isConnected());

		auditor.submitAuditTask();
		rw.run();

		for (int i = 0; i < 10; i++) {
			if (!rw.isRunning() && !auditor.isRunning()) {
				break;
			}
			Thread.sleep(1000);
		}
		assertFalse("Replication worker should NOT be running", rw.isRunning());
		assertFalse("Auditor should NOT be running", auditor.isRunning());
	}

	private void killAllBookies(LedgerHandle lh, BookieId excludeBK) throws Exception {
		// Killing all bookies except newly replicated bookie
		for (Entry<Long, ? extends List<BookieId>> entry : lh.getLedgerMetadata().getAllEnsembles().entrySet()) {
			List<BookieId> bookies = entry.getValue();
			for (BookieId bookie : bookies) {
				if (bookie.equals(excludeBK)) {
					continue;
				}
				killBookie(bookie);
			}
		}
	}

	private void verifyRecoveredLedgers(LedgerHandle lh, long startEntryId, long endEntryId)
			throws BKException, InterruptedException {
		LedgerHandle lhs = bkc.openLedgerNoRecovery(lh.getId(), BookKeeper.DigestType.CRC32, TESTPASSWD);
		Enumeration<LedgerEntry> entries = lhs.readEntries(startEntryId, endEntryId);
		assertTrue("Should have the elements", entries.hasMoreElements());
		while (entries.hasMoreElements()) {
			LedgerEntry entry = entries.nextElement();
			assertEquals("TestReplicationWorker", new String(entry.getEntry()));
		}
	}

	class MockZooKeeperClient extends ZooKeeperClient {
		private final String connectString;
		private final int sessionTimeoutMs;
		private final ZooKeeperWatcherBase watcherManager;
		private volatile String pathOfSetDataToFail;
		private volatile String pathOfDeleteToFail;
		private AtomicInteger numOfTimesSetDataFailed = new AtomicInteger();
		private AtomicInteger numOfTimesDeleteFailed = new AtomicInteger();

		MockZooKeeperClient(String connectString, int sessionTimeoutMs, ZooKeeperWatcherBase watcher)
				throws IOException {
			/*
			 * in OperationalRetryPolicy maxRetries is set to 0. So it wont
			 * retry incase of any error/exception.
			 */
			super(connectString, sessionTimeoutMs, watcher,
					new BoundExponentialBackoffRetryPolicy(sessionTimeoutMs, sessionTimeoutMs, Integer.MAX_VALUE),
					new BoundExponentialBackoffRetryPolicy(sessionTimeoutMs, sessionTimeoutMs, 0),
					NullStatsLogger.INSTANCE, 1, 0, false);
			this.connectString = connectString;
			this.sessionTimeoutMs = sessionTimeoutMs;
			this.watcherManager = watcher;
		}

		@Override
		protected ZooKeeper createZooKeeper() throws IOException {
			return new MockZooKeeper(this.connectString, this.sessionTimeoutMs, this.watcherManager, false);
		}

		private void setPathOfSetDataToFail(String pathOfSetDataToFail) {
			this.pathOfSetDataToFail = pathOfSetDataToFail;
		}

		private void setPathOfDeleteToFail(String pathOfDeleteToFail) {
			this.pathOfDeleteToFail = pathOfDeleteToFail;
		}

		private int getNumOfTimesSetDataFailed() {
			return numOfTimesSetDataFailed.get();
		}

		private int getNumOfTimesDeleteFailed() {
			return numOfTimesDeleteFailed.get();
		}

		class MockZooKeeper extends ZooKeeper {
			public MockZooKeeper(String connectString, int sessionTimeout, Watcher watcher, boolean canBeReadOnly)
					throws IOException {
				super(connectString, sessionTimeout, watcher, canBeReadOnly);
			}

			@Override
			public void setData(final String path, final byte[] data, final int version, final StatCallback cb,
					final Object context) {
				if ((pathOfSetDataToFail != null) && (pathOfSetDataToFail.equals(path))) {
					/*
					 * if pathOfSetDataToFail matches with the path of the node,
					 * then callback with CONNECTIONLOSS error.
					 */
					LOG.error("setData of MockZooKeeper, is failing with CONNECTIONLOSS for path: {}", path);
					numOfTimesSetDataFailed.incrementAndGet();
					cb.processResult(KeeperException.Code.CONNECTIONLOSS.intValue(), path, context, null);
				} else {
					super.setData(path, data, version, cb, context);
				}
			}

			@Override
			public void delete(final String path, final int version) throws KeeperException, InterruptedException {
				if ((pathOfDeleteToFail != null) && (pathOfDeleteToFail.equals(path))) {
					/*
					 * if pathOfDeleteToFail matches with the path of the node,
					 * then throw CONNECTIONLOSS exception.
					 */
					LOG.error("delete of MockZooKeeper, is failing with CONNECTIONLOSS for path: {}", path);
					numOfTimesDeleteFailed.incrementAndGet();
					throw new KeeperException.ConnectionLossException();
				} else {
					super.delete(path, version);
				}
			}
		}
	}

	@Test
	public void testRWShutDownInTheCaseOfZKOperationFailures() throws Exception {
		/*
		 * create MockZooKeeperClient instance and wait for it to be connected.
		 */
		int zkSessionTimeOut = 10000;
		ZooKeeperWatcherBase zooKeeperWatcherBase = new ZooKeeperWatcherBase(zkSessionTimeOut,
				NullStatsLogger.INSTANCE);
		MockZooKeeperClient zkFaultInjectionWrapper = new MockZooKeeperClient(zkUtil.getZooKeeperConnectString(),
				zkSessionTimeOut, zooKeeperWatcherBase);
		zkFaultInjectionWrapper.waitForConnection();
		assertEquals("zkFaultInjectionWrapper should be in connected state", States.CONNECTED,
				zkFaultInjectionWrapper.getState());
		long oldZkInstanceSessionId = zkFaultInjectionWrapper.getSessionId();

		/*
		 * create ledger and add entries.
		 */
		BookKeeper bkWithMockZK = new BookKeeper(baseClientConf, zkFaultInjectionWrapper);
		long ledgerId = 567L;
		LedgerHandle lh = bkWithMockZK.createLedgerAdv(ledgerId, 2, 2, 2, BookKeeper.DigestType.CRC32, TESTPASSWD,
				null);
		for (int i = 0; i < 10; i++) {
			lh.addEntry(i, data);
		}
		lh.close();

		/*
		 * trigger Expired event so that MockZooKeeperClient would run
		 * 'clientCreator' and create new zk handle. In this case it would
		 * create MockZooKeeper instance.
		 */
		zooKeeperWatcherBase.process(new WatchedEvent(EventType.None, KeeperState.Expired, ""));
		zkFaultInjectionWrapper.waitForConnection();
		for (int i = 0; i < 10; i++) {
			if (zkFaultInjectionWrapper.getState() == States.CONNECTED) {
				break;
			}
			Thread.sleep(200);
		}
		assertEquals("zkFaultInjectionWrapper should be in connected state", States.CONNECTED,
				zkFaultInjectionWrapper.getState());
		assertNotEquals("Session Id of old and new ZK instance should be different", oldZkInstanceSessionId,
				zkFaultInjectionWrapper.getSessionId());

		/*
		 * Kill a Bookie, so that ledger becomes underreplicated. Since totally
		 * 3 bookies are available and the ensemblesize of the current ledger is
		 * 2, we should be able to replicate to the other bookie.
		 */
		BookieId replicaToKill = lh.getLedgerMetadata().getAllEnsembles().get(0L).get(0);
		LOG.info("Killing Bookie id {}", replicaToKill);
		killBookie(replicaToKill);

		/*
		 * Start RW.
		 */
		ReplicationWorker rw = new ReplicationWorker(baseConf, bkWithMockZK, false, NullStatsLogger.INSTANCE);
		rw.start();
		try {
			for (int i = 0; i < 40; i++) {
				if (rw.isRunning()) {
					break;
				}
				LOG.info("Waiting for the RW to start...");
				Thread.sleep(500);
			}
			assertTrue("RW should be running", rw.isRunning());

			/*
			 * Since Auditor is not running, ledger needs to be marked
			 * underreplicated explicitly. But before marking ledger
			 * underreplicated, set paths for which MockZooKeeper's setData and
			 * Delete operation to fail.
			 *
			 * ZK.setData will be called by 'updateEnsembleInfo' operation after
			 * completion of copying to a new bookie. ZK.delete will be called by
			 * RW.logBKExceptionAndReleaseLedger and finally block in
			 * 'rereplicate(long ledgerIdToReplicate)'
			 */
			AbstractZkLedgerManager absZKLedgerManager = (AbstractZkLedgerManager) ledgerManager;
			String ledgerPath = absZKLedgerManager.getLedgerPath(ledgerId);
			String urLockPath = ZkLedgerUnderreplicationManager
					.getUrLedgerLockZnode(ZkLedgerUnderreplicationManager.getUrLockPath(zkLedgersRootPath), ledgerId);
			zkFaultInjectionWrapper.setPathOfSetDataToFail(ledgerPath);
			zkFaultInjectionWrapper.setPathOfDeleteToFail(urLockPath);
			underReplicationManager.markLedgerUnderreplicated(lh.getId(), replicaToKill.toString());

			/*
			 * Since there is only one RW, it will try to replicate underreplicated
			 * ledger. After completion of copying it to a new bookie, it will try
			 * to update ensembleinfo. Which would fail with our MockZK. After that
			 * it would try to delete lock znode as part of
			 * RW.logBKExceptionAndReleaseLedger, which will also fail because of
			 * our MockZK. In the finally block in 'rereplicate(long
			 * ledgerIdToReplicate)' it would try one more time to delete the ledger
			 * and once again it will fail because of our MockZK. So RW gives up and
			 * shutdowns itself.
			 */
			for (int i = 0; i < 40; i++) {
				if (!rw.isRunning()) {
					break;
				}
				LOG.info("Waiting for the RW to shutdown...");
				Thread.sleep(500);
			}

			/*
			 * as described earlier, numOfTimes setDataFailed should be 1 and
			 * numOfTimes deleteFailed should be 2
			 */
			assertEquals("NumOfTimesSetDataFailed", 1, zkFaultInjectionWrapper.getNumOfTimesSetDataFailed());
			assertEquals("NumOfTimesDeleteFailed", 2, zkFaultInjectionWrapper.getNumOfTimesDeleteFailed());
			assertFalse("RW should be shutdown", rw.isRunning());
		} finally {
			rw.shutdown();
			zkFaultInjectionWrapper.close();
			bkWithMockZK.close();
		}
	}
}
