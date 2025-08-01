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

import static com.google.common.base.Predicates.compose;
import static com.google.common.base.Predicates.in;
import static com.google.common.collect.Iterables.all;
import static com.google.common.collect.Iterables.getLast;
import static com.google.common.collect.Iterables.getOnlyElement;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Set;

import org.jclouds.aws.AWSResponseException;
import org.jclouds.compute.internal.BaseComputeServiceContextLiveTest;
import org.jclouds.ec2.EC2Api;
import org.jclouds.ec2.domain.SecurityGroup;
import org.jclouds.ec2.domain.UserIdGroupPair;
import org.jclouds.net.domain.IpPermission;
import org.jclouds.net.domain.IpProtocol;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;

/**
 * Tests behavior of {@code SecurityGroupApi}
 */
@Test(groups = "live", singleThreaded = true, testName = "SecurityGroupApiLiveTest")
public class SecurityGroupApiLiveTest extends BaseComputeServiceContextLiveTest {
	public SecurityGroupApiLiveTest() {
		provider = "ec2";
	}

	private EC2Api ec2Api;
	protected SecurityGroupApi client;

	@Override
	@BeforeClass(groups = { "integration", "live" })
	public void setupContext() {
		super.setupContext();
		ec2Api = view.unwrapApi(EC2Api.class);
		client = ec2Api.getSecurityGroupApi().get();
	}

	@Test
	void testDescribe() {
		for (String region : ec2Api.getConfiguredRegions()) {
			Set<SecurityGroup> allResults = client.describeSecurityGroupsInRegion(region);
			assertNotNull(allResults);
			if (!allResults.isEmpty()) {
				final SecurityGroup group = getLast(allResults);
				// in case there are multiple groups with the same name, which is the case with VPC
				ImmutableSet<SecurityGroup> expected = FluentIterable.from(allResults)
						.filter(new Predicate<SecurityGroup>() {
							@Override
							public boolean apply(SecurityGroup in) {
								return group.getName().equals(in.getName());
							}
						}).toSet();
				ImmutableSet<SecurityGroup> result = ImmutableSet
						.copyOf(client.describeSecurityGroupsInRegion(region, group.getName()));
				// the above command has a chance of returning less groups than the original
				assertTrue(expected.containsAll(result), "group(s) for name not found");
			}
		}
	}

	@Test
	void testFilter() {
		for (String region : ec2Api.getConfiguredRegions()) {
			Set<SecurityGroup> allResults = client.describeSecurityGroupsInRegion(region);
			assertNotNull(allResults);
			if (!allResults.isEmpty()) {
				final SecurityGroup group = getLast(allResults);
				// in case there are multiple groups with the same name, which is the case with VPC
				ImmutableSet<SecurityGroup> expected = FluentIterable.from(allResults)
						.filter(new Predicate<SecurityGroup>() {
							@Override
							public boolean apply(SecurityGroup in) {
								return group.getName().equals(in.getName());
							}
						}).toSet();
				ImmutableSet<SecurityGroup> result = ImmutableSet
						.copyOf(client.describeSecurityGroupsInRegionWithFilter(region, ImmutableMultimap
								.<String, String>builder().put("group-name", group.getName()).build()));
				// the above command has a chance of returning less groups than the original
				assertTrue(expected.containsAll(result), "group(s) for name not found");
			}
		}
	}

	@Test(expectedExceptions = AWSResponseException.class)
	void testFilterInvalid() {
		for (String region : ec2Api.getConfiguredRegions()) {
			Set<SecurityGroup> allResults = client.describeSecurityGroupsInRegion(region);
			assertNotNull(allResults);
			if (!allResults.isEmpty()) {
				final SecurityGroup group = getLast(allResults);
				// in case there are multiple groups with the same name, which is the case with VPC
				ImmutableSet<SecurityGroup> expected = FluentIterable.from(allResults)
						.filter(new Predicate<SecurityGroup>() {
							@Override
							public boolean apply(SecurityGroup in) {
								return group.getName().equals(in.getName());
							}
						}).toSet();
				ImmutableSet<SecurityGroup> result = ImmutableSet
						.copyOf(client.describeSecurityGroupsInRegionWithFilter(region, ImmutableMultimap
								.<String, String>builder().put("invalid-filter", group.getName()).build()));
			}
		}
	}

