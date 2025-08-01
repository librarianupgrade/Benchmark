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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractOriginatingFileProvider;
import org.apache.commons.vfs2.provider.LocalFileProvider;
import org.apache.commons.vfs2.provider.UriParser;

/**
 * A file system provider, which uses direct file access.
 */
public class DefaultLocalFileProvider extends AbstractOriginatingFileProvider implements LocalFileProvider {

	/** The provider's capabilities. */
	public static final Collection<Capability> capabilities = Collections.unmodifiableCollection(Arrays.asList(
			Capability.CREATE, Capability.DELETE, Capability.RENAME, Capability.GET_TYPE, Capability.GET_LAST_MODIFIED,
			Capability.SET_LAST_MODIFIED_FILE, Capability.SET_LAST_MODIFIED_FOLDER, Capability.LIST_CHILDREN,
			Capability.READ_CONTENT, Capability.URI, Capability.WRITE_CONTENT, Capability.APPEND_CONTENT,
			Capability.RANDOM_ACCESS_READ, Capability.RANDOM_ACCESS_SET_LENGTH, Capability.RANDOM_ACCESS_WRITE));

	/**
	 * Constructs a new provider.
	 */
	public DefaultLocalFileProvider() {
		if (SystemUtils.IS_OS_WINDOWS) {
			setFileNameParser(new WindowsFileNameParser());
		} else {
			setFileNameParser(new GenericFileNameParser());
		}
	}

	/**
	 * Creates the file system.
	 */
	@Override
	protected FileSystem doCreateFileSystem(final FileName name, final FileSystemOptions fileSystemOptions)
			throws FileSystemException {
		// Create the file system
		final LocalFileName rootName = (LocalFileName) name;
		return new LocalFileSystem(rootName, rootName.getRootFile(), fileSystemOptions);
	}

	/**
	 * Finds a local file.
	 *
	 * @param file The File to locate.
	 * @return the located FileObject.
	 * @throws FileSystemException if an error occurs.
	 */
	@Override
	public FileObject findLocalFile(final File file) throws FileSystemException {
		return findLocalFile(UriParser.encode(file.getAbsolutePath()));
		// return findLocalFile(file.getAbsolutePath());
	}

	/**
	 * Finds a local file, from its local name.
	 *
	 * @param name The name of the file to locate.
	 * @return the located FileObject.
	 * @throws FileSystemException if an error occurs.
	 */
	@Override
	public FileObject findLocalFile(final String name) throws FileSystemException {
		final String scheme = "file:";
		final StringBuilder uri = new StringBuilder(name.length() + scheme.length());
		uri.append(scheme);
		uri.append(name);
		final FileName fileName = parseUri(null, uri.toString());
		return findFile(fileName, null);
	}

	@Override
	public Collection<Capability> getCapabilities() {
		return capabilities;
	}

	/**
	 * Determines if a name is an absolute file name.
	 *
	 * @param name The file name.
	 * @return true if the name is absolute, false otherwise.
	 */
	@Override
	public boolean isAbsoluteLocalName(final String name) {
		return ((LocalFileNameParser) getFileNameParser()).isAbsoluteName(name);
	}
}
