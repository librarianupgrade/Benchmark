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
package org.jclouds.ec2.features;

import static org.jclouds.ec2.options.DescribeAvailabilityZonesOptions.Builder.availabilityZones;
import static org.jclouds.ec2.options.DescribeRegionsOptions.Builder.regions;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.net.URI;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;

import org.jclouds.compute.internal.BaseComputeServiceContextLiveTest;
import org.jclouds.ec2.EC2Api;
import org.jclouds.ec2.domain.AvailabilityZoneInfo;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.Maps;

/**
 * Tests behavior of {@code AvailabilityZoneAndRegionApi}
 */
@Test(groups = "live", singleThreaded = true, testName = "AvailabilityZoneAndRegionApiLiveTest")
public class AvailabilityZoneAndRegionApiLiveTest extends BaseComputeServiceContextLiveTest {
	public AvailabilityZoneAndRegionApiLiveTest() {
		provider = "ec2";
	}

	private EC2Api ec2Api;
	private AvailabilityZoneAndRegionApi client;

	@Override
	@BeforeClass(groups = { "integration", "live" })
	public void setupContext() {
		super.setupContext();
		ec2Api = view.unwrapApi(EC2Api.class);
		client = ec2Api.getAvailabilityZoneAndRegionApi().get();
	}

	public void testDescribeAvailabilityZones() {
		for (String region : ec2Api.getConfiguredRegions()) {
			Set<AvailabilityZoneInfo> allResults = client.describeAvailabilityZonesInRegion(region);
			assertNotNull(allResults);
			assert !allResults.isEmpty() : allResults.size();
			Iterator<AvailabilityZoneInfo> iterator = allResults.iterator();
			String id1 = iterator.next().getZone();
			Set<AvailabilityZoneInfo> oneResult = client.describeAvailabilityZonesInRegion(region,
					availabilityZones(id1));
			assertNotNull(oneResult);
			assertEquals(oneResult.size(), 1);
			iterator = allResults.iterator();
			assertEquals(iterator.next().getZone(), id1);
		}
	}

	public void testDescribeRegions() {
		SortedMap<String, URI> allResults = Maps.newTreeMap();
		allResults.putAll(client.describeRegions());
		assertNotNull(allResults);
		assert !allResults.isEmpty() : allResults.size();
		Iterator<Entry<String, URI>> iterator = allResults.entrySet().iterator();
		String r1 = iterator.next().getKey();
		SortedMap<String, URI> oneResult = Maps.newTreeMap();
		oneResult.putAll(client.describeRegions(regions(r1)));
		assertNotNull(oneResult);
		assertEquals(oneResult.size(), 1);
		iterator = oneResult.entrySet().iterator();
		assertEquals(iterator.next().getKey(), r1);
	}

}
