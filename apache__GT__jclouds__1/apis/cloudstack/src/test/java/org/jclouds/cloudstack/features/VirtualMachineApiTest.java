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
import java.net.URLEncoder;
import java.nio.charset.Charset;

import org.jclouds.Fallbacks.EmptySetOnNotFoundOr404;
import org.jclouds.Fallbacks.NullOnNotFoundOr404;
import org.jclouds.cloudstack.options.UpdateVirtualMachineOptions;
import org.jclouds.fallbacks.MapHttp4xxCodesToExceptions;
import org.jclouds.cloudstack.internal.BaseCloudStackApiTest;
import org.jclouds.cloudstack.options.AssignVirtualMachineOptions;
import org.jclouds.cloudstack.options.ListVirtualMachinesOptions;
import org.jclouds.cloudstack.options.StopVirtualMachineOptions;
import org.jclouds.functions.IdentityFunction;
import org.jclouds.http.functions.ParseFirstJsonValueNamed;
import org.jclouds.rest.internal.GeneratedHttpRequest;
import org.testng.annotations.Test;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.Invokable;
import static com.google.common.io.BaseEncoding.base64;

/**
 * Tests behavior of {@code VirtualMachineApi}
 */
// NOTE:without testName, this will not call @Before* and fail w/NPE during
// surefire
@Test(groups = "unit", testName = "VirtualMachineApiTest")
public class VirtualMachineApiTest extends BaseCloudStackApiTest<VirtualMachineApi> {
	public void testListVirtualMachines() throws SecurityException, NoSuchMethodException, IOException {
		Invokable<?, ?> method = method(VirtualMachineApi.class, "listVirtualMachines",
				ListVirtualMachinesOptions[].class);
		GeneratedHttpRequest httpRequest = processor.createRequest(method, ImmutableList.of());

		assertRequestLineEquals(httpRequest,
				"GET http://localhost:8080/client/api?response=json&command=listVirtualMachines&listAll=true HTTP/1.1");
		assertNonPayloadHeadersEqual(httpRequest, "Accept: application/json\n");
		assertPayloadEquals(httpRequest, null, null, false);

		assertResponseParserClassEquals(method, httpRequest, ParseFirstJsonValueNamed.class);
		assertSaxResponseParserClassEquals(method, null);
		assertFallbackClassEquals(method, EmptySetOnNotFoundOr404.class);

		checkFilters(httpRequest);

	}

	public void testListVirtualMachinesOptions() throws SecurityException, NoSuchMethodException, IOException {
		Invokable<?, ?> method = method(VirtualMachineApi.class, "listVirtualMachines",
				ListVirtualMachinesOptions[].class);
		GeneratedHttpRequest httpRequest = processor.createRequest(method, ImmutableList.<Object>of(
				ListVirtualMachinesOptions.Builder.accountInDomain("adrian", "6").usesVirtualNetwork(true)));

		assertRequestLineEquals(httpRequest,
				"GET http://localhost:8080/client/api?response=json&command=listVirtualMachines&listAll=true&account=adrian&domainid=6&forvirtualnetwork=true HTTP/1.1");
		assertNonPayloadHeadersEqual(httpRequest, "Accept: application/json\n");
		assertPayloadEquals(httpRequest, null, null, false);

		assertResponseParserClassEquals(method, httpRequest, ParseFirstJsonValueNamed.class);
		assertSaxResponseParserClassEquals(method, null);
		assertFallbackClassEquals(method, EmptySetOnNotFoundOr404.class);

		checkFilters(httpRequest);

	}

	public void testGetVirtualMachine() throws SecurityException, NoSuchMethodException, IOException {
		Invokable<?, ?> method = method(VirtualMachineApi.class, "getVirtualMachine", String.class);
		GeneratedHttpRequest httpRequest = processor.createRequest(method, ImmutableList.<Object>of(5));

		assertRequestLineEquals(httpRequest,
				"GET http://localhost:8080/client/api?response=json&command=listVirtualMachines&listAll=true&id=5 HTTP/1.1");
		assertNonPayloadHeadersEqual(httpRequest, "Accept: application/json\n");
		assertPayloadEquals(httpRequest, null, null, false);

		assertResponseParserClassEquals(method, httpRequest,
				Functions.compose(IdentityFunction.INSTANCE, IdentityFunction.INSTANCE).getClass());
		assertSaxResponseParserClassEquals(method, null);
		assertFallbackClassEquals(method, NullOnNotFoundOr404.class);

		checkFilters(httpRequest);

	}

