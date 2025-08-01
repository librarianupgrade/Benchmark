/*
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */

package org.apache.directory.api.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for FileUtils.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class FileUtilsTest {
	private Path tmpFolder;

	@BeforeEach
	public void init() throws IOException {
		tmpFolder = Files.createTempDirectory(FileUtilsTest.class.getSimpleName());
	}

	@AfterEach
	public void cleanup() {
		FileUtils.deleteQuietly(tmpFolder.toFile());
	}

	@Test
	public void testOpenOutputStreamAppendToNonExistingFileInNonExistingFolder() throws Exception {
		File nonExistingFile = new File(tmpFolder.toFile(),
				"testOpenOutputStreamAppendToNonExistingFileInNonExistingFolder/testOpenOutputStreamAppendToNonExistingFileInNonExistingFolder");

		OutputStream os1 = FileUtils.openOutputStream(nonExistingFile, true);
		os1.write('a');
		os1.write('b');
		os1.write('c');
		os1.close();

		OutputStream os2 = FileUtils.openOutputStream(nonExistingFile, true);
		os2.write('x');
		os2.write('y');
		os2.write('z');
		os2.close();

		String content = FileUtils.readFileToString(nonExistingFile, StandardCharsets.UTF_8);

		assertEquals("abcxyz", content);
	}

	@Test
	public void testOpenOutputStreamAppendToExistingFile() throws Exception {
		File existingFile = new File(tmpFolder.toFile(), "testOpenOutputStreamAppendToExistingFile");
		FileUtils.writeStringToFile(existingFile, "abc", StandardCharsets.UTF_8, false);

		OutputStream os = FileUtils.openOutputStream(existingFile, true);
		os.write('x');
		os.write('y');
		os.write('z');
		os.close();

		String content = FileUtils.readFileToString(existingFile, StandardCharsets.UTF_8);

		assertEquals("abcxyz", content);
	}

	@Test
	public void testOpenOutputStreamNotAppendToNonExistingFile() throws Exception {
		File nonExistingFile = new File(tmpFolder.toFile(),
				"testOpenOutputStreamNotAppendToNonExistingFile/testOpenOutputStreamNotAppendToNonExistingFile");

		OutputStream os1 = FileUtils.openOutputStream(nonExistingFile, false);
		os1.write('a');
		os1.write('b');
		os1.write('c');
		os1.close();

		OutputStream os2 = FileUtils.openOutputStream(nonExistingFile, false);
		os2.write('x');
		os2.write('y');
		os2.write('z');
		os2.close();

		String content = FileUtils.readFileToString(nonExistingFile, StandardCharsets.UTF_8);

		assertEquals("xyz", content);
	}

	@Test
	public void testOpenOutputStreamNotAppendToExistingFile() throws Exception {
		File existingFile = new File(tmpFolder.toFile(), "testOpenOutputStreamNotAppendToExistingFile");
		FileUtils.writeStringToFile(existingFile, "abc", StandardCharsets.UTF_8, false);

		OutputStream os = FileUtils.openOutputStream(existingFile, false);
		os.write('x');
		os.write('y');
		os.write('z');
		os.close();

		String content = FileUtils.readFileToString(existingFile, StandardCharsets.UTF_8);

		assertEquals("xyz", content);
	}
}
