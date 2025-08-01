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
package org.apache.commons.vfs2.provider.local;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.RandomAccessContent;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.commons.vfs2.provider.UriParser;
import org.apache.commons.vfs2.util.FileObjectUtils;
import org.apache.commons.vfs2.util.RandomAccessMode;

/**
 * A file object implementation which uses direct file access.
 */
public class LocalFile extends AbstractFileObject<LocalFileSystem> {

	private final String rootFile;

	private File file;

	/**
	 * Creates a non-root file.
	 *
	 * @param fileSystem the file system this file belongs to.
	 * @param rootFile the root file for the file system.
	 * @param name the file name on this file system.
	 */
	protected LocalFile(final LocalFileSystem fileSystem, final String rootFile, final AbstractFileName name) {
		super(name, fileSystem);
		this.rootFile = rootFile;
	}

	/**
	 * Attaches this file object to its file resource.
	 */
	@Override
	protected void doAttach() throws Exception {
		if (file == null) {
			// Remove the "file:///"
			// LocalFileName localFileName = (LocalFileName) getName();
			final String fileName = rootFile + getName().getPathDecoded();
			// fileName = UriParser.decode(fileName);
			file = new File(fileName);
		}
	}

	/**
	 * Creates this folder.
	 */
	@Override
	protected void doCreateFolder() throws Exception {
		if (!file.mkdirs()) {
			throw new FileSystemException("vfs.provider.local/create-folder.error", file);
		}
	}

	/**
	 * Deletes this file, and all children.
	 */
	@Override
	protected void doDelete() throws Exception {
		if (!file.delete()) {
			throw new FileSystemException("vfs.provider.local/delete-file.error", file);
		}
	}

	/**
	 * Returns the size of the file content (in bytes).
	 */
	@Override
	protected long doGetContentSize() throws Exception {
		return file.length();
	}

	/**
	 * Creates an input stream to read the file contents.
	 */
	@Override
	protected InputStream doGetInputStream(final int bufferSize) throws Exception {
		return new FileInputStream(file);
	}

	/**
	 * Gets the last modified time of this file.
	 */
	@Override
	protected long doGetLastModifiedTime() throws FileSystemException {
		// Workaround OpenJDK 8 and 9 bug JDK-8177809
		// https://bugs.openjdk.java.net/browse/JDK-8177809
		try {
			return Files.getLastModifiedTime(file.toPath()).toMillis();
		} catch (final IOException e) {
			throw new FileSystemException("vfs.provider/get-last-modified.error", file, e);
		}
	}

	/**
	 * Creates an output stream to write the file content to.
	 */
	@Override
	protected OutputStream doGetOutputStream(final boolean append) throws Exception {
		// TODO Reuse Apache Commons IO
		// @formatter:off
        return Files.newOutputStream(file.toPath(), append ?
            new OpenOption[] {StandardOpenOption.CREATE, StandardOpenOption.APPEND} :
            new OpenOption[] {StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING});
        // @formatter:on
	}

	@Override
	protected RandomAccessContent doGetRandomAccessContent(final RandomAccessMode mode) throws Exception {
		return new LocalFileRandomAccessContent(file, mode);
	}

	/**
	 * Returns the file's type.
	 */
	@Override
	protected FileType doGetType() {
		if (!file.exists()) {
			return FileType.IMAGINARY;
		}
		if (file.isDirectory()) {
			return FileType.FOLDER;
		}
		return FileType.FILE;
	}

	/**
	 * Determines if this file is hidden.
	 */
	@Override
	protected boolean doIsExecutable() {
		return file.canExecute();
	}

	/**
	 * Determines if this file is hidden.
	 */
	@Override
	protected boolean doIsHidden() {
		return file.isHidden();
	}

	/**
	 * Determines if this file can be read.
	 */
	@Override
	protected boolean doIsReadable() throws FileSystemException {
		return file.canRead();
	}

	@Override
	protected boolean doIsSameFile(final FileObject destFile) throws FileSystemException {
		if (!FileObjectUtils.isInstanceOf(destFile, LocalFile.class)) {
			return false;
		}

		final LocalFile destLocalFile = (LocalFile) FileObjectUtils.getAbstractFileObject(destFile);
		if (!exists() || !destLocalFile.exists()) {
			return false;
		}

		try {
			return file.getCanonicalPath().equals(destLocalFile.file.getCanonicalPath());
		} catch (final IOException e) {
			throw new FileSystemException(e);
		}
	}

	/**
	 * Determines if this file is a symbolic link.
	 *
	 * @since 2.4
	 */
	@Override
	protected boolean doIsSymbolicLink() throws FileSystemException {
		return Files.isSymbolicLink(file.toPath());
	}

	/**
	 * Determines if this file can be written to.
	 */
	@Override
	protected boolean doIsWriteable() throws FileSystemException {
		return file.canWrite();
	}

	/**
	 * Returns the children of the file.
	 */
	@Override
	protected String[] doListChildren() throws Exception {
		return UriParser.encode(file.list());
	}

	/**
	 * rename this file
	 */
	@Override
	protected void doRename(final FileObject newFile) throws Exception {
		final LocalFile newLocalFile = (LocalFile) FileObjectUtils.getAbstractFileObject(newFile);

		if (!file.renameTo(newLocalFile.getLocalFile())) {
			throw new FileSystemException("vfs.provider.local/rename-file.error", file.toString(), newFile.toString());
		}
	}

	@Override
	protected boolean doSetExecutable(final boolean executable, final boolean ownerOnly) throws Exception {
		return file.setExecutable(executable, ownerOnly);
	}

	/**
	 * Sets the last modified time of this file.
	 *
	 * @since 2.0
	 */
	@Override
	protected boolean doSetLastModifiedTime(final long modtime) throws FileSystemException {
		return file.setLastModified(modtime);
	}

	@Override
	protected boolean doSetReadable(final boolean readable, final boolean ownerOnly) throws Exception {
		return file.setReadable(readable, ownerOnly);
	}

	@Override
	protected boolean doSetWritable(final boolean writable, final boolean ownerOnly) throws Exception {
		return file.setWritable(writable, ownerOnly);
	}

	/**
	 * Gets the local file that this file object represents.
	 *
	 * @return the local file that this file object represents.
	 */
	protected File getLocalFile() {
		return file;
	}

	/**
	 * Returns the URI of the file.
	 *
	 * @return The URI of the file.
	 */
	@Override
	public String toString() {
		try {
			// VFS-325: URI may contain percent-encoded values as part of file name, so decode
			// those characters before returning
			return UriParser.decode(getName().getURI());
		} catch (final FileSystemException e) {
			return getName().getURI();
		}
	}
}
