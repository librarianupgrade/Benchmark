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
package org.apache.distributedlog.tools;

import static com.google.common.base.Preconditions.checkArgument;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.netty.buffer.ByteBuf;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.bookkeeper.client.BKException;
import org.apache.bookkeeper.client.BookKeeper;
import org.apache.bookkeeper.client.BookKeeperAccessor;
import org.apache.bookkeeper.client.BookKeeperAdmin;
import org.apache.bookkeeper.client.LedgerEntry;
import org.apache.bookkeeper.client.LedgerHandle;
import org.apache.bookkeeper.client.LedgerReader;
import org.apache.bookkeeper.common.concurrent.FutureEventListener;
import org.apache.bookkeeper.common.concurrent.FutureUtils;
import org.apache.bookkeeper.net.BookieId;
import org.apache.bookkeeper.proto.BookkeeperInternalCallbacks;
import org.apache.bookkeeper.util.IOUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.distributedlog.BKDistributedLogNamespace;
import org.apache.distributedlog.BookKeeperClient;
import org.apache.distributedlog.BookKeeperClientBuilder;
import org.apache.distributedlog.DLSN;
import org.apache.distributedlog.DistributedLogConfiguration;
import org.apache.distributedlog.DistributedLogConstants;
import org.apache.distributedlog.Entry;
import org.apache.distributedlog.LogRecord;
import org.apache.distributedlog.LogRecordWithDLSN;
import org.apache.distributedlog.LogSegmentMetadata;
import org.apache.distributedlog.ZooKeeperClient;
import org.apache.distributedlog.ZooKeeperClientBuilder;
import org.apache.distributedlog.api.AsyncLogReader;
import org.apache.distributedlog.api.AsyncLogWriter;
import org.apache.distributedlog.api.DistributedLogManager;
import org.apache.distributedlog.api.LogReader;
import org.apache.distributedlog.api.namespace.Namespace;
import org.apache.distributedlog.api.namespace.NamespaceBuilder;
import org.apache.distributedlog.auditor.DLAuditor;
import org.apache.distributedlog.bk.LedgerAllocator;
import org.apache.distributedlog.bk.LedgerAllocatorUtils;
import org.apache.distributedlog.callback.NamespaceListener;
import org.apache.distributedlog.exceptions.LogNotFoundException;
import org.apache.distributedlog.impl.BKNamespaceDriver;
import org.apache.distributedlog.impl.metadata.BKDLConfig;
import org.apache.distributedlog.logsegment.LogSegmentMetadataStore;
import org.apache.distributedlog.metadata.LogSegmentMetadataStoreUpdater;
import org.apache.distributedlog.metadata.MetadataUpdater;
import org.apache.distributedlog.namespace.NamespaceDriver;
import org.apache.distributedlog.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *DistributedLogTool.
*/
public class DistributedLogTool extends Tool {

	private static final Logger logger = LoggerFactory.getLogger(DistributedLogTool.class);

	static final List<String> EMPTY_LIST = Lists.newArrayList();

	static int compareByCompletionTime(long time1, long time2) {
		return time1 > time2 ? 1 : (time1 < time2 ? -1 : 0);
	}

	static final Comparator<LogSegmentMetadata> LOGSEGMENT_COMPARATOR_BY_TIME = new Comparator<LogSegmentMetadata>() {
		@Override
		public int compare(LogSegmentMetadata o1, LogSegmentMetadata o2) {
			if (o1.isInProgress() && o2.isInProgress()) {
				return compareByCompletionTime(o1.getFirstTxId(), o2.getFirstTxId());
			} else if (!o1.isInProgress() && !o2.isInProgress()) {
				return compareByCompletionTime(o1.getCompletionTime(), o2.getCompletionTime());
			} else if (o1.isInProgress() && !o2.isInProgress()) {
				return compareByCompletionTime(o1.getFirstTxId(), o2.getCompletionTime());
			} else {
				return compareByCompletionTime(o1.getCompletionTime(), o2.getFirstTxId());
			}
		}
	};

	static DLSN parseDLSN(String dlsnStr) throws ParseException {
		if (dlsnStr.equals("InitialDLSN")) {
			return DLSN.InitialDLSN;
		}
		String[] parts = dlsnStr.split(",");
		if (parts.length != 3) {
			throw new ParseException("Invalid dlsn : " + dlsnStr);
		}
		try {
			return new DLSN(Long.parseLong(parts[0]), Long.parseLong(parts[1]), Long.parseLong(parts[2]));
		} catch (Exception nfe) {
			throw new ParseException("Invalid dlsn : " + dlsnStr);
		}
	}

	/**
	 * Per DL Command, which parses basic options. e.g. uri.
	 */
	protected abstract static class PerDLCommand extends OptsCommand {

		protected Options options = new Options();
		protected final DistributedLogConfiguration dlConf;
		protected URI uri;
		protected String zkAclId = null;
		protected boolean force = false;
		protected Namespace namespace = null;

		protected PerDLCommand(String name, String description) {
			super(name, description);
			dlConf = new DistributedLogConfiguration();
			// Tools are allowed to read old metadata as long as they can interpret it
			dlConf.setDLLedgerMetadataSkipMinVersionCheck(true);
			options.addOption("u", "uri", true, "DistributedLog URI");
			options.addOption("c", "conf", true, "DistributedLog Configuration File");
			options.addOption("a", "zk-acl-id", true, "Zookeeper ACL ID");
			options.addOption("f", "force", false, "Force command (no warnings or prompts)");
		}

		@Override
		protected int runCmd(CommandLine commandLine) throws Exception {
			try {
				parseCommandLine(commandLine);
			} catch (ParseException pe) {
				System.err.println("ERROR: failed to parse commandline : '" + pe.getMessage() + "'");
				printUsage();
				return -1;
			}
			try {
				return runCmd();
			} finally {
				if (null != namespace) {
					namespace.close();
				}
			}
		}

		protected abstract int runCmd() throws Exception;

		@Override
		protected Options getOptions() {
			return options;
		}

		protected void parseCommandLine(CommandLine cmdline) throws ParseException {
			if (!cmdline.hasOption("u")) {
				throw new ParseException("No distributedlog uri provided.");
			}
			uri = URI.create(cmdline.getOptionValue("u"));
			if (cmdline.hasOption("c")) {
				String configFile = cmdline.getOptionValue("c");
				try {
					dlConf.loadConf(new File(configFile).toURI().toURL());
				} catch (ConfigurationException e) {
					throw new ParseException("Failed to load distributedlog configuration from " + configFile + ".");
				} catch (MalformedURLException e) {
					throw new ParseException(
							"Failed to load distributedlog configuration from " + configFile + ": malformed uri.");
				}
			}
			if (cmdline.hasOption("a")) {
				zkAclId = cmdline.getOptionValue("a");
			}
			if (cmdline.hasOption("f")) {
				force = true;
			}
		}

		protected DistributedLogConfiguration getConf() {
			return dlConf;
		}

		protected URI getUri() {
			return uri;
		}

		protected void setUri(URI uri) {
			this.uri = uri;
		}

		protected String getZkAclId() {
			return zkAclId;
		}

		protected void setZkAclId(String zkAclId) {
			this.zkAclId = zkAclId;
		}

		protected boolean getForce() {
			return force;
		}

		protected void setForce(boolean force) {
			this.force = force;
		}

		protected Namespace getNamespace() throws IOException {
			if (null == this.namespace) {
				this.namespace = NamespaceBuilder.newBuilder().uri(getUri()).conf(getConf()).build();
			}
			return this.namespace;
		}

		protected LogSegmentMetadataStore getLogSegmentMetadataStore() throws IOException {
			return getNamespace().getNamespaceDriver().getLogStreamMetadataStore(NamespaceDriver.Role.READER)
					.getLogSegmentMetadataStore();
		}

		protected ZooKeeperClient getZooKeeperClient() throws IOException {
			NamespaceDriver driver = getNamespace().getNamespaceDriver();
			assert (driver instanceof BKNamespaceDriver);
			return ((BKNamespaceDriver) driver).getWriterZKC();
		}

		protected BookKeeperClient getBookKeeperClient() throws IOException {
			NamespaceDriver driver = getNamespace().getNamespaceDriver();
			assert (driver instanceof BKNamespaceDriver);
			return ((BKNamespaceDriver) driver).getReaderBKC();
		}
	}

	/**
	 * Base class for simple command with no resource setup requirements.
	 */
	public abstract static class SimpleCommand extends OptsCommand {

		protected final Options options = new Options();

		SimpleCommand(String name, String description) {
			super(name, description);
		}

		@Override
		protected int runCmd(CommandLine commandLine) throws Exception {
			try {
				parseCommandLine(commandLine);
			} catch (ParseException pe) {
				System.err.println("ERROR: failed to parse commandline : '" + pe.getMessage() + "'");
				printUsage();
				return -1;
			}
			return runSimpleCmd();
		}

		protected abstract int runSimpleCmd() throws Exception;

		protected abstract void parseCommandLine(CommandLine cmdline) throws ParseException;

		@Override
		protected Options getOptions() {
			return options;
		}
	}

	/**
	 * Per Stream Command, which parse common options for per stream. e.g. stream name.
	 */
	abstract static class PerStreamCommand extends PerDLCommand {

		protected String streamName;

		protected PerStreamCommand(String name, String description) {
			super(name, description);
			options.addOption("s", "stream", true, "Stream Name");
		}

		@Override
		protected void parseCommandLine(CommandLine cmdline) throws ParseException {
			super.parseCommandLine(cmdline);
			if (!cmdline.hasOption("s")) {
				throw new ParseException("No stream name provided.");
			}
			streamName = cmdline.getOptionValue("s");
		}

		protected String getStreamName() {
			return streamName;
		}

		protected void setStreamName(String streamName) {
			this.streamName = streamName;
		}
	}

	/**
	 * NOTE: we might consider adding a command to 'delete' namespace. The implementation of the namespace
	 *       driver should implement the 'delete' operation.
	 */
	protected static class DeleteAllocatorPoolCommand extends PerDLCommand {

		int concurrency = 1;
		String allocationPoolPath = DistributedLogConstants.ALLOCATION_POOL_NODE;

		DeleteAllocatorPoolCommand() {
			super("delete_allocator_pool", "Delete allocator pool for a given distributedlog instance");
			options.addOption("t", "concurrency", true, "Concurrency on deleting allocator pool.");
			options.addOption("ap", "allocation-pool-path", true, "Ledger Allocation Pool Path");
		}

		@Override
		protected void parseCommandLine(CommandLine cmdline) throws ParseException {
			super.parseCommandLine(cmdline);
			if (cmdline.hasOption("t")) {
				concurrency = Integer.parseInt(cmdline.getOptionValue("t"));
				if (concurrency <= 0) {
					throw new ParseException(
							"Invalid concurrency value : " + concurrency + ": it must be greater or equal to 0.");
				}
			}
			if (cmdline.hasOption("ap")) {
				allocationPoolPath = cmdline.getOptionValue("ap");
				if (!allocationPoolPath.startsWith(".") || !allocationPoolPath.contains("allocation")) {
					throw new ParseException("Invalid allocation pool path : " + allocationPoolPath
							+ ": it must starts with a '.' and must contains 'allocation'");
				}
			}
		}

