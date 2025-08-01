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
package org.apache.commons.vfs2.provider;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.Certificate;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileContentInfo;
import org.apache.commons.vfs2.FileContentInfoFactory;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.RandomAccessContent;
import org.apache.commons.vfs2.util.MonitorInputStream;
import org.apache.commons.vfs2.util.MonitorOutputStream;
import org.apache.commons.vfs2.util.MonitorRandomAccessContent;
import org.apache.commons.vfs2.util.RandomAccessMode;
import org.apache.commons.vfs2.util.RawMonitorInputStream;

/**
 * The content of a file.
 */
public final class DefaultFileContent implements FileContent {

	/*
	 * static final int STATE_NONE = 0; static final int STATE_READING = 1; static final int STATE_WRITING = 2; static
	 * final int STATE_RANDOM_ACCESS = 3;
	 */

	/**
	 * An input stream for reading content. Provides buffering, and end-of-stream monitoring.
	 */
	private final class FileContentInputStream extends MonitorInputStream {
		// avoid gc
		private final FileObject file;

		FileContentInputStream(final FileObject file, final InputStream instr) {
			super(instr);
			this.file = file;
		}

		FileContentInputStream(final FileObject file, final InputStream instr, final int bufferSize) {
			super(instr, bufferSize);
			this.file = file;
		}

		/**
		 * Closes this input stream.
		 */
		@Override
		public void close() throws FileSystemException {
			try {
				super.close();
			} catch (final IOException e) {
				throw new FileSystemException("vfs.provider/close-instr.error", file, e);
			}
		}

		/**
		 * Called after the stream has been closed.
		 */
		@Override
		protected void onClose() throws IOException {
			try {
				super.onClose();
			} finally {
				endInput(this);
			}
		}
	}

	/**
	 * An output stream for writing content.
	 */
	final class FileContentOutputStream extends MonitorOutputStream {
		// avoid gc
		private final FileObject file;

		FileContentOutputStream(final FileObject file, final OutputStream outstr) {
			super(outstr);
			this.file = file;
		}

		FileContentOutputStream(final FileObject file, final OutputStream outstr, final int bufferSize) {
			super(outstr, bufferSize);
			this.file = file;
		}

		/**
		 * Closes this output stream.
		 */
		@Override
		public void close() throws FileSystemException {
			try {
				super.close();
			} catch (final IOException e) {
				throw new FileSystemException("vfs.provider/close-outstr.error", file, e);
			}
		}

		/**
		 * Called after this stream is closed.
		 */
		@Override
		protected void onClose() throws IOException {
			try {
				super.onClose();
			} finally {
				try {
					endOutput();
				} catch (final Exception e) {
					throw new FileSystemException("vfs.provider/close-outstr.error", file, e);
				}
			}
		}
	}

	/**
	 * An input/output stream for reading/writing content on random positions
	 */
	private final class FileRandomAccessContent extends MonitorRandomAccessContent {
		// also avoids gc
		private final FileObject file;

		FileRandomAccessContent(final FileObject file, final RandomAccessContent content) {
			super(content);
			this.file = file;
		}

		@Override
		public void close() throws FileSystemException {
			try {
				super.close();
			} catch (final IOException e) {
				throw new FileSystemException("vfs.provider/close-rac.error", file, e);
			}
		}

		/**
		 * Called after the stream has been closed.
		 */
		@Override
		protected void onClose() throws IOException {
			try {
				super.onClose();
			} finally {
				endRandomAccess(this);
			}
		}
	}

	/**
	 * An input stream for reading content. Provides buffering, and end-of-stream monitoring.
	 * <p>
	 * This is the same as {@link FileContentInputStream} but without the buffering.
	 * </p>
	 */
	private final class RawFileContentInputStream extends RawMonitorInputStream {
		// avoid gc
		private final FileObject file;

		RawFileContentInputStream(final FileObject file, final InputStream instr) {
			super(instr);
			this.file = file;
		}

		/**
		 * Closes this input stream.
		 */
		@Override
		public void close() throws FileSystemException {
			try {
				super.close();
			} catch (final IOException e) {
				throw new FileSystemException("vfs.provider/close-instr.error", file, e);
			}
		}