	public void testRebootVirtualMachine() throws SecurityException, NoSuchMethodException, IOException {
		Invokable<?, ?> method = method(VirtualMachineApi.class, "rebootVirtualMachine", String.class);
		GeneratedHttpRequest httpRequest = processor.createRequest(method, ImmutableList.<Object>of(5));

		assertRequestLineEquals(httpRequest,
				"GET http://localhost:8080/client/api?response=json&command=rebootVirtualMachine&id=5 HTTP/1.1");
		assertNonPayloadHeadersEqual(httpRequest, "Accept: application/json\n");
		assertPayloadEquals(httpRequest, null, null, false);

		assertResponseParserClassEquals(method, httpRequest, ParseFirstJsonValueNamed.class);
		assertSaxResponseParserClassEquals(method, null);
		assertFallbackClassEquals(method, MapHttp4xxCodesToExceptions.class);

		checkFilters(httpRequest);

	}

	public void testStartVirtualMachine() throws SecurityException, NoSuchMethodException, IOException {
		Invokable<?, ?> method = method(VirtualMachineApi.class, "startVirtualMachine", String.class);
		GeneratedHttpRequest httpRequest = processor.createRequest(method, ImmutableList.<Object>of(5));

		assertRequestLineEquals(httpRequest,
				"GET http://localhost:8080/client/api?response=json&command=startVirtualMachine&id=5 HTTP/1.1");
		assertNonPayloadHeadersEqual(httpRequest, "Accept: application/json\n");
		assertPayloadEquals(httpRequest, null, null, false);

		assertResponseParserClassEquals(method, httpRequest, ParseFirstJsonValueNamed.class);
		assertSaxResponseParserClassEquals(method, null);
		assertFallbackClassEquals(method, MapHttp4xxCodesToExceptions.class);

		checkFilters(httpRequest);

	}

	public void testStopVirtualMachine() throws SecurityException, NoSuchMethodException, IOException {
		Invokable<?, ?> method = method(VirtualMachineApi.class, "stopVirtualMachine", String.class);
		GeneratedHttpRequest httpRequest = processor.createRequest(method, ImmutableList.<Object>of("5"));

		assertRequestLineEquals(httpRequest,
				"GET http://localhost:8080/client/api?response=json&command=stopVirtualMachine&id=5 HTTP/1.1");
		assertNonPayloadHeadersEqual(httpRequest, "Accept: application/json\n");
		assertPayloadEquals(httpRequest, null, null, false);

		assertResponseParserClassEquals(method, httpRequest, ParseFirstJsonValueNamed.class);
		assertSaxResponseParserClassEquals(method, null);
		assertFallbackClassEquals(method, MapHttp4xxCodesToExceptions.class);

		checkFilters(httpRequest);

	}

	public void testStopVirtualMachineForced() throws SecurityException, NoSuchMethodException, IOException {
		Invokable<?, ?> method = method(VirtualMachineApi.class, "stopVirtualMachine", String.class,
				StopVirtualMachineOptions.class);

		GeneratedHttpRequest httpRequest = processor.createRequest(method,
				ImmutableList.<Object>of("5", StopVirtualMachineOptions.Builder.forced(true)));

		assertRequestLineEquals(httpRequest,
				"GET http://localhost:8080/client/api?response=json&command=stopVirtualMachine&id=5&forced=true HTTP/1.1");
		assertNonPayloadHeadersEqual(httpRequest, "Accept: application/json\n");
		assertPayloadEquals(httpRequest, null, null, false);

		assertResponseParserClassEquals(method, httpRequest, ParseFirstJsonValueNamed.class);
		assertSaxResponseParserClassEquals(method, null);
		assertFallbackClassEquals(method, MapHttp4xxCodesToExceptions.class);

		checkFilters(httpRequest);

	}

	public void testResetPasswordForVirtualMachine() throws SecurityException, NoSuchMethodException, IOException {
		Invokable<?, ?> method = method(VirtualMachineApi.class, "resetPasswordForVirtualMachine", String.class);
		GeneratedHttpRequest httpRequest = processor.createRequest(method, ImmutableList.<Object>of(5));

		assertRequestLineEquals(httpRequest,
				"GET http://localhost:8080/client/api?response=json&command=resetPasswordForVirtualMachine&id=5 HTTP/1.1");
		assertNonPayloadHeadersEqual(httpRequest, "Accept: application/json\n");
		assertPayloadEquals(httpRequest, null, null, false);

		assertResponseParserClassEquals(method, httpRequest, ParseFirstJsonValueNamed.class);
		assertSaxResponseParserClassEquals(method, null);
		assertFallbackClassEquals(method, MapHttp4xxCodesToExceptions.class);

		checkFilters(httpRequest);

	}

