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
package org.jclouds.ec2.compute;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.domain.TemplateBuilderSpec;
import org.jclouds.compute.internal.BaseComputeServiceLiveTest;
import org.jclouds.compute.options.TemplateOptions;
import org.jclouds.compute.predicates.NodePredicates;
import org.jclouds.domain.Location;
import org.jclouds.domain.LocationScope;
import org.jclouds.domain.LoginCredentials;
import org.jclouds.ec2.EC2Api;
import org.jclouds.ec2.compute.options.EC2TemplateOptions;
import org.jclouds.ec2.domain.BlockDevice;
import org.jclouds.ec2.domain.KeyPair;
import org.jclouds.ec2.domain.PublicIpInstanceIdPair;
import org.jclouds.ec2.domain.RunningInstance;
import org.jclouds.ec2.domain.SecurityGroup;
import org.jclouds.ec2.domain.Snapshot;
import org.jclouds.ec2.domain.Volume;
import org.jclouds.ec2.features.ElasticBlockStoreApi;
import org.jclouds.ec2.features.InstanceApi;
import org.jclouds.ec2.features.KeyPairApi;
import org.jclouds.ec2.features.SecurityGroupApi;
import org.jclouds.ec2.reference.EC2Constants;
import org.jclouds.net.domain.IpProtocol;
import org.jclouds.scriptbuilder.domain.Statements;
import org.jclouds.sshj.config.SshjSshClientModule;
import org.jclouds.util.InetAddresses2;
import org.testng.SkipException;
import org.testng.annotations.Test;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.net.HostAndPort;
import com.google.inject.Module;

@Test(groups = "live", singleThreaded = true)
public class EC2ComputeServiceLiveTest extends BaseComputeServiceLiveTest {

	protected TemplateBuilderSpec ebsTemplate;

	public EC2ComputeServiceLiveTest() {
		provider = "ec2";
	}

	@Override
	protected Module getSshModule() {
		return new SshjSshClientModule();
	}

	@Override
	protected void checkUserMetadataContains(NodeMetadata node, ImmutableMap<String, String> userMetadata) {
		if (view.unwrapApi(EC2Api.class).getTagApi().isPresent()) {
			super.checkUserMetadataContains(node, userMetadata);
		} else {
			assertTrue(node.getUserMetadata().isEmpty(),
					"not expecting metadata when tag extension isn't present: " + node);
		}
	}

	@Override
	protected void checkTagsInNodeEquals(NodeMetadata node, ImmutableSet<String> tags) {
		if (view.unwrapApi(EC2Api.class).getTagApi().isPresent()) {
			super.checkTagsInNodeEquals(node, tags);
		} else {
			assertTrue(node.getTags().isEmpty(), "not expecting tags when tag extension isn't present: " + node);
		}
	}

	@Test(enabled = true, dependsOnMethods = "testCorrectAuthException")
	public void testImagesResolveCorrectly() {
		Template defaultTemplate = client.templateBuilder().build();
		assertEquals(defaultTemplate.getImage().getId(),
				defaultTemplate.getImage().getLocation().getId() + "/" + defaultTemplate.getImage().getProviderId());
		Template byId = client.templateBuilder().imageId(defaultTemplate.getImage().getId()).build();
		assertEquals(byId.getImage(), defaultTemplate.getImage());
	}

