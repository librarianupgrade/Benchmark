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
package org.jclouds.profitbricks.binder.loadbalancer;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import com.google.common.collect.ImmutableList;

import org.jclouds.profitbricks.domain.LoadBalancer;
import org.testng.annotations.Test;

@Test(groups = "unit", testName = "DeregisterLoadBalancerRequestBinderTest")
public class DeregisterLoadBalancerRequestBinderTest {

	@Test
	public void testDeregisterPayload() {
		DeregisterLoadBalancerRequestBinder binder = new DeregisterLoadBalancerRequestBinder();

		String actual = binder.createPayload(
				LoadBalancer.Request.createDeregisteringPayload("load-balancer-id", ImmutableList.of("1", "2")));

		assertNotNull(actual, "Binder returned null payload");
		assertEquals(expectedPayload, actual);
	}

	private final String expectedPayload = ("        <ws:deregisterServersOnLoadBalancer>\n"
			+ "                <serverIds>1</serverIds>\n" + "                <serverIds>2</serverIds>\n"
			+ "                <loadBalancerId>load-balancer-id</loadBalancerId>\n"
			+ "        </ws:deregisterServersOnLoadBalancer>").replaceAll("\\s+", "");
}
