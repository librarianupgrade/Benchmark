/*
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

package org.apache.bookkeeper.bookie;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import org.apache.bookkeeper.conf.ServerConfiguration;
import org.apache.bookkeeper.util.NativeIO;
import org.apache.bookkeeper.util.ZeroBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple wrapper around FileChannel to add versioning
 * information to the file.
 */
class JournalChannel implements Closeable {
	private static final Logger LOG = LoggerFactory.getLogger(JournalChannel.class);

	static final long MB = 1024 * 1024L;
	final BookieFileChannel channel;
	final int fd;
	final FileChannel fc;
	final int formatVersion;
	BufferedChannel bc;
	long nextPrealloc = 0;

	final byte[] magicWord = "BKLG".getBytes(UTF_8);

	static final int SECTOR_SIZE = 512;
	private static final int START_OF_FILE = -12345;
	private static final long cacheDropLagBytes = 8 * MB;

	// No header
	static final int V1 = 1;
	// Adding header
	static final int V2 = 2;
	// Adding ledger key
	static final int V3 = 3;
	// Adding fencing key
	static final int V4 = 4;
	// 1) expanding header to 512
	// 2) Padding writes to align sector size
	static final int V5 = 5;
	// Adding explicit lac entry
	public static final int V6 = 6;

	static final int HEADER_SIZE = SECTOR_SIZE; // align header to sector size
	static final int VERSION_HEADER_SIZE = 8; // 4byte magic word, 4 byte version
	static final int MIN_COMPAT_JOURNAL_FORMAT_VERSION = V1;
	static final int CURRENT_JOURNAL_FORMAT_VERSION = V6;

	private final long preAllocSize;
	private final int journalAlignSize;
	private final boolean fRemoveFromPageCache;
	public final ByteBuffer zeros;

	// The position of the file channel's last drop position
	private long lastDropPosition = 0L;

	final ServerConfiguration configuration;

	// Mostly used by tests
	JournalChannel(File journalDirectory, long logId) throws IOException {
		this(journalDirectory, logId, 4 * MB, 65536, START_OF_FILE, new ServerConfiguration(),
				new DefaultFileChannelProvider());
	}

	// Open journal for scanning starting from the first record in journal.
	JournalChannel(File journalDirectory, long logId, long preAllocSize, int writeBufferSize, ServerConfiguration conf,
			FileChannelProvider provider) throws IOException {
		this(journalDirectory, logId, preAllocSize, writeBufferSize, START_OF_FILE, conf, provider);
	}

	// Open journal for scanning starting from given position.
	JournalChannel(File journalDirectory, long logId, long preAllocSize, int writeBufferSize, long position,
			ServerConfiguration conf, FileChannelProvider provider) throws IOException {
		this(journalDirectory, logId, preAllocSize, writeBufferSize, SECTOR_SIZE, position, false, V5,
				Journal.BufferedChannelBuilder.DEFAULT_BCBUILDER, conf, provider, null);
	}

	// Open journal to write
	JournalChannel(File journalDirectory, long logId, long preAllocSize, int writeBufferSize, int journalAlignSize,
			boolean fRemoveFromPageCache, int formatVersionToWrite, ServerConfiguration conf,
			FileChannelProvider provider) throws IOException {
		this(journalDirectory, logId, preAllocSize, writeBufferSize, journalAlignSize, fRemoveFromPageCache,
				formatVersionToWrite, Journal.BufferedChannelBuilder.DEFAULT_BCBUILDER, conf, provider, null);
	}

	JournalChannel(File journalDirectory, long logId, long preAllocSize, int writeBufferSize, int journalAlignSize,
			boolean fRemoveFromPageCache, int formatVersionToWrite, Journal.BufferedChannelBuilder bcBuilder,
			ServerConfiguration conf, FileChannelProvider provider, Long toReplaceLogId) throws IOException {
		this(journalDirectory, logId, preAllocSize, writeBufferSize, journalAlignSize, START_OF_FILE,
				fRemoveFromPageCache, formatVersionToWrite, bcBuilder, conf, provider, toReplaceLogId);
	}

