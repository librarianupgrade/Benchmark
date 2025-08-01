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
package org.jclouds.openstack.functions;

import static org.testng.Assert.assertEquals;

import java.net.URI;

import org.jclouds.Constants;
import org.jclouds.http.HttpResponse;
import org.jclouds.openstack.domain.AuthenticationResponse;
import org.jclouds.openstack.reference.AuthHeaders;
import org.testng.annotations.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;

/**
 * Tests behavior of {@code ParseAuthenticationResponseFromHeaders}
 */
@Test(groups = "unit", testName = "ParseAuthenticationResponseFromHeadersTest")
public class ParseAuthenticationResponseFromHeadersTest {

	Injector i = Guice.createInjector(new AbstractModule() {

		@Override
		protected void configure() {
			bindConstant().annotatedWith(Names.named(Constants.PROPERTY_API_VERSION)).to("1");
		}

	});

	public void testReplaceLocalhost() {
		ParseAuthenticationResponseFromHeaders parser = i.getInstance(ParseAuthenticationResponseFromHeaders.class);
		parser = parser.setHostToReplace("fooman");

		HttpResponse response = HttpResponse.builder().statusCode(204).message("No Content")
				.addHeader("X-Auth-Token", "token").addHeader("X-Storage-Token", "token")
				.addHeader("X-Storage-Url", "http://127.0.0.1:8080/v1/token")
				.addHeader("X-CDN-Management-Url", "http://127.0.0.1:8080/cdn-mgmt")
				.addHeader("X-Server-Management-Url", "http://127.0.0.1:8080/srv-mgmt").build();

		AuthenticationResponse md = parser.apply(response);
		assertEquals(md.getAuthToken(), "token");
		assertEquals(md.getServices().get(AuthHeaders.STORAGE_URL), URI.create("http://fooman:8080/v1/token"));
		assertEquals(md.getServices().get(AuthHeaders.CDN_MANAGEMENT_URL), URI.create("http://fooman:8080/cdn-mgmt"));
		assertEquals(md.getServices().get(AuthHeaders.SERVER_MANAGEMENT_URL),
				URI.create("http://fooman:8080/srv-mgmt"));
	}

	public void testHandleHeadersCaseInsensitively() {
		ParseAuthenticationResponseFromHeaders parser = i.getInstance(ParseAuthenticationResponseFromHeaders.class);
		parser = parser.setHostToReplace("fooman");

		HttpResponse response = HttpResponse.builder().statusCode(204).message("No Content")
				.addHeader("x-auth-token", "token").addHeader("x-storage-token", "token")
				.addHeader("x-storage-url", "http://127.0.0.1:8080/v1/token")
				.addHeader("x-cdn-management-url", "http://127.0.0.1:8080/cdn-mgmt")
				.addHeader("x-server-management-url", "http://127.0.0.1:8080/srv-mgmt").build();
		AuthenticationResponse md = parser.apply(response);
		assertEquals(md.getAuthToken(), "token");
		assertEquals(md.getServices().get(AuthHeaders.STORAGE_URL), URI.create("http://fooman:8080/v1/token"));
		assertEquals(md.getServices().get(AuthHeaders.CDN_MANAGEMENT_URL), URI.create("http://fooman:8080/cdn-mgmt"));
		assertEquals(md.getServices().get(AuthHeaders.SERVER_MANAGEMENT_URL),
				URI.create("http://fooman:8080/srv-mgmt"));
	}
}
