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
package org.apache.commons.vfs2;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Configures file systems individually with these options.
 * <p>
 * To configure a file system, you set properties on a {@link FileSystemOptions} object. Most file systems provide a
 * {@link FileSystemConfigBuilder} with specific options for that file system.
 * </p>
 * <p>
 * To use the options, pass them to {@link FileSystemManager#resolveFile(String,FileSystemOptions)}. From there, the
 * options apply to all files that are resolved relative to that file.
 * </p>
 *
 * @see org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder
 * @see org.apache.commons.vfs2.provider.ftp.FtpFileSystemConfigBuilder
 * @see org.apache.commons.vfs2.provider.ftps.FtpsFileSystemConfigBuilder
 * @see org.apache.commons.vfs2.provider.hdfs.HdfsFileSystemConfigBuilder
 * @see org.apache.commons.vfs2.provider.http.HttpFileSystemConfigBuilder
 * @see org.apache.commons.vfs2.provider.ram.RamFileSystemConfigBuilder
 * @see org.apache.commons.vfs2.provider.res.ResourceFileSystemConfigBuilder
 * @see org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder
 * @see org.apache.commons.vfs2.provider.zip.ZipFileSystemConfigBuilder
 *
 */
public final class FileSystemOptions implements Cloneable, Comparable<FileSystemOptions> {

	/**
	 * Keys in the options Map.
	 */
	private static final class FileSystemOptionKey implements Comparable<FileSystemOptionKey> {
		/** Constant used to create hashcode */
		private static final int HASH = 29;

		/** The FileSystem class */
		private final Class<? extends FileSystem> fileSystemClass;

		/** The option name */
		private final String name;

		// TODO: the parameter name suggests that the class should only be a
		// a FileSystem, however some of the tests pass in DefaultFileSystemConfigBuilder
		private FileSystemOptionKey(final Class<? extends FileSystem> fileSystemClass, final String name) {
			this.fileSystemClass = fileSystemClass;
			this.name = name;
		}

		@Override
		public int compareTo(final FileSystemOptionKey o) {
			final int ret = fileSystemClass.getName().compareTo(o.fileSystemClass.getName());
			if (ret != 0) {
				return ret;
			}
			return name.compareTo(o.name);
		}

		@Override
		public boolean equals(final Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			final FileSystemOptionKey that = (FileSystemOptionKey) o;

			if (!fileSystemClass.equals(that.fileSystemClass)) {
				return false;
			}
			return name.equals(that.name);
		}

		@Override
		public int hashCode() {
			int result;
			result = fileSystemClass.hashCode();
			result = HASH * result + name.hashCode();
			return result;
		}

		@Override
		public String toString() {
			return fileSystemClass.getName() + "." + name;
		}
	}

	/** The options */
	private final Map<FileSystemOptionKey, Object> options;

	/**
	 * Constructs a new instance.
	 */
	public FileSystemOptions() {
		this(new TreeMap<>());
	}

	protected FileSystemOptions(final Map<FileSystemOptionKey, Object> options) {
		this.options = options;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 2.0
	 */
	@Override
	public Object clone() {
		return new FileSystemOptions(new TreeMap<>(options));
	}

	@Override
	public int compareTo(final FileSystemOptions other) {
		if (this == other) {
			// the same instance
			return 0;
		}

		final int propsSz = options == null ? 0 : options.size();
		final int propsFkSz = other.options == null ? 0 : other.options.size();
		if (propsSz < propsFkSz) {
			return -1;
		}
		if (propsSz > propsFkSz) {
			return 1;
		}
		if (propsSz == 0) {
			// props empty
			return 0;
		}

		// ensure proper sequence of options
		final SortedMap<FileSystemOptionKey, Object> myOptions = options instanceof SortedMap
				? (SortedMap<FileSystemOptionKey, Object>) options
				: new TreeMap<>(options);
		final SortedMap<FileSystemOptionKey, Object> theirOptions = other.options instanceof SortedMap
				? (SortedMap<FileSystemOptionKey, Object>) other.options
				: new TreeMap<>(other.options);
		final Iterator<FileSystemOptionKey> optKeysIter = myOptions.keySet().iterator();
		final Iterator<FileSystemOptionKey> otherKeysIter = theirOptions.keySet().iterator();
		while (optKeysIter.hasNext()) {
			final int comp = optKeysIter.next().compareTo(otherKeysIter.next());
			if (comp != 0) {
				return comp;
			}
		}

		final int hash = Arrays.deepHashCode(myOptions.values().toArray());
		final int hashFk = Arrays.deepHashCode(theirOptions.values().toArray());
		return Integer.compare(hash, hashFk);

		// TODO: compare Entry by Entry ??
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final FileSystemOptions other = (FileSystemOptions) obj;
		return compareTo(other) == 0;
	}

	<T> T getOption(final Class<? extends FileSystem> fileSystemClass, final String name) {
		final FileSystemOptionKey key = new FileSystemOptionKey(fileSystemClass, name);
		return (T) options.get(key);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		if (options == null) {
			result = prime * result;
		} else {
			final SortedMap<FileSystemOptionKey, Object> myOptions = options instanceof SortedMap
					? (SortedMap<FileSystemOptionKey, Object>) options
					: new TreeMap<>(options);
			result = prime * result + myOptions.keySet().hashCode();
			result = prime * result + Arrays.deepHashCode(myOptions.values().toArray());
		}
		return result;
	}

	boolean hasOption(final Class<? extends FileSystem> fileSystemClass, final String name) {
		final FileSystemOptionKey key = new FileSystemOptionKey(fileSystemClass, name);
		return options.containsKey(key);
	}

	void setOption(final Class<? extends FileSystem> fileSystemClass, final String name, final Object value) {
		options.put(new FileSystemOptionKey(fileSystemClass, name), value);
	}

	int size() {
		return options.size();
	}

	@Override
	public String toString() {
		return options.toString();
	}
}
