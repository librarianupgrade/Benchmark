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
import static org.testng.Assert.assertNotNull;

import org.jclouds.cloudstack.domain.NetworkType;
import org.jclouds.cloudstack.domain.Zone;
import org.jclouds.cloudstack.internal.BaseCloudStackApiLiveTest;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;

/**
 * Tests behavior of {@code GlobalZoneApi}
 */
@Test(groups = "live", singleThreaded = true, testName = "GlobalZoneApiLiveTest")
public class GlobalZoneApiLiveTest extends BaseCloudStackApiLiveTest {

	@Test
	public void testCreateUpdateDeleteZone() {
		skipIfNotGlobalAdmin();

		Zone zone = null;
		String zoneName = prefix + "-zone";
		try {
			zone = globalAdminClient.getZoneApi().createZone(zoneName, NetworkType.BASIC, "8.8.8.8", "10.10.10.10");

			assertNotNull(zone);
			assertEquals(zone, globalAdminClient.getZoneApi().getZone(zone.getId()));
			assertEquals(zone.getNetworkType(), NetworkType.BASIC);
			assertEquals(zone.getDNS(), ImmutableList.of("8.8.8.8"));
			assertEquals(zone.getInternalDNS(), ImmutableList.of("10.10.10.10"));

			Zone updated = globalAdminClient.getZoneApi().updateZone(zone.getId(),
					name(zoneName + "-2").externalDns(ImmutableList.of("8.8.4.4")));
			assertEquals(updated.getId(), zone.getId());
			assertEquals(updated.getDNS(), ImmutableList.of("8.8.4.4"));

		} finally {
			if (zone != null) {
				globalAdminClient.getZoneApi().deleteZone(zone.getId());
			}
		}

	}

}