	/**
	 * Create a journal file.
	 * Allows injection of BufferedChannelBuilder for testing purposes.
	 *
	 * @param journalDirectory
	 *          directory to store the journal file.
	 * @param logId
	 *          log id for the journal file.
	 * @param preAllocSize
	 *          pre allocation size.
	 * @param writeBufferSize
	 *          write buffer size.
	 * @param journalAlignSize
	 *          size to align journal writes.
	 * @param position
	 *          position to start read/write
	 * @param fRemoveFromPageCache
	 *          whether to remove cached pages from page cache.
	 * @param formatVersionToWrite
	 *          format version to write
	 * @throws IOException
	 */
	private JournalChannel(File journalDirectory, long logId, long preAllocSize, int writeBufferSize,
			int journalAlignSize, long position, boolean fRemoveFromPageCache, int formatVersionToWrite,
			Journal.BufferedChannelBuilder bcBuilder, ServerConfiguration conf, FileChannelProvider provider,
			Long toReplaceLogId) throws IOException {
		this.journalAlignSize = journalAlignSize;
		this.zeros = ByteBuffer.allocate(journalAlignSize);
		this.preAllocSize = preAllocSize - preAllocSize % journalAlignSize;
		this.fRemoveFromPageCache = fRemoveFromPageCache;
		this.configuration = conf;

		boolean reuseFile = false;
		File fn = new File(journalDirectory, Long.toHexString(logId) + ".txn");
		if (toReplaceLogId != null && logId != toReplaceLogId && provider.supportReuseFile()) {
			File toReplaceFile = new File(journalDirectory, Long.toHexString(toReplaceLogId) + ".txn");
			if (toReplaceFile.exists()) {
				renameJournalFile(toReplaceFile, fn);
				provider.notifyRename(toReplaceFile, fn);
				reuseFile = true;
			}
		}
		channel = provider.open(fn, configuration);

		if (formatVersionToWrite < V4) {
			throw new IOException("Invalid journal format to write : version = " + formatVersionToWrite);
		}

		LOG.info("Opening journal {}", fn);
		if (!channel.fileExists(fn)) { // create new journal file to write, write version
			if (!fn.createNewFile()) {
				LOG.error("Journal file {}, that shouldn't exist, already exists. "
						+ " is there another bookie process running?", fn);
				throw new IOException("File " + fn + " suddenly appeared, is another bookie process running?");
			}
			fc = channel.getFileChannel();
			formatVersion = formatVersionToWrite;
			writeHeader(bcBuilder, writeBufferSize);
		} else if (reuseFile) { // Open an existing journal to write, it needs fileChannelProvider support reuse file.
			fc = channel.getFileChannel();
			formatVersion = formatVersionToWrite;
			writeHeader(bcBuilder, writeBufferSize);
		} else { // open an existing file to read.
			fc = channel.getFileChannel();
			bc = null; // readonly

			ByteBuffer bb = ByteBuffer.allocate(VERSION_HEADER_SIZE);
			int c = fc.read(bb);
			bb.flip();

			if (c == VERSION_HEADER_SIZE) {
				byte[] first4 = new byte[4];
				bb.get(first4);

				if (Arrays.equals(first4, magicWord)) {
					formatVersion = bb.getInt();
				} else {
					// pre magic word journal, reset to 0;
					formatVersion = V1;
				}
			} else {
				// no header, must be old version
				formatVersion = V1;
			}

			if (formatVersion < MIN_COMPAT_JOURNAL_FORMAT_VERSION || formatVersion > CURRENT_JOURNAL_FORMAT_VERSION) {
				String err = String.format(
						"Invalid journal version, unable to read." + " Expected between (%d) and (%d), got (%d)",
						MIN_COMPAT_JOURNAL_FORMAT_VERSION, CURRENT_JOURNAL_FORMAT_VERSION, formatVersion);
				LOG.error(err);
				throw new IOException(err);
			}

			try {
				if (position == START_OF_FILE) {
					if (formatVersion >= V5) {
						fc.position(HEADER_SIZE);
					} else if (formatVersion >= V2) {
						fc.position(VERSION_HEADER_SIZE);
					} else {
						fc.position(0);
					}
				} else {
					fc.position(position);
				}
			} catch (IOException e) {
				LOG.error("Bookie journal file can seek to position :", e);
				throw e;
			}
		}
		if (fRemoveFromPageCache) {
			this.fd = NativeIO.getSysFileDescriptor(channel.getFD());
		} else {
			this.fd = -1;
		}
	}

	private void writeHeader(Journal.BufferedChannelBuilder bcBuilder, int writeBufferSize) throws IOException {
		int headerSize = (V4 == formatVersion) ? VERSION_HEADER_SIZE : HEADER_SIZE;
		ByteBuffer bb = ByteBuffer.allocate(headerSize);
		ZeroBuffer.put(bb);
		bb.clear();
		bb.put(magicWord);
		bb.putInt(formatVersion);
		bb.clear();
		fc.write(bb);

		bc = bcBuilder.create(fc, writeBufferSize);
		forceWrite(true);
		nextPrealloc = this.preAllocSize;
		fc.write(zeros, nextPrealloc - journalAlignSize);
	}

	public static void renameJournalFile(File source, File target) throws IOException {
		if (source == null || target == null || !source.renameTo(target)) {
			LOG.error("Failed to rename file {} to {}", source, target);
			throw new IOException("Failed to rename file " + source + " to " + target);
		}
	}

	int getFormatVersion() {
		return formatVersion;
	}

	BufferedChannel getBufferedChannel() throws IOException {
		if (bc == null) {
			throw new IOException("Read only journal channel");
		}
		return bc;
	}

	void preAllocIfNeeded(long size) throws IOException {
		if (bc.position() + size > nextPrealloc) {
			nextPrealloc += preAllocSize;
			zeros.clear();
			fc.write(zeros, nextPrealloc - journalAlignSize);
		}
	}

	int read(ByteBuffer dst) throws IOException {
		return fc.read(dst);
	}

	@Override
	public void close() throws IOException {
		if (bc != null) {
			bc.close();
		}
	}

	public void forceWrite(boolean forceMetadata) throws IOException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Journal ForceWrite");
		}
		long newForceWritePosition = bc.forceWrite(forceMetadata);
		//
		// For POSIX_FADV_DONTNEED, we want to drop from the beginning
		// of the file to a position prior to the current position.
		//
		// The cacheDropLagBytes is to prevent dropping a page that will
		// be appended again, which would introduce random seeking on journal
		// device.
		//
		// <======== drop ==========>
		//                           <-----------LAG------------>
		// +------------------------+---------------------------O
		// lastDropPosition     newDropPos             lastForceWritePosition
		//
		if (fRemoveFromPageCache) {
			long newDropPos = newForceWritePosition - cacheDropLagBytes;
			if (lastDropPosition < newDropPos) {
				NativeIO.bestEffortRemoveFromPageCache(fd, lastDropPosition, newDropPos - lastDropPosition);
			}
			this.lastDropPosition = newDropPos;
		}
	}

}
