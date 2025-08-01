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
package org.apache.commons.vfs2.impl;

import java.net.URLConnection;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileContentInfo;
import org.apache.commons.vfs2.FileContentInfoFactory;

/**
 * The FileContentInfoFilenameFactory.
 * <p>
 * Uses the file name extension to determine the content-type. The content-encoding is not resolved.
 * </p>
 */
public class FileContentInfoFilenameFactory implements FileContentInfoFactory {

	private static final FileContentInfo NULL_INSTANCE = new DefaultFileContentInfo(null, null);

	@Override
	public FileContentInfo create(final FileContent fileContent) {
		String contentType = null;

		final String name = fileContent.getFile().getName().getBaseName();
		if (name != null) {
			contentType = URLConnection.getFileNameMap().getContentTypeFor(name);
		}

		// optimize object creation for common case
		if (contentType == null) {
			return NULL_INSTANCE;
		}
		return new DefaultFileContentInfo(contentType, null);
	}
}