		@Override
		protected int runCmd() throws Exception {
			String rootPath = getUri().getPath() + "/" + allocationPoolPath;
			final ScheduledExecutorService allocationExecutor = Executors.newSingleThreadScheduledExecutor();
			ExecutorService executorService = Executors.newFixedThreadPool(concurrency);
			checkArgument(getNamespace() instanceof BKDistributedLogNamespace);
			BKDistributedLogNamespace bkns = (BKDistributedLogNamespace) getNamespace();
			final ZooKeeperClient zkc = ((BKNamespaceDriver) bkns.getNamespaceDriver()).getWriterZKC();
			final BookKeeperClient bkc = ((BKNamespaceDriver) bkns.getNamespaceDriver()).getReaderBKC();
			try {
				List<String> pools = zkc.get().getChildren(rootPath, false);
				final LinkedBlockingQueue<String> poolsToDelete = new LinkedBlockingQueue<String>();
				if (getForce() || IOUtils.confirmPrompt("Are you sure you want to delete allocator pools : " + pools)) {
					for (String pool : pools) {
						poolsToDelete.add(rootPath + "/" + pool);
					}
					final CountDownLatch doneLatch = new CountDownLatch(concurrency);
					for (int i = 0; i < concurrency; i++) {
						final int tid = i;
						executorService.submit(new Runnable() {
							@Override
							public void run() {
								while (!poolsToDelete.isEmpty()) {
									String poolPath = poolsToDelete.poll();
									if (null == poolPath) {
										break;
									}
									try {
										LedgerAllocator allocator = LedgerAllocatorUtils.createLedgerAllocatorPool(
												poolPath, 0, getConf(), zkc, bkc, allocationExecutor);
										allocator.delete();
										System.out.println("Deleted allocator pool : " + poolPath + " .");
									} catch (IOException ioe) {
										System.err.println("Failed to delete allocator pool " + poolPath + " : "
												+ ioe.getMessage());
									}
								}
								doneLatch.countDown();
								System.out.println("Thread " + tid + " is done.");
							}
						});
					}
					doneLatch.await();
				}
			} finally {
				executorService.shutdown();
				allocationExecutor.shutdown();
			}
			return 0;
		}

		@Override
		protected String getUsage() {
			return "delete_allocator_pool";
		}
	}

	/**
	 * List distributedlog associated info.
	 */
	public static class ListCommand extends PerDLCommand {

		boolean printMetadata = false;
		boolean printHex = false;

		ListCommand() {
			super("list", "list streams of a given distributedlog instance");
			options.addOption("m", "meta", false, "Print metadata associated with each stream");
			options.addOption("x", "hex", false, "Print metadata in hex format");
		}

		@Override
		protected void parseCommandLine(CommandLine cmdline) throws ParseException {
			super.parseCommandLine(cmdline);
			printMetadata = cmdline.hasOption("m");
			printHex = cmdline.hasOption("x");
		}

		@Override
		protected String getUsage() {
			return "list [options]";
		}

		@Override
		protected int runCmd() throws Exception {
			printStreams(getNamespace());
			return 0;
		}

		@SuppressWarnings("deprecation")
		protected void printStreams(Namespace namespace) throws Exception {
			Iterator<String> streams = namespace.getLogs();
			System.out.println("Streams under " + getUri() + " : ");
			System.out.println("--------------------------------");
			while (streams.hasNext()) {
				String streamName = streams.next();
				System.out.println(streamName);
				if (!printMetadata) {
					continue;
				}
				org.apache.distributedlog.api.MetadataAccessor accessor = namespace.getNamespaceDriver()
						.getMetadataAccessor(streamName);
				byte[] metadata = accessor.getMetadata();
				if (null == metadata || metadata.length == 0) {
					continue;
				}
				if (printHex) {
					System.out.println(Hex.encodeHexString(metadata));
				} else {
					System.out.println(new String(metadata, UTF_8));
				}
				System.out.println();
			}
			System.out.println("--------------------------------");
		}
	}

	/**
	 * watch and report changes for a dl namespace.
	 */
	public static class WatchNamespaceCommand extends PerDLCommand implements NamespaceListener {
		private Set<String> currentSet = Sets.<String>newHashSet();
		private CountDownLatch doneLatch = new CountDownLatch(1);

		WatchNamespaceCommand() {
			super("watch", "watch and report changes for a dl namespace");
		}

		@Override
		protected void parseCommandLine(CommandLine cmdline) throws ParseException {
			super.parseCommandLine(cmdline);
		}

		@Override
		protected String getUsage() {
			return "watch [options]";
		}

		@Override
		protected int runCmd() throws Exception {
			watchAndReportChanges(getNamespace());
			doneLatch.await();
			return 0;
		}

		@Override
		public synchronized void onStreamsChanged(Iterator<String> streams) {
			Set<String> updatedSet = Sets.newHashSet(streams);
			Set<String> oldStreams = Sets.difference(currentSet, updatedSet);
			Set<String> newStreams = Sets.difference(updatedSet, currentSet);
			currentSet = updatedSet;

			System.out.println("Old streams : ");
			for (String stream : oldStreams) {
				System.out.println(stream);
			}

			System.out.println("New streams : ");
			for (String stream : newStreams) {
				System.out.println(stream);
			}

			System.out.println();
		}

		protected void watchAndReportChanges(Namespace namespace) throws Exception {
			namespace.registerNamespaceListener(this);
		}
	}

	/**
	 * Inspect streams under a given dl uri to find any potential corruptions.
	 */
	protected static class InspectCommand extends PerDLCommand {

		int numThreads = 1;
		String streamPrefix = null;
		boolean printInprogressOnly = false;
		boolean dumpEntries = false;
		boolean orderByTime = false;
		boolean printStreamsOnly = false;
		boolean checkInprogressOnly = false;

		InspectCommand() {
			super("inspect", "Inspect streams under a given dl uri to find any potential corruptions");
			options.addOption("t", "threads", true, "Number threads to do inspection.");
			options.addOption("ft", "filter", true, "Stream filter by prefix");
			options.addOption("i", "inprogress", false, "Print inprogress log segments only");
			options.addOption("d", "dump", false, "Dump entries of inprogress log segments");
			options.addOption("ot", "orderbytime", false, "Order the log segments by completion time");
			options.addOption("pso", "print-stream-only", false, "Print streams only");
			options.addOption("cio", "check-inprogress-only", false, "Check duplicated inprogress only");
		}

		@Override
		protected void parseCommandLine(CommandLine cmdline) throws ParseException {
			super.parseCommandLine(cmdline);
			if (cmdline.hasOption("t")) {
				numThreads = Integer.parseInt(cmdline.getOptionValue("t"));
			}
			if (cmdline.hasOption("ft")) {
				streamPrefix = cmdline.getOptionValue("ft");
			}
			printInprogressOnly = cmdline.hasOption("i");
			dumpEntries = cmdline.hasOption("d");
			orderByTime = cmdline.hasOption("ot");
			printStreamsOnly = cmdline.hasOption("pso");
			checkInprogressOnly = cmdline.hasOption("cio");
		}

		@Override
		protected int runCmd() throws Exception {
			SortedMap<String, List<Pair<LogSegmentMetadata, List<String>>>> corruptedCandidates = new TreeMap<String, List<Pair<LogSegmentMetadata, List<String>>>>();
			inspectStreams(corruptedCandidates);
			System.out.println("Corrupted Candidates : ");
			if (printStreamsOnly) {
				System.out.println(corruptedCandidates.keySet());
				return 0;
			}
			for (Map.Entry<String, List<Pair<LogSegmentMetadata, List<String>>>> entry : corruptedCandidates
					.entrySet()) {
				System.out.println(entry.getKey() + " : \n");
				for (Pair<LogSegmentMetadata, List<String>> pair : entry.getValue()) {
					System.out.println("\t - " + pair.getLeft());
					if (printInprogressOnly && dumpEntries) {
						int i = 0;
						for (String entryData : pair.getRight()) {
							System.out.println("\t" + i + "\t: " + entryData);
							++i;
						}
					}
				}
				System.out.println();
			}
			return 0;
		}

		private void inspectStreams(
				final SortedMap<String, List<Pair<LogSegmentMetadata, List<String>>>> corruptedCandidates)
				throws Exception {
			Iterator<String> streamCollection = getNamespace().getLogs();
			final List<String> streams = new ArrayList<String>();
			while (streamCollection.hasNext()) {
				String s = streamCollection.next();
				if (null != streamPrefix) {
					if (s.startsWith(streamPrefix)) {
						streams.add(s);
					}
				} else {
					streams.add(s);
				}
			}
			if (0 == streams.size()) {
				return;
			}
			println("Streams : " + streams);
			if (!getForce()
					&& !IOUtils.confirmPrompt("Are you sure you want to inspect " + streams.size() + " streams")) {
				return;
			}
			numThreads = Math.min(streams.size(), numThreads);
			final int numStreamsPerThreads = streams.size() / numThreads;
			Thread[] threads = new Thread[numThreads];
			for (int i = 0; i < numThreads; i++) {
				final int tid = i;
				threads[i] = new Thread("Inspect-" + i) {
					@Override
					public void run() {
						try {
							inspectStreams(streams, tid, numStreamsPerThreads, corruptedCandidates);
							System.out.println("Thread " + tid + " finished.");
						} catch (Exception e) {
							System.err.println("Thread " + tid + " quits with exception : " + e.getMessage());
						}
					}
				};
				threads[i].start();
			}
			for (int i = 0; i < numThreads; i++) {
				threads[i].join();
			}
		}