		/**
		 * Called after the stream has been closed.
		 */
		@Override
		protected void onClose() throws IOException {
			try {
				super.onClose();
			} finally {
				endInput(this);
			}
		}
	}

	static final int STATE_CLOSED = 0;
	static final int STATE_OPENED = 1;

	private static final Certificate[] EMPTY_CERTIFICATE_ARRAY = {};

	/**
	 * The default buffer size for {@link #write(OutputStream)}.
	 */
	private static final int WRITE_BUFFER_SIZE = 4096;
	private final AbstractFileObject<?> fileObject;

	private Map<String, Object> attrs;
	private Map<String, Object> roAttrs;

	private FileContentInfo fileContentInfo;

	private final FileContentInfoFactory fileContentInfoFactory;

	private final ThreadLocal<FileContentThreadData> threadLocal = ThreadLocal.withInitial(FileContentThreadData::new);

	private boolean resetAttributes;

	/**
	 * Counts open streams for this file.
	 */
	private int openStreams;

	/**
	 * Constructs a new instance.
	 *
	 * @param fileObject The file object.
	 * @param fileContentInfoFactory The info factory.
	 */
	public DefaultFileContent(final AbstractFileObject fileObject,
			final FileContentInfoFactory fileContentInfoFactory) {
		this.fileObject = fileObject;
		this.fileContentInfoFactory = fileContentInfoFactory;
	}

	private InputStream buildInputStream(final int bufferSize) throws FileSystemException {
		/*
		 * if (getThreadData().getState() == STATE_WRITING || getThreadData().getState() == STATE_RANDOM_ACCESS) { throw new
		 * FileSystemException("vfs.provider/read-in-use.error", file); }
		 */
		// Get the raw input stream
		// @formatter:off
        final InputStream inputStream = bufferSize == 0
                ? fileObject.getInputStream()
                : fileObject.getInputStream(bufferSize);
        // @formatter:on
		// Double buffering may take place here.
		//        final InputStream wrappedInputStream = bufferSize == 0
		//                    ? new FileContentInputStream(fileObject, inputStream)
		//                    : new FileContentInputStream(fileObject, inputStream, bufferSize);

		final InputStream wrappedInputStream;
		if (inputStream instanceof BufferedInputStream) {
			// Don't double buffer.
			wrappedInputStream = new RawFileContentInputStream(fileObject, inputStream);
		} else {
			// @formatter:off
            wrappedInputStream = bufferSize == 0
                    ? new FileContentInputStream(fileObject, inputStream)
                    : new FileContentInputStream(fileObject, inputStream, bufferSize);
            // @formatter:on
		}
		getFileContentThreadData().add(wrappedInputStream);
		streamOpened();

		return wrappedInputStream;
	}

	private OutputStream buildOutputStream(final boolean bAppend, final int bufferSize) throws FileSystemException {
		/*
		 * if (getThreadData().getState() != STATE_NONE)
		 */
		final FileContentThreadData threadData = getFileContentThreadData();

		if (threadData.getOutputStream() != null) {
			throw new FileSystemException("vfs.provider/write-in-use.error", fileObject);
		}

		// Get the raw output stream
		final OutputStream outstr = fileObject.getOutputStream(bAppend);

		// Create and set wrapper
		final FileContentOutputStream wrapped = bufferSize == 0 ? new FileContentOutputStream(fileObject, outstr)
				: new FileContentOutputStream(fileObject, outstr, bufferSize);
		threadData.setOutputStream(wrapped);
		streamOpened();

		return wrapped;
	}

