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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.zip.ZipFileObject;
import org.apache.commons.vfs2.provider.zip.ZipFileSystem;

/**
 * A read-only file system for Jar files.
 */
public class JarFileSystem extends ZipFileSystem {
	private Attributes attributes;

	protected JarFileSystem(final AbstractFileName rootName, final FileObject file,
			final FileSystemOptions fileSystemOptions) throws FileSystemException {
		super(rootName, file, fileSystemOptions);
	}

	// @Override
	// protected FileObject createFile(AbstractFileName name) throws FileSystemException
	// {
	// return new JarFileObject(name, null, this, false);
	// }

	/**
	 * Returns the capabilities of this file system.
	 */
	@Override
	protected void addCapabilities(final Collection<Capability> caps) {
		// super.addCapabilities(caps);
		caps.addAll(JarFileProvider.CAPABILITIES);
	}

	@Override
	protected ZipFile createZipFile(final File file) throws FileSystemException {
		try {
			return new JarFile(file);
		} catch (final IOException ioe) {
			throw new FileSystemException("vfs.provider.jar/open-jar-file.error", file, ioe);
		}
	}

	@Override
	protected ZipFileObject createZipFileObject(final AbstractFileName name, final ZipEntry entry)
			throws FileSystemException {
		return new JarFileObject(name, entry, this, true);
	}

	Object getAttribute(final Name attrName) throws FileSystemException {
		try {
			final Attributes attr = getAttributes();
			return attr.getValue(attrName);
		} catch (final IOException ioe) {
			throw new FileSystemException(attrName.toString(), ioe);
		}
	}

	/**
	 * Retrieves the attribute with the specified name. The default implementation simply throws an exception.
	 *
	 * @param attrName The attribute's name.
	 * @return The value of the attribute.
	 * @throws FileSystemException if an error occurs.
	 */
	@Override
	public Object getAttribute(final String attrName) throws FileSystemException {
		final Name name = lookupName(attrName);
		return getAttribute(name);
	}

	Attributes getAttributes() throws IOException {
		if (attributes == null) {
			final Manifest man = ((JarFile) getZipFile()).getManifest();
			if (man == null) {
				attributes = new Attributes(1);
			} else {
				attributes = man.getMainAttributes();
				if (attributes == null) {
					attributes = new Attributes(1);
				}
			}
		}

		return attributes;
	}

	@Override
	protected ZipFile getZipFile() throws FileSystemException {
		// make accessible
		return super.getZipFile();
	}

	Name lookupName(final String attrName) {
		if (Name.CLASS_PATH.toString().equals(attrName)) {
			return Name.CLASS_PATH;
		}
		if (Name.CONTENT_TYPE.toString().equals(attrName)) {
			return Name.CONTENT_TYPE;
		}
		if (Name.EXTENSION_INSTALLATION.toString().equals(attrName)) {
			return Name.EXTENSION_INSTALLATION;
		}
		if (Name.EXTENSION_LIST.toString().equals(attrName)) {
			return Name.EXTENSION_LIST;
		}
		if (Name.EXTENSION_NAME.toString().equals(attrName)) {
			return Name.EXTENSION_NAME;
		}
		if (Name.IMPLEMENTATION_TITLE.toString().equals(attrName)) {
			return Name.IMPLEMENTATION_TITLE;
		}
		if (Name.IMPLEMENTATION_URL.toString().equals(attrName)) {
			return Name.IMPLEMENTATION_URL;
		}
		if (Name.IMPLEMENTATION_VENDOR.toString().equals(attrName)) {
			return Name.IMPLEMENTATION_VENDOR;
		}
		if (Name.IMPLEMENTATION_VENDOR_ID.toString().equals(attrName)) {
			return Name.IMPLEMENTATION_VENDOR_ID;
		}
		if (Name.IMPLEMENTATION_VERSION.toString().equals(attrName)) {
			return Name.IMPLEMENTATION_VENDOR;
		}
		if (Name.MAIN_CLASS.toString().equals(attrName)) {
			return Name.MAIN_CLASS;
		}
		if (Name.MANIFEST_VERSION.toString().equals(attrName)) {
			return Name.MANIFEST_VERSION;
		}
		if (Name.SEALED.toString().equals(attrName)) {
			return Name.SEALED;
		}
		if (Name.SIGNATURE_VERSION.toString().equals(attrName)) {
			return Name.SIGNATURE_VERSION;
		}
		if (Name.SPECIFICATION_TITLE.toString().equals(attrName)) {
			return Name.SPECIFICATION_TITLE;
		}
		if (Name.SPECIFICATION_VENDOR.toString().equals(attrName)) {
			return Name.SPECIFICATION_VENDOR;
		}
		if (Name.SPECIFICATION_VERSION.toString().equals(attrName)) {
			return Name.SPECIFICATION_VERSION;
		}
		return new Name(attrName);
	}

}