	public void testChangeServiceForVirtualMachine() throws SecurityException, NoSuchMethodException, IOException {
		Invokable<?, ?> method = method(VirtualMachineApi.class, "changeServiceForVirtualMachine", String.class,
				String.class);
		GeneratedHttpRequest httpRequest = processor.createRequest(method, ImmutableList.<Object>of(5, 6));

		assertRequestLineEquals(httpRequest,
				"GET http://localhost:8080/client/api?response=json&command=changeServiceForVirtualMachine&id=5&serviceofferingid=6 HTTP/1.1");
		assertNonPayloadHeadersEqual(httpRequest, "Accept: application/json\n");
		assertPayloadEquals(httpRequest, null, null, false);

		assertResponseParserClassEquals(method, httpRequest, ParseFirstJsonValueNamed.class);
		assertSaxResponseParserClassEquals(method, null);
		assertFallbackClassEquals(method, MapHttp4xxCodesToExceptions.class);

		checkFilters(httpRequest);

	}

	public void testUpdateVirtualMachine() throws SecurityException, NoSuchMethodException, IOException {
		Invokable<?, ?> method = method(VirtualMachineApi.class, "updateVirtualMachine", String.class,
				UpdateVirtualMachineOptions.class);
		byte[] unencodedData = "userData".getBytes(Charset.forName("utf-8"));
		UpdateVirtualMachineOptions options = UpdateVirtualMachineOptions.Builder.displayName("disp").group("test")
				.haEnable(true).osTypeId("osid").userData(unencodedData);
		GeneratedHttpRequest httpRequest = processor.createRequest(method, ImmutableList.<Object>of("5", options));

		String base64UrlEncodedData = URLEncoder.encode(base64().encode(unencodedData), "utf-8");

		assertRequestLineEquals(httpRequest,
				"GET http://localhost:8080/client/api?response=json&command=updateVirtualMachine"
						+ "&id=5&displayname=disp&group=test&haenable=true&ostypeid=osid&userdata="
						+ base64UrlEncodedData + " HTTP/1.1");
		assertNonPayloadHeadersEqual(httpRequest, "Accept: application/json\n");
		assertPayloadEquals(httpRequest, null, null, false);

		assertResponseParserClassEquals(method, httpRequest, ParseFirstJsonValueNamed.class);
		assertSaxResponseParserClassEquals(method, null);
		assertFallbackClassEquals(method, MapHttp4xxCodesToExceptions.class);

		checkFilters(httpRequest);

	}

	public void testDestroyVirtualMachine() throws SecurityException, NoSuchMethodException, IOException {
		Invokable<?, ?> method = method(VirtualMachineApi.class, "destroyVirtualMachine", String.class);
		GeneratedHttpRequest httpRequest = processor.createRequest(method, ImmutableList.<Object>of(5));

		assertRequestLineEquals(httpRequest,
				"GET http://localhost:8080/client/api?response=json&command=destroyVirtualMachine&id=5 HTTP/1.1");
		assertNonPayloadHeadersEqual(httpRequest, "Accept: application/json\n");
		assertPayloadEquals(httpRequest, null, null, false);

		assertResponseParserClassEquals(method, httpRequest, ParseFirstJsonValueNamed.class);
		assertSaxResponseParserClassEquals(method, null);
		assertFallbackClassEquals(method, NullOnNotFoundOr404.class);

		checkFilters(httpRequest);

	}

	public void testAssignVirtualMachine() throws SecurityException, NoSuchMethodException, IOException {
		Invokable<?, ?> method = method(VirtualMachineApi.class, "assignVirtualMachine", String.class,
				AssignVirtualMachineOptions[].class);
		GeneratedHttpRequest httpRequest = processor.createRequest(method,
				ImmutableList.<Object>of("abcd", AssignVirtualMachineOptions.Builder.accountInDomain("adrian", "6")));

		assertRequestLineEquals(httpRequest,
				"GET http://localhost:8080/client/api?response=json&command=assignVirtualMachine&virtualmachineid=abcd&account=adrian&domainid=6 HTTP/1.1");
		assertNonPayloadHeadersEqual(httpRequest, "Accept: application/json\n");
		assertPayloadEquals(httpRequest, null, null, false);

		assertResponseParserClassEquals(method, httpRequest, ParseFirstJsonValueNamed.class);
		assertSaxResponseParserClassEquals(method, null);
		assertFallbackClassEquals(method, MapHttp4xxCodesToExceptions.class);

		checkFilters(httpRequest);

	}
}
