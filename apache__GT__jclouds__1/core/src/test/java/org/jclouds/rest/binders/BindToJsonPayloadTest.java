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
package org.jclouds.rest.binders;

import static org.testng.Assert.assertEquals;

import java.io.File;

import org.jclouds.http.HttpRequest;
import org.jclouds.json.Json;
import org.jclouds.json.internal.GsonWrapper;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;

/**
 * Tests behavior of {@code BindToJsonPayload}
 */
@Test(groups = "unit", testName = "BindToJsonPayloadTest")
public class BindToJsonPayloadTest {

	Json json = new GsonWrapper(new Gson());

	@Test
	public void testMap() throws SecurityException, NoSuchMethodException {
		BindToJsonPayload binder = new BindToJsonPayload(json);

		HttpRequest request = HttpRequest.builder().method("GET").endpoint("http://momma").build();
		request = binder.bindToRequest(request, ImmutableMap.of("imageName", "foo", "serverId", "2"));
		assertEquals(request.getPayload().getRawContent(), "{\"imageName\":\"foo\",\"serverId\":\"2\"}");
		assertEquals(request.getPayload().getContentMetadata().getContentType(), "application/json");

	}

	@Test
	public void testSomethingNotAMap() throws SecurityException, NoSuchMethodException {
		BindToJsonPayload binder = new BindToJsonPayload(json);

		HttpRequest request = HttpRequest.builder().method("GET").endpoint("http://momma").build();
		request = binder.bindToRequest(request, new File("foo"));
		assertEquals(request.getPayload().getRawContent(), "{\"path\":\"foo\"}");
		assertEquals(request.getPayload().getContentMetadata().getContentType(), "application/json");

	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testNullIsBad() {
		BindToJsonPayload binder = new BindToJsonPayload(json);
		binder.bindToRequest(HttpRequest.builder().method("GET").endpoint("http://momma").build(), null);
	}
}
