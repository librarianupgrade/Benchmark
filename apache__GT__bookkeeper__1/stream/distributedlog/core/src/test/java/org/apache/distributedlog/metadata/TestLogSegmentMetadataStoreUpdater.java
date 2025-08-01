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
package org.apache.distributedlog.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.apache.bookkeeper.common.util.OrderedScheduler;
import org.apache.distributedlog.DLMTestUtil;
import org.apache.distributedlog.DLSN;
import org.apache.distributedlog.DistributedLogConfiguration;
import org.apache.distributedlog.LogRecordWithDLSN;
import org.apache.distributedlog.LogSegmentMetadata;
import org.apache.distributedlog.TestZooKeeperClientBuilder;
import org.apache.distributedlog.ZooKeeperClient;
import org.apache.distributedlog.ZooKeeperClusterTestCase;
import org.apache.distributedlog.impl.ZKLogSegmentMetadataStore;
import org.apache.distributedlog.logsegment.LogSegmentMetadataStore;
import org.apache.distributedlog.util.Utils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test update for {@link LogSegmentMetadataStore}s.
 */
public class TestLogSegmentMetadataStoreUpdater extends ZooKeeperClusterTestCase {

	static final Logger LOG = LoggerFactory.getLogger(TestLogSegmentMetadataStoreUpdater.class);

	private ZooKeeperClient zkc;
	private OrderedScheduler scheduler;
	private LogSegmentMetadataStore metadataStore;
	private DistributedLogConfiguration conf = new DistributedLogConfiguration()
			.setDLLedgerMetadataLayoutVersion(LogSegmentMetadata.LEDGER_METADATA_CURRENT_LAYOUT_VERSION);

	@Before
	public void setup() throws Exception {
		scheduler = OrderedScheduler.newSchedulerBuilder().name("test-logsegment-metadata-store-updater").numThreads(1)
				.build();
		zkc = TestZooKeeperClientBuilder.newBuilder().uri(createURI("/")).sessionTimeoutMs(10000).build();
		metadataStore = new ZKLogSegmentMetadataStore(conf, zkc, scheduler);
	}

	@After
	public void tearDown() throws Exception {
		metadataStore.close();
		scheduler.shutdown();
		zkc.close();
	}

	private URI createURI(String path) {
		return URI.create("distributedlog://127.0.0.1:" + zkPort + path);
	}

	Map<Long, LogSegmentMetadata> readLogSegments(String ledgerPath) throws Exception {
		return DLMTestUtil.readLogSegments(zkc, ledgerPath);
	}