		private void inspectStreams(List<String> streams, int tid, int numStreamsPerThreads,
				SortedMap<String, List<Pair<LogSegmentMetadata, List<String>>>> corruptedCandidates) throws Exception {
			int startIdx = tid * numStreamsPerThreads;
			int endIdx = Math.min(streams.size(), (tid + 1) * numStreamsPerThreads);
			for (int i = startIdx; i < endIdx; i++) {
				String s = streams.get(i);
				BookKeeperClient bkc = getBookKeeperClient();
				DistributedLogManager dlm = getNamespace().openLog(s);
				try {
					List<LogSegmentMetadata> segments = dlm.getLogSegments();
					if (segments.size() <= 1) {
						continue;
					}
					boolean isCandidate = false;
					if (checkInprogressOnly) {
						Set<Long> inprogressSeqNos = new HashSet<Long>();
						for (LogSegmentMetadata segment : segments) {
							if (segment.isInProgress()) {
								inprogressSeqNos.add(segment.getLogSegmentSequenceNumber());
							}
						}
						for (LogSegmentMetadata segment : segments) {
							if (!segment.isInProgress()
									&& inprogressSeqNos.contains(segment.getLogSegmentSequenceNumber())) {
								isCandidate = true;
							}
						}
					} else {
						LogSegmentMetadata firstSegment = segments.get(0);
						long lastSeqNo = firstSegment.getLogSegmentSequenceNumber();

						for (int j = 1; j < segments.size(); j++) {
							LogSegmentMetadata nextSegment = segments.get(j);
							if (lastSeqNo + 1 != nextSegment.getLogSegmentSequenceNumber()) {
								isCandidate = true;
								break;
							}
							++lastSeqNo;
						}
					}
					if (isCandidate) {
						if (orderByTime) {
							Collections.sort(segments, LOGSEGMENT_COMPARATOR_BY_TIME);
						}
						List<Pair<LogSegmentMetadata, List<String>>> ledgers = new ArrayList<Pair<LogSegmentMetadata, List<String>>>();
						for (LogSegmentMetadata seg : segments) {
							LogSegmentMetadata segment = seg;
							List<String> dumpedEntries = new ArrayList<String>();
							if (segment.isInProgress()) {
								LedgerHandle lh = bkc.get().openLedgerNoRecovery(segment.getLogSegmentId(),
										BookKeeper.DigestType.CRC32, dlConf.getBKDigestPW().getBytes(UTF_8));
								try {
									long lac = lh.readLastConfirmed();
									segment = segment.mutator().setLastEntryId(lac).build();
									if (printInprogressOnly && dumpEntries && lac >= 0) {
										Enumeration<LedgerEntry> entries = lh.readEntries(0L, lac);
										while (entries.hasMoreElements()) {
											LedgerEntry entry = entries.nextElement();
											dumpedEntries.add(new String(entry.getEntry(), UTF_8));
										}
									}
								} finally {
									lh.close();
								}
							}
							if (printInprogressOnly) {
								if (segment.isInProgress()) {
									ledgers.add(Pair.of(segment, dumpedEntries));
								}
							} else {
								ledgers.add(Pair.of(segment, EMPTY_LIST));
							}
						}
						synchronized (corruptedCandidates) {
							corruptedCandidates.put(s, ledgers);
						}
					}
				} finally {
					dlm.close();
				}
			}
		}

		@Override
		protected String getUsage() {
			return "inspect [options]";
		}
	}

	/**
	 * Command used to truncate streams under a given dl uri.
	 */
	protected static class TruncateCommand extends PerDLCommand {

		int numThreads = 1;
		String streamPrefix = null;
		boolean deleteStream = false;

		TruncateCommand() {
			super("truncate", "truncate streams under a given dl uri");
			options.addOption("t", "threads", true, "Number threads to do truncation");
			options.addOption("ft", "filter", true, "Stream filter by prefix");
			options.addOption("d", "delete", false, "Delete Stream");
		}

		@Override
		protected void parseCommandLine(CommandLine cmdline) throws ParseException {
			super.parseCommandLine(cmdline);
			if (cmdline.hasOption("t")) {
				numThreads = Integer.parseInt(cmdline.getOptionValue("t"));
			}
			if (cmdline.hasOption("ft")) {
				streamPrefix = cmdline.getOptionValue("ft");
			}
			if (cmdline.hasOption("d")) {
				deleteStream = true;
			}
		}

		@Override
		protected String getUsage() {
			return "truncate [options]";
		}

		protected void setFilter(String filter) {
			this.streamPrefix = filter;
		}

		@Override
		protected int runCmd() throws Exception {
			getConf().setZkAclId(getZkAclId());
			return truncateStreams(getNamespace());
		}

		private int truncateStreams(final Namespace namespace) throws Exception {
			Iterator<String> streamCollection = namespace.getLogs();
			final List<String> streams = new ArrayList<String>();
			while (streamCollection.hasNext()) {
				String s = streamCollection.next();
				if (null != streamPrefix) {
					if (s.startsWith(streamPrefix)) {
						streams.add(s);
					}
				} else {
					streams.add(s);
				}
			}
			if (0 == streams.size()) {
				return 0;
			}
			System.out.println("Streams : " + streams);
			if (!getForce() && !IOUtils.confirmPrompt("Do you want to truncate " + streams.size() + " streams ?")) {
				return 0;
			}
			numThreads = Math.min(streams.size(), numThreads);
			final int numStreamsPerThreads = streams.size() / numThreads + 1;
			Thread[] threads = new Thread[numThreads];
			for (int i = 0; i < numThreads; i++) {
				final int tid = i;
				threads[i] = new Thread("Truncate-" + i) {
					@Override
					public void run() {
						try {
							truncateStreams(namespace, streams, tid, numStreamsPerThreads);
							System.out.println("Thread " + tid + " finished.");
						} catch (IOException e) {
							System.err.println("Thread " + tid + " quits with exception : " + e.getMessage());
						}
					}
				};
				threads[i].start();
			}
			for (int i = 0; i < numThreads; i++) {
				threads[i].join();
			}
			return 0;
		}

		private void truncateStreams(Namespace namespace, List<String> streams, int tid, int numStreamsPerThreads)
				throws IOException {
			int startIdx = tid * numStreamsPerThreads;
			int endIdx = Math.min(streams.size(), (tid + 1) * numStreamsPerThreads);
			for (int i = startIdx; i < endIdx; i++) {
				String s = streams.get(i);
				DistributedLogManager dlm = namespace.openLog(s);
				try {
					if (deleteStream) {
						dlm.delete();
					} else {
						dlm.purgeLogsOlderThan(Long.MAX_VALUE);
					}
				} finally {
					dlm.close();
				}
			}
		}
	}

	/**
	 * Simple bk client.
	 */
	public static class SimpleBookKeeperClient {
		BookKeeperClient bkc;
		ZooKeeperClient zkc;

		public SimpleBookKeeperClient(DistributedLogConfiguration conf, URI uri) {
			try {
				zkc = ZooKeeperClientBuilder.newBuilder().sessionTimeoutMs(conf.getZKSessionTimeoutMilliseconds())
						.zkAclId(conf.getZkAclId()).uri(uri).build();
				BKDLConfig bkdlConfig = BKDLConfig.resolveDLConfig(zkc, uri);
				BKDLConfig.propagateConfiguration(bkdlConfig, conf);
				bkc = BookKeeperClientBuilder.newBuilder().zkc(zkc).dlConfig(conf)
						.ledgersPath(bkdlConfig.getBkLedgersPath()).name("dlog").build();
			} catch (Exception e) {
				close();
			}
		}

		public BookKeeperClient client() {
			return bkc;
		}

		public void close() {
			if (null != bkc) {
				bkc.close();
			}
			if (null != zkc) {
				zkc.close();
			}
		}
	}

	/**
	 * Command used to show metadata of a given stream and list segments.
	 */
	protected static class ShowCommand extends PerStreamCommand {

		SimpleBookKeeperClient bkc = null;
		boolean listSegments = true;
		boolean listEppStats = false;
		long firstLid = 0;
		long lastLid = -1;

		ShowCommand() {
			super("show", "show metadata of a given stream and list segments");
			options.addOption("ns", "no-log-segments", false, "Do not list log segment metadata");
			options.addOption("lp", "placement-stats", false, "Show ensemble placement stats");
			options.addOption("fl", "first-ledger", true, "First log sement no");
			options.addOption("ll", "last-ledger", true, "Last log sement no");
		}

		@Override
		protected void parseCommandLine(CommandLine cmdline) throws ParseException {
			super.parseCommandLine(cmdline);
			if (cmdline.hasOption("fl")) {
				try {
					firstLid = Long.parseLong(cmdline.getOptionValue("fl"));
				} catch (NumberFormatException nfe) {
					throw new ParseException("Invalid ledger id " + cmdline.getOptionValue("fl"));
				}
			}
			if (firstLid < 0) {
				throw new IllegalArgumentException("Invalid ledger id " + firstLid);
			}
			if (cmdline.hasOption("ll")) {
				try {
					lastLid = Long.parseLong(cmdline.getOptionValue("ll"));
				} catch (NumberFormatException nfe) {
					throw new ParseException("Invalid ledger id " + cmdline.getOptionValue("ll"));
				}
			}
			if (lastLid != -1 && firstLid > lastLid) {
				throw new IllegalArgumentException("Invalid ledger ids " + firstLid + " " + lastLid);
			}
			listSegments = !cmdline.hasOption("ns");
			listEppStats = cmdline.hasOption("lp");
		}

		@Override
		protected int runCmd() throws Exception {
			DistributedLogManager dlm = getNamespace().openLog(getStreamName());
			try {
				if (listEppStats) {
					bkc = new SimpleBookKeeperClient(getConf(), getUri());
				}
				printMetadata(dlm);
			} finally {
				dlm.close();
				if (null != bkc) {
					bkc.close();
				}
			}
			return 0;
		}

		private void printMetadata(DistributedLogManager dlm) throws Exception {
			printHeader(dlm);
			if (listSegments) {
				System.out.println("Ledgers : ");
				List<LogSegmentMetadata> segments = dlm.getLogSegments();
				for (LogSegmentMetadata segment : segments) {
					if (include(segment)) {
						printLedgerRow(segment);
					}
				}
			}
		}

		private void printHeader(DistributedLogManager dlm) throws Exception {
			DLSN firstDlsn = FutureUtils.result(dlm.getFirstDLSNAsync());
			boolean endOfStreamMarked = dlm.isEndOfStreamMarked();
			DLSN lastDlsn = dlm.getLastDLSN();
			long firstTxnId = dlm.getFirstTxId();
			long lastTxnId = dlm.getLastTxId();
			long recordCount = dlm.getLogRecordCount();
			String result = String.format(
					"Stream : (firstTxId=%d, lastTxid=%d, firstDlsn=%s,"
							+ " lastDlsn=%s, endOfStreamMarked=%b, recordCount=%d)",
					firstTxnId, lastTxnId, getDlsnName(firstDlsn), getDlsnName(lastDlsn), endOfStreamMarked,
					recordCount);
			System.out.println(result);
			if (listEppStats) {
				printEppStatsHeader(dlm);
			}
		}

		boolean include(LogSegmentMetadata segment) {
			return (firstLid <= segment.getLogSegmentSequenceNumber()
					&& (lastLid == -1 || lastLid >= segment.getLogSegmentSequenceNumber()));
		}

