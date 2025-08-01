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

package org.apache.servicecomb.transport.rest.client;

import io.vertx.core.http.HttpVersion;

public class Http2TransportHttpClientOptionsSPI extends HttpTransportHttpClientOptionsSPI {
	public static final String CLIENT_NAME = "http2-transport-client";

	@Override
	public String clientName() {
		return CLIENT_NAME;
	}

	@Override
	public int getOrder() {
		return super.getOrder() + 1;
	}

	@Override
	public boolean enabled() {
		return TransportClientConfig.isHttp2TransportClientEnabled();
	}

	@Override
	public HttpVersion getHttpVersion() {
		return HttpVersion.HTTP_2;
	}

	@Override
	public String getWorkerPoolName() {
		return "pool-worker-transport-client-http2";
	}

	@Override
	public boolean isUseAlpn() {
		return TransportClientConfig.getUseAlpn();
	}

	@Override
	public int getHttp2MultiplexingLimit() {
		return TransportClientConfig.getHttp2MultiplexingLimit();
	}

	@Override
	public int getHttp2MaxPoolSize() {
		return TransportClientConfig.getHttp2ConnectionMaxPoolSize();
	}

	@Override
	public int getIdleTimeoutInSeconds() {
		return TransportClientConfig.getHttp2ConnectionIdleTimeoutInSeconds();
	}

	@Override
	public int getKeepAliveTimeout() {
		return TransportClientConfig.getHttp2ConnectionKeepAliveTimeoutInSeconds();
	}
}
