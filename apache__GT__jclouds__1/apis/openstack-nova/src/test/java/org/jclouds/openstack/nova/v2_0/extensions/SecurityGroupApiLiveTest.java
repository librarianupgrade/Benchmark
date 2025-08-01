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
package org.jclouds.openstack.nova.v2_0.extensions;

import static org.testng.Assert.assertNotNull;

import java.util.Set;

import org.jclouds.net.domain.IpProtocol;
import org.jclouds.openstack.nova.v2_0.domain.Ingress;
import org.jclouds.openstack.nova.v2_0.domain.SecurityGroup;
import org.jclouds.openstack.nova.v2_0.domain.SecurityGroupRule;
import org.jclouds.openstack.nova.v2_0.internal.BaseNovaApiLiveTest;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;

/**
 * Tests behavior of {@code SecurityGroupApi}
 */
@Test(groups = "live", testName = "SecurityGroupApiLiveTest", singleThreaded = true)
public class SecurityGroupApiLiveTest extends BaseNovaApiLiveTest {

	public static final String SECURITY_GROUP_NAME = "testsg";

	public void list() throws Exception {
		for (String regionId : api.getConfiguredRegions()) {
			SecurityGroupApi securityGroupApi = api.getSecurityGroupApi(regionId).get();
			Set<? extends SecurityGroup> securityGroupsList = securityGroupApi.list().toSet();
			assertNotNull(securityGroupsList);
		}
	}

	public void createGetAndDeleteSecurityGroup() throws Exception {
		for (String regionId : api.getConfiguredRegions()) {
			SecurityGroupApi securityGroupApi = api.getSecurityGroupApi(regionId).get();
			SecurityGroup securityGroup = null;
			String id;
			try {
				securityGroup = securityGroupApi.createWithDescription(SECURITY_GROUP_NAME, "test security group");
				assertNotNull(securityGroup);
				id = securityGroup.getId();
				SecurityGroup theGroup = securityGroupApi.get(id);
				assertNotNull(theGroup);
			} finally {
				if (securityGroup != null) {
					securityGroupApi.delete(securityGroup.getId());
				}
			}
		}
	}

	public void createAndDeleteSecurityGroupRule() throws Exception {
		for (String regionId : api.getConfiguredRegions()) {
			SecurityGroupApi securityGroupApi = api.getSecurityGroupApi(regionId).get();
			SecurityGroup securityGroup = null;

			try {
				securityGroup = securityGroupApi.createWithDescription(SECURITY_GROUP_NAME, "test security group");
				assertNotNull(securityGroup);

				for (int port : ImmutableSet.of(22, 8080)) {
					SecurityGroupRule rule = securityGroupApi.createRuleAllowingCidrBlock(securityGroup.getId(),
							Ingress.builder().ipProtocol(IpProtocol.TCP).fromPort(port).toPort(port).build(),
							"0.0.0.0/0");
					assertNotNull(rule);

					SecurityGroupRule rule2 = securityGroupApi.createRuleAllowingSecurityGroupId(securityGroup.getId(),
							Ingress.builder().ipProtocol(IpProtocol.TCP).fromPort(port).toPort(port).build(),
							securityGroup.getId());

					assertNotNull(rule2);
				}
				securityGroup = securityGroupApi.get(securityGroup.getId());

			} finally {
				if (securityGroup != null) {
					securityGroupApi.delete(securityGroup.getId());
				}
			}
		}

	}
}