		private void printEppStatsHeader(DistributedLogManager dlm) throws Exception {
			String label = "Ledger Placement :";
			System.out.println(label);
			Map<BookieId, Integer> totals = new HashMap<BookieId, Integer>();
			List<LogSegmentMetadata> segments = dlm.getLogSegments();
			for (LogSegmentMetadata segment : segments) {
				if (include(segment)) {
					merge(totals, getBookieStats(segment));
				}
			}
			List<Map.Entry<BookieId, Integer>> entries = new ArrayList<Map.Entry<BookieId, Integer>>(totals.entrySet());
			Collections.sort(entries, new Comparator<Map.Entry<BookieId, Integer>>() {
				@Override
				public int compare(Map.Entry<BookieId, Integer> o1, Map.Entry<BookieId, Integer> o2) {
					return o2.getValue() - o1.getValue();
				}
			});
			int width = 0;
			int totalEntries = 0;
			for (Map.Entry<BookieId, Integer> entry : entries) {
				width = Math.max(width, label.length() + 1 + entry.getKey().toString().length());
				totalEntries += entry.getValue();
			}
			for (Map.Entry<BookieId, Integer> entry : entries) {
				System.out.println(String.format("%" + width + "s\t%6.2f%%\t\t%d", entry.getKey(),
						entry.getValue() * 1.0 / totalEntries, entry.getValue()));
			}
		}

		private void printLedgerRow(LogSegmentMetadata segment) throws Exception {
			System.out.println(segment.getLogSegmentSequenceNumber() + "\t: " + segment);
		}

		private Map<BookieId, Integer> getBookieStats(LogSegmentMetadata segment) throws Exception {
			Map<BookieId, Integer> stats = new HashMap<BookieId, Integer>();
			LedgerHandle lh = bkc.client().get().openLedgerNoRecovery(segment.getLogSegmentId(),
					BookKeeper.DigestType.CRC32, getConf().getBKDigestPW().getBytes(UTF_8));
			long eidFirst = 0;
			for (SortedMap.Entry<Long, ? extends List<BookieId>> entry : LedgerReader.bookiesForLedger(lh).entrySet()) {
				long eidLast = entry.getKey();
				long count = eidLast - eidFirst + 1;
				for (BookieId bookie : entry.getValue()) {
					merge(stats, bookie, (int) count);
				}
				eidFirst = eidLast;
			}
			return stats;
		}

		void merge(Map<BookieId, Integer> m, BookieId bookie, Integer count) {
			if (m.containsKey(bookie)) {
				m.put(bookie, count + m.get(bookie));
			} else {
				m.put(bookie, count);
			}
		}

		void merge(Map<BookieId, Integer> m1, Map<BookieId, Integer> m2) {
			for (Map.Entry<BookieId, Integer> entry : m2.entrySet()) {
				merge(m1, entry.getKey(), entry.getValue());
			}
		}

		String getDlsnName(DLSN dlsn) {
			if (dlsn.equals(DLSN.InvalidDLSN)) {
				return "InvalidDLSN";
			}
			return dlsn.toString();
		}

		@Override
		protected String getUsage() {
			return "show [options]";
		}
	}

	static class CountCommand extends PerStreamCommand {

		DLSN startDLSN = null;
		DLSN endDLSN = null;

		protected CountCommand() {
			super("count", "count number records between dlsns");
		}

		@Override
		protected void parseCommandLine(CommandLine cmdline) throws ParseException {
			super.parseCommandLine(cmdline);
			String[] args = cmdline.getArgs();
			if (args.length < 1) {
				throw new ParseException("Must specify at least start dlsn.");
			}
			if (args.length >= 1) {
				startDLSN = parseDLSN(args[0]);
			}
			if (args.length >= 2) {
				endDLSN = parseDLSN(args[1]);
			}
		}

		@Override
		protected int runCmd() throws Exception {
			DistributedLogManager dlm = getNamespace().openLog(getStreamName());
			try {
				long count = 0;
				if (null == endDLSN) {
					count = countToLastRecord(dlm);
				} else {
					count = countFromStartToEnd(dlm);
				}
				System.out.println("total is " + count + " records.");
				return 0;
			} finally {
				dlm.close();
			}
		}

		int countFromStartToEnd(DistributedLogManager dlm) throws Exception {
			int count = 0;
			try {
				LogReader reader = dlm.getInputStream(startDLSN);
				try {
					LogRecordWithDLSN record = reader.readNext(false);
					LogRecordWithDLSN preRecord = record;
					System.out.println("first record : " + record);
					while (null != record) {
						if (record.getDlsn().compareTo(endDLSN) > 0) {
							break;
						}
						++count;
						if (count % 1000 == 0) {
							logger.info("read {} records from {}...", count, getStreamName());
						}
						preRecord = record;
						record = reader.readNext(false);
					}
					System.out.println("last record : " + preRecord);
				} finally {
					reader.close();
				}
			} finally {
				dlm.close();
			}
			return count;
		}

		long countToLastRecord(DistributedLogManager dlm) throws Exception {
			return FutureUtils.result(dlm.getLogRecordCountAsync(startDLSN));
		}

		@Override
		protected String getUsage() {
			return "count <start> <end>";
		}
	}

	/**
	 * Command used to delete a given stream.
	 */
	public static class DeleteCommand extends PerStreamCommand {

		protected DeleteCommand() {
			super("delete", "delete a given stream");
		}

		@Override
		protected int runCmd() throws Exception {
			getConf().setZkAclId(getZkAclId());
			DistributedLogManager dlm = getNamespace().openLog(getStreamName());
			try {
				dlm.delete();
			} finally {
				dlm.close();
			}
			return 0;
		}

		@Override
		protected String getUsage() {
			return "delete";
		}
	}

	/**
	 * Command used to delete given ledgers.
	 */
	public static class DeleteLedgersCommand extends PerDLCommand {

		private final List<Long> ledgers = new ArrayList<Long>();

		int numThreads = 1;

		protected DeleteLedgersCommand() {
			super("delete_ledgers", "delete given ledgers");
			options.addOption("l", "ledgers", true, "List of ledgers, separated by comma");
			options.addOption("lf", "ledgers-file", true, "File of list of ledgers, each line has a ledger id");
			options.addOption("t", "concurrency", true, "Number of threads to run deletions");
		}

		@Override
		protected void parseCommandLine(CommandLine cmdline) throws ParseException {
			super.parseCommandLine(cmdline);
			if (cmdline.hasOption("l") && cmdline.hasOption("lf")) {
				throw new ParseException("Please specify ledgers: either use list or use file only.");
			}
			if (!cmdline.hasOption("l") && !cmdline.hasOption("lf")) {
				throw new ParseException(
						"No ledgers specified." + " Please specify ledgers either use list or use file only.");
			}
			if (cmdline.hasOption("l")) {
				String ledgersStr = cmdline.getOptionValue("l");
				String[] ledgerStrs = ledgersStr.split(",");
				for (String ledgerStr : ledgerStrs) {
					ledgers.add(Long.parseLong(ledgerStr));
				}
			}
			if (cmdline.hasOption("lf")) {
				BufferedReader br = null;
				try {

					br = new BufferedReader(new InputStreamReader(
							new FileInputStream(new File(cmdline.getOptionValue("lf"))), UTF_8.name()));
					String line;
					while ((line = br.readLine()) != null) {
						ledgers.add(Long.parseLong(line));
					}
				} catch (FileNotFoundException e) {
					throw new ParseException("No ledgers file " + cmdline.getOptionValue("lf") + " found.");
				} catch (IOException e) {
					throw new ParseException("Invalid ledgers file " + cmdline.getOptionValue("lf") + " found.");
				} finally {
					if (null != br) {
						try {
							br.close();
						} catch (IOException e) {
							// no-op
						}
					}
				}
			}
			if (cmdline.hasOption("t")) {
				numThreads = Integer.parseInt(cmdline.getOptionValue("t"));
			}
		}

		@Override
		protected String getUsage() {
			return "delete_ledgers [options]";
		}

		@Override
		protected int runCmd() throws Exception {
			ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
			try {
				final AtomicInteger numLedgers = new AtomicInteger(0);
				final CountDownLatch doneLatch = new CountDownLatch(numThreads);
				final AtomicInteger numFailures = new AtomicInteger(0);
				final LinkedBlockingQueue<Long> ledgerQueue = new LinkedBlockingQueue<Long>();
				ledgerQueue.addAll(ledgers);
				for (int i = 0; i < numThreads; i++) {
					final int tid = i;
					executorService.submit(new Runnable() {
						@Override
						public void run() {
							while (true) {
								Long ledger = ledgerQueue.poll();
								if (null == ledger) {
									break;
								}
								try {
									getBookKeeperClient().get().deleteLedger(ledger);
									int numLedgersDeleted = numLedgers.incrementAndGet();
									if (numLedgersDeleted % 1000 == 0) {
										System.out.println("Deleted " + numLedgersDeleted + " ledgers.");
									}
								} catch (BKException.BKNoSuchLedgerExistsOnMetadataServerException e) {
									int numLedgersDeleted = numLedgers.incrementAndGet();
									if (numLedgersDeleted % 1000 == 0) {
										System.out.println("Deleted " + numLedgersDeleted + " ledgers.");
									}
								} catch (Exception e) {
									numFailures.incrementAndGet();
									break;
								}
							}
							doneLatch.countDown();
							System.out.println("Thread " + tid + " quits");
						}
					});
				}
				doneLatch.await();
				if (numFailures.get() > 0) {
					throw new IOException("Encounter " + numFailures.get() + " failures during deleting ledgers");
				}
			} finally {
				executorService.shutdown();
			}
			return 0;
		}
	}

	/**
	 * Command used to create streams under a given namespace.
	 */
	public static class CreateCommand extends PerDLCommand {

		final List<String> streams = new ArrayList<String>();

		String streamPrefix = null;
		String streamExpression = null;

		CreateCommand() {
			super("create", "create streams under a given namespace");
			options.addOption("r", "prefix", true, "Prefix of stream name. E.g. 'QuantumLeapTest-'.");
			options.addOption("e", "expression", true, "Expression to generate stream suffix. "
					+ "Currently we support range 'x-y', list 'x,y,z' and name 'xyz'");
		}

		@Override
		protected void parseCommandLine(CommandLine cmdline) throws ParseException {
			super.parseCommandLine(cmdline);
			if (cmdline.hasOption("r")) {
				streamPrefix = cmdline.getOptionValue("r");
			}
			if (cmdline.hasOption("e")) {
				streamExpression = cmdline.getOptionValue("e");
			}
			if (null == streamPrefix || null == streamExpression) {
				throw new ParseException("Please specify stream prefix & expression.");
			}
		}

		protected void generateStreams(String streamPrefix, String streamExpression) throws ParseException {
			// parse the stream expression
			if (streamExpression.contains("-")) {
				// a range expression
				String[] parts = streamExpression.split("-");
				if (parts.length != 2) {
					throw new ParseException("Invalid stream index range : " + streamExpression);
				}
				try {
					int start = Integer.parseInt(parts[0]);
					int end = Integer.parseInt(parts[1]);
					if (start > end) {
						throw new ParseException("Invalid stream index range : " + streamExpression);
					}
					for (int i = start; i <= end; i++) {
						streams.add(streamPrefix + i);
					}
				} catch (NumberFormatException nfe) {
					throw new ParseException("Invalid stream index range : " + streamExpression);
				}
			} else if (streamExpression.contains(",")) {
				// a list expression
				String[] parts = streamExpression.split(",");
				try {
					for (String part : parts) {
						int idx = Integer.parseInt(part);
						streams.add(streamPrefix + idx);
					}
				} catch (NumberFormatException nfe) {
					throw new ParseException("Invalid stream suffix list : " + streamExpression);
				}
			} else {
				streams.add(streamPrefix + streamExpression);
			}
		}

