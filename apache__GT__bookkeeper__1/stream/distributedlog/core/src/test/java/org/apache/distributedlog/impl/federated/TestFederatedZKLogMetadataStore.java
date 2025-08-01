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
package org.apache.distributedlog.impl.federated;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.bookkeeper.common.util.OrderedScheduler;
import org.apache.distributedlog.DistributedLogConfiguration;
import org.apache.distributedlog.TestDistributedLogBase;
import org.apache.distributedlog.TestZooKeeperClientBuilder;
import org.apache.distributedlog.ZooKeeperClient;
import org.apache.distributedlog.ZooKeeperClientUtils;
import org.apache.distributedlog.callback.NamespaceListener;
import org.apache.distributedlog.exceptions.LogExistsException;
import org.apache.distributedlog.exceptions.UnexpectedException;
import org.apache.distributedlog.impl.BKNamespaceDriver;
import org.apache.distributedlog.metadata.LogMetadataStore;
import org.apache.distributedlog.util.Utils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

/**
 * Test ZK based metadata store.
 */
public class TestFederatedZKLogMetadataStore extends TestDistributedLogBase {

	private static final int zkSessionTimeoutMs = 2000;
	private static final int maxLogsPerSubnamespace = 10;

	static class TestNamespaceListener implements NamespaceListener {

		final CountDownLatch doneLatch = new CountDownLatch(1);
		final AtomicReference<Iterator<String>> resultHolder = new AtomicReference<Iterator<String>>();

		@Override
		public void onStreamsChanged(Iterator<String> streams) {
			resultHolder.set(streams);
			if (streams.hasNext()) {
				doneLatch.countDown();
			}
		}

		Iterator<String> getResult() {
			return resultHolder.get();
		}

		void waitForDone() throws InterruptedException {
			doneLatch.await();
		}
	}

	static class TestNamespaceListenerWithExpectedSize implements NamespaceListener {

		final int expectedSize;
		final CountDownLatch doneLatch = new CountDownLatch(1);
		final AtomicReference<Set<String>> resultHolder = new AtomicReference<Set<String>>();

		TestNamespaceListenerWithExpectedSize(int expectedSize) {
			this.expectedSize = expectedSize;
		}

		Set<String> getResult() {
			return resultHolder.get();
		}

		@Override
		public void onStreamsChanged(Iterator<String> logsIter) {
			List<String> logList = Lists.newArrayList(logsIter);
			if (logList.size() < expectedSize) {
				return;
			}
			resultHolder.set(Sets.newTreeSet(logList));
			doneLatch.countDown();
		}

		void waitForDone() throws InterruptedException {
			doneLatch.await();
		}
	}

	@Rule
	public TestName runtime = new TestName();
	protected final DistributedLogConfiguration baseConf = new DistributedLogConfiguration()
			.setFederatedMaxLogsPerSubnamespace(maxLogsPerSubnamespace);
	protected ZooKeeperClient zkc;
	protected FederatedZKLogMetadataStore metadataStore;
	protected OrderedScheduler scheduler;
	protected URI uri;

	@Before
	public void setup() throws Exception {
		zkc = TestZooKeeperClientBuilder.newBuilder().uri(createDLMURI("/")).sessionTimeoutMs(zkSessionTimeoutMs)
				.build();
		scheduler = OrderedScheduler.newSchedulerBuilder().name("test-zk-logmetadata-store").numThreads(2).build();
		DistributedLogConfiguration conf = new DistributedLogConfiguration();
		conf.addConfiguration(baseConf);
		this.uri = createDLMURI("/" + runtime.getMethodName());
		FederatedZKLogMetadataStore.createFederatedNamespace(uri, zkc);
		metadataStore = new FederatedZKLogMetadataStore(conf, uri, zkc, scheduler);
	}

	@After
	public void teardown() throws Exception {
		if (null != zkc) {
			zkc.close();
		}
		if (null != scheduler) {
			scheduler.shutdown();
		}
	}

	private void deleteLog(String logName) throws Exception {
		Optional<URI> logUriOptional = Utils.ioResult(metadataStore.getLogLocation(logName));
		assertTrue(logUriOptional.isPresent());
		URI logUri = logUriOptional.get();
		zkc.get().delete(logUri.getPath() + "/" + logName, -1);
	}

