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
package org.jclouds.cloudstack.compute;

import static com.google.common.collect.Iterables.getFirst;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.util.Map;
import java.util.Random;

import org.jclouds.cloudstack.compute.options.CloudStackTemplateOptions;
import org.jclouds.cloudstack.compute.strategy.CloudStackComputeServiceAdapter;
import org.jclouds.cloudstack.domain.IPForwardingRule;
import org.jclouds.cloudstack.domain.ServiceOffering;
import org.jclouds.cloudstack.domain.VirtualMachine;
import org.jclouds.cloudstack.internal.BaseCloudStackApiLiveTest;
import org.jclouds.cloudstack.predicates.TemplatePredicates;
import org.jclouds.compute.ComputeServiceAdapter.NodeAndInitialCredentials;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.functions.DefaultCredentialsFromImageOrOverridingCredentials;
import org.jclouds.compute.strategy.PrioritizeCredentialsFromTemplate;
import org.jclouds.domain.Credentials;
import org.jclouds.ssh.SshKeys;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.net.HostAndPort;
import com.google.common.net.InetAddresses;

@Test(groups = "live", singleThreaded = true, testName = "CloudStackComputeServiceAdapterLiveTest")
public class CloudStackComputeServiceAdapterLiveTest extends BaseCloudStackApiLiveTest {

	private CloudStackComputeServiceAdapter adapter;
	private NodeAndInitialCredentials<VirtualMachine> vm;

	private String keyPairName;
	private Map<String, String> keyPair;
	Map<String, Credentials> credentialStore = Maps.newLinkedHashMap();

	@BeforeGroups(groups = { "live" })
	public void setupContext() {
		super.setupContext();

		adapter = context.utils().injector().getInstance(CloudStackComputeServiceAdapter.class);

		keyPairName = prefix + "-adapter-test-keypair";
		keyPair = SshKeys.generate();

		client.getSSHKeyPairApi().deleteSSHKeyPair(keyPairName);
		client.getSSHKeyPairApi().registerSSHKeyPair(keyPairName, keyPair.get("public"));
	}

	@Test
	public void testListLocations() {
		assertFalse(Iterables.isEmpty(adapter.listLocations()));
	}

	private static final PrioritizeCredentialsFromTemplate prioritizeCredentialsFromTemplate = new PrioritizeCredentialsFromTemplate(
			new DefaultCredentialsFromImageOrOverridingCredentials());

	@Test
	public void testCreateNodeWithGroupEncodedIntoName() {
		String group = prefix + "-foo";
		String name = group + "-node-" + new Random().nextInt();
		Template template = view.getComputeService().templateBuilder().build();

		if (!client.getTemplateApi().getTemplateInZone(template.getImage().getId(), template.getLocation().getId())
				.isPasswordEnabled()) {

			// TODO: look at SecurityGroupApiLiveTest for how to do this
			template.getOptions().as(CloudStackTemplateOptions.class).keyPair(keyPairName);
		}
		vm = adapter.createNodeWithGroupEncodedIntoName(group, name, template);

		assertEquals(vm.getNode().getDisplayName(), name);
		// check to see if we setup a NAT rule (conceding we could check this from
		// cache)
		IPForwardingRule rule = getFirst(client.getNATApi().getIPForwardingRulesForVirtualMachine(vm.getNode().getId()),
				null);

		String address = rule != null ? rule.getIPAddress() : vm.getNode().getIPAddress();

		loginCredentials = prioritizeCredentialsFromTemplate.apply(template, vm.getCredentials());

		assert InetAddresses.isInetAddress(address) : vm;
		HostAndPort socket = HostAndPort.fromParts(address, 22);
		checkSSH(socket);
	}

	@Test
	public void testListHardwareProfiles() {
		Iterable<ServiceOffering> profiles = adapter.listHardwareProfiles();
		assertFalse(Iterables.isEmpty(profiles));

		for (ServiceOffering profile : profiles) {
			// TODO: check that the results are valid
		}
	}

	@Test
	public void testListImages() {
		Iterable<org.jclouds.cloudstack.domain.Template> templates = adapter.listImages();
		assertFalse(Iterables.isEmpty(templates));

		for (org.jclouds.cloudstack.domain.Template template : templates) {
			assert TemplatePredicates.isReady().apply(template) : template;
		}
	}

	@AfterGroups(groups = "live")
	@Override
	protected void tearDownContext() {
		if (vm != null)
			adapter.destroyNode(vm.getNodeId());
		super.tearDownContext();
	}
}
