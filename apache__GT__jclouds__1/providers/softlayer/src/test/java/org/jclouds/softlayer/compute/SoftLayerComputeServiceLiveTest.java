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
package org.jclouds.softlayer.compute;

import java.util.Properties;

import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.internal.BaseComputeServiceLiveTest;
import org.jclouds.sshj.config.SshjSshClientModule;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Module;

@Test(groups = "live", enabled = true, singleThreaded = true)
public class SoftLayerComputeServiceLiveTest extends BaseComputeServiceLiveTest {

	public SoftLayerComputeServiceLiveTest() {
		provider = "softlayer";
		group = "soft-layer";
	}

	@Override
	protected Module getSshModule() {
		return new SshjSshClientModule();
	}

	// softlayer does not support metadata
	@Override
	protected void checkUserMetadataContains(NodeMetadata node, ImmutableMap<String, String> userMetadata) {
		assert node.getUserMetadata().equals(ImmutableMap.<String, String>of())
				: String.format("node userMetadata did not match %s %s", userMetadata, node);
	}

	@Override
	public void testOptionToNotBlock() {
		// start call is blocking anyway.
	}

	@Override
	protected Properties setupProperties() {
		Properties properties = super.setupProperties();
		properties.setProperty("jclouds.ssh.max-retries", "20");
		return properties;
	}
}
