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
package org.jclouds.rackspace.cloudloadbalancers.v1.features;

import static org.jclouds.rackspace.cloudloadbalancers.v1.predicates.LoadBalancerPredicates.awaitAvailable;
import static org.jclouds.rackspace.cloudloadbalancers.v1.predicates.LoadBalancerPredicates.awaitDeleted;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import org.jclouds.rackspace.cloudloadbalancers.v1.domain.ConnectionThrottle;
import org.jclouds.rackspace.cloudloadbalancers.v1.domain.LoadBalancer;
import org.jclouds.rackspace.cloudloadbalancers.v1.domain.CreateLoadBalancer;
import org.jclouds.rackspace.cloudloadbalancers.v1.domain.AddNode;
import org.jclouds.rackspace.cloudloadbalancers.v1.domain.VirtualIP.Type;
import org.jclouds.rackspace.cloudloadbalancers.v1.internal.BaseCloudLoadBalancersApiLiveTest;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.Test;

import com.google.common.collect.Iterables;

@Test(groups = "live", singleThreaded = true, testName = "ConnectionApiLiveTest")
public class ConnectionApiLiveTest extends BaseCloudLoadBalancersApiLiveTest {
	private LoadBalancer lb;
	private String region;

	public void testCreateLoadBalancer() {
		AddNode addNode = AddNode.builder().address("192.168.1.1").port(8080).build();
		CreateLoadBalancer createLB = CreateLoadBalancer.builder().name(prefix + "-jclouds").protocol("HTTP").port(80)
				.virtualIPType(Type.PUBLIC).node(addNode).build();

		region = Iterables.getFirst(api.getConfiguredRegions(), null);
		lb = api.getLoadBalancerApi(region).create(createLB);

		assertTrue(awaitAvailable(api.getLoadBalancerApi(region)).apply(lb));
	}

	@Test(dependsOnMethods = "testCreateLoadBalancer")
	public void testCreateAndGetConnectionThrottling() throws Exception {
		api.getConnectionApi(region, lb.getId())
				.createOrUpdateConnectionThrottle(ConnectionApiExpectTest.getConnectionThrottle());
		assertTrue(awaitAvailable(api.getLoadBalancerApi(region)).apply(lb));

		ConnectionThrottle connectionThrottle = api.getConnectionApi(region, lb.getId()).getConnectionThrottle();

		assertEquals(connectionThrottle, ConnectionApiExpectTest.getConnectionThrottle());
	}

	@Test(dependsOnMethods = "testCreateAndGetConnectionThrottling")
	public void testRemoveAndGetConnectionThrottle() throws Exception {
		assertTrue(api.getConnectionApi(region, lb.getId()).deleteConnectionThrottle());
		assertTrue(awaitAvailable(api.getLoadBalancerApi(region)).apply(lb));

		ConnectionThrottle connectionThrottle = api.getConnectionApi(region, lb.getId()).getConnectionThrottle();

		assertNull(connectionThrottle);
	}

	@Test(dependsOnMethods = "testRemoveAndGetConnectionThrottle")
	public void testEnableAndIsConnectionLogging() throws Exception {
		api.getConnectionApi(region, lb.getId()).enableConnectionLogging();
		assertTrue(awaitAvailable(api.getLoadBalancerApi(region)).apply(lb));

		boolean isConnectionLogging = api.getConnectionApi(region, lb.getId()).isConnectionLogging();

		assertTrue(isConnectionLogging);
	}

	@Test(dependsOnMethods = "testEnableAndIsConnectionLogging")
	public void testDisableAndIsConnectionLogging() throws Exception {
		api.getConnectionApi(region, lb.getId()).disableConnectionLogging();
		assertTrue(awaitAvailable(api.getLoadBalancerApi(region)).apply(lb));

		boolean isConnectionLogging = api.getConnectionApi(region, lb.getId()).isConnectionLogging();

		assertFalse(isConnectionLogging);
	}

	@Override
	@AfterGroups(groups = "live")
	protected void tearDown() {
		assertTrue(awaitAvailable(api.getLoadBalancerApi(region)).apply(lb));
		api.getLoadBalancerApi(region).delete(lb.getId());
		assertTrue(awaitDeleted(api.getLoadBalancerApi(region)).apply(lb));
		super.tearDown();
	}
}
