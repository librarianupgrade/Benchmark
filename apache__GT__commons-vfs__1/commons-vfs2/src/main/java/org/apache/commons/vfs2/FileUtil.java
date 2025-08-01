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

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.vfs2.util.FileObjectUtils;

/**
 * Utility methods for dealing with FileObjects.
 *
 * @deprecated Use {@link org.apache.commons.vfs2.util.FileObjectUtils}.
 */
@Deprecated
public final class FileUtil {

	/**
	 * No instances.
	 */
	private FileUtil() {
		// empty
	}

	/**
	 * Copies the content from a source file to a destination file.
	 *
	 * @param srcFile The source FileObject.
	 * @param destFile The target FileObject
	 * @throws IOException If an error occurs copying the file.
	 * @see FileContent#write(FileContent)
	 * @see FileContent#write(FileObject)
	 * @deprecated Use {@link org.apache.commons.vfs2.util.FileObjectUtils#writeContent(FileObject, FileObject)}.
	 */
	@Deprecated
	public static void copyContent(final FileObject srcFile, final FileObject destFile) throws IOException {
		FileObjectUtils.writeContent(srcFile, destFile);
	}

	/**
	 * Returns the content of a file, as a byte array.
	 *
	 * @param file The file to get the content of.
	 * @return The content as a byte array.
	 * @throws IOException if the file content cannot be accessed.
	 * @deprecated Use {@link org.apache.commons.vfs2.util.FileObjectUtils#getContentAsByteArray(FileObject)}.
	 */
	@Deprecated
	public static byte[] getContent(final FileObject file) throws IOException {
		return FileObjectUtils.getContentAsByteArray(file);
	}

	/**
	 * Writes the content of a file to an OutputStream.
	 *
	 * @param file The FileObject to write.
	 * @param output The OutputStream to write to.
	 * @throws IOException if an error occurs writing the file.
	 * @see FileContent#write(OutputStream)
	 * @deprecated Use {@link org.apache.commons.vfs2.util.FileObjectUtils#writeContent(FileObject, OutputStream)}.
	 */
	@Deprecated
	public static void writeContent(final FileObject file, final OutputStream output) throws IOException {
		FileObjectUtils.writeContent(file, output);
	}

}