		@Override
		protected int runCmd() throws Exception {
			generateStreams(streamPrefix, streamExpression);
			if (streams.isEmpty()) {
				System.out.println("Nothing to create.");
				return 0;
			}
			if (!getForce() && !IOUtils.confirmPrompt("You are going to create streams : " + streams)) {
				return 0;
			}
			getConf().setZkAclId(getZkAclId());
			for (String stream : streams) {
				getNamespace().createLog(stream);
			}
			return 0;
		}

		@Override
		protected String getUsage() {
			return "create [options]";
		}

		protected void setPrefix(String prefix) {
			this.streamPrefix = prefix;
		}

		protected void setExpression(String expression) {
			this.streamExpression = expression;
		}
	}

	/**
	 * Command used to dump records of a given stream.
	 */
	protected static class DumpCommand extends PerStreamCommand {

		boolean printHex = false;
		boolean skipPayload = false;
		Long fromTxnId = null;
		DLSN fromDLSN = null;
		int count = 100;

		DumpCommand() {
			super("dump", "dump records of a given stream");
			options.addOption("x", "hex", false, "Print record in hex format");
			options.addOption("sp", "skip-payload", false, "Skip printing the payload of the record");
			options.addOption("o", "offset", true, "Txn ID to start dumping.");
			options.addOption("n", "seqno", true, "Sequence Number to start dumping");
			options.addOption("e", "eid", true, "Entry ID to start dumping");
			options.addOption("t", "slot", true, "Slot to start dumping");
			options.addOption("l", "limit", true, "Number of entries to dump. Default is 100.");
		}

		@Override
		protected void parseCommandLine(CommandLine cmdline) throws ParseException {
			super.parseCommandLine(cmdline);
			printHex = cmdline.hasOption("x");
			skipPayload = cmdline.hasOption("sp");
			if (cmdline.hasOption("o")) {
				try {
					fromTxnId = Long.parseLong(cmdline.getOptionValue("o"));
				} catch (NumberFormatException nfe) {
					throw new ParseException("Invalid txn id " + cmdline.getOptionValue("o"));
				}
			}
			if (cmdline.hasOption("l")) {
				try {
					count = Integer.parseInt(cmdline.getOptionValue("l"));
				} catch (NumberFormatException nfe) {
					throw new ParseException("Invalid count " + cmdline.getOptionValue("l"));
				}
				if (count <= 0) {
					throw new ParseException("Negative count found : " + count);
				}
			}
			if (cmdline.hasOption("n")) {
				long seqno;
				try {
					seqno = Long.parseLong(cmdline.getOptionValue("n"));
				} catch (NumberFormatException nfe) {
					throw new ParseException("Invalid sequence number " + cmdline.getOptionValue("n"));
				}
				long eid;
				if (cmdline.hasOption("e")) {
					eid = Long.parseLong(cmdline.getOptionValue("e"));
				} else {
					eid = 0;
				}
				long slot;
				if (cmdline.hasOption("t")) {
					slot = Long.parseLong(cmdline.getOptionValue("t"));
				} else {
					slot = 0;
				}
				fromDLSN = new DLSN(seqno, eid, slot);
			}
			if (null == fromTxnId && null == fromDLSN) {
				throw new ParseException("No start Txn/DLSN is specified.");
			}
		}

		@Override
		protected int runCmd() throws Exception {
			DistributedLogManager dlm = getNamespace().openLog(getStreamName());
			long totalCount = dlm.getLogRecordCount();
			try {
				AsyncLogReader reader;
				Object startOffset;
				try {
					DLSN lastDLSN = FutureUtils.result(dlm.getLastDLSNAsync());
					System.out.println("Last DLSN : " + lastDLSN);
					if (null == fromDLSN) {
						reader = dlm.getAsyncLogReader(fromTxnId);
						startOffset = fromTxnId;
					} else {
						reader = dlm.getAsyncLogReader(fromDLSN);
						startOffset = fromDLSN;
					}
				} catch (LogNotFoundException lee) {
					System.out.println("No stream found to dump records.");
					return 0;
				}
				try {
					System.out.println(
							String.format("Dump records for %s (from = %s, dump" + " count = %d, total records = %d)",
									getStreamName(), startOffset, count, totalCount));

					dumpRecords(reader);
				} finally {
					Utils.close(reader);
				}
			} finally {
				dlm.close();
			}
			return 0;
		}

		private void dumpRecords(AsyncLogReader reader) throws Exception {
			int numRead = 0;
			LogRecord record = FutureUtils.result(reader.readNext());
			while (record != null) {
				// dump the record
				dumpRecord(record);
				++numRead;
				if (numRead >= count) {
					break;
				}
				record = FutureUtils.result(reader.readNext());
			}
			if (numRead == 0) {
				System.out.println("No records.");
			} else {
				System.out.println("------------------------------------------------");
			}
		}

		private void dumpRecord(LogRecord record) {
			System.out.println("------------------------------------------------");
			if (record instanceof LogRecordWithDLSN) {
				System.out.println("Record (txn = " + record.getTransactionId() + ", bytes = "
						+ record.getPayload().length + ", dlsn = " + ((LogRecordWithDLSN) record).getDlsn()
						+ ", sequence id = " + ((LogRecordWithDLSN) record).getSequenceId() + ")");
			} else {
				System.out.println(
						"Record (txn = " + record.getTransactionId() + ", bytes = " + record.getPayload().length + ")");
			}
			System.out.println();

			if (skipPayload) {
				return;
			}

			if (printHex) {
				System.out.println(Hex.encodeHexString(record.getPayload()));
			} else {
				System.out.println(new String(record.getPayload(), UTF_8));
			}
		}

		@Override
		protected String getUsage() {
			return "dump [options]";
		}

		protected void setFromTxnId(Long fromTxnId) {
			this.fromTxnId = fromTxnId;
		}
	}

	/**
	 * TODO: refactor inspect & inspectstream.
	 * TODO: support force
	 * inspectstream -lac -gap (different options for different operations for a single stream)
	 * inspect -lac -gap (inspect the namespace, which will use inspect stream)
	 */
	static class InspectStreamCommand extends PerStreamCommand {

		InspectStreamCommand() {
			super("inspectstream", "Inspect a given stream to identify any metadata corruptions");
		}

		@Override
		protected int runCmd() throws Exception {
			DistributedLogManager dlm = getNamespace().openLog(getStreamName());
			try {
				return inspectAndRepair(dlm.getLogSegments());
			} finally {
				dlm.close();
			}
		}

		protected int inspectAndRepair(List<LogSegmentMetadata> segments) throws Exception {
			LogSegmentMetadataStore metadataStore = getLogSegmentMetadataStore();
			ZooKeeperClient zkc = getZooKeeperClient();
			BKDLConfig bkdlConfig = BKDLConfig.resolveDLConfig(zkc, getUri());
			BKDLConfig.propagateConfiguration(bkdlConfig, getConf());
			BookKeeperClient bkc = BookKeeperClientBuilder.newBuilder().dlConfig(getConf())
					.zkServers(bkdlConfig.getBkZkServersForReader()).ledgersPath(bkdlConfig.getBkLedgersPath())
					.name("dlog").build();
			try {
				List<LogSegmentMetadata> segmentsToRepair = inspectLogSegments(bkc, segments);
				if (segmentsToRepair.isEmpty()) {
					System.out.println("The stream is good. No log segments to repair.");
					return 0;
				}
				System.out.println(segmentsToRepair.size() + " segments to repair : ");
				System.out.println(segmentsToRepair);
				System.out.println();
				if (!IOUtils.confirmPrompt("Do you want to repair them (Y/N): ")) {
					return 0;
				}
				repairLogSegments(metadataStore, bkc, segmentsToRepair);
				return 0;
			} finally {
				bkc.close();
			}
		}

		protected List<LogSegmentMetadata> inspectLogSegments(BookKeeperClient bkc, List<LogSegmentMetadata> segments)
				throws Exception {
			List<LogSegmentMetadata> segmentsToRepair = new ArrayList<LogSegmentMetadata>();
			for (LogSegmentMetadata segment : segments) {
				if (!segment.isInProgress() && !inspectLogSegment(bkc, segment)) {
					segmentsToRepair.add(segment);
				}
			}
			return segmentsToRepair;
		}

		/**
		 * Inspect a given log segment.
		 *
		 * @param bkc
		 *          bookkeeper client
		 * @param metadata
		 *          metadata of the log segment to
		 * @return true if it is a good stream, false if the stream has inconsistent metadata.
		 * @throws Exception
		 */
		protected boolean inspectLogSegment(BookKeeperClient bkc, LogSegmentMetadata metadata) throws Exception {
			if (metadata.isInProgress()) {
				System.out.println("Skip inprogress log segment " + metadata);
				return true;
			}
			long ledgerId = metadata.getLogSegmentId();
			LedgerHandle lh = bkc.get().openLedger(ledgerId, BookKeeper.DigestType.CRC32,
					getConf().getBKDigestPW().getBytes(UTF_8));
			LedgerHandle readLh = bkc.get().openLedger(ledgerId, BookKeeper.DigestType.CRC32,
					getConf().getBKDigestPW().getBytes(UTF_8));
			LedgerReader lr = new LedgerReader(bkc.get());
			final AtomicReference<List<LedgerEntry>> entriesHolder = new AtomicReference<List<LedgerEntry>>(null);
			final AtomicInteger rcHolder = new AtomicInteger(-1234);
			final CountDownLatch doneLatch = new CountDownLatch(1);
			try {
				lr.forwardReadEntriesFromLastConfirmed(readLh,
						new BookkeeperInternalCallbacks.GenericCallback<List<LedgerEntry>>() {
							@Override
							public void operationComplete(int rc, List<LedgerEntry> entries) {
								rcHolder.set(rc);
								entriesHolder.set(entries);
								doneLatch.countDown();
							}
						});
				doneLatch.await();
				if (BKException.Code.OK != rcHolder.get()) {
					throw BKException.create(rcHolder.get());
				}
				List<LedgerEntry> entries = entriesHolder.get();
				long lastEntryId;
				if (entries.isEmpty()) {
					lastEntryId = LedgerHandle.INVALID_ENTRY_ID;
				} else {
					LedgerEntry lastEntry = entries.get(entries.size() - 1);
					lastEntryId = lastEntry.getEntryId();
				}
				if (lastEntryId != lh.getLastAddConfirmed()) {
					System.out.println("Inconsistent Last Add Confirmed Found for LogSegment "
							+ metadata.getLogSegmentSequenceNumber() + ": ");
					System.out.println("\t metadata: " + metadata);
					System.out.println("\t lac in ledger metadata is " + lh.getLastAddConfirmed()
							+ ", but lac in bookies is " + lastEntryId);
					return false;
				} else {
					return true;
				}
			} finally {
				lh.close();
				readLh.close();
			}
		}

