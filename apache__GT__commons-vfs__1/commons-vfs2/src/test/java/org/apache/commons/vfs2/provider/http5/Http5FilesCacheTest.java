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
package org.apache.commons.vfs2.provider.http5;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.junit.jupiter.api.Test;

/**
 * Tests https://issues.apache.org/jira/browse/VFS-426.
 */
public class Http5FilesCacheTest {

	/**
	 * Tests https://issues.apache.org/jira/browse/VFS-426
	 */
	@Test
	public void testQueryStringUrls() throws FileSystemException {
		final String noQueryStringUrl = "http5://commons.apache.org/vfs";
		final String queryStringUrl = "http5://commons.apache.org/vfs?query=string";
		final String queryStringUrl2 = "http5://commons.apache.org/vfs?query=string&more=stuff";

		final FileSystemManager fileSystemManager = VFS.getManager();

		final FileObject noQueryFile = fileSystemManager.resolveFile(noQueryStringUrl);
		assertEquals(noQueryStringUrl, noQueryFile.getURL().toExternalForm());

		final FileObject queryFile = fileSystemManager.resolveFile(queryStringUrl);
		assertEquals(queryStringUrl, queryFile.getURL().toExternalForm()); // failed for VFS-426

		final FileObject queryFile2 = fileSystemManager.resolveFile(queryStringUrl2);
		assertEquals(queryStringUrl2, queryFile2.getURL().toExternalForm()); // failed for VFS-426
	}

}