	@Test
	void testCreateSecurityGroup() {
		String groupName = PREFIX + "1";
		cleanupAndSleep(groupName);
		try {
			String groupDescription = PREFIX + "1 description";
			client.deleteSecurityGroupInRegion(null, groupName);
			client.createSecurityGroupInRegion(null, groupName, groupDescription);
			verifySecurityGroup(groupName, groupDescription);
		} finally {
			client.deleteSecurityGroupInRegion(null, groupName);
		}
	}

	protected void cleanupAndSleep(String groupName) {
		try {
			client.deleteSecurityGroupInRegion(null, groupName);
			Thread.sleep(2000);
		} catch (Exception e) {

		}
	}

	@Test
	void testAuthorizeSecurityGroupIngressCidr() {
		String groupName = PREFIX + "ingress";
		cleanupAndSleep(groupName);
		try {
			client.createSecurityGroupInRegion(null, groupName, groupName);
			client.authorizeSecurityGroupIngressInRegion(null, groupName, IpProtocol.TCP, 80, 80, "0.0.0.0/0");
			assertEventually(new GroupHasPermission(client, groupName, new TCPPort80AllIPs()));

			client.revokeSecurityGroupIngressInRegion(null, groupName, IpProtocol.TCP, 80, 80, "0.0.0.0/0");
			assertEventually(new GroupHasNoPermissions(client, groupName));
		} finally {
			client.deleteSecurityGroupInRegion(null, groupName);
		}
	}

	@Test
	void testAuthorizeSecurityGroupIngressSourcePort() {
		String groupName = PREFIX + "ingress";
		cleanupAndSleep(groupName);
		try {
			client.createSecurityGroupInRegion(null, groupName, groupName);
			client.authorizeSecurityGroupIngressInRegion(null, groupName, IpProtocol.TCP, 80, 80, "0.0.0.0/0");
			assertEventually(new GroupHasPermission(client, groupName, new TCPPort80AllIPs()));

			client.revokeSecurityGroupIngressInRegion(null, groupName, IpProtocol.TCP, 80, 80, "0.0.0.0/0");
			assertEventually(new GroupHasNoPermissions(client, groupName));
		} finally {
			client.deleteSecurityGroupInRegion(null, groupName);
		}
	}

	private void verifySecurityGroup(String groupName, String description) {
		Set<SecurityGroup> oneResult = client.describeSecurityGroupsInRegion(null, groupName);
		assertNotNull(oneResult);
		assertEquals(oneResult.size(), 1);
		SecurityGroup listPair = oneResult.iterator().next();
		assertEquals(listPair.getName(), groupName);
		assertEquals(listPair.getDescription(), description);
	}

	@Test
	void testAuthorizeSecurityGroupIngressSourceGroup() {
		final String group1Name = PREFIX + "ingress1";
		String group2Name = PREFIX + "ingress2";
		cleanupAndSleep(group2Name);
		cleanupAndSleep(group1Name);
		try {
			client.createSecurityGroupInRegion(null, group1Name, group1Name);
			client.createSecurityGroupInRegion(null, group2Name, group2Name);
			ensureGroupsExist(group1Name, group2Name);
			client.authorizeSecurityGroupIngressInRegion(null, group1Name, IpProtocol.TCP, 80, 80, "0.0.0.0/0");
			assertEventually(new GroupHasPermission(client, group1Name, new TCPPort80AllIPs()));
			Set<SecurityGroup> oneResult = client.describeSecurityGroupsInRegion(null, group1Name);
			assertNotNull(oneResult);
			assertEquals(oneResult.size(), 1);
			final SecurityGroup group = oneResult.iterator().next();
			assertEquals(group.getName(), group1Name);
			final UserIdGroupPair to = new UserIdGroupPair(group.getOwnerId(), group1Name);
			client.authorizeSecurityGroupIngressInRegion(null, group2Name, to);
			assertEventually(new GroupHasPermission(client, group2Name, new Predicate<IpPermission>() {
				@Override
				public boolean apply(IpPermission arg0) {
					return arg0.getTenantIdGroupNamePairs()
							.equals(ImmutableMultimap.of(group.getOwnerId(), group1Name));
				}
			}));

			client.revokeSecurityGroupIngressInRegion(null, group2Name,
					new UserIdGroupPair(group.getOwnerId(), group1Name));
			assertEventually(new GroupHasNoPermissions(client, group2Name));
		} finally {
			client.deleteSecurityGroupInRegion(null, group2Name);
			client.deleteSecurityGroupInRegion(null, group1Name);
		}
	}