	@Test(enabled = true, dependsOnMethods = "testCompareSizes")
	public void testExtendedOptionsAndLogin() throws Exception {
		final SecureRandom random = new SecureRandom();

		SecurityGroupApi securityGroupClient = view.unwrapApi(EC2Api.class).getSecurityGroupApi().get();

		KeyPairApi keyPairClient = view.unwrapApi(EC2Api.class).getKeyPairApi().get();

		InstanceApi instanceClient = view.unwrapApi(EC2Api.class).getInstanceApi().get();

		String group = this.group + "o";

		TemplateOptions options = client.templateOptions();

		options.as(EC2TemplateOptions.class).securityGroups(group);
		options.as(EC2TemplateOptions.class).clientToken(Integer.toHexString(random.nextInt(65536 * 1024)));

		String startedId = null;
		try {
			cleanupExtendedStuffInRegion(null, securityGroupClient, keyPairClient, group);

			// create a security group that allows ssh in so that our scripts later
			// will work
			securityGroupClient.createSecurityGroupInRegion(null, group, group);
			securityGroupClient.authorizeSecurityGroupIngressInRegion(null, group, IpProtocol.TCP, 22, 22, "0.0.0.0/0");

			// create a keypair to pass in as well
			KeyPair result = keyPairClient.createKeyPairInRegion(null, group);
			options.as(EC2TemplateOptions.class).keyPair(result.getKeyName());

			// pass in the private key, so that we can run a script with it
			assert result.getKeyMaterial() != null : result;
			options.overrideLoginPrivateKey(result.getKeyMaterial());

			// an arbitrary command to run
			options.runScript(Statements.exec("find /usr"));

			Set<? extends NodeMetadata> nodes = client.createNodesInGroup(group, 1, options);
			NodeMetadata first = Iterables.get(nodes, 0);
			assert first.getCredentials() != null : first;
			assert first.getCredentials().identity != null : first;

			// Verify that the output of createNodesInGroup is the same.
			assertEquals(client.createNodesInGroup(group, 1, options), nodes,
					"Idempotency failing - got different instances");

			startedId = Iterables.getOnlyElement(nodes).getProviderId();

			RunningInstance instance = getInstance(instanceClient, startedId);

			assertEquals(instance.getKeyName(), group);

			// make sure we made our dummy group and also let in the user's group
			assertEquals(ImmutableSortedSet.copyOf(instance.getGroupNames()),
					ImmutableSortedSet.<String>of("jclouds#" + group, group));

			// make sure our dummy group has no rules
			SecurityGroup secgroup = Iterables
					.getOnlyElement(securityGroupClient.describeSecurityGroupsInRegion(null, "jclouds#" + group));
			assert secgroup.size() == 0 : secgroup;

			// try to run a script with the original keyPair
			runScriptWithCreds(group, first.getOperatingSystem(), LoginCredentials.builder()
					.user(first.getCredentials().identity).privateKey(result.getKeyMaterial()).build());

		} finally {
			client.destroyNodesMatching(NodePredicates.inGroup(group));
			if (startedId != null) {
				// ensure we didn't delete these resources!
				assertEquals(keyPairClient.describeKeyPairsInRegion(null, group).size(), 1);
				assertEquals(securityGroupClient.describeSecurityGroupsInRegion(null, group).size(), 1);
			}
			cleanupExtendedStuffInRegion(null, securityGroupClient, keyPairClient, group);
		}
	}

	@Test(enabled = true) //, dependsOnMethods = "testCompareSizes")
	public void testAutoIpAllocation() throws Exception {
		ComputeServiceContext context = null;
		String group = this.group + "aip";
		try {
			Properties overrides = setupProperties();
			overrides.setProperty(EC2Constants.PROPERTY_EC2_AUTO_ALLOCATE_ELASTIC_IPS, "true");

			context = createView(overrides, setupModules());

			TemplateOptions options = client.templateOptions();

			options.blockOnPort(22, 300);
			options.inboundPorts(22);

			// create a node
			Set<? extends NodeMetadata> nodes = context.getComputeService().createNodesInGroup(group, 1, options);
			assertEquals(nodes.size(), 1, "One node should have been created");

			// Get public IPs (We should get 1)
			NodeMetadata node = Iterables.get(nodes, 0);
			String region = node.getLocation().getParent().getId();
			Set<String> publicIps = node.getPublicAddresses();
			assertFalse(Iterables.isEmpty(publicIps), String.format("no public addresses attached to node %s", node));
			assertEquals(Iterables.size(publicIps), 1);

			// Check that the address is public and port 22 is accessible
			String ip = Iterables.getOnlyElement(publicIps);
			assertFalse(InetAddresses2.isPrivateIPAddress(ip));
			HostAndPort socket = HostAndPort.fromParts(ip, 22);
			assertTrue(socketTester.apply(socket), String.format("failed to open socket %s on node %s", socket, node));

			// check that there is an elastic ip correlating to it
			EC2Api ec2 = context.unwrapApi(EC2Api.class);
			Set<PublicIpInstanceIdPair> ipidpairs = ec2.getElasticIPAddressApi().get().describeAddressesInRegion(region,
					publicIps.toArray(new String[0]));
			assertEquals(ipidpairs.size(), 1,
					String.format("there should only be one address pair (%s)", Iterables.toString(ipidpairs)));

			// check that the elastic ip is in node.publicAddresses
			PublicIpInstanceIdPair ipidpair = Iterables.get(ipidpairs, 0);
			assertEquals(region + "/" + ipidpair.getInstanceId(), node.getId());

			// delete the node
			context.getComputeService().destroyNodesMatching(NodePredicates.inGroup(group));

			// check that the ip is deallocated
			Set<PublicIpInstanceIdPair> ipidcheck = ec2.getElasticIPAddressApi().get().describeAddressesInRegion(region,
					ipidpair.getPublicIp());
			assertTrue(Iterables.isEmpty(ipidcheck),
					String.format("there should be no address pairs (%s)", Iterables.toString(ipidcheck)));
		} finally {
			context.getComputeService().destroyNodesMatching(NodePredicates.inGroup(group));
			if (context != null)
				context.close();
		}
	}

