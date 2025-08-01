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
package org.jclouds.aws.ec2.features;

import static com.google.common.collect.Iterables.getOnlyElement;
import static com.google.common.collect.Sets.newTreeSet;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.jclouds.util.Predicates2.retry;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

import java.util.Set;
import java.util.SortedSet;

import org.jclouds.aws.AWSResponseException;
import org.jclouds.aws.domain.Region;
import org.jclouds.aws.ec2.AWSEC2Api;
import org.jclouds.aws.ec2.domain.PlacementGroup;
import org.jclouds.aws.ec2.domain.PlacementGroup.State;
import org.jclouds.aws.ec2.predicates.PlacementGroupAvailable;
import org.jclouds.aws.ec2.predicates.PlacementGroupDeleted;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.OsFamily;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.internal.BaseComputeServiceContextLiveTest;
import org.jclouds.compute.predicates.NodePredicates;
import org.jclouds.ec2.compute.domain.EC2HardwareBuilder;
import org.jclouds.ec2.domain.InstanceType;
import org.jclouds.scriptbuilder.domain.Statements;
import org.jclouds.scriptbuilder.statements.java.InstallJDK;
import org.jclouds.scriptbuilder.statements.login.AdminAccess;
import org.jclouds.sshj.config.SshjSshClientModule;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMultimap;
import com.google.inject.Module;

/**
 * Tests behavior of {@code PlacementGroupApi}
 */
@Test(groups = "live", singleThreaded = true, testName = "PlacementGroupApiLiveTest")
public class PlacementGroupApiLiveTest extends BaseComputeServiceContextLiveTest {
	private final Set<String> supportedRegions = Region.DEFAULT_REGIONS;

	public PlacementGroupApiLiveTest() {
		provider = "aws-ec2";
	}

	private AWSEC2Api client;
	private Predicate<PlacementGroup> availableTester;
	private Predicate<PlacementGroup> deletedTester;
	private PlacementGroup group;

	@Override
	@BeforeClass(groups = { "integration", "live" })
	public void setupContext() {
		super.setupContext();
		client = view.unwrapApi(AWSEC2Api.class);
		availableTester = retry(new PlacementGroupAvailable(client), 60, 1, SECONDS);
		deletedTester = retry(new PlacementGroupDeleted(client), 60, 1, SECONDS);
	}

	@Test
	public void testDescribe() {
		for (String region : supportedRegions) {
			SortedSet<PlacementGroup> allResults = newTreeSet(
					client.getPlacementGroupApi().get().describePlacementGroupsInRegion(region));
			assertNotNull(allResults);
			if (allResults.size() >= 1) {
				PlacementGroup group = allResults.last();
				SortedSet<PlacementGroup> result = newTreeSet(
						client.getPlacementGroupApi().get().describePlacementGroupsInRegion(region, group.getName()));
				assertNotNull(result);
				PlacementGroup compare = result.last();
				assertEquals(compare, group);
			}
		}

		for (String region : client.getAvailabilityZoneAndRegionApi().get().describeRegions().keySet()) {
			if (!supportedRegions.contains(region))
				try {
					client.getPlacementGroupApi().get().describePlacementGroupsInRegion(region);
					fail("should be unsupported for region: " + region);
				} catch (UnsupportedOperationException e) {
				}
		}
	}

	@Test
	public void testFilter() {
		for (String region : supportedRegions) {
			SortedSet<PlacementGroup> allResults = newTreeSet(
					client.getPlacementGroupApi().get().describePlacementGroupsInRegion(region));
			assertNotNull(allResults);
			if (allResults.size() >= 1) {
				PlacementGroup group = allResults.last();
				SortedSet<PlacementGroup> result = newTreeSet(client.getPlacementGroupApi().get()
						.describePlacementGroupsInRegionWithFilter(region, ImmutableMultimap.<String, String>builder()
								.put("group-name", group.getName()).build()));
				assertNotNull(result);
				PlacementGroup compare = result.last();
				assertEquals(compare, group);
			}
		}
	}

	@Test(expectedExceptions = AWSResponseException.class)
	public void testFilterInvalid() {
		for (String region : supportedRegions) {
			SortedSet<PlacementGroup> allResults = newTreeSet(
					client.getPlacementGroupApi().get().describePlacementGroupsInRegion(region));
			assertNotNull(allResults);
			if (allResults.size() >= 1) {
				PlacementGroup group = allResults.last();
				client.getPlacementGroupApi().get().describePlacementGroupsInRegionWithFilter(region,
						ImmutableMultimap.<String, String>builder().put("invalid-filter", group.getName()).build());
			}
		}
	}

	@Test
	public void testCreatePlacementGroup() {
		String groupName = PREFIX + "1";
		for (String region : supportedRegions) {

			client.getPlacementGroupApi().get().deletePlacementGroupInRegion(region, groupName);
			client.getPlacementGroupApi().get().createPlacementGroupInRegion(region, groupName);

			verifyPlacementGroup(region, groupName);
		}
	}

	private void verifyPlacementGroup(String region, String groupName) {
		assert availableTester.apply(new PlacementGroup(region, groupName, "cluster", State.PENDING)) : group;
		Set<PlacementGroup> oneResult = client.getPlacementGroupApi().get().describePlacementGroupsInRegion(region,
				groupName);
		assertNotNull(oneResult);
		assertEquals(oneResult.size(), 1);
		group = oneResult.iterator().next();
		assertEquals(group.getName(), groupName);
		assertEquals(group.getStrategy(), "cluster");
		assert availableTester.apply(group) : group;
	}

	public void testStartHS1Instance() throws Exception {

		Template template = view.getComputeService().templateBuilder()
				.fromHardware(EC2HardwareBuilder.hs1_8xlarge().build()).osFamily(OsFamily.AMZN_LINUX).build();
		assert template != null : "The returned template was null, but it should have a value.";
		assertEquals(template.getHardware().getProviderId(), InstanceType.HS1_8XLARGE);
		assertEquals(template.getImage().getUserMetadata().get("virtualizationType"), "hvm");
		assertEquals(template.getImage().getUserMetadata().get("hypervisor"), "xen");

		template.getOptions().runScript(Statements.newStatementList(AdminAccess.standard(), InstallJDK.fromOpenJDK()));

		String group = PREFIX + "cccluster";
		view.getComputeService().destroyNodesMatching(NodePredicates.inGroup(group));
		// TODO make this not lookup an explicit region
		client.getPlacementGroupApi().get().deletePlacementGroupInRegion(null, "jclouds#" + group + "#us-east-1");

		try {
			Set<? extends NodeMetadata> nodes = view.getComputeService().createNodesInGroup(group, 1, template);
			NodeMetadata node = getOnlyElement(nodes);

			getOnlyElement(getOnlyElement(
					client.getInstanceApi().get().describeInstancesInRegion(null, node.getProviderId())));

		} catch (RunNodesException e) {
			System.err.println(e.getNodeErrors().keySet());
			Throwables.propagate(e);
		} finally {
			view.getComputeService().destroyNodesMatching(NodePredicates.inGroup(group));
		}
	}

	public static final String PREFIX = System.getProperty("user.name") + "ec2";

	@Override
	@AfterClass(groups = { "integration", "live" })
	protected void tearDownContext() {
		if (group != null) {
			client.getPlacementGroupApi().get().deletePlacementGroupInRegion(group.getRegion(), group.getName());
			assert deletedTester.apply(group) : group;
		}
		super.tearDownContext();
	}

	@Override
	protected Module getSshModule() {
		return new SshjSshClientModule();
	}
}