	/**
	 * Closes all resources used by the content, including all streams, readers and writers.
	 *
	 * @throws FileSystemException if an error occurs.
	 */
	@Override
	public void close() throws FileSystemException {
		FileSystemException caught = null;
		try {
			final FileContentThreadData threadData = getFileContentThreadData();

			// Close the input stream
			while (threadData.hasInputStream()) {
				final InputStream inputStream = threadData.removeInputStream(0);
				try {
					if (inputStream instanceof FileContentInputStream) {
						((FileContentInputStream) inputStream).close();
					} else if (inputStream instanceof RawFileContentInputStream) {
						((RawFileContentInputStream) inputStream).close();
					} else {
						caught = new FileSystemException("Unsupported InputStream type: " + inputStream);
					}
				} catch (final FileSystemException ex) {
					caught = ex;

				}
			}

			// Close the randomAccess stream
			while (threadData.hasRandomAccessContent()) {
				final FileRandomAccessContent randomAccessContent = (FileRandomAccessContent) threadData
						.removeRandomAccessContent(0);
				try {
					randomAccessContent.close();
				} catch (final FileSystemException ex) {
					caught = ex;
				}
			}

			// Close the output stream
			final FileContentOutputStream outputStream = threadData.getOutputStream();
			if (outputStream != null) {
				threadData.setOutputStream(null);
				try {
					outputStream.close();
				} catch (final FileSystemException ex) {
					caught = ex;
				}
			}
		} finally {
			threadLocal.remove();
		}

		// throw last error (out >> rac >> input) after all closes have been tried
		if (caught != null) {
			throw caught;
		}
	}

	/**
	 * Handles the end of input stream.
	 */
	private void endInput(final InputStream instr) {
		final FileContentThreadData fileContentThreadData = threadLocal.get();
		if (fileContentThreadData != null) {
			fileContentThreadData.remove(instr);
		}
		if (fileContentThreadData == null || !fileContentThreadData.hasStreams()) {
			// remove even when no value is set to remove key
			threadLocal.remove();
		}
		streamClosed();
	}

	/**
	 * Handles the end of output stream.
	 */
	private void endOutput() throws Exception {
		final FileContentThreadData fileContentThreadData = threadLocal.get();
		if (fileContentThreadData != null) {
			fileContentThreadData.setOutputStream(null);
		}
		if (fileContentThreadData == null || !fileContentThreadData.hasStreams()) {
			// remove even when no value is set to remove key
			threadLocal.remove();
		}
		streamClosed();
		fileObject.endOutput();
	}

	/**
	 * Handles the end of random access.
	 */
	private void endRandomAccess(final RandomAccessContent rac) {
		final FileContentThreadData fileContentThreadData = threadLocal.get();
		if (fileContentThreadData != null) {
			fileContentThreadData.remove(rac);
		}
		if (fileContentThreadData == null || !fileContentThreadData.hasStreams()) {
			// remove even when no value is set to remove key
			threadLocal.remove();
		}
		streamClosed();
	}

	/**
	 * Gets the value of an attribute.
	 *
	 * @param attrName The attribute name.
	 * @return The value of the attribute or null.
	 * @throws FileSystemException if an error occurs.
	 */
	@Override
	public Object getAttribute(final String attrName) throws FileSystemException {
		getAttributes();
		return attrs.get(attrName);
	}

	/**
	 * Lists the attributes of this file.
	 *
	 * @return An array of attribute names.
	 * @throws FileSystemException if an error occurs.
	 */
	@Override
	public String[] getAttributeNames() throws FileSystemException {
		getAttributes();
		final Set<String> names = attrs.keySet();
		return names.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
	}

	/**
	 * Returns a read-only map of this file's attributes.
	 *
	 * @return a Map of the file's attributes.
	 * @throws FileSystemException if an error occurs.
	 */
	@Override
	public Map<String, Object> getAttributes() throws FileSystemException {
		if (!fileObject.getType().hasAttributes()) {
			throw new FileSystemException("vfs.provider/get-attributes-no-exist.error", fileObject);
		}
		if (resetAttributes || roAttrs == null) {
			try {
				synchronized (this) {
					attrs = fileObject.doGetAttributes();
					roAttrs = Collections.unmodifiableMap(attrs);
					resetAttributes = false;
				}
			} catch (final Exception e) {
				throw new FileSystemException("vfs.provider/get-attributes.error", fileObject, e);
			}
		}
		return roAttrs;
	}

