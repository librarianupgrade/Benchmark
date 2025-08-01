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
package org.jclouds.openstack.cinder.v1.features;

import static org.testng.Assert.assertNotNull;

import java.util.Set;

import org.jclouds.openstack.cinder.v1.domain.VolumeType;
import org.jclouds.openstack.cinder.v1.internal.BaseCinderApiLiveTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

import com.google.common.collect.Iterables;

/**
 * Tests behavior of VolumeTypeApi.
 */
@Test(groups = "live", testName = "VolumeTypeApiLiveTest", singleThreaded = true)
public class VolumeTypeApiLiveTest extends BaseCinderApiLiveTest {
	private VolumeTypeApi volumeTypeApi;
	private String region;

	@BeforeGroups(groups = { "integration", "live" })
	@Override
	public void setup() {
		super.setup();
		region = Iterables.getLast(api.getConfiguredRegions(), "nova");
		volumeTypeApi = api.getVolumeTypeApi(region);
	}

	@AfterClass(groups = { "integration", "live" })
	@Override
	protected void tearDown() {
		super.tearDown();
	}

	public void testListAndGetVolumeTypes() {
		Set<? extends VolumeType> volumeTypes = volumeTypeApi.list().toSet();
		assertNotNull(volumeTypes);

		for (VolumeType vt : volumeTypes) {
			VolumeType details = volumeTypeApi.get(vt.getId());
			assertNotNull(details);
		}
	}
}
