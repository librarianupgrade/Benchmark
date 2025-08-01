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
package org.jclouds.cloudstack.features;

import static org.jclouds.reflect.Reflection2.method;

import java.io.IOException;

import org.jclouds.Fallbacks.EmptySetOnNotFoundOr404;
import org.jclouds.Fallbacks.NullOnNotFoundOr404;
import org.jclouds.cloudstack.functions.CloudStackFallbacks.VoidOnNotFoundOr404OrUnableToFindAccountOwner;
import org.jclouds.cloudstack.internal.BaseCloudStackApiTest;
import org.jclouds.cloudstack.options.AssociateIPAddressOptions;
import org.jclouds.cloudstack.options.ListPublicIPAddressesOptions;
import org.jclouds.fallbacks.MapHttp4xxCodesToExceptions;
import org.jclouds.functions.IdentityFunction;
import org.jclouds.http.functions.ParseFirstJsonValueNamed;
import org.jclouds.http.functions.ReleasePayloadAndReturn;
import org.jclouds.http.functions.UnwrapOnlyJsonValue;
import org.jclouds.rest.internal.GeneratedHttpRequest;
import org.testng.annotations.Test;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.Invokable;

/**
 * Tests behavior of {@code AddressApi}
 */
// NOTE:without testName, this will not call @Before* and fail w/NPE during
// surefire
@Test(groups = "unit", testName = "AddressApiTest")
public class AddressApiTest extends BaseCloudStackApiTest<AddressApi> {
	public void testListPublicIPAddresses() throws SecurityException, NoSuchMethodException, IOException {
		Invokable<?, ?> method = method(AddressApi.class, "listPublicIPAddresses",
				ListPublicIPAddressesOptions[].class);
		GeneratedHttpRequest httpRequest = processor.createRequest(method, ImmutableList.of());

		assertRequestLineEquals(httpRequest,
				"GET http://localhost:8080/client/api?response=json&command=listPublicIpAddresses&listAll=true HTTP/1.1");
		assertNonPayloadHeadersEqual(httpRequest, "Accept: application/json\n");
		assertPayloadEquals(httpRequest, null, null, false);

		assertResponseParserClassEquals(method, httpRequest, ParseFirstJsonValueNamed.class);
		assertSaxResponseParserClassEquals(method, null);
		assertFallbackClassEquals(method, EmptySetOnNotFoundOr404.class);

		checkFilters(httpRequest);

	}

	public void testListPublicIPAddressesOptions() throws SecurityException, NoSuchMethodException, IOException {
		Invokable<?, ?> method = method(AddressApi.class, "listPublicIPAddresses",
				ListPublicIPAddressesOptions[].class);
		GeneratedHttpRequest httpRequest = processor.createRequest(method, ImmutableList.<Object>of(
				ListPublicIPAddressesOptions.Builder.accountInDomain("adrian", "6").usesVirtualNetwork(true)));

		assertRequestLineEquals(httpRequest,
				"GET http://localhost:8080/client/api?response=json&command=listPublicIpAddresses&listAll=true&account=adrian&domainid=6&forvirtualnetwork=true HTTP/1.1");
		assertNonPayloadHeadersEqual(httpRequest, "Accept: application/json\n");
		assertPayloadEquals(httpRequest, null, null, false);

		assertResponseParserClassEquals(method, httpRequest, ParseFirstJsonValueNamed.class);
		assertSaxResponseParserClassEquals(method, null);
		assertFallbackClassEquals(method, EmptySetOnNotFoundOr404.class);

		checkFilters(httpRequest);

	}

	public void testGetPublicIPAddress() throws SecurityException, NoSuchMethodException, IOException {
		Invokable<?, ?> method = method(AddressApi.class, "getPublicIPAddress", String.class);
		GeneratedHttpRequest httpRequest = processor.createRequest(method, ImmutableList.<Object>of(5));

		assertRequestLineEquals(httpRequest,
				"GET http://localhost:8080/client/api?response=json&command=listPublicIpAddresses&listAll=true&id=5 HTTP/1.1");
		assertNonPayloadHeadersEqual(httpRequest, "Accept: application/json\n");
		assertPayloadEquals(httpRequest, null, null, false);

		assertResponseParserClassEquals(method, httpRequest,
				Functions.compose(IdentityFunction.INSTANCE, IdentityFunction.INSTANCE).getClass());
		assertSaxResponseParserClassEquals(method, null);
		assertFallbackClassEquals(method, NullOnNotFoundOr404.class);

		checkFilters(httpRequest);

	}

	public void testAssociateIPAddressInZone() throws SecurityException, NoSuchMethodException, IOException {
		Invokable<?, ?> method = method(AddressApi.class, "associateIPAddressInZone", String.class,
				AssociateIPAddressOptions[].class);
		GeneratedHttpRequest httpRequest = processor.createRequest(method, ImmutableList.<Object>of(6));

		assertRequestLineEquals(httpRequest,
				"GET http://localhost:8080/client/api?response=json&command=associateIpAddress&zoneid=6 HTTP/1.1");
		assertNonPayloadHeadersEqual(httpRequest, "Accept: application/json\n");
		assertPayloadEquals(httpRequest, null, null, false);

		assertResponseParserClassEquals(method, httpRequest, UnwrapOnlyJsonValue.class);
		assertSaxResponseParserClassEquals(method, null);
		assertFallbackClassEquals(method, MapHttp4xxCodesToExceptions.class);

		checkFilters(httpRequest);

	}

	public void testDisassociateIPAddress() throws SecurityException, NoSuchMethodException, IOException {
		Invokable<?, ?> method = method(AddressApi.class, "disassociateIPAddress", String.class);
		GeneratedHttpRequest httpRequest = processor.createRequest(method, ImmutableList.<Object>of(5));

		assertRequestLineEquals(httpRequest,
				"GET http://localhost:8080/client/api?response=json&command=disassociateIpAddress&id=5 HTTP/1.1");
		assertNonPayloadHeadersEqual(httpRequest, "");
		assertPayloadEquals(httpRequest, null, null, false);

		assertResponseParserClassEquals(method, httpRequest, ReleasePayloadAndReturn.class);
		assertSaxResponseParserClassEquals(method, null);
		assertFallbackClassEquals(method, VoidOnNotFoundOr404OrUnableToFindAccountOwner.class);

		checkFilters(httpRequest);

	}
}
