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
package org.apache.commons.vfs2.provider.zip;

import static org.apache.commons.vfs2.VfsTestUtils.getTestResource;

import java.io.File;

import org.apache.commons.vfs2.AbstractProviderTestConfig;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.ProviderTestSuite;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;

import junit.framework.Test;

/**
 * Tests for the Zip file system.
 */
public class ZipProviderTestCase extends AbstractProviderTestConfig {

	/**
	 * Creates the test suite for the zip file system.
	 */
	public static Test suite() throws Exception {
		return new ProviderTestSuite(new ZipProviderTestCase(), true);
	}

	/**
	 * Returns the base folder for read tests.
	 */
	@Override
	public FileObject getBaseTestFolder(final FileSystemManager manager) throws Exception {
		final File zipFile = getTestResource("test.zip");
		final String uri = "zip:file:" + zipFile.getAbsolutePath() + "!/";
		return manager.resolveFile(uri);
	}

	/**
	 * Prepares the file system manager.
	 */
	@Override
	public void prepare(final DefaultFileSystemManager manager) throws Exception {
		manager.addProvider("zip", new ZipFileProvider());
		manager.addExtensionMap("zip", "zip");
		manager.addMimeTypeMap(MIME_TYPE_APPLICATION_ZIP, "zip");
	}

}
