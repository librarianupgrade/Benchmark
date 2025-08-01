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
package org.apache.commons.vfs2.provider;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileType;

/**
 * A file name for layered files.
 */
public class LayeredFileName extends AbstractFileName {

	/**
	 * The layer separator character '{@value #LAYER_SEPARATOR}'.
	 *
	 * @since 2.10.0
	 */
	public static final char LAYER_SEPARATOR = '!';

	static final char SCHEME_SEPARATOR = ':';

	private final FileName outerUri;

	/**
	 * Constructs a new instance.
	 *
	 * @param scheme The scheme.
	 * @param outerUri outer file name.
	 * @param path the absolute path, maybe empty or null.
	 * @param type the file type.
	 */
	public LayeredFileName(final String scheme, final FileName outerUri, final String path, final FileType type) {
		super(scheme, path, type);
		this.outerUri = outerUri;
	}

	@Override
	protected void appendRootUri(final StringBuilder buffer, final boolean addPassword) {
		buffer.append(getScheme());
		buffer.append(SCHEME_SEPARATOR);
		buffer.append(getOuterName().getURI());
		buffer.append(LAYER_SEPARATOR);
	}

	/**
	 * Creates a FileName.
	 *
	 * @param path The file URI.
	 * @param type The FileType.
	 * @return The FileName.
	 */
	@Override
	public FileName createName(final String path, final FileType type) {
		return new LayeredFileName(getScheme(), getOuterName(), path, type);
	}

	/**
	 * Returns the URI of the outer file.
	 *
	 * @return The FileName.
	 */
	public FileName getOuterName() {
		return outerUri;
	}
}