	@Test(timeout = 60000)
	public void testBasicOperations() throws Exception {
		TestNamespaceListener listener = new TestNamespaceListener();
		metadataStore.registerNamespaceListener(listener);
		String logName = "test-log-1";
		URI logUri = Utils.ioResult(metadataStore.createLog(logName));
		assertEquals(uri, logUri);
		Optional<URI> logLocation = Utils.ioResult(metadataStore.getLogLocation(logName));
		assertTrue(logLocation.isPresent());
		assertEquals(uri, logLocation.get());
		Optional<URI> notExistLogLocation = Utils.ioResult(metadataStore.getLogLocation("non-existent-log"));
		assertFalse(notExistLogLocation.isPresent());
		// listener should receive notification
		listener.waitForDone();
		Iterator<String> logsIter = listener.getResult();
		assertTrue(logsIter.hasNext());
		assertEquals(logName, logsIter.next());
		assertFalse(logsIter.hasNext());
		// get logs should return the log
		Iterator<String> newLogsIter = Utils.ioResult(metadataStore.getLogs(""));
		assertTrue(newLogsIter.hasNext());
		assertEquals(logName, newLogsIter.next());
		assertFalse(newLogsIter.hasNext());
	}

	@Test(timeout = 60000)
	public void testMultipleListeners() throws Exception {
		TestNamespaceListener listener1 = new TestNamespaceListener();
		TestNamespaceListener listener2 = new TestNamespaceListener();
		metadataStore.registerNamespaceListener(listener1);
		metadataStore.registerNamespaceListener(listener2);
		String logName = "test-multiple-listeners";
		URI logUri = Utils.ioResult(metadataStore.createLog(logName));
		assertEquals(uri, logUri);
		listener1.waitForDone();
		listener2.waitForDone();
		Iterator<String> logsIter1 = listener1.getResult();
		Iterator<String> logsIter2 = listener2.getResult();
		assertTrue(Iterators.elementsEqual(logsIter1, logsIter2));
	}

	@Test(timeout = 60000)
	public void testCreateLog() throws Exception {
		DistributedLogConfiguration conf = new DistributedLogConfiguration();
		conf.addConfiguration(baseConf);
		ZooKeeperClient anotherZkc = TestZooKeeperClientBuilder.newBuilder().uri(uri)
				.sessionTimeoutMs(zkSessionTimeoutMs).build();
		FederatedZKLogMetadataStore anotherMetadataStore = new FederatedZKLogMetadataStore(conf, uri, anotherZkc,
				scheduler);
		for (int i = 0; i < 2 * maxLogsPerSubnamespace; i++) {
			LogMetadataStore createStore, checkStore;
			if (i % 2 == 0) {
				createStore = metadataStore;
				checkStore = anotherMetadataStore;
			} else {
				createStore = anotherMetadataStore;
				checkStore = metadataStore;
			}
			String logName = "test-create-log-" + i;
			URI logUri = Utils.ioResult(createStore.createLog(logName));
			Optional<URI> logLocation = Utils.ioResult(checkStore.getLogLocation(logName));
			assertTrue("Log " + logName + " doesn't exist", logLocation.isPresent());
			assertEquals("Different log location " + logLocation.get() + " is found", logUri, logLocation.get());
		}
		assertEquals(2, metadataStore.getSubnamespaces().size());
		assertEquals(2, anotherMetadataStore.getSubnamespaces().size());
	}

	@Test(timeout = 60000)
	public void testDuplicatedLogs() throws Exception {
		DistributedLogConfiguration conf = new DistributedLogConfiguration();
		conf.addConfiguration(baseConf);

		String logName = "test-log";
		Utils.ioResult(metadataStore.createLog(logName));

		URI subNs1 = Utils.ioResult(metadataStore.createSubNamespace());
		URI subNs2 = Utils.ioResult(metadataStore.createSubNamespace());

		String duplicatedLogName = "test-duplicated-logs";
		// Create same log in different sub namespaces
		metadataStore.createLogInNamespaceSync(subNs1, duplicatedLogName);
		metadataStore.createLogInNamespaceSync(subNs2, duplicatedLogName);

		try {
			Utils.ioResult(metadataStore.createLog("non-existent-log"));
			fail("should throw exception when duplicated log found");
		} catch (UnexpectedException ue) {
			// should throw unexpected exception
			assertTrue(metadataStore.duplicatedLogFound.get());
		}
		try {
			Utils.ioResult(metadataStore.getLogLocation(logName));
			fail("should throw exception when duplicated log found");
		} catch (UnexpectedException ue) {
			// should throw unexpected exception
			assertTrue(metadataStore.duplicatedLogFound.get());
		}
		try {
			Utils.ioResult(metadataStore.getLogLocation("non-existent-log"));
			fail("should throw exception when duplicated log found");
		} catch (UnexpectedException ue) {
			// should throw unexpected exception
			assertTrue(metadataStore.duplicatedLogFound.get());
		}
		try {
			Utils.ioResult(metadataStore.getLogLocation(duplicatedLogName));
			fail("should throw exception when duplicated log found");
		} catch (UnexpectedException ue) {
			// should throw unexpected exception
			assertTrue(metadataStore.duplicatedLogFound.get());
		}
		try {
			Utils.ioResult(metadataStore.getLogs(""));
			fail("should throw exception when duplicated log found");
		} catch (UnexpectedException ue) {
			// should throw unexpected exception
			assertTrue(metadataStore.duplicatedLogFound.get());
		}
	}

