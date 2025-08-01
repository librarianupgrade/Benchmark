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

import static org.apache.commons.vfs2.VfsTestUtils.getTestDirectoryFile;

import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.vfs2.AbstractProviderTestConfig;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.PermissionsTests;
import org.apache.commons.vfs2.ProviderTestSuite;

import junit.framework.Test;

/**
 * Tests for the local file system.
 */
public class LocalProviderTestCase extends AbstractProviderTestConfig {

	/**
	 * Creates the test suite for the local file system.
	 */
	public static Test suite() throws Exception {
		final ProviderTestSuite testSuite = new ProviderTestSuite(new LocalProviderTestCase());

		testSuite.addTests(FileNameTests.class);
		// testSuite.addTests(TempFileTests.class);
		// VFS-325
		testSuite.addTests(UrlTests.class);
		testSuite.addTests(PermissionsTests.class);

		if (SystemUtils.IS_OS_WINDOWS) {
			testSuite.addTests(WindowsFileNameTests.class);
		}

		return testSuite;
	}

	/**
	 * Returns the base folder for tests.
	 */
	@Override
	public FileObject getBaseTestFolder(final FileSystemManager manager) throws Exception {
		return manager.toFileObject(getTestDirectoryFile());
	}

}