		protected void repairLogSegments(LogSegmentMetadataStore metadataStore, BookKeeperClient bkc,
				List<LogSegmentMetadata> segments) throws Exception {
			BookKeeperAdmin bkAdmin = new BookKeeperAdmin(bkc.get());
			try {
				MetadataUpdater metadataUpdater = LogSegmentMetadataStoreUpdater.createMetadataUpdater(getConf(),
						metadataStore);
				for (LogSegmentMetadata segment : segments) {
					repairLogSegment(bkAdmin, metadataUpdater, segment);
				}
			} finally {
				bkAdmin.close();
			}
		}

		protected void repairLogSegment(BookKeeperAdmin bkAdmin, MetadataUpdater metadataUpdater,
				LogSegmentMetadata segment) throws Exception {
			if (segment.isInProgress()) {
				System.out.println("Skip inprogress log segment " + segment);
				return;
			}
			LedgerHandle lh = bkAdmin.openLedger(segment.getLogSegmentId());
			long lac = lh.getLastAddConfirmed();
			Enumeration<LedgerEntry> entries = lh.readEntries(lac, lac);
			if (!entries.hasMoreElements()) {
				throw new IOException("Entry " + lac + " isn't found for " + segment);
			}
			LedgerEntry lastEntry = entries.nextElement();
			Entry.Reader reader = Entry.newBuilder()
					.setLogSegmentInfo(segment.getLogSegmentSequenceNumber(), segment.getStartSequenceId())
					.setEntryId(lastEntry.getEntryId())
					.setEnvelopeEntry(LogSegmentMetadata.supportsEnvelopedEntries(segment.getVersion()))
					.setEntry(lastEntry.getEntryBuffer()).buildReader();
			lastEntry.getEntryBuffer().release();
			LogRecordWithDLSN record = reader.nextRecord();
			LogRecordWithDLSN lastRecord = null;
			while (null != record) {
				lastRecord = record;
				record = reader.nextRecord();
			}
			if (null == lastRecord) {
				throw new IOException("No record found in entry " + lac + " for " + segment);
			}
			System.out.println("Updating last record for " + segment + " to " + lastRecord);
			if (!IOUtils.confirmPrompt("Do you want to make this change (Y/N): ")) {
				return;
			}
			metadataUpdater.updateLastRecord(segment, lastRecord);
		}

		@Override
		protected String getUsage() {
			return "inspectstream [options]";
		}
	}

	interface BKCommandRunner {
		int run(ZooKeeperClient zkc, BookKeeperClient bkc) throws Exception;
	}

	abstract static class PerBKCommand extends PerDLCommand {

		protected PerBKCommand(String name, String description) {
			super(name, description);
		}

		@Override
		protected int runCmd() throws Exception {
			return runBKCommand(new BKCommandRunner() {
				@Override
				public int run(ZooKeeperClient zkc, BookKeeperClient bkc) throws Exception {
					return runBKCmd(zkc, bkc);
				}
			});
		}

		protected int runBKCommand(BKCommandRunner runner) throws Exception {
			return runner.run(getZooKeeperClient(), getBookKeeperClient());
		}

		protected abstract int runBKCmd(ZooKeeperClient zkc, BookKeeperClient bkc) throws Exception;
	}

	/**
	 * Per Ledger Command, which parse common options for per ledger. e.g. ledger id.
	 */
	abstract static class PerLedgerCommand extends PerDLCommand {

		protected long ledgerId;

		protected PerLedgerCommand(String name, String description) {
			super(name, description);
			options.addOption("l", "ledger", true, "Ledger ID");
		}

		@Override
		protected void parseCommandLine(CommandLine cmdline) throws ParseException {
			super.parseCommandLine(cmdline);
			if (!cmdline.hasOption("l")) {
				throw new ParseException("No ledger provided.");
			}
			ledgerId = Long.parseLong(cmdline.getOptionValue("l"));
		}

		protected long getLedgerID() {
			return ledgerId;
		}

		protected void setLedgerId(long ledgerId) {
			this.ledgerId = ledgerId;
		}
	}

	/**
	 * Command used to force recover ledger.
	 */
	protected static class RecoverLedgerCommand extends PerLedgerCommand {

		RecoverLedgerCommand() {
			super("recoverledger", "force recover ledger");
		}

		@Override
		protected int runCmd() throws Exception {
			LedgerHandle lh = getBookKeeperClient().get().openLedgerNoRecovery(getLedgerID(),
					BookKeeper.DigestType.CRC32, dlConf.getBKDigestPW().getBytes(UTF_8));
			final CountDownLatch doneLatch = new CountDownLatch(1);
			final AtomicInteger resultHolder = new AtomicInteger(-1234);
			BookkeeperInternalCallbacks.GenericCallback<Void> recoverCb = new BookkeeperInternalCallbacks.GenericCallback<Void>() {
				@Override
				public void operationComplete(int rc, Void result) {
					resultHolder.set(rc);
					doneLatch.countDown();
				}
			};
			try {
				BookKeeperAccessor.forceRecoverLedger(lh, recoverCb);
				doneLatch.await();
				if (BKException.Code.OK != resultHolder.get()) {
					throw BKException.create(resultHolder.get());
				}
			} finally {
				lh.close();
			}
			return 0;
		}

		@Override
		protected String getUsage() {
			return "recoverledger [options]";
		}
	}

	/**
	 * Command used to find the stream for a given ledger.
	 */
	protected static class FindLedgerCommand extends PerLedgerCommand {

		FindLedgerCommand() {
			super("findledger", "find the stream for a given ledger");
		}

		@Override
		protected int runCmd() throws Exception {
			Iterator<String> logs = getNamespace().getLogs();
			while (logs.hasNext()) {
				String logName = logs.next();
				if (processLog(logName)) {
					System.out.println("Found ledger " + getLedgerID() + " at log stream '" + logName + "'");
				}
			}
			return 0;
		}

		boolean processLog(String logName) throws Exception {
			DistributedLogManager dlm = getNamespace().openLog(logName);
			try {
				List<LogSegmentMetadata> segments = dlm.getLogSegments();
				for (LogSegmentMetadata segment : segments) {
					if (getLedgerID() == segment.getLogSegmentId()) {
						System.out.println("Found ledger " + getLedgerID() + " at log segment " + segment
								+ " for stream '" + logName + "'");
						return true;
					}
				}
				return false;
			} finally {
				dlm.close();
			}
		}
	}

	/**
	 * Command used to read last add confirmed for a given ledger.
	 */
	protected static class ReadLastConfirmedCommand extends PerLedgerCommand {

		ReadLastConfirmedCommand() {
			super("readlac", "read last add confirmed for a given ledger");
		}

		@Override
		protected int runCmd() throws Exception {
			LedgerHandle lh = getBookKeeperClient().get().openLedgerNoRecovery(getLedgerID(),
					BookKeeper.DigestType.CRC32, dlConf.getBKDigestPW().getBytes(UTF_8));
			try {
				long lac = lh.readLastConfirmed();
				System.out.println("LastAddConfirmed: " + lac);
			} finally {
				lh.close();
			}
			return 0;
		}

		@Override
		protected String getUsage() {
			return "readlac [options]";
		}
	}

	/**
	 * Command used to read entries for a given ledger.
	 */
	protected static class ReadEntriesCommand extends PerLedgerCommand {

		Long fromEntryId;
		Long untilEntryId;
		boolean printHex = false;
		boolean skipPayload = false;
		boolean readAllBookies = false;
		boolean readLac = false;
		boolean corruptOnly = false;

		int metadataVersion = LogSegmentMetadata.LEDGER_METADATA_CURRENT_LAYOUT_VERSION;

		ReadEntriesCommand() {
			super("readentries", "read entries for a given ledger");
			options.addOption("x", "hex", false, "Print record in hex format");
			options.addOption("sp", "skip-payload", false, "Skip printing the payload of the record");
			options.addOption("fid", "from", true, "Entry id to start reading");
			options.addOption("uid", "until", true, "Entry id to read until");
			options.addOption("bks", "all-bookies", false, "Read entry from all bookies");
			options.addOption("lac", "last-add-confirmed", false,
					"Return last add confirmed rather than entry payload");
			options.addOption("ver", "metadata-version", true, "The log segment metadata version to use");
			options.addOption("bad", "corrupt-only", false, "Display info for corrupt entries only");
		}

		@Override
		protected void parseCommandLine(CommandLine cmdline) throws ParseException {
			super.parseCommandLine(cmdline);
			printHex = cmdline.hasOption("x");
			skipPayload = cmdline.hasOption("sp");
			if (cmdline.hasOption("fid")) {
				fromEntryId = Long.parseLong(cmdline.getOptionValue("fid"));
			}
			if (cmdline.hasOption("uid")) {
				untilEntryId = Long.parseLong(cmdline.getOptionValue("uid"));
			}
			if (cmdline.hasOption("ver")) {
				metadataVersion = Integer.parseInt(cmdline.getOptionValue("ver"));
			}
			corruptOnly = cmdline.hasOption("bad");
			readAllBookies = cmdline.hasOption("bks");
			readLac = cmdline.hasOption("lac");
		}

		@Override
		protected int runCmd() throws Exception {
			LedgerHandle lh = getBookKeeperClient().get().openLedgerNoRecovery(getLedgerID(),
					BookKeeper.DigestType.CRC32, dlConf.getBKDigestPW().getBytes(UTF_8));
			try {
				if (null == fromEntryId) {
					fromEntryId = 0L;
				}
				if (null == untilEntryId) {
					untilEntryId = lh.readLastConfirmed();
				}
				if (untilEntryId >= fromEntryId) {
					if (readAllBookies) {
						LedgerReader lr = new LedgerReader(getBookKeeperClient().get());
						if (readLac) {
							readLacsFromAllBookies(lr, lh, fromEntryId, untilEntryId);
						} else {
							readEntriesFromAllBookies(lr, lh, fromEntryId, untilEntryId);
						}
					} else {
						simpleReadEntries(lh, fromEntryId, untilEntryId);
					}
				} else {
					System.out.println("No entries.");
				}
			} finally {
				lh.close();
			}
			return 0;
		}