	@Test(timeout = 60000)
	public void testGetLogLocationWhenCacheMissed() throws Exception {
		String logName = "test-get-location-when-cache-missed";
		URI logUri = Utils.ioResult(metadataStore.createLog(logName));
		assertEquals(uri, logUri);
		metadataStore.removeLogFromCache(logName);
		Optional<URI> logLocation = Utils.ioResult(metadataStore.getLogLocation(logName));
		assertTrue(logLocation.isPresent());
		assertEquals(logUri, logLocation.get());
	}

	@Test(timeout = 60000, expected = LogExistsException.class)
	public void testCreateLogWhenCacheMissed() throws Exception {
		String logName = "test-create-log-when-cache-missed";
		URI logUri = Utils.ioResult(metadataStore.createLog(logName));
		assertEquals(uri, logUri);
		metadataStore.removeLogFromCache(logName);
		Utils.ioResult(metadataStore.createLog(logName));
	}

	@Test(timeout = 60000, expected = LogExistsException.class)
	public void testCreateLogWhenLogExists() throws Exception {
		String logName = "test-create-log-when-log-exists";
		URI logUri = Utils.ioResult(metadataStore.createLog(logName));
		assertEquals(uri, logUri);
		Utils.ioResult(metadataStore.createLog(logName));
	}

	private Set<String> createLogs(int numLogs, String prefix) throws Exception {
		Set<String> expectedLogs = Sets.newTreeSet();
		for (int i = 0; i < numLogs; i++) {
			String logName = prefix + i;
			Utils.ioResult(metadataStore.createLog(logName));
			expectedLogs.add(logName);
		}
		return expectedLogs;
	}

	private Set<String> createLogs(URI uri, int numLogs, String prefix) throws Exception {
		Set<String> expectedLogs = Sets.newTreeSet();
		for (int i = 0; i < numLogs; i++) {
			String logName = prefix + i;
			metadataStore.createLogInNamespaceSync(uri, logName);
			expectedLogs.add(logName);
		}
		return expectedLogs;
	}

	@Test(timeout = 60000)
	public void testGetLogs() throws Exception {
		int numLogs = 3 * maxLogsPerSubnamespace;
		Set<String> expectedLogs = createLogs(numLogs, "test-get-logs");
		Set<String> receivedLogs;
		do {
			TimeUnit.MILLISECONDS.sleep(20);
			receivedLogs = new TreeSet<String>();
			Iterator<String> logs = Utils.ioResult(metadataStore.getLogs(""));
			receivedLogs.addAll(Lists.newArrayList(logs));
		} while (receivedLogs.size() < numLogs);
		assertEquals(numLogs, receivedLogs.size());
		assertTrue(Sets.difference(expectedLogs, receivedLogs).isEmpty());
	}

	@Test(timeout = 60000)
	public void testNamespaceListener() throws Exception {
		int numLogs = 3 * maxLogsPerSubnamespace;
		TestNamespaceListenerWithExpectedSize listener = new TestNamespaceListenerWithExpectedSize(numLogs);
		metadataStore.registerNamespaceListener(listener);
		Set<String> expectedLogs = createLogs(numLogs, "test-namespace-listener");
		listener.waitForDone();
		Set<String> receivedLogs = listener.getResult();
		assertEquals(numLogs, receivedLogs.size());
		assertTrue(Sets.difference(expectedLogs, receivedLogs).isEmpty());

		Random r = new Random(System.currentTimeMillis());
		int logId = r.nextInt(numLogs);
		String logName = "test-namespace-listener" + logId;
		TestNamespaceListener deleteListener = new TestNamespaceListener();
		metadataStore.registerNamespaceListener(deleteListener);
		deleteLog(logName);
		deleteListener.waitForDone();
		Set<String> logsAfterDeleted = Sets.newTreeSet(Lists.newArrayList(deleteListener.getResult()));
		assertEquals(numLogs - 1, logsAfterDeleted.size());
		expectedLogs.remove(logName);
		assertTrue(Sets.difference(expectedLogs, receivedLogs).isEmpty());
	}

