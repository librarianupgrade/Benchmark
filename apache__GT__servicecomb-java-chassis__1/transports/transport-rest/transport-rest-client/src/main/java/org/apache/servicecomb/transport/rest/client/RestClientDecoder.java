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

import static org.apache.servicecomb.transport.rest.client.RestClientExceptionCodes.FAILED_TO_DECODE_REST_FAIL_RESPONSE;
import static org.apache.servicecomb.transport.rest.client.RestClientExceptionCodes.FAILED_TO_DECODE_REST_SUCCESS_RESPONSE;

import org.apache.servicecomb.common.rest.codec.produce.ProduceProcessor;
import org.apache.servicecomb.common.rest.codec.produce.ProduceProcessorManager;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.exception.Exceptions;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JavaType;

import io.reactivex.rxjava3.core.Flowable;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientRequest;
import jakarta.ws.rs.core.HttpHeaders;

public class RestClientDecoder {
	private static final Logger LOGGER = LoggerFactory.getLogger(RestClientDecoder.class);

	public Response decode(Invocation invocation, Response response) {
		invocation.getInvocationStageTrace().startConsumerDecodeResponse();
		if (response.getResult() instanceof Buffer) {
			Object result = extractBody(invocation, response, response.getResult());
			response.setResult(result);

			if (response.isFailed()) {
				throw Exceptions.create(response.getStatus(), response.getResult());
			}
		}

		if (response.getResult() instanceof Flowable<?>) {
			Flowable<Buffer> flowable = response.getResult();
			Flowable<Object> encoded = flowable.map(buffer -> extractBody(invocation, response, buffer));
			response.setResult(encoded);
		}

		invocation.getInvocationStageTrace().finishConsumerDecodeResponse();
		return response;
	}

	protected Object extractBody(Invocation invocation, Response response, Buffer buffer) {
		ProduceProcessor produceProcessor = safeFindProduceProcessor(invocation, response);
		JavaType responseType = invocation.findResponseType(response.getStatusCode());

		try {
			return produceProcessor.decodeResponse(buffer, responseType);
		} catch (Exception e) {
			throw createDecodeException(invocation, response, e);
		}
	}

	private ProduceProcessor safeFindProduceProcessor(Invocation invocation, Response response) {
		String contentType = extractContentType(response);
		ProduceProcessor produceProcessor = ProduceProcessorManager.INSTANCE
				.createProduceProcessor(invocation.getOperationMeta(), response.getStatusCode(), contentType, null);
		if (produceProcessor != null) {
			return produceProcessor;
		}

		RestClientTransportContext transportContext = invocation.getTransportContext();
		HttpClientRequest httpClientRequest = transportContext.getHttpClientRequest();
		LOGGER.warn(
				"operation={}, method={}, endpoint={}, uri={}, statusCode={}, reasonPhrase={}, response content-type={} is not supported in operation.",
				invocation.getMicroserviceQualifiedName(), httpClientRequest.getMethod(),
				invocation.getEndpoint().getEndpoint(), httpClientRequest.getURI(), response.getStatusCode(),
				response.getReasonPhrase(), response.getHeader(HttpHeaders.CONTENT_TYPE));

		// This happens outside the runtime such as Servlet filter response. Here we give a default json parser to it
		// and keep user data not get lost.
		return ProduceProcessorManager.INSTANCE.findDefaultProcessor();
	}

	protected String extractContentType(Response response) {
		String contentType = response.getHeader(HttpHeaders.CONTENT_TYPE);
		if (contentType == null) {
			return null;
		}

		int idx = contentType.indexOf(";");
		return idx == -1 ? contentType : contentType.substring(0, idx);
	}

	protected InvocationException createDecodeException(Invocation invocation, Response response, Exception e) {
		RestClientTransportContext transportContext = invocation.getTransportContext();
		HttpClientRequest httpClientRequest = transportContext.getHttpClientRequest();

		LOGGER.warn(
				"failed to decode response body, " + "operation={}, method={}, endpoint={}, uri={}, "
						+ "statusCode={}, reasonPhrase={}, content-type={}.",
				invocation.getMicroserviceQualifiedName(), httpClientRequest.getMethod(),
				invocation.getEndpoint().getEndpoint(), httpClientRequest.getURI(), response.getStatusCode(),
				response.getReasonPhrase(), response.getHeader(HttpHeaders.CONTENT_TYPE));

		if (response.isSucceed()) {
			return Exceptions.consumer(FAILED_TO_DECODE_REST_SUCCESS_RESPONSE,
					"failed to decode success response body.", e);
		}
		return Exceptions.consumer(FAILED_TO_DECODE_REST_FAIL_RESPONSE, "failed to decode fail response body.", e);
	}
}
