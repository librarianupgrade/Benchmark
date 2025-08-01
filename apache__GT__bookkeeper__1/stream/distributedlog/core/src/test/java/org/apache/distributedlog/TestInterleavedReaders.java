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
package org.apache.distributedlog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.distributedlog.api.DistributedLogManager;
import org.apache.distributedlog.api.LogReader;
import org.apache.distributedlog.util.Utils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test Cases for InterleavedReaders.
 */
public class TestInterleavedReaders extends TestDistributedLogBase {
	static final Logger LOG = LoggerFactory.getLogger(TestInterleavedReaders.class);

	static {
		conf.setOutputBufferSize(0);
		conf.setImmediateFlushEnabled(true);
	}

	private int drainStreams(LogReader reader0, int num0, LogReader reader1, int num1) throws Exception {
		// Allow time for watches to fire
		Thread.sleep(15);
		int numTrans = 0;
		LogRecord record;
		int i = 0;
		while (i < num0) {
			record = reader0.readNext(false);
			if (null != record) {
				assertTrue((record.getTransactionId() % 2 == 0));
				DLMTestUtil.verifyLogRecord(record);
				numTrans++;
				i++;
				LOG.info("Read record {}", record);
			}
		}
		i = 0;
		while (i < num1) {
			record = reader1.readNext(false);
			if (null != record) {
				assertTrue((record.getTransactionId() % 2 == 1));
				DLMTestUtil.verifyLogRecord(record);
				numTrans++;
				i++;
				LOG.info("Read record {}", record);
			}
		}
		return numTrans;
	}

	@Test(timeout = 60000)
	public void testInterleavedReaders() throws Exception {
		String name = "distrlog-interleaved";
		BKDistributedLogManager dlmwrite0 = createNewDLM(conf, name + "-0");
		BKDistributedLogManager dlmreader0 = createNewDLM(conf, name + "-0");
		BKDistributedLogManager dlmwrite1 = createNewDLM(conf, name + "-1");
		BKDistributedLogManager dlmreader1 = createNewDLM(conf, name + "-1");

		LogReader reader0 = null;
		LogReader reader1 = null;
		long txid = 1;
		int numTrans = 0;

		BKAsyncLogWriter writer0 = dlmwrite0.startAsyncLogSegmentNonPartitioned();
		BKAsyncLogWriter writer1 = dlmwrite1.startAsyncLogSegmentNonPartitioned();
		for (long j = 1; j <= 4; j++) {
			for (int k = 1; k <= 10; k++) {
				Utils.ioResult(writer1.write(DLMTestUtil.getLogRecordInstance(txid++)));
				Utils.ioResult(writer0.write(DLMTestUtil.getLogRecordInstance(txid++)));
			}
			Utils.ioResult(writer1.writeControlRecord(DLMTestUtil.getLogRecordInstance(txid - 1)));
			Utils.ioResult(writer0.writeControlRecord(DLMTestUtil.getLogRecordInstance(txid - 1)));
			if (null == reader0) {
				reader0 = dlmreader0.getInputStream(1);
			}
			if (null == reader1) {
				reader1 = dlmreader1.getInputStream(1);
			}
			numTrans += drainStreams(reader0, 10, reader1, 10);
			assertEquals((txid - 1), numTrans);
		}
		reader0.close();
		reader1.close();
		dlmreader0.close();
		dlmwrite0.close();
		dlmreader1.close();
		dlmwrite1.close();
	}

	@Test(timeout = 60000)
	public void testInterleavedReadersWithRollingEdge() throws Exception {
		String name = "distrlog-interleaved-rolling-edge";
		BKDistributedLogManager dlmwrite0 = createNewDLM(conf, name + "-0");
		BKDistributedLogManager dlmreader0 = createNewDLM(conf, name + "-0");
		BKDistributedLogManager dlmwrite1 = createNewDLM(conf, name + "-1");
		BKDistributedLogManager dlmreader1 = createNewDLM(conf, name + "-1");

		LogReader reader0 = null;
		LogReader reader1 = null;
		long txid = 1;
		int numTrans = 0;

		BKAsyncLogWriter writer0 = dlmwrite0.startAsyncLogSegmentNonPartitioned();
		BKAsyncLogWriter writer1 = dlmwrite1.startAsyncLogSegmentNonPartitioned();
		for (long j = 1; j <= 4; j++) {
			if (j > 1) {
				writer0.setForceRolling(true);
				writer1.setForceRolling(true);
			}
			for (int k = 1; k <= 2; k++) {
				Utils.ioResult(writer1.write(DLMTestUtil.getLogRecordInstance(txid++)));
				Utils.ioResult(writer0.write(DLMTestUtil.getLogRecordInstance(txid++)));
				writer0.setForceRolling(false);
				writer1.setForceRolling(false);
			}
			Utils.ioResult(writer1.writeControlRecord(DLMTestUtil.getLogRecordInstance(txid - 1)));
			Utils.ioResult(writer0.writeControlRecord(DLMTestUtil.getLogRecordInstance(txid - 1)));
			LOG.info("Completed {} write", j);
			if (null == reader0) {
				reader0 = dlmreader0.getInputStream(1);
			}
			if (null == reader1) {
				reader1 = dlmreader1.getInputStream(1);
			}
			numTrans += drainStreams(reader0, 2, reader1, 2);
			assertEquals((txid - 1), numTrans);
		}
		reader0.close();
		reader1.close();
		dlmreader0.close();
		dlmwrite0.close();
		dlmreader1.close();
		dlmwrite1.close();
	}

