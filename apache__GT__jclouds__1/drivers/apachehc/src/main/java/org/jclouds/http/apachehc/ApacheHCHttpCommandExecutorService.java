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
package org.jclouds.http.apachehc;

import static com.google.common.hash.Hashing.md5;
import static com.google.common.io.BaseEncoding.base64;
import static org.jclouds.Constants.PROPERTY_IDEMPOTENT_METHODS;
import static org.jclouds.http.HttpUtils.filterOutContentHeaders;

import java.io.IOException;
import java.net.URI;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.jclouds.http.HttpRequest;
import org.jclouds.http.HttpResponse;
import org.jclouds.http.HttpUtils;
import org.jclouds.http.IOExceptionRetryHandler;
import org.jclouds.http.handlers.DelegatingErrorHandler;
import org.jclouds.http.handlers.DelegatingRetryHandler;
import org.jclouds.http.internal.BaseHttpCommandExecutorService;
import org.jclouds.http.internal.HttpWire;
import org.jclouds.io.ByteStreams2;
import org.jclouds.io.ContentMetadataCodec;
import org.jclouds.io.Payload;
import org.jclouds.io.Payloads;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

/**
 * Simple implementation of a {@link HttpCommandExecutorService}, Apache Components HttpClient 4.x.
 */
public class ApacheHCHttpCommandExecutorService extends BaseHttpCommandExecutorService<HttpUriRequest> {
	private final HttpClient client;
	private final ApacheHCUtils apacheHCUtils;

	@Inject
	ApacheHCHttpCommandExecutorService(HttpUtils utils, ContentMetadataCodec contentMetadataCodec,
			DelegatingRetryHandler retryHandler, IOExceptionRetryHandler ioRetryHandler,
			DelegatingErrorHandler errorHandler, HttpWire wire, HttpClient client,
			@Named(PROPERTY_IDEMPOTENT_METHODS) String idempotentMethods) {
		super(utils, contentMetadataCodec, retryHandler, ioRetryHandler, errorHandler, wire, idempotentMethods);
		this.client = client;
		this.apacheHCUtils = new ApacheHCUtils(contentMetadataCodec);
	}

	@Override
	protected HttpUriRequest convert(HttpRequest request) throws IOException {
		HttpUriRequest returnVal = apacheHCUtils.convertToApacheRequest(request);
		if (request.getPayload() != null && request.getPayload().getContentMetadata().getContentMD5() != null) {
			String md5 = base64().encode(ByteStreams2.hashAndClose(request.getPayload().openStream(), md5()).asBytes());
			returnVal.addHeader("Content-MD5", md5);
		}

		return returnVal;
	}

	@Override
	protected HttpResponse invoke(HttpUriRequest nativeRequest) throws IOException {
		org.apache.http.HttpResponse apacheResponse = executeRequest(nativeRequest);

		Payload payload = null;
		if (apacheResponse.getEntity() != null)
			try {
				payload = Payloads.newInputStreamPayload(apacheResponse.getEntity().getContent());
				if (apacheResponse.getEntity().getContentLength() >= 0)
					payload.getContentMetadata().setContentLength(apacheResponse.getEntity().getContentLength());
				if (apacheResponse.getEntity().getContentType() != null)
					payload.getContentMetadata().setContentType(apacheResponse.getEntity().getContentType().getValue());
			} catch (IOException e) {
				logger.warn(e, "couldn't receive payload for request: %s", nativeRequest.getRequestLine());
				throw e;
			}
		Multimap<String, String> headers = LinkedHashMultimap.create();
		for (Header header : apacheResponse.getAllHeaders()) {
			headers.put(header.getName(), header.getValue());
		}
		if (payload != null) {
			contentMetadataCodec.fromHeaders(payload.getContentMetadata(), headers);
		}
		return HttpResponse.builder().statusCode(apacheResponse.getStatusLine().getStatusCode())
				.message(apacheResponse.getStatusLine().getReasonPhrase()).payload(payload)
				.headers(filterOutContentHeaders(headers)).build();
	}

	private org.apache.http.HttpResponse executeRequest(HttpUriRequest nativeRequest)
			throws IOException, ClientProtocolException {
		URI endpoint = URI.create(nativeRequest.getRequestLine().getUri());
		HttpHost host = new HttpHost(endpoint.getHost(), endpoint.getPort(), endpoint.getScheme());
		org.apache.http.HttpResponse nativeResponse = client.execute(host, nativeRequest);
		return nativeResponse;
	}

	@Override
	protected void cleanup(HttpUriRequest nativeResponse) {
		// No cleanup necessary
	}
}
