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
package org.jclouds.openstack.swift.v1.blobstore.integration;

import static org.jclouds.openstack.keystone.v2_0.config.KeystoneProperties.CREDENTIAL_TYPE;

import java.util.Properties;

import org.jclouds.blobstore.integration.internal.BaseContainerLiveTest;
import org.testng.annotations.Test;

@Test(groups = "live", testName = "SwiftContainerLiveTest")
public class SwiftContainerLiveTest extends BaseContainerLiveTest {

	public SwiftContainerLiveTest() {
		provider = "openstack-swift";
	}

	@Override
	protected Properties setupProperties() {
		Properties props = super.setupProperties();
		setIfTestSystemPropertyPresent(props, CREDENTIAL_TYPE);
		return props;
	}
}
