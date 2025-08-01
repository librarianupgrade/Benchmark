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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.cache.OnCallRefreshFileObject;
import org.apache.commons.vfs2.function.VfsConsumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class Jira733Test {

	@AfterEach
	@BeforeEach
	public void reset() throws FileSystemException {
		VFS.reset();
	}

	@Test
	public void testZipParentLayer() throws Exception {
		final File file = new File("src/test/resources/test-data/test.zip");
		final String nestedPath = "zip:" + file.getAbsolutePath() + "!/read-tests/file1.txt";
		try (FileObject fileObject = VFS.getManager().resolveFile(nestedPath);
				final FileObject wrappedFileObject = new OnCallRefreshFileObject(fileObject)) {
			// VFS.getManager().getFilesCache().close();
			assertNotNull(wrappedFileObject.getFileSystem().getParentLayer(), "getParentLayer() 1");
			wrappedFileObject.exists();
			wrappedFileObject.getContent();
			assertNotNull(wrappedFileObject.getFileSystem().getParentLayer(), "getParentLayer() 2");
		}
	}

	private void testZipParentLayer(final VfsConsumer<FileObject> consumer) throws Exception {
		final File file = new File("src/test/resources/test-data/test.zip");
		assertTrue(file.exists());
		final String nestedPath = "zip:" + file.getAbsolutePath() + "!/read-tests/file1.txt";
		try (FileObject fileObject = VFS.getManager().resolveFile(nestedPath);
				final FileObject wrappedFileObject = new OnCallRefreshFileObject(fileObject)) {
			assertTrue(fileObject instanceof ZipFileObject);
			@SuppressWarnings({ "unused", "resource" })
			final ZipFileObject zipFileObject = (ZipFileObject) fileObject;
			assertNotNull(wrappedFileObject.getFileSystem().getParentLayer(), "getParentLayer() 1");
			consumer.accept(wrappedFileObject);
			assertNotNull(wrappedFileObject.getFileSystem().getParentLayer(), "getParentLayer() 2");
		}
	}

	@Test
	public void testZipParentLayer_exists() throws Exception {
		testZipParentLayer(FileObject::exists);
	}

	@Test
	public void testZipParentLayer_exists_getContents() throws Exception {
		testZipParentLayer(fileObject -> {
			fileObject.exists();
			fileObject.getContent();
		});
	}

	@Test
	public void testZipParentLayer_getChildren() throws Exception {
		testZipParentLayer(fileObject -> fileObject.getParent().getChildren());
	}

	@Test
	public void testZipParentLayer_getContents() throws Exception {
		testZipParentLayer(FileObject::getContent);
	}

	@Test
	public void testZipParentLayer_getType() throws Exception {
		testZipParentLayer(FileObject::getType);
	}

	@Test
	public void testZipParentLayer_isAttached() throws Exception {
		testZipParentLayer(FileObject::isAttached);
	}

	@Test
	public void testZipParentLayer_isContentOpen() throws Exception {
		testZipParentLayer(FileObject::isContentOpen);
	}

	@Test
	public void testZipParentLayer_isExecutable() throws Exception {
		testZipParentLayer(FileObject::isExecutable);
	}

	@Test
	public void testZipParentLayer_isFile() throws Exception {
		testZipParentLayer(FileObject::isFile);
	}

	@Test
	public void testZipParentLayer_isFolder() throws Exception {
		testZipParentLayer(FileObject::isFolder);
	}

	@Test
	public void testZipParentLayer_isHidden() throws Exception {
		testZipParentLayer(FileObject::isHidden);
	}

	@Test
	public void testZipParentLayer_isReadable() throws Exception {
		testZipParentLayer(FileObject::isReadable);
	}

	@Test
	public void testZipParentLayer_isWriteable() throws Exception {
		testZipParentLayer(FileObject::isWriteable);
	}

}
