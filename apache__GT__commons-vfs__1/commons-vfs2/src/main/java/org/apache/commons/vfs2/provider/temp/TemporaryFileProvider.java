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
package org.apache.commons.vfs2.provider.temp;

import java.io.File;
import java.util.Collection;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileProvider;
import org.apache.commons.vfs2.provider.UriParser;
import org.apache.commons.vfs2.provider.local.DefaultLocalFileProvider;
import org.apache.commons.vfs2.provider.local.LocalFileSystem;

/**
 * A provider for temporary files.
 */
public class TemporaryFileProvider extends AbstractFileProvider implements Comparable<Object> {

	private File rootFile;

	/*
	 * private final static FileName tmpFileName = new AbstractFileName("tmp", "/") { protected FileName
	 * createName(String absPath) { return null; }
	 *
	 * protected void appendRootUri(StringBuffer buffer) { } };
	 */

	/**
	 * Constructs a new instance.
	 */
	public TemporaryFileProvider() {
	}

	/**
	 * Constructs a new instance.
	 *
	 * @param rootFile The root file.
	 */
	public TemporaryFileProvider(final File rootFile) {
		this();
		this.rootFile = rootFile;
	}

	@Override
	public int compareTo(final Object o) {
		final int h1 = hashCode();
		final int h2 = o.hashCode();
		return Integer.compare(h1, h2);
	}

	/**
	 * Locates a file object, by absolute URI.
	 *
	 * @param baseFile The base FileObject.
	 * @param uri The URI of the file to be located.
	 * @param fileSystemOptions FileSystemOptions to use to locate or create the file.
	 * @return The FileObject.
	 * @throws FileSystemException if an error occurs.
	 */
	@Override
	public synchronized FileObject findFile(final FileObject baseFile, final String uri,
			final FileSystemOptions fileSystemOptions) throws FileSystemException {
		// Parse the name
		final StringBuilder buffer = new StringBuilder(uri);
		final String scheme = UriParser.extractScheme(getContext().getFileSystemManager().getSchemes(), uri, buffer);
		UriParser.fixSeparators(buffer);
		UriParser.normalisePath(buffer);
		final String path = buffer.toString();

		// Create the temp file system if it does not exist
		// FileSystem filesystem = findFileSystem( this, (Properties) null);
		FileSystem filesystem = findFileSystem(this, fileSystemOptions);
		if (filesystem == null) {
			if (rootFile == null) {
				rootFile = getContext().getTemporaryFileStore().allocateFile("tempfs");
			}
			final FileName rootName = getContext().parseURI(scheme + ":" + FileName.ROOT_PATH);
			// final FileName rootName =
			// new LocalFileName(scheme, scheme + ":", FileName.ROOT_PATH);
			filesystem = new LocalFileSystem(rootName, rootFile.getAbsolutePath(), fileSystemOptions);
			addFileSystem(this, filesystem);
		}

		// Find the file
		return filesystem.resolveFile(path);
	}

	@Override
	public Collection<Capability> getCapabilities() {
		return DefaultLocalFileProvider.capabilities;
	}
}
