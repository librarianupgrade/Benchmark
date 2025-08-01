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

import org.apache.commons.vfs2.FileContentInfo;

/**
 * The default file content information.
 */
public class DefaultFileContentInfo implements FileContentInfo {

	private final String contentType;
	private final String contentEncoding;

	/**
	 * Constructs a new instance.
	 *
	 * @param contentType A content type.
	 * @param contentEncoding A content encoding.
	 */
	public DefaultFileContentInfo(final String contentType, final String contentEncoding) {
		this.contentType = contentType;
		this.contentEncoding = contentEncoding;
	}

	@Override
	public String getContentEncoding() {
		return contentEncoding;
	}

	@Override
	public String getContentType() {
		return contentType;
	}
}