	@Override
	protected Properties setupProperties() {
		Properties overrides = super.setupProperties();
		String ebsSpec = setIfTestSystemPropertyPresent(overrides, provider + ".ebs-template");
		if (ebsSpec != null)
			ebsTemplate = TemplateBuilderSpec.parse(ebsSpec);
		return overrides;
	}

	/**
	* Note we cannot use the micro size as it has no ephemeral space.
	*/
	@Test
	public void testMapEBS() throws Exception {
		if (ebsTemplate == null) {
			throw new SkipException("Test cannot run without the parameter test." + provider
					+ ".ebs-template; this property should be in the format defined in TemplateBuilderSpec");
		}
		InstanceApi instanceClient = view.unwrapApi(EC2Api.class).getInstanceApi().get();

		ElasticBlockStoreApi ebsClient = view.unwrapApi(EC2Api.class).getElasticBlockStoreApi().get();

		String group = this.group + "e";
		int volumeSize = 8;

		final Template template = view.getComputeService().templateBuilder().from(ebsTemplate).build();

		Location zone = Iterables.find(view.getComputeService().listAssignableLocations(), new Predicate<Location>() {

			@Override
			public boolean apply(Location arg0) {
				return arg0.getScope() == LocationScope.ZONE
						&& arg0.getParent().getId().equals(template.getLocation().getId());
			}

		});

		// create volume only to make a snapshot
		Volume volume = ebsClient.createVolumeInAvailabilityZone(zone.getId(), 4);
		// Sleep for 5 seconds to make sure the volume creation finishes.
		Thread.sleep(5000);

		Snapshot snapshot = ebsClient.createSnapshotInRegion(volume.getRegion(), volume.getId());
		ebsClient.deleteVolumeInRegion(volume.getRegion(), volume.getId());

		template.getOptions().as(EC2TemplateOptions.class)//
				// .unmapDeviceNamed("/dev/foo)
				.mapEphemeralDeviceToDeviceName("/dev/sdm", "ephemeral0")//
				.mapNewVolumeToDeviceName("/dev/sdn", volumeSize, true)//
				.mapEBSSnapshotToDeviceName("/dev/sdo", snapshot.getId(), volumeSize, true);

		try {
			NodeMetadata node = Iterables.getOnlyElement(client.createNodesInGroup(group, 1, template));

			// TODO figure out how to validate the ephemeral drive. perhaps with df -k?

			Map<String, BlockDevice> devices = instanceClient.getBlockDeviceMappingForInstanceInRegion(
					node.getLocation().getParent().getId(), node.getProviderId());

			BlockDevice device = devices.get("/dev/sdn");
			// check delete on termination
			assertTrue(device.isDeleteOnTermination());

			volume = Iterables.getOnlyElement(
					ebsClient.describeVolumesInRegion(node.getLocation().getParent().getId(), device.getVolumeId()));
			// check volume size
			assertEquals(volumeSize, volume.getSize());

			device = devices.get("/dev/sdo");
			// check delete on termination
			assertTrue(device.isDeleteOnTermination());

			volume = Iterables.getOnlyElement(
					ebsClient.describeVolumesInRegion(node.getLocation().getParent().getId(), device.getVolumeId()));
			// check volume size
			assertEquals(volumeSize, volume.getSize());
			// check volume's snapshot id
			assertEquals(snapshot.getId(), volume.getSnapshotId());

		} finally {
			client.destroyNodesMatching(NodePredicates.inGroup(group));
			ebsClient.deleteSnapshotInRegion(snapshot.getRegion(), snapshot.getId());
		}
	}

	/**
	* Gets the instance with the given ID from the default region
	* 
	* @throws NoSuchElementException If no instance with that id exists, or the instance is in a different region
	*/
	public static RunningInstance getInstance(InstanceApi instanceClient, String id) {
		RunningInstance instance = Iterables
				.getOnlyElement(Iterables.getOnlyElement(instanceClient.describeInstancesInRegion(null, id)));
		return instance;
	}

	protected static void cleanupExtendedStuffInRegion(String region, SecurityGroupApi securityGroupClient,
			KeyPairApi keyPairClient, String group) throws InterruptedException {
		try {
			for (SecurityGroup secgroup : securityGroupClient.describeSecurityGroupsInRegion(region))
				if (secgroup.getName().startsWith("jclouds#" + group) || secgroup.getName().equals(group)) {
					securityGroupClient.deleteSecurityGroupInRegion(region, secgroup.getName());
				}
		} catch (Exception e) {

		}
		try {
			for (KeyPair pair : keyPairClient.describeKeyPairsInRegion(region))
				if (pair.getKeyName().startsWith("jclouds#" + group) || pair.getKeyName().equals(group)) {
					keyPairClient.deleteKeyPairInRegion(region, pair.getKeyName());
				}
		} catch (Exception e) {

		}
		Thread.sleep(2000);
	}

}
