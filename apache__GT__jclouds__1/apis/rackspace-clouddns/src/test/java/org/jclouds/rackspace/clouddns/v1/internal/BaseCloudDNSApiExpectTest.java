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
package org.jclouds.rackspace.clouddns.v1.internal;

import javax.ws.rs.core.MediaType;

import org.jclouds.http.HttpRequest;
import org.jclouds.http.HttpResponse;
import org.jclouds.rackspace.cloudidentity.v2_0.internal.RackspaceFixture;
import org.jclouds.rest.internal.BaseRestApiExpectTest;

public class BaseCloudDNSApiExpectTest<T> extends BaseRestApiExpectTest<T> {
	protected HttpRequest rackspaceAuthWithUsernameAndApiKey;

	protected String authToken;
	protected HttpResponse responseWithAccess;

	public BaseCloudDNSApiExpectTest() {
		provider = "rackspace-clouddns";

		rackspaceAuthWithUsernameAndApiKey = RackspaceFixture.INSTANCE.initialAuthWithUsernameAndApiKey(identity,
				credential);

		authToken = RackspaceFixture.INSTANCE.getAuthToken();
		responseWithAccess = RackspaceFixture.INSTANCE.responseWithAccess();
	}

	@Override
	protected HttpRequestComparisonType compareHttpRequestAsType(HttpRequest input) {
		return HttpRequestComparisonType.JSON;
	}

	protected HttpRequest.Builder<?> authenticatedGET() {
		return HttpRequest.builder().method("GET").addHeader("Accept", MediaType.APPLICATION_JSON)
				.addHeader("X-Auth-Token", authToken);
	}
}