	/**
	 * Returns the certificates used to sign this file.
	 *
	 * @return An array of Certificates.
	 * @throws FileSystemException if an error occurs.
	 */
	@Override
	public Certificate[] getCertificates() throws FileSystemException {
		if (!fileObject.exists()) {
			throw new FileSystemException("vfs.provider/get-certificates-no-exist.error", fileObject);
		}
		/*
		 * if (getThreadData().getState() == STATE_WRITING || getThreadData().getState() == STATE_RANDOM_ACCESS) { throw
		 * new FileSystemException("vfs.provider/get-certificates-writing.error", file); }
		 */

		try {
			final Certificate[] certs = fileObject.doGetCertificates();
			if (certs != null) {
				return certs;
			}
			return EMPTY_CERTIFICATE_ARRAY;
		} catch (final Exception e) {
			throw new FileSystemException("vfs.provider/get-certificates.error", fileObject, e);
		}
	}

	/**
	 * Gets the FileContentInfo which describes the content-type, content-encoding.
	 *
	 * @return The FileContentInfo.
	 * @throws FileSystemException if an error occurs.
	 */
	@Override
	public FileContentInfo getContentInfo() throws FileSystemException {
		if (fileContentInfo == null) {
			fileContentInfo = fileContentInfoFactory.create(this);
		}

		return fileContentInfo;
	}

	/**
	 * Returns the file that this is the content of.
	 *
	 * @return the FileObject.
	 */
	@Override
	public FileObject getFile() {
		return fileObject;
	}

	private FileContentThreadData getFileContentThreadData() {
		return this.threadLocal.get();
	}

	/**
	 * Returns an input stream for reading the content.
	 *
	 * @return The InputStream
	 * @throws FileSystemException if an error occurs.
	 */
	@Override
	public InputStream getInputStream() throws FileSystemException {
		return buildInputStream(0);
	}

	/**
	 * Returns an input stream for reading the content.
	 *
	 * @param bufferSize The buffer size to use.
	 * @return The InputStream
	 * @throws FileSystemException if an error occurs.
	 * @since 2.4
	 */
	@Override
	public InputStream getInputStream(final int bufferSize) throws FileSystemException {
		return buildInputStream(bufferSize);
	}

	/**
	 * Returns the last-modified timestamp.
	 *
	 * @return The last modified timestamp.
	 * @throws FileSystemException if an error occurs.
	 */
	@Override
	public long getLastModifiedTime() throws FileSystemException {
		/*
		 * if (getThreadData().getState() == STATE_WRITING || getThreadData().getState() == STATE_RANDOM_ACCESS) { throw
		 * new FileSystemException("vfs.provider/get-last-modified-writing.error", file); }
		 */
		if (!fileObject.getType().hasAttributes()) {
			throw new FileSystemException("vfs.provider/get-last-modified-no-exist.error", fileObject);
		}
		try {
			return fileObject.doGetLastModifiedTime();
		} catch (final Exception e) {
			throw new FileSystemException("vfs.provider/get-last-modified.error", fileObject, e);
		}
	}

	/**
	 * Returns an output stream for writing the content.
	 *
	 * @return The OutputStream for the file.
	 * @throws FileSystemException if an error occurs.
	 */
	@Override
	public OutputStream getOutputStream() throws FileSystemException {
		return getOutputStream(false);
	}

	/**
	 * Returns an output stream for writing the content in append mode.
	 *
	 * @param bAppend true if the data written should be appended.
	 * @return The OutputStream for the file.
	 * @throws FileSystemException if an error occurs.
	 */
	@Override
	public OutputStream getOutputStream(final boolean bAppend) throws FileSystemException {
		return buildOutputStream(bAppend, 0);
	}

	/**
	 * Returns an output stream for writing the content in append mode.
	 *
	 * @param bAppend true if the data written should be appended.
	 * @param bufferSize The buffer size to use.
	 * @return The OutputStream for the file.
	 * @throws FileSystemException if an error occurs.
	 * @since 2.4
	 */
	@Override
	public OutputStream getOutputStream(final boolean bAppend, final int bufferSize) throws FileSystemException {
		return buildOutputStream(bAppend, bufferSize);
	}

