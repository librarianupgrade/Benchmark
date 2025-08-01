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
package io.milton.httpclient;

import io.milton.http.values.Pair;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author brad
 */
public class HttpResult {
	private final int statusCode;
	private final List<Pair<String, String>> allHeaders;

	private Map<String, String> headers;

	public HttpResult(int statusCode, List<Pair<String, String>> allHeaders) {
		this.statusCode = statusCode;
		this.allHeaders = allHeaders;
	}

	public Map<String, String> getHeaders() {
		if (headers == null) {
			headers = new LinkedHashMap<>();
			for (Pair<String, String> p : allHeaders) {
				headers.put(p.getObject1(), p.getObject2());
			}
		}
		return headers;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public List<String> getHeaderValues(String name) {
		List<String> list = new ArrayList<>();
		for (Pair<String, String> h : allHeaders) {
			if (h.getObject1().equals(name)) {
				list.add(h.getObject2());
			}
		}
		return list;
	}
}
