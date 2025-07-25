/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.milton.servlet;

import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PartWrapper implements io.milton.http.FileItem {

	final Part wrapped;

	final String name;

	private Map<String, String> mapOfHeaders;

	/**
	 * strip path information provided by IE
	 *
	 * @param s
	 * @return
	 */
	public static String fixIEFileName(String s) {
		if (s.contains("\\")) {
			int pos = s.lastIndexOf('\\');
			s = s.substring(pos + 1);
		}
		return s;
	}

	public PartWrapper(Part wrapped) {
		this.wrapped = wrapped;
		name = fixIEFileName(wrapped.getName());
	}

	@Override
	public String getContentType() {
		return wrapped.getContentType();
	}

	@Override
	public String getFieldName() {
		return wrapped.getName();
	}

	@Override
	public InputStream getInputStream() {
		try {
			return wrapped.getInputStream();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public OutputStream getOutputStream() {
		throw new UnsupportedOperationException("Unsupported operation");
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public long getSize() {
		return wrapped.getSize();
	}

	@Override
	public Map<String, String> getHeaders() {
		if (mapOfHeaders == null) {
			mapOfHeaders = new HashMap<>();
			if (wrapped.getHeaderNames() != null) {
				Collection<String> headers = wrapped.getHeaderNames();
				for (String headerName : headers) {
					String s = wrapped.getHeader(headerName);
					mapOfHeaders.put(headerName, s);
				}
			}
		}
		return mapOfHeaders;
	}
}