	public static final class TCPPort80AllIPs implements Predicate<IpPermission> {
		@Override
		public boolean apply(IpPermission arg0) {
			return arg0.getIpProtocol() == IpProtocol.TCP && arg0.getFromPort() == 80 && arg0.getToPort() == 80
					&& arg0.getCidrBlocks().equals(ImmutableSet.of("0.0.0.0/0"));
		}
	}

	public static final class GroupHasPermission implements Runnable {
		private final SecurityGroupApi client;
		private final String group;
		private final Predicate<IpPermission> permission;

		public GroupHasPermission(SecurityGroupApi client, String group, Predicate<IpPermission> permission) {
			this.client = client;
			this.group = group;
			this.permission = permission;
		}

		public void run() {
			try {
				Set<SecurityGroup> oneResult = client.describeSecurityGroupsInRegion(null, group);
				assert all(getOnlyElement(oneResult), permission) : permission + ": " + oneResult;
			} catch (Exception e) {
				throw new AssertionError(e);
			}
		}
	}

	public static final class GroupHasNoPermissions implements Runnable {
		private final SecurityGroupApi client;
		private final String group;

		public GroupHasNoPermissions(SecurityGroupApi client, String group) {
			this.client = client;
			this.group = group;
		}

		public void run() {
			try {
				Set<SecurityGroup> oneResult = client.describeSecurityGroupsInRegion(null, group);
				assertNotNull(oneResult);
				assertEquals(oneResult.size(), 1);
				SecurityGroup listPair = oneResult.iterator().next();
				assertEquals(listPair.size(), 0);
			} catch (Exception e) {
				throw new AssertionError(e);
			}
		}
	}

	protected void ensureGroupsExist(String group1Name, String group2Name) {
		Set<SecurityGroup> twoResults = client.describeSecurityGroupsInRegion(null, group1Name, group2Name);
		assertNotNull(twoResults);
		assertTrue(twoResults.size() >= 2); // in VPC could be multiple groups with the same name

		assertTrue(all(twoResults,
				compose(in(ImmutableSet.of(group1Name, group2Name)), new Function<SecurityGroup, String>() {
					@Override
					public String apply(SecurityGroup in) {
						return in.getName();
					}
				})));
	}

	private static final int INCONSISTENCY_WINDOW = 5000;

	/**
	* Due to eventual consistency, container commands may not return correctly
	* immediately. Hence, we will try up to the inconsistency window to see if
	* the assertion completes.
	*/
	protected static void assertEventually(Runnable assertion) {
		long start = System.currentTimeMillis();
		AssertionError error = null;
		for (int i = 0; i < 30; i++) {
			try {
				assertion.run();
				if (i > 0)
					System.err.printf("%d attempts and %dms asserting %s%n", i + 1, System.currentTimeMillis() - start,
							assertion.getClass().getSimpleName());
				return;
			} catch (AssertionError e) {
				error = e;
			}
			try {
				Thread.sleep(INCONSISTENCY_WINDOW / 30);
			} catch (InterruptedException e) {
			}
		}
		if (error != null)
			throw error;

	}

	public static final String PREFIX = System.getProperty("user.name") + "-ec2";

}