		private void readEntriesFromAllBookies(LedgerReader ledgerReader, LedgerHandle lh, long fromEntryId,
				long untilEntryId) throws Exception {
			for (long eid = fromEntryId; eid <= untilEntryId; ++eid) {
				final CountDownLatch doneLatch = new CountDownLatch(1);
				final AtomicReference<Set<LedgerReader.ReadResult<ByteBuf>>> resultHolder = new AtomicReference<>();
				ledgerReader.readEntriesFromAllBookies(lh, eid, (rc, readResults) -> {
					if (BKException.Code.OK == rc) {
						resultHolder.set(readResults);
					} else {
						resultHolder.set(null);
					}
					doneLatch.countDown();
				});
				doneLatch.await();
				Set<LedgerReader.ReadResult<ByteBuf>> readResults = resultHolder.get();
				if (null == readResults) {
					throw new IOException("Failed to read entry " + eid);
				}
				boolean printHeader = true;
				for (LedgerReader.ReadResult<ByteBuf> rr : readResults) {
					if (corruptOnly) {
						if (BKException.Code.DigestMatchException == rr.getResultCode()) {
							if (printHeader) {
								System.out.println("\t" + eid + "\t:");
								printHeader = false;
							}
							System.out.println("\tbookie=" + rr.getBookieAddress());
							System.out.println("\t-------------------------------");
							System.out.println("status = " + BKException.getMessage(rr.getResultCode()));
							System.out.println("\t-------------------------------");
						}
					} else {
						if (printHeader) {
							System.out.println("\t" + eid + "\t:");
							printHeader = false;
						}
						System.out.println("\tbookie=" + rr.getBookieAddress());
						System.out.println("\t-------------------------------");
						if (BKException.Code.OK == rr.getResultCode()) {
							Entry.Reader reader = Entry.newBuilder().setLogSegmentInfo(lh.getId(), 0L).setEntryId(eid)
									.setEntry(rr.getValue())
									.setEnvelopeEntry(LogSegmentMetadata.supportsEnvelopedEntries(metadataVersion))
									.buildReader();
							rr.getValue().release();
							printEntry(reader);
						} else {
							System.out.println("status = " + BKException.getMessage(rr.getResultCode()));
						}
						System.out.println("\t-------------------------------");
					}
				}
			}
		}

		private void readLacsFromAllBookies(LedgerReader ledgerReader, LedgerHandle lh, long fromEntryId,
				long untilEntryId) throws Exception {
			for (long eid = fromEntryId; eid <= untilEntryId; ++eid) {
				final CountDownLatch doneLatch = new CountDownLatch(1);
				final AtomicReference<Set<LedgerReader.ReadResult<Long>>> resultHolder = new AtomicReference<Set<LedgerReader.ReadResult<Long>>>();
				ledgerReader.readLacs(lh, eid,
						new BookkeeperInternalCallbacks.GenericCallback<Set<LedgerReader.ReadResult<Long>>>() {
							@Override
							public void operationComplete(int rc, Set<LedgerReader.ReadResult<Long>> readResults) {
								if (BKException.Code.OK == rc) {
									resultHolder.set(readResults);
								} else {
									resultHolder.set(null);
								}
								doneLatch.countDown();
							}
						});
				doneLatch.await();
				Set<LedgerReader.ReadResult<Long>> readResults = resultHolder.get();
				if (null == readResults) {
					throw new IOException("Failed to read entry " + eid);
				}
				System.out.println("\t" + eid + "\t:");
				for (LedgerReader.ReadResult<Long> rr : readResults) {
					System.out.println("\tbookie=" + rr.getBookieAddress());
					System.out.println("\t-------------------------------");
					if (BKException.Code.OK == rr.getResultCode()) {
						System.out.println("Eid = " + rr.getEntryId() + ", Lac = " + rr.getValue());
					} else {
						System.out.println("status = " + BKException.getMessage(rr.getResultCode()));
					}
					System.out.println("\t-------------------------------");
				}
			}
		}

		private void simpleReadEntries(LedgerHandle lh, long fromEntryId, long untilEntryId) throws Exception {
			Enumeration<LedgerEntry> entries = lh.readEntries(fromEntryId, untilEntryId);
			long i = fromEntryId;
			System.out.println("Entries:");
			while (entries.hasMoreElements()) {
				LedgerEntry entry = entries.nextElement();
				System.out.println("\t" + i + "(eid=" + entry.getEntryId() + ")\t: ");
				Entry.Reader reader = Entry.newBuilder().setLogSegmentInfo(0L, 0L).setEntryId(entry.getEntryId())
						.setEntry(entry.getEntryBuffer())
						.setEnvelopeEntry(LogSegmentMetadata.supportsEnvelopedEntries(metadataVersion)).buildReader();
				entry.getEntryBuffer().release();
				printEntry(reader);
				++i;
			}
		}

		private void printEntry(Entry.Reader reader) throws Exception {
			LogRecordWithDLSN record = reader.nextRecord();
			while (null != record) {
				System.out.println("\t" + record);
				if (!skipPayload) {
					if (printHex) {
						System.out.println(Hex.encodeHexString(record.getPayload()));
					} else {
						System.out.println(new String(record.getPayload(), UTF_8));
					}
				}
				System.out.println();
				record = reader.nextRecord();
			}
		}

		@Override
		protected String getUsage() {
			return "readentries [options]";
		}
	}

	/**
	 * Command associated with audit.
	 */
	protected abstract static class AuditCommand extends OptsCommand {

		protected final Options options = new Options();
		protected final DistributedLogConfiguration dlConf;
		protected final List<URI> uris = new ArrayList<URI>();
		protected String zkAclId = null;
		protected boolean force = false;

		protected AuditCommand(String name, String description) {
			super(name, description);
			dlConf = new DistributedLogConfiguration();
			options.addOption("u", "uris", true, "List of distributedlog uris, separated by comma");
			options.addOption("c", "conf", true, "DistributedLog Configuration File");
			options.addOption("a", "zk-acl-id", true, "ZooKeeper ACL ID");
			options.addOption("f", "force", false, "Force command (no warnings or prompts)");
		}

		@Override
		protected int runCmd(CommandLine commandLine) throws Exception {
			try {
				parseCommandLine(commandLine);
			} catch (ParseException pe) {
				System.err.println("ERROR: failed to parse commandline : '" + pe.getMessage() + "'");
				printUsage();
				return -1;
			}
			return runCmd();
		}

		protected abstract int runCmd() throws Exception;

		@Override
		protected Options getOptions() {
			return options;
		}

		protected void parseCommandLine(CommandLine cmdline) throws ParseException {
			if (!cmdline.hasOption("u")) {
				throw new ParseException("No distributedlog uri provided.");
			}
			String urisStr = cmdline.getOptionValue("u");
			for (String uriStr : urisStr.split(",")) {
				uris.add(URI.create(uriStr));
			}
			if (cmdline.hasOption("c")) {
				String configFile = cmdline.getOptionValue("c");
				try {
					dlConf.loadConf(new File(configFile).toURI().toURL());
				} catch (ConfigurationException e) {
					throw new ParseException("Failed to load distributedlog configuration from " + configFile + ".");
				} catch (MalformedURLException e) {
					throw new ParseException(
							"Failed to load distributedlog configuration from malformed " + configFile + ".");
				}
			}
			if (cmdline.hasOption("a")) {
				zkAclId = cmdline.getOptionValue("a");
			}
			if (cmdline.hasOption("f")) {
				force = true;
			}
		}

		protected DistributedLogConfiguration getConf() {
			return dlConf;
		}

		protected List<URI> getUris() {
			return uris;
		}

		protected String getZkAclId() {
			return zkAclId;
		}

		protected boolean getForce() {
			return force;
		}

	}

	static class AuditLedgersCommand extends AuditCommand {

		String ledgersFilePrefix;
		final List<List<String>> allocationPaths = new ArrayList<List<String>>();

		AuditLedgersCommand() {
			super("audit_ledgers", "Audit ledgers between bookkeeper and DL uris");
			options.addOption("lf", "ledgers-file", true, "Prefix of filename to store ledgers");
			options.addOption("ap", "allocation-paths", true, "Allocation paths per uri. E.g ap10;ap11,ap20");
		}

		@Override
		protected void parseCommandLine(CommandLine cmdline) throws ParseException {
			super.parseCommandLine(cmdline);
			if (cmdline.hasOption("lf")) {
				ledgersFilePrefix = cmdline.getOptionValue("lf");
			} else {
				throw new ParseException("No file specified to store leak ledgers");
			}
			if (cmdline.hasOption("ap")) {
				String[] aps = cmdline.getOptionValue("ap").split(",");
				for (String ap : aps) {
					List<String> list = new ArrayList<String>();
					String[] array = ap.split(";");
					Collections.addAll(list, array);
					allocationPaths.add(list);
				}
			} else {
				throw new ParseException("No allocation paths provided.");
			}
		}

