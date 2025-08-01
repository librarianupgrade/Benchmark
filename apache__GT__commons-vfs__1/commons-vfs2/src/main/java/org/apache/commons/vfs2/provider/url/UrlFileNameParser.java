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

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.provider.AbstractFileNameParser;
import org.apache.commons.vfs2.provider.URLFileName;
import org.apache.commons.vfs2.provider.URLFileNameParser;
import org.apache.commons.vfs2.provider.VfsComponentContext;
import org.apache.commons.vfs2.provider.local.GenericFileNameParser;

/**
 * Implementation for any java.net.url based file system.
 * <p>
 * Composite of URLFilenameParser and GenericFilenameParser
 */
public class UrlFileNameParser extends AbstractFileNameParser {

	private final URLFileNameParser urlFileNameParser = new URLFileNameParser(80);
	private final GenericFileNameParser genericFileNameParser = new GenericFileNameParser();

	/**
	 * Constructs a new instance.
	 */
	public UrlFileNameParser() {
	}

	/**
	 * This method counts the slashes after the scheme.
	 *
	 * @param fileName The file name.
	 * @return number of slashes
	 */
	protected int countSlashes(final String fileName) {
		int state = 0;
		int nuofSlash = 0;
		for (int pos = 0; pos < fileName.length(); pos++) {
			final char c = fileName.charAt(pos);
			if (state == 0) {
				if (c >= 'a' && c <= 'z') {
					continue;
				}
				if (c == ':') {
					state++;
					continue;
				}
			} else if (state == 1) {
				if (c != '/') {
					return nuofSlash;
				}
				nuofSlash++;
			}
		}
		return nuofSlash;
	}

	@Override
	public boolean encodeCharacter(final char ch) {
		return super.encodeCharacter(ch) || ch == '?';
	}

	/**
	 * Guess if the given file name is a URL with host or not.
	 * <p>
	 * VFS treats such URLs differently.
	 * </p>
	 * <p>
	 * A file name is URL-based if the base is a {@code URLFileName} or there are only 2 slashes after the scheme. e.g:
	 * {@code http://host/path}, {@code file:/path/to/file}, {@code file:///path/to/file}.
	 * </p>
	 *
	 * @param base The file name is relative to this base.
	 * @param fileName The file name.
	 * @return true if file name contains two slashes or base was URLFileName.
	 */
	protected boolean isUrlBased(final FileName base, final String fileName) {
		if (base instanceof URLFileName) {
			return true;
		}

		return countSlashes(fileName) == 2;
	}

	/**
	 * Parse a URI.
	 *
	 * @param context The component context.
	 * @param base The base FileName.
	 * @param uri The target file name.
	 * @return The FileName.
	 * @throws FileSystemException if an error occurs
	 */
	@Override
	public FileName parseUri(final VfsComponentContext context, final FileName base, final String uri)
			throws FileSystemException {
		if (isUrlBased(base, uri)) {
			return urlFileNameParser.parseUri(context, base, uri);
		}
		return genericFileNameParser.parseUri(context, base, uri);
	}
}
