/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs2.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An InputStream that provides end-of-stream monitoring.
 * <p>
 * This is the same as {@link MonitorInputStream} but without the buffering.
 * </p>
 *
 * @since 2.5.0
 */
public class RawMonitorInputStream extends FilterInputStream {

	private static final int EOF_CHAR = -1;
	private final AtomicBoolean finished = new AtomicBoolean(false);
	private final AtomicLong atomicCount = new AtomicLong();

	//    @Override
	//    public synchronized void reset() throws IOException {
	//        if (!finished.get()) {
	//            super.reset();
	//        }
	//    }
	//
	//    @Override
	//    public synchronized long skip(long n) throws IOException {
	//        if (finished.get()) {
	//            return 0;
	//        }
	//        return super.skip(n);
	//    }

	/**
	 * Constructs a MonitorInputStream from the passed InputStream.
	 *
	 * @param inputStream The input stream to wrap.
	 */
	public RawMonitorInputStream(final InputStream inputStream) {
		super(inputStream);
	}

	/**
	 * Returns 0 if the stream is at EOF, else the underlying inputStream will be queried.
	 *
	 * @return The number of bytes that are available.
	 * @throws IOException if an error occurs.
	 */
	@Override
	public synchronized int available() throws IOException {
		if (finished.get()) {
			return 0;
		}

		return super.available();
	}

	/**
	 * Closes this input stream and releases any system resources associated with the stream.
	 *
	 * @throws IOException if an error occurs.
	 */
	@Override
	public void close() throws IOException {
		final boolean closed = finished.getAndSet(true);
		if (closed) {
			return;
		}

		// Close the stream
		IOException exc = null;
		try {
			super.close();
		} catch (final IOException ioe) {
			exc = ioe;
		}

		// Notify that the stream has been closed
		try {
			onClose();
		} catch (final IOException ioe) {
			exc = ioe;
		}

		if (exc != null) {
			throw exc;
		}
	}

	/**
	 * Gets the number of bytes read by this input stream.
	 *
	 * @return The number of bytes read by this input stream.
	 */
	public long getCount() {
		return atomicCount.get();
	}

	@Override
	public synchronized void mark(final int readlimit) {
		// TODO Auto-generated method stub
		super.mark(readlimit);
	}

	/**
	 * Called after the stream has been closed. This implementation does nothing.
	 *
	 * @throws IOException if an error occurs.
	 */
	protected void onClose() throws IOException {
		// noop
	}

	/**
	 * Reads a character.
	 *
	 * @return The character that was read as an integer.
	 * @throws IOException if an error occurs.
	 */
	@Override
	public int read() throws IOException { // lgtm [java/non-sync-override]
		if (finished.get()) {
			return EOF_CHAR;
		}

		final int ch = super.read();
		if (ch != EOF_CHAR) {
			atomicCount.incrementAndGet();
		}

		return ch;
	}

	/**
	 * Reads bytes from this input stream.
	 *
	 * @param buffer A byte array in which to place the characters read.
	 * @param offset The offset at which to start reading.
	 * @param length The maximum number of bytes to read.
	 * @return The number of bytes read.
	 * @throws IOException if an error occurs.
	 */
	@Override
	public int read(final byte[] buffer, final int offset, final int length) throws IOException { // lgtm [java/non-sync-override]
		if (finished.get()) {
			return EOF_CHAR;
		}

		final int nread = super.read(buffer, offset, length);
		if (nread != EOF_CHAR) {
			atomicCount.addAndGet(nread);
		}
		return nread;
	}
}