	@Test(timeout = 60000)
	public void testChangeSequenceNumber() throws Exception {
		String ledgerPath = "/testChangeSequenceNumber";
		zkc.get().create(ledgerPath, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		Map<Long, LogSegmentMetadata> completedLogSegments = new HashMap<Long, LogSegmentMetadata>();
		// Create 5 completed log segments
		for (int i = 1; i <= 5; i++) {
			LogSegmentMetadata segment = DLMTestUtil.completedLogSegment(ledgerPath, i, (i - 1) * 100, i * 100 - 1, 100,
					i, 100, 0);
			completedLogSegments.put(((long) i), segment);
			LOG.info("Create completed segment {} : {}", segment.getZkPath(), segment);
			segment.write(zkc);
		}
		// Create a smaller inprogress log segment
		long inprogressSeqNo = 3;
		LogSegmentMetadata segment = DLMTestUtil.inprogressLogSegment(ledgerPath, inprogressSeqNo, 5 * 100,
				inprogressSeqNo);
		LOG.info("Create inprogress segment {} : {}", segment.getZkPath(), segment);
		segment.write(zkc);

		Map<Long, LogSegmentMetadata> segmentList = readLogSegments(ledgerPath);
		assertEquals(5, segmentList.size());

		// Dryrun
		MetadataUpdater dryrunUpdater = new DryrunLogSegmentMetadataStoreUpdater(conf, metadataStore);
		Utils.ioResult(dryrunUpdater.changeSequenceNumber(segment, 6L));

		segmentList = readLogSegments(ledgerPath);
		assertEquals(5, segmentList.size());

		// Fix the inprogress log segments

		MetadataUpdater updater = LogSegmentMetadataStoreUpdater.createMetadataUpdater(conf, metadataStore);
		Utils.ioResult(updater.changeSequenceNumber(segment, 6L));

		segmentList = readLogSegments(ledgerPath);
		assertEquals(6, segmentList.size());

		// check first 5 log segments
		for (int i = 1; i <= 5; i++) {
			LogSegmentMetadata s = segmentList.get((long) i);
			assertNotNull(s);
			assertEquals(completedLogSegments.get((long) i), s);
		}

		// get log segment 6
		LogSegmentMetadata segmentChanged = segmentList.get(6L);
		assertNotNull(segmentChanged);
		assertEquals(6L, segmentChanged.getLogSegmentSequenceNumber());
		assertTrue(segmentChanged.isInProgress());
		assertEquals(5 * 100, segmentChanged.getFirstTxId());
		assertEquals(3L, segmentChanged.getLogSegmentId());
	}

	@Test(timeout = 60000)
	public void testUpdateLastDLSN() throws Exception {
		String ledgerPath = "/testUpdateLastDLSN";
		zkc.get().create(ledgerPath, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		// Create 1 completed log segment
		LogSegmentMetadata completedLogSegment = DLMTestUtil.completedLogSegment(ledgerPath, 1L, 0L, 99L, 100, 1L, 99L,
				0L);
		completedLogSegment.write(zkc);
		// Create 1 inprogress log segment
		LogSegmentMetadata inprogressLogSegment = DLMTestUtil.inprogressLogSegment(ledgerPath, 2L, 100L, 2L);
		inprogressLogSegment.write(zkc);

		DLSN badLastDLSN = new DLSN(99L, 0L, 0L);
		DLSN goodLastDLSN1 = new DLSN(1L, 100L, 0L);
		DLSN goodLastDLSN2 = new DLSN(2L, 200L, 0L);

		LogRecordWithDLSN badRecord = DLMTestUtil.getLogRecordWithDLSNInstance(badLastDLSN, 100L);
		LogRecordWithDLSN goodRecord1 = DLMTestUtil.getLogRecordWithDLSNInstance(goodLastDLSN1, 100L);
		LogRecordWithDLSN goodRecord2 = DLMTestUtil.getLogRecordWithDLSNInstance(goodLastDLSN2, 200L);

		// Dryrun
		MetadataUpdater dryrunUpdater = new DryrunLogSegmentMetadataStoreUpdater(conf, metadataStore);
		try {
			Utils.ioResult(dryrunUpdater.updateLastRecord(completedLogSegment, badRecord));
			fail("Should fail on updating dlsn that in different log segment");
		} catch (IllegalArgumentException iae) {
			// expected
		}
		try {
			Utils.ioResult(dryrunUpdater.updateLastRecord(inprogressLogSegment, goodRecord2));
			fail("Should fail on updating dlsn for an inprogress log segment");
		} catch (IllegalStateException ise) {
			// expected
		}
		LogSegmentMetadata updatedCompletedLogSegment = Utils
				.ioResult(dryrunUpdater.updateLastRecord(completedLogSegment, goodRecord1));
		assertEquals(goodLastDLSN1, updatedCompletedLogSegment.getLastDLSN());
		assertEquals(goodRecord1.getTransactionId(), updatedCompletedLogSegment.getLastTxId());
		assertTrue(updatedCompletedLogSegment.isRecordLastPositioninThisSegment(goodRecord1));

		Map<Long, LogSegmentMetadata> segmentList = readLogSegments(ledgerPath);
		assertEquals(2, segmentList.size());

		LogSegmentMetadata readCompletedLogSegment = segmentList.get(1L);
		assertNotNull(readCompletedLogSegment);
		assertEquals(completedLogSegment, readCompletedLogSegment);

		LogSegmentMetadata readInprogressLogSegment = segmentList.get(2L);
		assertNotNull(readInprogressLogSegment);
		assertEquals(inprogressLogSegment, readInprogressLogSegment);

		// Fix the last dlsn
		MetadataUpdater updater = LogSegmentMetadataStoreUpdater.createMetadataUpdater(conf, metadataStore);
		try {
			Utils.ioResult(updater.updateLastRecord(completedLogSegment, badRecord));
			fail("Should fail on updating dlsn that in different log segment");
		} catch (IllegalArgumentException iae) {
			// expected
		}
		try {
			Utils.ioResult(updater.updateLastRecord(inprogressLogSegment, goodRecord2));
			fail("Should fail on updating dlsn for an inprogress log segment");
		} catch (IllegalStateException ise) {
			// expected
		}
		updatedCompletedLogSegment = Utils.ioResult(updater.updateLastRecord(completedLogSegment, goodRecord1));
		assertEquals(goodLastDLSN1, updatedCompletedLogSegment.getLastDLSN());
		assertEquals(goodRecord1.getTransactionId(), updatedCompletedLogSegment.getLastTxId());
		assertTrue(updatedCompletedLogSegment.isRecordLastPositioninThisSegment(goodRecord1));

		segmentList = readLogSegments(ledgerPath);
		assertEquals(2, segmentList.size());

		readCompletedLogSegment = segmentList.get(1L);
		assertNotNull(readCompletedLogSegment);
		assertEquals(goodLastDLSN1, readCompletedLogSegment.getLastDLSN());
		assertEquals(goodRecord1.getTransactionId(), readCompletedLogSegment.getLastTxId());
		assertTrue(readCompletedLogSegment.isRecordLastPositioninThisSegment(goodRecord1));
		assertEquals(updatedCompletedLogSegment, readCompletedLogSegment);
		assertEquals(completedLogSegment.getCompletionTime(), readCompletedLogSegment.getCompletionTime());
		assertEquals(completedLogSegment.getFirstTxId(), readCompletedLogSegment.getFirstTxId());
		assertEquals(completedLogSegment.getLogSegmentId(), readCompletedLogSegment.getLogSegmentId());
		assertEquals(completedLogSegment.getLogSegmentSequenceNumber(),
				readCompletedLogSegment.getLogSegmentSequenceNumber());
		assertEquals(completedLogSegment.getRegionId(), readCompletedLogSegment.getRegionId());
		assertEquals(completedLogSegment.getZkPath(), readCompletedLogSegment.getZkPath());
		assertEquals(completedLogSegment.getZNodeName(), readCompletedLogSegment.getZNodeName());

		readInprogressLogSegment = segmentList.get(2L);
		assertNotNull(readInprogressLogSegment);
		assertEquals(inprogressLogSegment, readInprogressLogSegment);
	}

	@Test(timeout = 60000)
	public void testChangeTruncationStatus() throws Exception {
		String ledgerPath = "/ledgers2";
		zkc.get().create(ledgerPath, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		Map<Long, LogSegmentMetadata> completedLogSegments = new HashMap<Long, LogSegmentMetadata>();
		// Create 5 completed log segments
		for (int i = 1; i <= 5; i++) {
			LogSegmentMetadata segment = DLMTestUtil.completedLogSegment(ledgerPath, i, (i - 1) * 100, i * 100 - 1, 100,
					i, 100, 0);
			completedLogSegments.put(((long) i), segment);
			LOG.info("Create completed segment {} : {}", segment.getZkPath(), segment);
			segment.write(zkc);
		}

		Map<Long, LogSegmentMetadata> segmentList = readLogSegments(ledgerPath);
		assertEquals(5, segmentList.size());

		long segmentToModify = 1L;

		// Dryrun
		MetadataUpdater dryrunUpdater = new DryrunLogSegmentMetadataStoreUpdater(conf, metadataStore);
		Utils.ioResult(dryrunUpdater.setLogSegmentTruncated(segmentList.get(segmentToModify)));

		segmentList = readLogSegments(ledgerPath);
		assertEquals(false, segmentList.get(segmentToModify).isTruncated());

		// change truncation for the 1st log segment
		MetadataUpdater updater = LogSegmentMetadataStoreUpdater.createMetadataUpdater(conf, metadataStore);
		Utils.ioResult(updater.setLogSegmentTruncated(segmentList.get(segmentToModify)));

		segmentList = readLogSegments(ledgerPath);
		assertEquals(true, segmentList.get(segmentToModify).isTruncated());
		assertEquals(false, segmentList.get(segmentToModify).isPartiallyTruncated());

		updater = LogSegmentMetadataStoreUpdater.createMetadataUpdater(conf, metadataStore);
		Utils.ioResult(updater.setLogSegmentActive(segmentList.get(segmentToModify)));

		segmentList = readLogSegments(ledgerPath);
		assertEquals(false, segmentList.get(segmentToModify).isTruncated());
		assertEquals(false, segmentList.get(segmentToModify).isPartiallyTruncated());

		updater = LogSegmentMetadataStoreUpdater.createMetadataUpdater(conf, metadataStore);
		Utils.ioResult(updater.setLogSegmentPartiallyTruncated(segmentList.get(segmentToModify),
				segmentList.get(segmentToModify).getFirstDLSN()));

		segmentList = readLogSegments(ledgerPath);
		assertEquals(false, segmentList.get(segmentToModify).isTruncated());
		assertEquals(true, segmentList.get(segmentToModify).isPartiallyTruncated());

		updater = LogSegmentMetadataStoreUpdater.createMetadataUpdater(conf, metadataStore);
		Utils.ioResult(updater.setLogSegmentActive(segmentList.get(segmentToModify)));

		segmentList = readLogSegments(ledgerPath);
		assertEquals(false, segmentList.get(segmentToModify).isTruncated());
		assertEquals(false, segmentList.get(segmentToModify).isPartiallyTruncated());
	}

}
