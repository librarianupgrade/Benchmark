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
package org.jclouds.providers.internal;

import static org.jclouds.Constants.PROPERTY_ISO3166_CODES;
import static org.jclouds.providers.AnonymousProviderMetadata.forApiOnEndpoint;
import static org.testng.Assert.assertEquals;

import java.util.Properties;

import org.jclouds.Constants;
import org.jclouds.http.IntegrationTestClient;
import org.jclouds.providers.ProviderMetadata;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;

@Test(groups = "unit", testName = "UpdateProviderMetadataFromPropertiesTest")
public class UpdateProviderMetadataFromPropertiesTest {

	@Test
	public void testProviderMetadataWithUpdatedEndpointUpdatesAndRetainsAllDefaultPropertiesExceptEndpoint() {
		ProviderMetadata md = forApiOnEndpoint(IntegrationTestClient.class, "http://localhost");

		Properties props = new Properties();
		props.putAll(md.getDefaultProperties());
		props.setProperty(Constants.PROPERTY_ENDPOINT, "http://nonlocal");

		ProviderMetadata newMd = new UpdateProviderMetadataFromProperties(md).apply(props);

		assertEquals(newMd.getEndpoint(), "http://nonlocal");
		assertEquals(newMd.getDefaultProperties(), md.getDefaultProperties());
	}

	@Test
	public void testProviderMetadataWithUpdatedIso3166CodesUpdatesAndRetainsAllDefaultPropertiesExceptIso3166Codes() {
		ProviderMetadata md = forApiOnEndpoint(IntegrationTestClient.class, "http://localhost");

		Properties props = new Properties();
		props.putAll(md.getDefaultProperties());
		props.setProperty(PROPERTY_ISO3166_CODES, "US-CA");

		ProviderMetadata newMd = new UpdateProviderMetadataFromProperties(md).apply(props);

		assertEquals(newMd.getIso3166Codes(), ImmutableSet.of("US-CA"));
		assertEquals(newMd.getDefaultProperties(), md.getDefaultProperties());
	}

	//TODO: add all the rest of the tests
}
