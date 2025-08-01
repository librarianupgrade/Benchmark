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
package org.apache.commons.vfs2;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.Serializable;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;

/**
 * Sanity check that a custom enum can be properly serialized and deserialized on a give JRE.
 *
 * @since 2.1
 */
public class FileTypeTest {

	private static class Fixture implements Serializable {
		private static final long serialVersionUID = 1L;
		private final FileType fileType = FileType.FILE;

		public FileType getFileType() {
			return this.fileType;
		}

	}

	private void assertFileTypeEquals(final FileType expected, final FileType actualFileType) {
		assertEquals(expected.getName(), actualFileType.getName());
		assertEquals(expected.hasAttributes(), actualFileType.hasAttributes());
		assertEquals(expected.hasChildren(), actualFileType.hasChildren());
		assertEquals(expected.hasContent(), actualFileType.hasContent());
	}

	private void test(final FileType expected) {
		final byte[] serialized = SerializationUtils.serialize(expected);
		final FileType actualFileType = SerializationUtils.deserialize(serialized);
		assertFileTypeEquals(expected, actualFileType);
	}

	@Test
	public void testSerializationContainer() {
		final Fixture expectedFixture = new Fixture();
		final byte[] serialized = SerializationUtils.serialize(expectedFixture);
		final Fixture actualFixture = SerializationUtils.deserialize(serialized);
		assertFileTypeEquals(expectedFixture.getFileType(), actualFixture.getFileType());
	}

	@Test
	public void testSerializationFile() {
		test(FileType.FILE);
	}

	@Test
	public void testSerializationFileOrFolder() {
		test(FileType.FILE_OR_FOLDER);
	}

	@Test
	public void testSerializationFolder() {
		test(FileType.FOLDER);
	}

	@Test
	public void testSerializationImaginary() {
		test(FileType.IMAGINARY);
	}

}
