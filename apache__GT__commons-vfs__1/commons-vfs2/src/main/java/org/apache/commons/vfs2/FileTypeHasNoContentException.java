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

/**
 * A file-not-folder exception which happens when trying to issue
 * {@link org.apache.commons.vfs2.FileObject#getChildren()} on a file.
 *
 * @since 2.0
 */
public class FileTypeHasNoContentException extends FileSystemException {

	/**
	 * serialVersionUID format is YYYYMMDD for the date of the last binary change.
	 */
	private static final long serialVersionUID = 20101208L;

	/**
	 * Constructs exception with the specified detail message.
	 *
	 * @param info one context information.
	 */
	public FileTypeHasNoContentException(final Object info) {
		super("vfs.provider/read-not-file.error", info);
	}

	/**
	 * Constructs exception with the specified detail message and cause.
	 *
	 * @param info one context information.
	 * @param cause the cause.
	 */
	public FileTypeHasNoContentException(final Object info, final Throwable cause) {
		super("vfs.provider/read-not-file.error", info, cause);
	}
}
