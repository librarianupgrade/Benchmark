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
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;

import org.jclouds.http.HttpResponseException;
import org.jclouds.rackspace.cloudloadbalancers.v1.domain.HistoricalUsage;
import org.jclouds.rackspace.cloudloadbalancers.v1.domain.LoadBalancer;
import org.jclouds.rackspace.cloudloadbalancers.v1.domain.CreateLoadBalancer;
import org.jclouds.rackspace.cloudloadbalancers.v1.domain.LoadBalancerStats;
import org.jclouds.rackspace.cloudloadbalancers.v1.domain.LoadBalancerUsage;
import org.jclouds.rackspace.cloudloadbalancers.v1.domain.AddNode;
import org.jclouds.rackspace.cloudloadbalancers.v1.domain.Protocol;
import org.jclouds.rackspace.cloudloadbalancers.v1.domain.VirtualIP.Type;
import org.jclouds.rackspace.cloudloadbalancers.v1.internal.BaseCloudLoadBalancersApiLiveTest;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.Test;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;

@Test(groups = "live", singleThreaded = true, testName = "ReportApiLiveTest")
public class ReportApiLiveTest extends BaseCloudLoadBalancersApiLiveTest {
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
	public void testReports() throws Exception {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, -1);
		Date yesterday = calendar.getTime();
		Date today = new Date();

		FluentIterable<LoadBalancer> loadBalancers = api.getReportApi(region)
				.listBillableLoadBalancers(yesterday, today).concat();
		assertNotNull(loadBalancers);

		HistoricalUsage historicalUsage = api.getReportApi(region).getHistoricalUsage(yesterday, today);
		assertNotEquals(historicalUsage.getAccountId(), 0);

		FluentIterable<LoadBalancerUsage> loadBalancerUsages = api.getReportApi(region)
				.listLoadBalancerUsage(lb.getId(), yesterday, today).concat();
		assertNotNull(loadBalancerUsages);

		loadBalancerUsages = api.getReportApi(region).listCurrentLoadBalancerUsage(lb.getId()).concat();
		assertNotNull(loadBalancerUsages);

		try {
			LoadBalancerStats loadBalancerStats = api.getReportApi(region).getLoadBalancerStats(lb.getId());
			assertNotNull(loadBalancerStats);
		} catch (HttpResponseException e) {
			// CLB sometimes doesn't like it when you get stats on a newly created LB so ignore a 500
			if (e.getResponse().getStatusCode() != 500) {
				throw e;
			}
		}

		Iterable<Protocol> protocols = api.getReportApi(region).listProtocols();
		assertTrue(!Iterables.isEmpty(protocols));

		Iterable<String> algorithms = api.getReportApi(region).listAlgorithms();
		assertTrue(!Iterables.isEmpty(algorithms));
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