	/**
	 * Returns an output stream for writing the content.
	 *
	 * @param bufferSize The buffer size to use.
	 * @return The OutputStream for the file.
	 * @throws FileSystemException if an error occurs.
	 * @since 2.4
	 */
	@Override
	public OutputStream getOutputStream(final int bufferSize) throws FileSystemException {
		return buildOutputStream(false, bufferSize);
	}

	/**
	 * Returns an input/output stream to use to read and write the content of the file in a random manner.
	 *
	 * @param mode The RandomAccessMode.
	 * @return A RandomAccessContent object to access the file.
	 * @throws FileSystemException if an error occurs.
	 */
	@Override
	public RandomAccessContent getRandomAccessContent(final RandomAccessMode mode) throws FileSystemException {
		/*
		 * if (getThreadData().getState() != STATE_NONE) { throw new
		 * FileSystemException("vfs.provider/read-in-use.error", file); }
		 */

		// Get the content
		final RandomAccessContent rastr = fileObject.getRandomAccessContent(mode);

		final FileRandomAccessContent rac = new FileRandomAccessContent(fileObject, rastr);

		getFileContentThreadData().add(rac);
		streamOpened();

		return rac;
	}

	/**
	 * Returns the size of the content (in bytes).
	 *
	 * @return The size of the content (in bytes).
	 * @throws FileSystemException if an error occurs.
	 */
	@Override
	public long getSize() throws FileSystemException {
		// Do some checking
		if (!fileObject.getType().hasContent()) {
			throw new FileSystemException("vfs.provider/get-size-not-file.error", fileObject);
		}
		/*
		 * if (getThreadData().getState() == STATE_WRITING || getThreadData().getState() == STATE_RANDOM_ACCESS) { throw
		 * new FileSystemException("vfs.provider/get-size-write.error", file); }
		 */

		try {
			// Get the size
			return fileObject.doGetContentSize();
		} catch (final Exception exc) {
			throw new FileSystemException("vfs.provider/get-size.error", exc, fileObject);
		}
	}

	/**
	 * Checks if an attribute exists.
	 *
	 * @param attrName The name of the attribute to check.
	 * @return true if the attribute is associated with the file.
	 * @throws FileSystemException if an error occurs.
	 * @since 2.0
	 */
	@Override
	public boolean hasAttribute(final String attrName) throws FileSystemException {
		if (!fileObject.getType().hasAttributes()) {
			throw new FileSystemException("vfs.provider/exists-attributes-no-exist.error", fileObject);
		}
		getAttributes();
		return attrs.containsKey(attrName);
	}

	/**
	 * Checks if an input and/or output stream is open.
	 * <p>
	 * This checks only the scope of the current thread.
	 * </p>
	 *
	 * @return true if this is the case
	 */
	@Override
	public boolean isOpen() {
		final FileContentThreadData fileContentThreadData = threadLocal.get();
		if (fileContentThreadData != null && fileContentThreadData.hasStreams()) {
			return true;
		}
		// threadData.get() created empty entry
		threadLocal.remove();
		return false;
	}

	/**
	 * Checks if an input or output stream is open. This checks all threads.
	 *
	 * @return true if this is the case
	 */
	public boolean isOpenGlobal() {
		synchronized (this) {
			return openStreams > 0;
		}
	}

	/**
	 * Removes an attribute.
	 *
	 * @param attrName The name of the attribute to remove.
	 * @throws FileSystemException if an error occurs.
	 * @since 2.0
	 */
	@Override
	public void removeAttribute(final String attrName) throws FileSystemException {
		if (!fileObject.getType().hasAttributes()) {
			throw new FileSystemException("vfs.provider/remove-attribute-no-exist.error", fileObject);
		}

		try {
			fileObject.doRemoveAttribute(attrName);
		} catch (final Exception e) {
			throw new FileSystemException("vfs.provider/remove-attribute.error", e, attrName, fileObject);
		}

		if (attrs != null) {
			attrs.remove(attrName);
		}
	}

