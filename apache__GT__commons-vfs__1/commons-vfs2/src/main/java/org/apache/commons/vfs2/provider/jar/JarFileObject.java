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
package org.apache.commons.vfs2.provider.jar;

import java.io.IOException;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.zip.ZipFileObject;

/**
 * A file in a Jar file system.
 */
public class JarFileObject extends ZipFileObject {

	private final JarFileSystem fs;

	private Attributes attributes;

	protected JarFileObject(final AbstractFileName name, final ZipEntry entry, final JarFileSystem fs,
			final boolean zipExists) throws FileSystemException {
		super(name, entry, fs, zipExists);
		if (entry != null) {
			// For Java 9 and up: Force the certificates to be read and cached now. This avoids an
			// IllegalStateException in java.util.jar.JarFile.isMultiRelease() when it tries
			// to read the certificates and the file is closed.
			((JarEntry) entry).getCertificates();
		}
		this.fs = fs;

		try {
			getAttributes(); // early get the attributes as the zip file might be closed
		} catch (final IOException e) {
			throw new FileSystemException(e);
		}
	}

	/**
	 * Adds the source attributes to the destination map.
	 */
	private void addAll(final Attributes src, final Map<String, Object> dest) {
		src.forEach((k, v) -> dest.put(String.valueOf(k), v));
	}

	/**
	 * Returns the value of an attribute.
	 */
	@Override
	protected Map<String, Object> doGetAttributes() throws Exception {
		final Map<String, Object> attrs = new HashMap<>();

		// Add the file system's attributes first
		final JarFileSystem fs = (JarFileSystem) getFileSystem();
		addAll(fs.getAttributes(), attrs);

		// Add this file's attributes
		addAll(getAttributes(), attrs);

		return attrs;
	}

	/**
	 * Return the certificates of this JarEntry.
	 */
	@Override
	protected Certificate[] doGetCertificates() {
		if (entry == null) {
			return null;
		}

		return ((JarEntry) entry).getCertificates();
	}

	/**
	 * Returns the attributes of this file.
	 */
	Attributes getAttributes() throws IOException {
		if (attributes == null) {
			if (entry == null) {
				attributes = new Attributes(1);
			} else {
				attributes = ((JarEntry) entry).getAttributes();
				if (attributes == null) {
					attributes = new Attributes(1);
				}
			}
		}

		return attributes;
	}

	/**
	 * Returns the Jar manifest.
	 */
	Manifest getManifest() throws IOException {
		if (fs.getZipFile() == null) {
			return null;
		}

		return ((JarFile) fs.getZipFile()).getManifest();
	}
}
