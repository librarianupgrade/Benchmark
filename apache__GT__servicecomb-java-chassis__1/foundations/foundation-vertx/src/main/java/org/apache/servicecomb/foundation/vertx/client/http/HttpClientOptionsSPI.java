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

package org.apache.servicecomb.foundation.vertx.client.http;

import org.apache.servicecomb.foundation.common.encrypt.Encryptions;
import org.apache.servicecomb.foundation.vertx.VertxTLSBuilder;

import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.http.WebSocketClientOptions;
import io.vertx.core.net.ClientOptionsBase;
import io.vertx.core.net.ProxyOptions;

/**
 * common Http Client Options must be set by implementations
 */
public interface HttpClientOptionsSPI {
	/* unique name in this service */
	String clientName();

	/* loading order */
	int getOrder();

	/* can turn off the client */
	boolean enabled();

	/* config tag is used for group configurations, like ssl, address resolver, etc. set config tag to distinguish
	 *  other clients configuration or read the common configuration. */
	String getConfigTag();

	/*****************  vert.x common settings ***************************/
	int getEventLoopPoolSize();

	boolean useSharedVertx();

	/*****************  vert.x vertical common settings ***************************/
	int getInstanceCount();

	boolean isWorker();

	String getWorkerPoolName();

	int getWorkerPoolSize();

	/*****************  http common settings ***************************/
	HttpVersion getHttpVersion();

	int getConnectTimeoutInMillis();

	int getIdleTimeoutInSeconds();

	boolean isTryUseCompression();

	int getMaxWaitQueueSize();

	int getMaxPoolSize();

	boolean isKeepAlive();

	int getMaxHeaderSize();

	int getKeepAliveTimeout();

	boolean enableLogActivity();

	/***************** http 2 settings ****************************/
	int getHttp2MultiplexingLimit();

	int getHttp2MaxPoolSize();

	boolean isUseAlpn();

	/*****************  proxy settings ***************************/
	boolean isProxyEnable();

	String getProxyHost();

	int getProxyPort();

	String getProxyUsername();

	String getProxyPassword();

	/*****************  ssl settings ***************************/
	boolean isSsl();

	static void buildClientOptionsBase(HttpClientOptionsSPI spi, ClientOptionsBase httpClientOptions) {
		httpClientOptions.setConnectTimeout(spi.getConnectTimeoutInMillis());
		httpClientOptions.setIdleTimeout(spi.getIdleTimeoutInSeconds());
		httpClientOptions.setLogActivity(spi.enableLogActivity());

		if (spi.isProxyEnable()) {
			ProxyOptions proxy = new ProxyOptions();
			proxy.setHost(spi.getProxyHost());
			proxy.setPort(spi.getProxyPort());
			proxy.setUsername(spi.getProxyUsername());
			proxy.setPassword(Encryptions.decode(spi.getProxyPassword(), spi.getConfigTag()));
			httpClientOptions.setProxyOptions(proxy);
		}

		if (spi.getHttpVersion() == HttpVersion.HTTP_2) {
			httpClientOptions.setUseAlpn(spi.isUseAlpn());
		}
	}

	static HttpClientOptions createHttpClientOptions(HttpClientOptionsSPI spi) {
		HttpClientOptions httpClientOptions = new HttpClientOptions();
		buildClientOptionsBase(spi, httpClientOptions);

		httpClientOptions.setProtocolVersion(spi.getHttpVersion());
		httpClientOptions.setDecompressionSupported(spi.isTryUseCompression());
		httpClientOptions.setMaxWaitQueueSize(spi.getMaxWaitQueueSize());
		httpClientOptions.setMaxPoolSize(spi.getMaxPoolSize());
		httpClientOptions.setKeepAlive(spi.isKeepAlive());
		httpClientOptions.setMaxHeaderSize(spi.getMaxHeaderSize());

		if (spi.getHttpVersion() == HttpVersion.HTTP_2) {
			httpClientOptions.setHttp2ClearTextUpgrade(false);
			httpClientOptions.setHttp2MultiplexingLimit(spi.getHttp2MultiplexingLimit());
			httpClientOptions.setHttp2MaxPoolSize(spi.getHttp2MaxPoolSize());
			httpClientOptions.setHttp2KeepAliveTimeout(spi.getKeepAliveTimeout());
		} else {
			httpClientOptions.setKeepAliveTimeout(spi.getKeepAliveTimeout());
		}

		if (spi.isSsl()) {
			VertxTLSBuilder.buildHttpClientOptions(spi.getConfigTag(), httpClientOptions);
		}

		return httpClientOptions;
	}

	static WebSocketClientOptions createWebSocketClientOptions(HttpClientOptionsSPI spi, boolean sslEnabled) {
		WebSocketClientOptions webSocketClientOptions = new WebSocketClientOptions();
		buildClientOptionsBase(spi, webSocketClientOptions);

		if (sslEnabled) {
			VertxTLSBuilder.buildWebSocketClientOptions(spi.getConfigTag(), webSocketClientOptions);
		}

		return webSocketClientOptions;
	}
}