	/**
	 * Used internally to flag situations where the file attributes should be retrieved again.
	 *
	 * @since 2.0
	 */
	public void resetAttributes() {
		resetAttributes = true;
	}

	/**
	 * Sets the value of an attribute.
	 *
	 * @param attrName The name of the attribute to add.
	 * @param value The value of the attribute.
	 * @throws FileSystemException if an error occurs.
	 */
	@Override
	public void setAttribute(final String attrName, final Object value) throws FileSystemException {
		if (!fileObject.getType().hasAttributes()) {
			throw new FileSystemException("vfs.provider/set-attribute-no-exist.error", attrName, fileObject);
		}
		try {
			fileObject.doSetAttribute(attrName, value);
		} catch (final Exception e) {
			throw new FileSystemException("vfs.provider/set-attribute.error", e, attrName, fileObject);
		}

		if (attrs != null) {
			attrs.put(attrName, value);
		}
	}

	/**
	 * Sets the last-modified timestamp.
	 *
	 * @param modTime The last modified timestamp.
	 * @throws FileSystemException if an error occurs.
	 */
	@Override
	public void setLastModifiedTime(final long modTime) throws FileSystemException {
		/*
		 * if (getThreadData().getState() == STATE_WRITING || getThreadData().getState() == STATE_RANDOM_ACCESS) { throw
		 * new FileSystemException("vfs.provider/set-last-modified-writing.error", file); }
		 */
		if (!fileObject.getType().hasAttributes()) {
			throw new FileSystemException("vfs.provider/set-last-modified-no-exist.error", fileObject);
		}
		try {
			if (!fileObject.doSetLastModifiedTime(modTime)) {
				throw new FileSystemException("vfs.provider/set-last-modified.error", fileObject);
			}
		} catch (final Exception e) {
			throw new FileSystemException("vfs.provider/set-last-modified.error", fileObject, e);
		}
	}

	void streamClosed() {
		synchronized (this) {
			if (openStreams > 0) {
				openStreams--;
				if (openStreams < 1) {
					fileObject.notifyAllStreamsClosed();
				}
			}
		}
		((AbstractFileSystem) fileObject.getFileSystem()).streamClosed();
	}

	void streamOpened() {
		synchronized (this) {
			openStreams++;
		}
		((AbstractFileSystem) fileObject.getFileSystem()).streamOpened();
	}

	/**
	 * Writes this content to another FileContent.
	 *
	 * @param fileContent The target FileContent.
	 * @return the total number of bytes written
	 * @throws IOException if an error occurs writing the content.
	 * @since 2.1
	 */
	@Override
	public long write(final FileContent fileContent) throws IOException {
		try (OutputStream output = fileContent.getOutputStream()) {
			return write(output);
		}
	}

	/**
	 * Writes this content to another FileObject.
	 *
	 * @param file The target FileObject.
	 * @return the total number of bytes written
	 * @throws IOException if an error occurs writing the content.
	 * @since 2.1
	 */
	@Override
	public long write(final FileObject file) throws IOException {
		return write(file.getContent());
	}

	/**
	 * Writes this content to an OutputStream.
	 *
	 * @param output The target OutputStream.
	 * @return the total number of bytes written
	 * @throws IOException if an error occurs writing the content.
	 * @since 2.1
	 */
	@Override
	public long write(final OutputStream output) throws IOException {
		return write(output, WRITE_BUFFER_SIZE);
	}

	/**
	 * Writes this content to an OutputStream.
	 *
	 * @param output The target OutputStream.
	 * @param bufferSize The buffer size to write data chunks.
	 * @return the total number of bytes written
	 * @throws IOException if an error occurs writing the file.
	 * @since 2.1
	 */
	@Override
	public long write(final OutputStream output, final int bufferSize) throws IOException {
		final InputStream input = getInputStream();
		long count = 0;
		try {
			// This read/write code from Apache Commons IO
			final byte[] buffer = new byte[bufferSize];
			int n;
			while (-1 != (n = input.read(buffer))) {
				output.write(buffer, 0, n);
				count += n;
			}
		} finally {
			input.close();
		}
		return count;
	}
}
