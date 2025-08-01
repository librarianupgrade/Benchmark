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
package org.jclouds.openstack.nova.v2_0.compute.functions;

import static org.testng.Assert.assertEquals;

import java.util.Map;
import java.util.Set;

import org.jclouds.compute.domain.Hardware;
import org.jclouds.compute.domain.Image;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.functions.GroupNamingConvention;
import org.jclouds.domain.Location;
import org.jclouds.domain.LocationBuilder;
import org.jclouds.domain.LocationScope;
import org.jclouds.openstack.nova.v2_0.compute.config.NovaComputeServiceContextModule;
import org.jclouds.openstack.nova.v2_0.domain.regionscoped.RegionAndName;
import org.jclouds.openstack.nova.v2_0.domain.regionscoped.ServerInRegion;
import org.jclouds.openstack.nova.v2_0.parse.ParseServerTest;
import org.testng.annotations.Test;

import com.google.common.base.Predicates;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Guice;

@Test(testName = "OrphanedGroupsByRegionIdTest")
public class OrphanedGroupsByRegionIdTest {

	Location provider = new LocationBuilder().scope(LocationScope.PROVIDER).id("openstack-nova")
			.description("openstack-nova").build();
	Location region = new LocationBuilder().id("az-1.region-a.geo-1").description("az-1.region-a.geo-1")
			.scope(LocationScope.REGION).parent(provider).build();
	Supplier<Map<String, Location>> locationIndex = Suppliers
			.<Map<String, Location>>ofInstance(ImmutableMap.<String, Location>of("az-1.region-a.geo-1", region));

	GroupNamingConvention.Factory namingConvention = Guice.createInjector()
			.getInstance(GroupNamingConvention.Factory.class);

	@Test
	public void testWhenComputeServiceSaysAllNodesAreDeadBothGroupsAreReturned() {

		ServerInRegion withoutHost = new ServerInRegion(new ServerInRegionToNodeMetadataTest().expectedServer(),
				"az-1.region-a.geo-1");
		ServerInRegion withHost = new ServerInRegion(new ParseServerTest().expected(), "az-1.region-a.geo-1");

		ServerInRegionToNodeMetadata converter = new ServerInRegionToNodeMetadata(
				NovaComputeServiceContextModule.toPortableNodeStatus, locationIndex,
				Suppliers.<Set<? extends Image>>ofInstance(ImmutableSet.<Image>of()),
				Suppliers.<Set<? extends Hardware>>ofInstance(ImmutableSet.<Hardware>of()), namingConvention);

		Set<? extends NodeMetadata> set = ImmutableSet.of(converter.apply(withHost), converter.apply(withoutHost));

		assertEquals(new OrphanedGroupsByRegionId(Predicates.<RegionAndName>alwaysTrue()).apply(set),
				ImmutableMultimap.<String, String>builder().putAll("az-1.region-a.geo-1", "sample", "test").build());
	}

	@Test
	public void testWhenComputeServiceSaysAllNodesAreDeadNoGroupsAreReturned() {

		ServerInRegion withoutHost = new ServerInRegion(new ServerInRegionToNodeMetadataTest().expectedServer(),
				"az-1.region-a.geo-1");
		ServerInRegion withHost = new ServerInRegion(new ParseServerTest().expected(), "az-1.region-a.geo-1");

		ServerInRegionToNodeMetadata converter = new ServerInRegionToNodeMetadata(
				NovaComputeServiceContextModule.toPortableNodeStatus, locationIndex,
				Suppliers.<Set<? extends Image>>ofInstance(ImmutableSet.<Image>of()),
				Suppliers.<Set<? extends Hardware>>ofInstance(ImmutableSet.<Hardware>of()), namingConvention);

		Set<? extends NodeMetadata> set = ImmutableSet.of(converter.apply(withHost), converter.apply(withoutHost));

		assertEquals(new OrphanedGroupsByRegionId(Predicates.<RegionAndName>alwaysFalse()).apply(set),
				ImmutableMultimap.<String, String>of());

	}
}
