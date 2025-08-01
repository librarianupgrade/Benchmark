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

import static org.jclouds.cloudstack.options.UpdateZoneOptions.Builder.name;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.jclouds.cloudstack.CloudStackContext;
import org.jclouds.cloudstack.domain.AllocationState;
import org.jclouds.cloudstack.domain.NetworkType;
import org.jclouds.cloudstack.domain.Zone;
import org.jclouds.cloudstack.internal.BaseCloudStackExpectTest;
import org.jclouds.http.HttpRequest;
import org.jclouds.http.HttpResponse;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;

/**
 * Test the CloudStack GlobalZoneApi
 */
@Test(groups = "unit", testName = "GlobalZoneApiExpectTest")
public class GlobalZoneApiExpectTest extends BaseCloudStackExpectTest<GlobalZoneApi> {

	HttpRequest createZone = HttpRequest.builder().method("GET").endpoint("http://localhost:8080/client/api")
			.addQueryParam("response", "json").addQueryParam("command", "createZone").addQueryParam("name", "test-zone")
			.addQueryParam("networktype", "Basic").addQueryParam("dns1", "8.8.8.8")
			.addQueryParam("internaldns1", "10.10.10.10").addQueryParam("apiKey", "identity")
			.addQueryParam("signature", "hWNmM2+Tsfb5DelQa/GJLN5DVWE=").addHeader("Accept", "application/json").build();

	public void testCreateZoneWhenResponseIs2xxAnd404() {
		GlobalZoneApi client = requestSendsResponse(createZone, HttpResponse.builder().statusCode(200)
				.payload(payloadFromResource("/createzoneresponse.json")).build());

		assertEquals(client.createZone("test-zone", NetworkType.BASIC, "8.8.8.8", "10.10.10.10"),
				Zone.builder().id("6").name("test-zone").DNS(ImmutableList.of("8.8.8.8"))
						.internalDNS(ImmutableList.of("10.10.10.10")).networkType(NetworkType.BASIC)
						.securityGroupsEnabled(true).allocationState(AllocationState.ENABLED)
						.zoneToken("7b6e27df-30a6-3024-9d8b-7971a3127f64").dhcpProvider("DhcpServer").build());

		client = requestSendsResponse(createZone, HttpResponse.builder().statusCode(404).build());
		assertNull(client.createZone("test-zone", NetworkType.BASIC, "8.8.8.8", "10.10.10.10"));
	}

	public void testUpdateZoneWhenResponseIs2xxAnd404() {
		HttpRequest request = HttpRequest.builder().method("GET").endpoint("http://localhost:8080/client/api")
				.addQueryParam("response", "json").addQueryParam("command", "updateZone").addQueryParam("id", "6")
				.addQueryParam("name", "test-zone").addQueryParam("dns1", "8.8.8.8").addQueryParam("apiKey", "identity")
				.addQueryParam("signature", "v19FdHKHztdT0IRloYFFn0eNbWM=")
				.headers(ImmutableMultimap.<String, String>builder().put("Accept", "application/json").build()).build();

		GlobalZoneApi client = requestSendsResponse(request, HttpResponse.builder().statusCode(200)
				.payload(payloadFromResource("/updatezoneresponse.json")).build());

		assertEquals(client.updateZone("6", name("test-zone").externalDns(ImmutableList.of("8.8.8.8"))),
				Zone.builder().id("6").name("test-zone").DNS(ImmutableList.of("8.8.8.8"))
						.internalDNS(ImmutableList.of("10.10.10.10")).networkType(NetworkType.BASIC)
						.securityGroupsEnabled(true).allocationState(AllocationState.ENABLED)
						.zoneToken("7b6e27df-30a6-3024-9d8b-7971a3127f64").dhcpProvider("DhcpServer").build());

		client = requestSendsResponse(request, HttpResponse.builder().statusCode(404).build());
		assertNull(client.updateZone("6", name("test-zone").externalDns(ImmutableList.of("8.8.8.8"))));
	}

	public void testDeleteZone() {
		GlobalZoneApi client = requestSendsResponse(
				HttpRequest.builder().method("GET").endpoint("http://localhost:8080/client/api")
						.addQueryParam("response", "json").addQueryParam("command", "deleteZone")
						.addQueryParam("id", "6").addQueryParam("apiKey", "identity")
						.addQueryParam("signature", "TfkzSIK8kzGJnIYo3DofECyuOII=")
						.addHeader("Accept", "application/json").build(),
				HttpResponse.builder().statusCode(200).payload(payloadFromResource("/deletezoneresponse.json"))
						.build());

		client.deleteZone("6");
	}

	@Override
	protected GlobalZoneApi clientFrom(CloudStackContext context) {
		return context.getGlobalApi().getZoneApi();
	}
}