	@Test(timeout = 60000)
	public void testCreateLogPickingFirstAvailableSubNamespace() throws Exception {
		URI subNs1 = Utils.ioResult(metadataStore.createSubNamespace());
		URI subNs2 = Utils.ioResult(metadataStore.createSubNamespace());

		Set<String> logs0 = createLogs(uri, maxLogsPerSubnamespace - 1, "test-ns0-");
		Set<String> logs1 = createLogs(subNs1, maxLogsPerSubnamespace, "test-ns1-");
		Set<String> logs2 = createLogs(subNs2, maxLogsPerSubnamespace, "test-ns2-");
		Set<String> allLogs = Sets.newTreeSet();
		allLogs.addAll(logs0);
		allLogs.addAll(logs1);
		allLogs.addAll(logs2);

		// make sure the metadata store saw all 29 logs
		Set<String> receivedLogs;
		do {
			TimeUnit.MILLISECONDS.sleep(20);
			receivedLogs = new TreeSet<String>();
			Iterator<String> logs = Utils.ioResult(metadataStore.getLogs(""));
			receivedLogs.addAll(Lists.newArrayList(logs));
		} while (receivedLogs.size() < 3 * maxLogsPerSubnamespace - 1);

		TestNamespaceListenerWithExpectedSize listener = new TestNamespaceListenerWithExpectedSize(
				3 * maxLogsPerSubnamespace + 1);
		metadataStore.registerNamespaceListener(listener);

		Set<URI> uris = Utils.ioResult(metadataStore.fetchSubNamespaces(null));
		assertEquals(3, uris.size());
		String testLogName = "test-pick-first-available-ns";
		URI createdURI = Utils.ioResult(metadataStore.createLog(testLogName));
		allLogs.add(testLogName);
		assertEquals(uri, createdURI);
		uris = Utils.ioResult(metadataStore.fetchSubNamespaces(null));
		assertEquals(3, uris.size());
		testLogName = "test-create-new-ns";
		URI newURI = Utils.ioResult(metadataStore.createLog(testLogName));
		allLogs.add(testLogName);
		assertFalse(uris.contains(newURI));
		uris = Utils.ioResult(metadataStore.fetchSubNamespaces(null));
		assertEquals(4, uris.size());

		listener.waitForDone();
		receivedLogs = listener.getResult();
		assertEquals(3 * maxLogsPerSubnamespace + 1, receivedLogs.size());
		assertEquals(allLogs, receivedLogs);
	}

	@Test(timeout = 60000)
	public void testZooKeeperSessionExpired() throws Exception {
		Set<String> allLogs = createLogs(2 * maxLogsPerSubnamespace, "test-zookeeper-session-expired-");
		TestNamespaceListenerWithExpectedSize listener = new TestNamespaceListenerWithExpectedSize(
				2 * maxLogsPerSubnamespace + 1);
		metadataStore.registerNamespaceListener(listener);
		ZooKeeperClientUtils.expireSession(zkc, BKNamespaceDriver.getZKServersFromDLUri(uri), zkSessionTimeoutMs);
		String testLogName = "test-log-name";
		allLogs.add(testLogName);

		DistributedLogConfiguration anotherConf = new DistributedLogConfiguration();
		anotherConf.addConfiguration(baseConf);
		ZooKeeperClient anotherZkc = TestZooKeeperClientBuilder.newBuilder().uri(uri)
				.sessionTimeoutMs(zkSessionTimeoutMs).build();
		FederatedZKLogMetadataStore anotherMetadataStore = new FederatedZKLogMetadataStore(anotherConf, uri, anotherZkc,
				scheduler);
		Utils.ioResult(anotherMetadataStore.createLog(testLogName));

		listener.waitForDone();
		Set<String> receivedLogs = listener.getResult();
		assertEquals(2 * maxLogsPerSubnamespace + 1, receivedLogs.size());
		assertEquals(allLogs, receivedLogs);
	}

}
