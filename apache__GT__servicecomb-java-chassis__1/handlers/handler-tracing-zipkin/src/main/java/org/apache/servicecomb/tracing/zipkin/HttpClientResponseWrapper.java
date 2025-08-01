/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.servicecomb.tracing.zipkin;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.swagger.invocation.Response;

import brave.http.HttpClientRequest;
import brave.http.HttpClientResponse;

class HttpClientResponseWrapper extends HttpClientResponse implements InvocationAware {
	final HttpClientRequest request;

	final Response response;

	final Invocation invocation;

	HttpClientResponseWrapper(Invocation invocation, Response response, HttpClientRequestWrapper request) {
		this.response = response;
		this.request = request;
		this.invocation = invocation;
	}

	@Override
	public int statusCode() {
		return response.getStatusCode();
	}

	@Override
	public Object unwrap() {
		return response;
	}

	@Override
	public HttpClientRequest request() {
		return request;
	}

	@Override
	public Throwable error() {
		return response.isFailed() ? response.getResult() : null;
	}

	@Override
	public Invocation getInvocation() {
		return invocation;
	}
}
