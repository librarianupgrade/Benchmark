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

package org.apache.servicecomb.common.accessLog.core.element.impl;

import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.core.event.ServerAccessLogEvent;
import org.apache.servicecomb.transport.rest.client.RestClientRequestParameters;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.impl.headers.HeadersMultiMap;
import io.vertx.ext.web.RoutingContext;

public class RequestHeaderItemTest {
	private static final String VAR_NAME = "varName";

	private static final RequestHeaderAccessItem ELEMENT = new RequestHeaderAccessItem(VAR_NAME);

	private StringBuilder strBuilder;

	private InvocationFinishEvent finishEvent;

	private ServerAccessLogEvent accessLogEvent;

	private RoutingContext routingContext;

	private Invocation invocation;

	private RestClientRequestParameters restClientRequest;

	private HttpServerRequest serverRequest;

	private HttpClientRequest clientRequest;

	private MultiMap headers;

	@BeforeEach
	public void initStrBuilder() {
		routingContext = Mockito.mock(RoutingContext.class);
		finishEvent = Mockito.mock(InvocationFinishEvent.class);
		invocation = Mockito.mock(Invocation.class);
		serverRequest = Mockito.mock(HttpServerRequest.class);
		restClientRequest = Mockito.mock(RestClientRequestParameters.class);
		clientRequest = Mockito.mock(HttpClientRequest.class);
		headers = Mockito.mock(MultiMap.class);

		accessLogEvent = new ServerAccessLogEvent();
		accessLogEvent.setRoutingContext(routingContext);
		strBuilder = new StringBuilder();
	}

	@Test
	public void serverFormattedElement() {
		HeadersMultiMap headers = new HeadersMultiMap();
		String testValue = "testValue";
		headers.add(VAR_NAME, testValue);
		when(routingContext.request()).thenReturn(serverRequest);
		when(serverRequest.headers()).thenReturn(headers);

		ELEMENT.appendServerFormattedItem(accessLogEvent, strBuilder);
		Assertions.assertEquals(testValue, strBuilder.toString());
		Assertions.assertEquals(ELEMENT.getVarName(), VAR_NAME);
	}

	@Test
	public void clientFormattedElement() {
		Map<String, Object> handlerContext = new HashMap<>();
		String testValue = "testValue";

		handlerContext.put(RestConst.INVOCATION_HANDLER_REQUESTCLIENT, restClientRequest);
		when(finishEvent.getInvocation()).thenReturn(invocation);
		when(invocation.getHandlerContext()).thenReturn(handlerContext);
		when(restClientRequest.getHttpClientRequest()).thenReturn(clientRequest);
		when(clientRequest.headers()).thenReturn(headers);
		when(headers.get(VAR_NAME)).thenReturn(testValue);

		ELEMENT.appendClientFormattedItem(finishEvent, strBuilder);
		Assertions.assertEquals(testValue, strBuilder.toString());
	}

	@Test
	public void serverFormattedElementIfHeaderIsNull() {
		when(routingContext.request()).thenReturn(serverRequest);
		when(serverRequest.headers()).thenReturn(null);

		ELEMENT.appendServerFormattedItem(accessLogEvent, strBuilder);
		Assertions.assertEquals("-", strBuilder.toString());
	}

	@Test
	public void clientFormattedElementIfHeaderIsNull() {
		Map<String, Object> handlerContext = new HashMap<>();
		handlerContext.put(RestConst.INVOCATION_HANDLER_REQUESTCLIENT, restClientRequest);
		when(finishEvent.getInvocation()).thenReturn(invocation);
		when(invocation.getHandlerContext()).thenReturn(handlerContext);
		when(restClientRequest.getHttpClientRequest()).thenReturn(clientRequest);
		when(clientRequest.headers()).thenReturn(null);

		ELEMENT.appendClientFormattedItem(finishEvent, strBuilder);
		Assertions.assertEquals("-", strBuilder.toString());
	}

	@Test
	public void serverFormattedElementIfNotFound() {
		HeadersMultiMap headers = new HeadersMultiMap();
		String testValue = "testValue";
		headers.add("anotherKey", testValue);
		when(routingContext.request()).thenReturn(serverRequest);
		when(serverRequest.headers()).thenReturn(headers);

		ELEMENT.appendServerFormattedItem(accessLogEvent, strBuilder);
		Assertions.assertEquals("-", strBuilder.toString());
	}
}
