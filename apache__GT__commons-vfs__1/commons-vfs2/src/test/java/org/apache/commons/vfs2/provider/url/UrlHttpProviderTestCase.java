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

import static org.apache.commons.vfs2.VfsTestUtils.getTestDirectoryFile;

import java.io.File;

import org.apache.commons.vfs2.AbstractProviderTestConfig;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.ProviderTestSuite;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;

import junit.framework.Test;

/**
 * Test cases for the generic provider.
 */
public class UrlHttpProviderTestCase extends AbstractProviderTestConfig {

	public static Test suite() throws Exception {
		return new ProviderTestSuite(new UrlHttpProviderTestCase());
	}

	/**
	 * Returns the base folder for tests.
	 */
	@Override
	public FileObject getBaseTestFolder(final FileSystemManager manager) throws Exception {
		final File baseDir = getTestDirectoryFile();
		return manager.resolveFile(baseDir.toURI().toURL().toExternalForm());
	}

	/**
	 * Prepares the file system manager. This implementation does nothing.
	 */
	@Override
	public void prepare(final DefaultFileSystemManager manager) throws Exception {
		manager.addProvider("http", new UrlFileProvider());
	}

}