		void dumpLedgers(Set<Long> ledgers, File targetFile) throws Exception {
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(targetFile), UTF_8.name()));
			try {
				for (Long ledger : ledgers) {
					pw.println(ledger);
				}
			} finally {
				pw.close();
			}
			System.out.println("Dump " + ledgers.size() + " ledgers to file : " + targetFile);
		}

		@Override
		protected int runCmd() throws Exception {
			if (!getForce() && !IOUtils.confirmPrompt(
					"Do you want to audit uris : " + getUris() + ", allocation paths = " + allocationPaths)) {
				return 0;
			}

			DLAuditor dlAuditor = new DLAuditor(getConf());
			try {
				Pair<Set<Long>, Set<Long>> bkdlLedgers = dlAuditor.collectLedgers(getUris(), allocationPaths);
				dumpLedgers(bkdlLedgers.getLeft(), new File(ledgersFilePrefix + "-bkledgers.txt"));
				dumpLedgers(bkdlLedgers.getRight(), new File(ledgersFilePrefix + "-dlledgers.txt"));
				dumpLedgers(Sets.difference(bkdlLedgers.getLeft(), bkdlLedgers.getRight()),
						new File(ledgersFilePrefix + "-leakledgers.txt"));
			} finally {
				dlAuditor.close();
			}
			return 0;
		}

		@Override
		protected String getUsage() {
			return "audit_ledgers [options]";
		}
	}

	/**
	 * Audit stream space usage for a given dl uri.
	 */
	public static class AuditDLSpaceCommand extends PerDLCommand {

		private String regex = null;

		AuditDLSpaceCommand() {
			super("audit_dl_space", "Audit stream space usage for a given dl uri");
			options.addOption("groupByRegex", true, "Group by the result of applying the regex to stream name");
		}

		@Override
		protected void parseCommandLine(CommandLine cmdline) throws ParseException {
			super.parseCommandLine(cmdline);
			if (cmdline.hasOption("groupByRegex")) {
				regex = cmdline.getOptionValue("groupByRegex");
			}
		}

		@Override
		protected int runCmd() throws Exception {
			DLAuditor dlAuditor = new DLAuditor(getConf());
			try {
				Map<String, Long> streamSpaceMap = dlAuditor.calculateStreamSpaceUsage(getUri());
				if (null != regex) {
					printGroupByRegexSpaceUsage(streamSpaceMap, regex);
				} else {
					printSpaceUsage(streamSpaceMap);
				}
			} finally {
				dlAuditor.close();
			}
			return 0;
		}

		@Override
		protected String getUsage() {
			return "audit_dl_space [options]";
		}

		private void printSpaceUsage(Map<String, Long> spaceMap) throws Exception {
			for (Map.Entry<String, Long> entry : spaceMap.entrySet()) {
				System.out.println(entry.getKey() + "\t" + entry.getValue());
			}
		}

		private void printGroupByRegexSpaceUsage(Map<String, Long> streamSpaceMap, String regex) throws Exception {
			Pattern pattern = Pattern.compile(regex);
			Map<String, Long> groupedUsageMap = new HashMap<String, Long>();
			for (Map.Entry<String, Long> entry : streamSpaceMap.entrySet()) {
				Matcher matcher = pattern.matcher(entry.getKey());
				String key = entry.getKey();
				boolean matches = matcher.matches();
				if (matches) {
					key = matcher.group(1);
				}
				Long value = entry.getValue();
				if (groupedUsageMap.containsKey(key)) {
					value += groupedUsageMap.get(key);
				}
				groupedUsageMap.put(key, value);
			}
			printSpaceUsage(groupedUsageMap);
		}
	}

	/**
	 * Audit bk space usage for a given dl uri.
	 */
	public static class AuditBKSpaceCommand extends PerDLCommand {

		AuditBKSpaceCommand() {
			super("audit_bk_space", "Audit bk space usage for a given dl uri");
		}

		@Override
		protected int runCmd() throws Exception {
			DLAuditor dlAuditor = new DLAuditor(getConf());
			try {
				long spaceUsage = dlAuditor.calculateLedgerSpaceUsage(uri);
				System.out.println("bookkeeper ledgers space usage \t " + spaceUsage);
			} finally {
				dlAuditor.close();
			}
			return 0;
		}

		@Override
		protected String getUsage() {
			return "audit_bk_space [options]";
		}
	}

	/**
	 * Command used to truncate a stream at a specific position.
	 */
	protected static class TruncateStreamCommand extends PerStreamCommand {

		DLSN dlsn = DLSN.InvalidDLSN;

		TruncateStreamCommand() {
			super("truncate_stream", "truncate a stream at a specific position");
			options.addOption("dlsn", true, "Truncate all records older than this dlsn");
		}

		public void setDlsn(DLSN dlsn) {
			this.dlsn = dlsn;
		}

		@Override
		protected void parseCommandLine(CommandLine cmdline) throws ParseException {
			super.parseCommandLine(cmdline);
			if (cmdline.hasOption("dlsn")) {
				dlsn = parseDLSN(cmdline.getOptionValue("dlsn"));
			}
		}

		@Override
		protected int runCmd() throws Exception {
			getConf().setZkAclId(getZkAclId());
			return truncateStream(getNamespace(), getStreamName(), dlsn);
		}

		private int truncateStream(final Namespace namespace, String streamName, DLSN dlsn) throws Exception {
			DistributedLogManager dlm = namespace.openLog(streamName);
			try {
				long totalRecords = dlm.getLogRecordCount();
				long recordsAfterTruncate = FutureUtils.result(dlm.getLogRecordCountAsync(dlsn));
				long recordsToTruncate = totalRecords - recordsAfterTruncate;
				if (!getForce() && !IOUtils.confirmPrompt("Do you want to truncate " + streamName + " at dlsn " + dlsn
						+ " (" + recordsToTruncate + " records)?")) {
					return 0;
				} else {
					AsyncLogWriter writer = dlm.startAsyncLogSegmentNonPartitioned();
					try {
						if (!FutureUtils.result(writer.truncate(dlsn))) {
							System.out.println("Failed to truncate.");
						}
						return 0;
					} finally {
						Utils.close(writer);
					}
				}
			} catch (Exception ex) {
				System.err.println("Failed to truncate " + ex);
				return 1;
			} finally {
				dlm.close();
			}
		}
	}

	/**
	 * Command used to Deserialize DLSN.
	 */
	public static class DeserializeDLSNCommand extends SimpleCommand {

		String base64Dlsn = "";

		DeserializeDLSNCommand() {
			super("deserialize_dlsn", "Deserialize DLSN");
			options.addOption("b64", "base64", true, "Base64 encoded dlsn");
		}

		protected void parseCommandLine(CommandLine cmdline) throws ParseException {
			if (cmdline.hasOption("b64")) {
				base64Dlsn = cmdline.getOptionValue("b64");
			} else {
				throw new IllegalArgumentException("Argument b64 is required");
			}
		}

		@Override
		protected int runSimpleCmd() throws Exception {
			System.out.println(DLSN.deserialize(base64Dlsn));
			return 0;
		}
	}

	/**
	 * Command used to Serialize DLSN.
	 */
	public static class SerializeDLSNCommand extends SimpleCommand {

		private DLSN dlsn = DLSN.InitialDLSN;
		private boolean hex = false;

		SerializeDLSNCommand() {
			super("serialize_dlsn", "Serialize DLSN. Default format is base64 string.");
			options.addOption("dlsn", true, "DLSN in comma separated format to serialize");
			options.addOption("x", "hex", false, "Emit hex-encoded string DLSN instead of base 64");
		}

		protected void parseCommandLine(CommandLine cmdline) throws ParseException {
			if (cmdline.hasOption("dlsn")) {
				dlsn = parseDLSN(cmdline.getOptionValue("dlsn"));
			}
			hex = cmdline.hasOption("x");
		}

		@Override
		protected int runSimpleCmd() throws Exception {
			if (hex) {
				byte[] bytes = dlsn.serializeBytes();
				String hexString = Hex.encodeHexString(bytes);
				System.out.println(hexString);
			} else {
				System.out.println(dlsn.serialize());
			}
			return 0;
		}
	}

	/**
	 * Command used to Delete the subscriber in subscription store.
	 */
	public static class DeleteSubscriberCommand extends PerDLCommand {

		int numThreads = 1;
		String streamPrefix = null;
		String subscriberId = null;

		DeleteSubscriberCommand() {
			super("delete_subscriber", "Delete the subscriber in subscription store. ");
			options.addOption("s", "subscriberId", true, "SubscriberId to remove from the stream");
			options.addOption("t", "threads", true, "Number of threads");
			options.addOption("ft", "filter", true, "Stream filter by prefix");
		}

		@Override
		protected void parseCommandLine(CommandLine cmdline) throws ParseException {
			super.parseCommandLine(cmdline);
			if (!cmdline.hasOption("s")) {
				throw new ParseException("No subscriberId provided.");
			} else {
				subscriberId = cmdline.getOptionValue("s");
			}
			if (cmdline.hasOption("t")) {
				numThreads = Integer.parseInt(cmdline.getOptionValue("t"));
			}
			if (cmdline.hasOption("ft")) {
				streamPrefix = cmdline.getOptionValue("ft");
			}
		}

		@Override
		protected String getUsage() {
			return "delete_subscriber [options]";
		}

		@Override
		protected int runCmd() throws Exception {
			getConf().setZkAclId(getZkAclId());
			return deleteSubscriber(getNamespace());
		}

		private int deleteSubscriber(final Namespace namespace) throws Exception {
			Iterator<String> streamCollection = namespace.getLogs();
			final List<String> streams = new ArrayList<String>();
			while (streamCollection.hasNext()) {
				String s = streamCollection.next();
				if (null != streamPrefix) {
					if (s.startsWith(streamPrefix)) {
						streams.add(s);
					}
				} else {
					streams.add(s);
				}
			}
			if (0 == streams.size()) {
				return 0;
			}
			System.out.println("Streams : " + streams);
			if (!getForce() && !IOUtils.confirmPrompt(
					"Do you want to delete subscriber " + subscriberId + " for " + streams.size() + " streams ?")) {
				return 0;
			}
			numThreads = Math.min(streams.size(), numThreads);
			final int numStreamsPerThreads = streams.size() / numThreads + 1;
			Thread[] threads = new Thread[numThreads];
			for (int i = 0; i < numThreads; i++) {
				final int tid = i;
				threads[i] = new Thread("RemoveSubscriberThread-" + i) {
					@Override
					public void run() {
						try {
							deleteSubscriber(namespace, streams, tid, numStreamsPerThreads);
							System.out.println("Thread " + tid + " finished.");
						} catch (Exception e) {
							System.err.println("Thread " + tid + " quits with exception : " + e.getMessage());
						}
					}
				};
				threads[i].start();
			}
			for (int i = 0; i < numThreads; i++) {
				threads[i].join();
			}
			return 0;
		}

		private void deleteSubscriber(Namespace namespace, List<String> streams, int tid, int numStreamsPerThreads)
				throws Exception {
			int startIdx = tid * numStreamsPerThreads;
			int endIdx = Math.min(streams.size(), (tid + 1) * numStreamsPerThreads);
			for (int i = startIdx; i < endIdx; i++) {
				final String s = streams.get(i);
				DistributedLogManager dlm = namespace.openLog(s);
				final CountDownLatch countDownLatch = new CountDownLatch(1);
				dlm.getSubscriptionsStore().deleteSubscriber(subscriberId)
						.whenComplete(new FutureEventListener<Boolean>() {
							@Override
							public void onFailure(Throwable cause) {
								System.out.println("Failed to delete subscriber for stream " + s);
								cause.printStackTrace();
								countDownLatch.countDown();
							}

							@Override
							public void onSuccess(Boolean value) {
								countDownLatch.countDown();
							}
						});
				countDownLatch.await();
				dlm.close();
			}
		}
	}

	public DistributedLogTool() {
		super();
		addCommand(new AuditBKSpaceCommand());
		addCommand(new AuditLedgersCommand());
		addCommand(new AuditDLSpaceCommand());
		addCommand(new CreateCommand());
		addCommand(new CountCommand());
		addCommand(new DeleteCommand());
		addCommand(new DeleteAllocatorPoolCommand());
		addCommand(new DeleteLedgersCommand());
		addCommand(new DumpCommand());
		addCommand(new FindLedgerCommand());
		addCommand(new InspectCommand());
		addCommand(new InspectStreamCommand());
		addCommand(new ListCommand());
		addCommand(new ReadLastConfirmedCommand());
		addCommand(new ReadEntriesCommand());
		// TODO: Fix it later, tracking by https://github.com/apache/distributedlog/issues/150
		// addCommand(new RecoverCommand());
		addCommand(new RecoverLedgerCommand());
		addCommand(new ShowCommand());
		addCommand(new TruncateCommand());
		addCommand(new TruncateStreamCommand());
		addCommand(new DeserializeDLSNCommand());
		addCommand(new SerializeDLSNCommand());
		addCommand(new WatchNamespaceCommand());
		addCommand(new DeleteSubscriberCommand());
	}

	@Override
	protected String getName() {
		return "dlog_tool";
	}

}
