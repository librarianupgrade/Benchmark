/**
 *
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
package org.apache.bookkeeper.bookie.storage.ldb;

import com.google.common.collect.Sets;
import io.netty.buffer.ByteBuf;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.bookkeeper.bookie.BookieImpl;
import org.apache.bookkeeper.bookie.DefaultEntryLogger;
import org.apache.bookkeeper.bookie.LedgerDirsManager;
import org.apache.bookkeeper.bookie.storage.EntryLogScanner;
import org.apache.bookkeeper.bookie.storage.ldb.KeyValueStorageFactory.DbConfigType;
import org.apache.bookkeeper.conf.ServerConfiguration;
import org.apache.bookkeeper.stats.NullStatsLogger;
import org.apache.bookkeeper.util.DiskChecker;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scan all entries in the entry log and rebuild the locations index.
 */
public class LocationsIndexRebuildOp {
	private final ServerConfiguration conf;

	public LocationsIndexRebuildOp(ServerConfiguration conf) {
		this.conf = conf;
	}

	public void initiate() throws IOException {
		LOG.info("Starting locations index rebuilding");
		File[] indexDirs = conf.getIndexDirs();
		if (indexDirs == null) {
			indexDirs = conf.getLedgerDirs();
		}
		if (indexDirs.length != conf.getLedgerDirs().length) {
			throw new IOException("ledger and index dirs size not matched");
		}
		long startTime = System.nanoTime();
		// Move locations index to a backup directory
		for (int i = 0; i < conf.getLedgerDirs().length; i++) {
			File ledgerDir = conf.getLedgerDirs()[i];
			File indexDir = indexDirs[i];
			String iBasePath = BookieImpl.getCurrentDirectory(indexDir).toString();
			Path indexCurrentPath = FileSystems.getDefault().getPath(iBasePath, "locations");
			String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date());
			Path backupPath = FileSystems.getDefault().getPath(iBasePath, "locations.BACKUP-" + timestamp);
			Files.move(indexCurrentPath, backupPath);

			LOG.info("Created locations index backup at {}", backupPath);

			File[] lDirs = new File[1];
			lDirs[0] = ledgerDir;
			DefaultEntryLogger entryLogger = new DefaultEntryLogger(conf, new LedgerDirsManager(conf, lDirs,
					new DiskChecker(conf.getDiskUsageThreshold(), conf.getDiskUsageWarnThreshold())));
			Set<Long> entryLogs = entryLogger.getEntryLogsSet();

			Set<Long> activeLedgers = getActiveLedgers(conf, KeyValueStorageRocksDB.factory, iBasePath);
			LOG.info("Found {} active ledgers in ledger manager", activeLedgers.size());

			KeyValueStorage newIndex = KeyValueStorageRocksDB.factory.newKeyValueStorage(iBasePath, "locations",
					DbConfigType.Default, conf);

			int totalEntryLogs = entryLogs.size();
			int completedEntryLogs = 0;
			LOG.info("Scanning {} entry logs", totalEntryLogs);

			for (long entryLogId : entryLogs) {
				entryLogger.scanEntryLog(entryLogId, new EntryLogScanner() {
					@Override
					public void process(long ledgerId, long offset, ByteBuf entry) throws IOException {
						long entryId = entry.getLong(8);

						// Actual location indexed is pointing past the entry size
						long location = (entryLogId << 32L) | (offset + 4);

						if (LOG.isDebugEnabled()) {
							LOG.debug("Rebuilding {}:{} at location {} / {}", ledgerId, entryId, location >> 32,
									location & (Integer.MAX_VALUE - 1));
						}

						// Update the ledger index page
						LongPairWrapper key = LongPairWrapper.get(ledgerId, entryId);
						LongWrapper value = LongWrapper.get(location);
						newIndex.put(key.array, value.array);
					}

					@Override
					public boolean accept(long ledgerId) {
						return activeLedgers.contains(ledgerId);
					}
				});

				++completedEntryLogs;
				LOG.info("Completed scanning of log {}.log -- {} / {}", Long.toHexString(entryLogId),
						completedEntryLogs, totalEntryLogs);
			}

			newIndex.sync();
			newIndex.close();
		}
		LOG.info("Rebuilding index is done. Total time: {}",
				DurationFormatUtils.formatDurationHMS(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)));
	}

	private Set<Long> getActiveLedgers(ServerConfiguration conf, KeyValueStorageFactory storageFactory, String basePath)
			throws IOException {
		LedgerMetadataIndex ledgers = new LedgerMetadataIndex(conf, storageFactory, basePath, NullStatsLogger.INSTANCE);
		Set<Long> activeLedgers = Sets.newHashSet();
		for (Long ledger : ledgers.getActiveLedgersInRange(0, Long.MAX_VALUE)) {
			activeLedgers.add(ledger);
		}

		ledgers.close();
		return activeLedgers;
	}

	private static final Logger LOG = LoggerFactory.getLogger(LocationsIndexRebuildOp.class);
}
