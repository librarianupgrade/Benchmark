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
package org.apache.commons.vfs2.provider.url;

import java.util.Collection;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileSystem;

/**
 * A File system backed by Java's URL API.
 */
public class UrlFileSystem extends AbstractFileSystem {

	protected UrlFileSystem(final FileName rootName, final FileSystemOptions fileSystemOptions) {
		super(rootName, null, fileSystemOptions);
	}

	/**
	 * Returns the capabilities of this file system.
	 */
	@Override
	protected void addCapabilities(final Collection<Capability> caps) {
		caps.addAll(UrlFileProvider.capabilities);
	}

	/**
	 * Creates a file object.
	 */
	@Override
	protected FileObject createFile(final AbstractFileName name) {
		return new UrlFileObject(this, name);
	}
}