	@Test(timeout = 60000)
	public void testInterleavedReadersWithRolling() throws Exception {
		String name = "distrlog-interleaved-rolling";
		BKDistributedLogManager dlmwrite0 = createNewDLM(conf, name + "-0");
		BKDistributedLogManager dlmreader0 = createNewDLM(conf, name + "-0");
		BKDistributedLogManager dlmwrite1 = createNewDLM(conf, name + "-1");
		BKDistributedLogManager dlmreader1 = createNewDLM(conf, name + "-1");

		LogReader reader0 = null;
		LogReader reader1 = null;
		long txid = 1;
		int numTrans = 0;

		BKAsyncLogWriter writer0 = dlmwrite0.startAsyncLogSegmentNonPartitioned();
		BKAsyncLogWriter writer1 = dlmwrite1.startAsyncLogSegmentNonPartitioned();
		for (long j = 1; j <= 2; j++) {
			for (int k = 1; k <= 6; k++) {
				if (k == 3) {
					writer0.setForceRolling(true);
					writer1.setForceRolling(true);
				}
				Utils.ioResult(writer1.write(DLMTestUtil.getLogRecordInstance(txid++)));
				Utils.ioResult(writer0.write(DLMTestUtil.getLogRecordInstance(txid++)));
				writer0.setForceRolling(false);
				writer1.setForceRolling(false);
			}
			Utils.ioResult(writer1.writeControlRecord(DLMTestUtil.getLogRecordInstance(txid - 1)));
			Utils.ioResult(writer0.writeControlRecord(DLMTestUtil.getLogRecordInstance(txid - 1)));
			if (null == reader0) {
				reader0 = dlmreader0.getInputStream(1);
			}
			if (null == reader1) {
				reader1 = dlmreader1.getInputStream(1);
			}
			numTrans += drainStreams(reader0, 6, reader1, 6);
			assertEquals((txid - 1), numTrans);
		}
		reader0.close();
		reader1.close();
		dlmreader0.close();
		dlmwrite0.close();
		dlmreader1.close();
		dlmwrite1.close();
	}

	@Test(timeout = 60000)
	public void testInterleavedReadersWithCleanup() throws Exception {
		String name = "distrlog-interleaved-cleanup";
		BKDistributedLogManager dlmwrite0 = createNewDLM(conf, name + "-0");
		BKDistributedLogManager dlmwrite1 = createNewDLM(conf, name + "-1");
		long txid = 1;
		Long retentionPeriodOverride = null;

		BKAsyncLogWriter writer0 = dlmwrite0.startAsyncLogSegmentNonPartitioned();
		BKAsyncLogWriter writer1 = dlmwrite1.startAsyncLogSegmentNonPartitioned();
		for (long j = 1; j <= 4; j++) {
			for (int k = 1; k <= 10; k++) {
				if (k == 5) {
					writer0.setForceRolling(true);
					writer0.overRideMinTimeStampToKeep(retentionPeriodOverride);
					writer1.setForceRolling(true);
					writer1.overRideMinTimeStampToKeep(retentionPeriodOverride);
				}
				DLSN dlsn1 = Utils.ioResult(writer1.write(DLMTestUtil.getLogRecordInstance(txid++)));
				LOG.info("writer1 write record {}", dlsn1);
				DLSN dlsn0 = Utils.ioResult(writer0.write(DLMTestUtil.getLogRecordInstance(txid++)));
				LOG.info("writer0 write record {}", dlsn0);
				if (k == 5) {
					writer0.setForceRolling(false);
					writer1.setForceRolling(false);
					retentionPeriodOverride = System.currentTimeMillis();
				}
				Thread.sleep(5);
			}
			Utils.ioResult(writer1.writeControlRecord(DLMTestUtil.getLogRecordInstance(txid - 1)));
			Utils.ioResult(writer0.writeControlRecord(DLMTestUtil.getLogRecordInstance(txid - 1)));
		}
		writer0.close();
		writer1.close();

		DistributedLogManager dlmreader0 = createNewDLM(conf, name + "-0");
		DistributedLogManager dlmreader1 = createNewDLM(conf, name + "-1");
		LogReader reader0 = dlmreader0.getInputStream(1);
		LogReader reader1 = dlmreader1.getInputStream(1);
		int numTrans = drainStreams(reader0, 15, reader1, 15);
		assertEquals(30, numTrans);
		reader0.close();
		reader1.close();
		dlmreader0.close();
		dlmwrite0.close();
		dlmreader1.close();
		dlmwrite1.close();
	}

