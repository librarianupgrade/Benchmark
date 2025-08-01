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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.vfs2.FileSystemException;

/**
 * An OutputStream that provides buffering and end-of-stream monitoring.
 */
public class MonitorOutputStream extends BufferedOutputStream {

	private final AtomicBoolean closed = new AtomicBoolean(false);

	/**
	 * Constructs a MonitorOutputStream from the passed OutputStream.
	 *
	 * @param out The output stream to wrap.
	 */
	public MonitorOutputStream(final OutputStream out) {
		super(out);
	}

	/**
	 * Constructs a MonitorOutputStream from the passed OutputStream and with the specified buffer size.
	 *
	 * @param out The output stream to wrap.
	 * @param bufferSize The buffer size to use.
	 * @since 2.4
	 */
	public MonitorOutputStream(final OutputStream out, final int bufferSize) {
		super(out, bufferSize);
	}

	/**
	 * Check if file is still open.
	 * <p>
	 * This is a workaround for an oddity with Java's BufferedOutputStream where you can write to even if the stream has
	 * been closed.
	 * </p>
	 *
	 * @throws FileSystemException if already closed.
	 * @since 2.0
	 */
	protected void assertOpen() throws FileSystemException {
		if (isClosed()) {
			throw new FileSystemException("vfs.provider/closed.error");
		}
	}

	/**
	 * Closes this output stream.
	 * <p>
	 * This makes sure the buffers are flushed, close the output stream and it will call {@link #onClose()} and re-throw
	 * last exception from any of the three.
	 * </p>
	 * <p>
	 * This does nothing if the stream is closed already.
	 * </p>
	 *
	 * @throws IOException if an IO error occurs.
	 */
	@Override
	public void close() throws IOException {
		// do not use super.close()
		// on Java 8 it might throw self suppression, see JDK-8042377
		// in older Java it silently ignores flush() errors
		if (closed.getAndSet(true)) {
			return;
		}

		IOException exc = null;

		// flush the buffer and out stream
		try {
			super.flush();
		} catch (final IOException ioe) {
			exc = ioe;
		}

		// close the out stream without using super.close()
		try {
			super.out.close();
		} catch (final IOException ioe) {
			exc = ioe;
		}

		// Notify of end of output
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
	 * @throws IOException if an error occurs.
	 * @since 2.0
	 */
	@Override
	public synchronized void flush() throws IOException {
		if (isClosed()) {
			return;
		}
		super.flush();
	}

	private boolean isClosed() {
		return closed.get();
	}

	/**
	 * Called after this stream is closed.
	 * <p>
	 * This implementation does nothing.
	 * </p>
	 *
	 * @throws IOException if an error occurs.
	 */
	// IOException is needed because subclasses may need to throw it
	protected void onClose() throws IOException {
	}

	/**
	 * @param b The byte array.
	 * @throws IOException if an error occurs.
	 * @since 2.0
	 */
	@Override
	public void write(final byte[] b) throws IOException {
		assertOpen();
		super.write(b);
	}

	/**
	 * @param b The byte array.
	 * @param off The offset into the array.
	 * @param len The number of bytes to write.
	 * @throws IOException if an error occurs.
	 * @since 2.0
	 */
	@Override
	public synchronized void write(final byte[] b, final int off, final int len) throws IOException {
		assertOpen();
		super.write(b, off, len);
	}

	/**
	 * @param b The character to write.
	 * @throws IOException if an error occurs.
	 * @since 2.0
	 */
	@Override
	public synchronized void write(final int b) throws IOException {
		assertOpen();
		super.write(b);
	}
}
