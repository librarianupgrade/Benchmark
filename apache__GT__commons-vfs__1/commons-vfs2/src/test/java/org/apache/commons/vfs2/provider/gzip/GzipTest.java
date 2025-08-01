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

package org.apache.commons.vfs2.provider.gzip;

import java.io.File;
import java.io.IOException;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class GzipTest {

	@Test
	public void testCreateGzipFileSystem() throws IOException {

		final File gzFile = new File("src/test/resources/test-data/好.txt.gz");
		FileSystemManager manager = VFS.getManager();

		try (FileObject localFileObject = manager.resolveFile(gzFile.getAbsolutePath());
				FileObject gzFileObject = manager.createFileSystem(localFileObject);) {
			Assert.assertTrue(gzFileObject instanceof GzipFileObject);
		}
	}
}