	@Test(timeout = 60000)
	public void testInterleavedReadersWithRecovery() throws Exception {
		String name = "distrlog-interleaved-recovery";
		BKDistributedLogManager dlmwrite0 = createNewDLM(conf, name + "-0");
		BKDistributedLogManager dlmreader0 = createNewDLM(conf, name + "-0");
		BKDistributedLogManager dlmwrite1 = createNewDLM(conf, name + "-1");
		BKDistributedLogManager dlmreader1 = createNewDLM(conf, name + "-1");

		LogReader reader0 = null;
		LogReader reader1 = null;
		long txid = 1;
		int numTrans = 0;

		BKAsyncLogWriter writer0 = dlmwrite0.startAsyncLogSegmentNonPartitioned();
		BKAsyncLogWriter writer1 = dlmwrite1.startAsyncLogSegmentNonPartitioned();
		for (long j = 1; j <= 2; j++) {
			for (int k = 1; k <= 6; k++) {
				if (k == 3) {
					writer0.setForceRecovery(true);
					writer1.setForceRecovery(true);
				}
				DLSN dlsn1 = Utils.ioResult(writer1.write(DLMTestUtil.getLogRecordInstance(txid++)));
				LOG.info("writer1 write record {} - txid = {}", dlsn1, txid - 1);
				DLSN dlsn0 = Utils.ioResult(writer0.write(DLMTestUtil.getLogRecordInstance(txid++)));
				LOG.info("writer0 write record {} - txid = {}", dlsn0, txid - 1);
				writer0.setForceRecovery(false);
				writer1.setForceRecovery(false);
			}
			Utils.ioResult(writer1.writeControlRecord(DLMTestUtil.getLogRecordInstance(txid - 1)));
			Utils.ioResult(writer0.writeControlRecord(DLMTestUtil.getLogRecordInstance(txid - 1)));
			if (null == reader0) {
				reader0 = dlmreader0.getInputStream(1);
			}
			if (null == reader1) {
				reader1 = dlmreader1.getInputStream(1);
			}
			numTrans += drainStreams(reader0, 6, reader1, 6);
			assertEquals((txid - 1), numTrans);
		}
		reader0.close();
		reader1.close();
		assertEquals(txid - 1, dlmreader0.getLogRecordCount() + dlmreader1.getLogRecordCount());
		dlmreader0.close();
		dlmwrite0.close();
		dlmreader1.close();
		dlmwrite1.close();
	}

	@Test(timeout = 60000)
	public void testInterleavedReadersWithRollingEdgeUnPartitioned() throws Exception {
		String name = "distrlog-interleaved-rolling-edge-unpartitioned";
		BKDistributedLogManager dlmwrite0 = createNewDLM(conf, name + "-0");
		BKDistributedLogManager dlmreader0 = createNewDLM(conf, name + "-0");
		BKDistributedLogManager dlmwrite1 = createNewDLM(conf, name + "-1");
		BKDistributedLogManager dlmreader1 = createNewDLM(conf, name + "-1");

		LogReader reader0 = null;
		LogReader reader1 = null;
		long txid = 1;
		int numTrans = 0;

		BKAsyncLogWriter writer0 = dlmwrite0.startAsyncLogSegmentNonPartitioned();
		BKAsyncLogWriter writer1 = dlmwrite1.startAsyncLogSegmentNonPartitioned();
		for (long j = 1; j <= 4; j++) {
			if (j > 1) {
				writer0.setForceRolling(true);
				writer1.setForceRolling(true);
			}
			for (int k = 1; k <= 2; k++) {
				Utils.ioResult(writer1.write(DLMTestUtil.getLogRecordInstance(txid++)));
				Utils.ioResult(writer0.write(DLMTestUtil.getLogRecordInstance(txid++)));
				writer0.setForceRolling(false);
				writer1.setForceRolling(false);
			}
			Utils.ioResult(writer1.writeControlRecord(DLMTestUtil.getLogRecordInstance(txid - 1)));
			Utils.ioResult(writer0.writeControlRecord(DLMTestUtil.getLogRecordInstance(txid - 1)));
			if (null == reader0) {
				reader0 = dlmreader0.getInputStream(1);
			}
			if (null == reader1) {
				reader1 = dlmreader1.getInputStream(1);
			}
			numTrans += drainStreams(reader0, 2, reader1, 2);
			assertEquals((txid - 1), numTrans);
		}
		reader0.close();
		reader1.close();
		dlmreader0.close();
		dlmreader1.close();
	}

}
