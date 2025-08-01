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
package org.apache.commons.vfs2.cache;

import static org.apache.commons.vfs2.VfsTestUtils.getTestDirectoryFile;

import org.apache.commons.vfs2.AbstractProviderTestConfig;
import org.apache.commons.vfs2.CacheTestSuite;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FilesCache;

import junit.framework.Test;

/**
 * Tests the {@link LRUFilesCache} using {@link LRUFilesCacheTests}.
 */
public class LRUFilesCacheTestCase extends AbstractProviderTestConfig {

	public static Test suite() throws Exception {
		final CacheTestSuite suite = new CacheTestSuite(new LRUFilesCacheTestCase());
		suite.addTests(LRUFilesCacheTests.class);
		return suite;
	}

	@Override
	public FilesCache createFilesCache() {
		return new LRUFilesCache(5);
	}

	@Override
	public FileObject getBaseTestFolder(final FileSystemManager manager) throws Exception {
		return manager.toFileObject(getTestDirectoryFile());
	}

}
