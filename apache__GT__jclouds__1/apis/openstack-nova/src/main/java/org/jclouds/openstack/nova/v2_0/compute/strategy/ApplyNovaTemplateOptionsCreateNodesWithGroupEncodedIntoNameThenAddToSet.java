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
package org.jclouds.openstack.nova.v2_0.compute.strategy;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.jclouds.ssh.SshKeys.fingerprintPrivateKey;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.jclouds.Constants;
import org.jclouds.compute.config.CustomizationResponse;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.functions.GroupNamingConvention;
import org.jclouds.compute.reference.ComputeServiceConstants;
import org.jclouds.compute.strategy.CreateNodeWithGroupEncodedIntoName;
import org.jclouds.compute.strategy.CustomizeNodeAndAddToGoodMapOrPutExceptionIntoBadMap;
import org.jclouds.compute.strategy.ListNodesStrategy;
import org.jclouds.compute.strategy.impl.CreateNodesWithGroupEncodedIntoNameThenAddToSet;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.compute.functions.AllocateAndAddFloatingIpToNode;
import org.jclouds.openstack.nova.v2_0.compute.options.NodeAndNovaTemplateOptions;
import org.jclouds.openstack.nova.v2_0.compute.options.NovaTemplateOptions;
import org.jclouds.openstack.nova.v2_0.domain.KeyPair;
import org.jclouds.openstack.nova.v2_0.domain.regionscoped.RegionAndName;
import org.jclouds.openstack.nova.v2_0.domain.regionscoped.RegionSecurityGroupNameAndPorts;
import org.jclouds.openstack.nova.v2_0.domain.regionscoped.SecurityGroupInRegion;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Ints;
import com.google.common.util.concurrent.Atomics;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

@Singleton
public class ApplyNovaTemplateOptionsCreateNodesWithGroupEncodedIntoNameThenAddToSet
		extends CreateNodesWithGroupEncodedIntoNameThenAddToSet {

	public static final String JCLOUDS_SG = "jclouds_securityGroup";
	public static final String JCLOUDS_KP = "jclouds_keyPair";

	private final AllocateAndAddFloatingIpToNode createAndAddFloatingIpToNode;
	protected final LoadingCache<RegionAndName, SecurityGroupInRegion> securityGroupCache;
	protected final LoadingCache<RegionAndName, KeyPair> keyPairCache;
	protected final NovaApi novaApi;

	@Inject
	protected ApplyNovaTemplateOptionsCreateNodesWithGroupEncodedIntoNameThenAddToSet(
			CreateNodeWithGroupEncodedIntoName addNodeWithTagStrategy, ListNodesStrategy listNodesStrategy,
			GroupNamingConvention.Factory namingConvention,
			CustomizeNodeAndAddToGoodMapOrPutExceptionIntoBadMap.Factory customizeNodeAndAddToGoodMapOrPutExceptionIntoBadMapFactory,
			@Named(Constants.PROPERTY_USER_THREADS) ListeningExecutorService userExecutor,
			AllocateAndAddFloatingIpToNode createAndAddFloatingIpToNode,
			LoadingCache<RegionAndName, SecurityGroupInRegion> securityGroupCache,
			LoadingCache<RegionAndName, KeyPair> keyPairCache, NovaApi novaApi) {
		super(addNodeWithTagStrategy, listNodesStrategy, namingConvention, userExecutor,
				customizeNodeAndAddToGoodMapOrPutExceptionIntoBadMapFactory);
		this.securityGroupCache = checkNotNull(securityGroupCache, "securityGroupCache");
		this.keyPairCache = checkNotNull(keyPairCache, "keyPairCache");
		this.createAndAddFloatingIpToNode = checkNotNull(createAndAddFloatingIpToNode, "createAndAddFloatingIpToNode");
		this.novaApi = checkNotNull(novaApi, "novaApi");
	}

	@Override
	public Map<?, ListenableFuture<Void>> execute(String group, int count, Template template,
			Set<NodeMetadata> goodNodes, Map<NodeMetadata, Exception> badNodes,
			Multimap<NodeMetadata, CustomizationResponse> customizationResponses) {

		Template mutableTemplate = template.clone();

		NovaTemplateOptions templateOptions = NovaTemplateOptions.class.cast(mutableTemplate.getOptions());

		assert template.getOptions().equals(templateOptions) : "options didn't clone properly";

		String region = mutableTemplate.getLocation().getId();
		ImmutableList.Builder<String> tagsBuilder = ImmutableList.builder();

		if (templateOptions.shouldAutoAssignFloatingIp()) {
			checkArgument(novaApi.getFloatingIPApi(region).isPresent(),
					"Floating IPs are required by options, but the extension is not available! options: %s",
					templateOptions);
		}

		boolean keyPairExtensionPresent = novaApi.getKeyPairApi(region).isPresent();
		if (templateOptions.shouldGenerateKeyPair()) {
			checkArgument(keyPairExtensionPresent,
					"Key Pairs are required by options, but the extension is not available! options: %s",
					templateOptions);
			KeyPair keyPair = keyPairCache.getUnchecked(
					RegionAndName.fromRegionAndName(region, namingConvention.create().sharedNameForGroup(group)));
			keyPairCache.asMap().put(RegionAndName.fromRegionAndName(region, keyPair.getName()), keyPair);
			templateOptions.keyPairName(keyPair.getName());
			tagsBuilder.add(JCLOUDS_KP);
		} else if (templateOptions.getKeyPairName() != null) {
			checkArgument(keyPairExtensionPresent,
					"Key Pairs are required by options, but the extension is not available! options: %s",
					templateOptions);
			if (templateOptions.getLoginPrivateKey() != null) {
				String pem = templateOptions.getLoginPrivateKey();
				KeyPair keyPair = KeyPair.builder().name(templateOptions.getKeyPairName())
						.fingerprint(fingerprintPrivateKey(pem)).privateKey(pem).build();
				keyPairCache.asMap().put(RegionAndName.fromRegionAndName(region, keyPair.getName()), keyPair);
			}
		}

		boolean securityGroupExtensionPresent = novaApi.getSecurityGroupApi(region).isPresent();
		List<Integer> inboundPorts = Ints.asList(templateOptions.getInboundPorts());
		if (!templateOptions.getGroups().isEmpty()) {
			checkArgument(securityGroupExtensionPresent,
					"Security groups are required by options, but the extension is not available! options: %s",
					templateOptions);
		} else if (securityGroupExtensionPresent) {
			if (templateOptions.getGroups().isEmpty() && !inboundPorts.isEmpty()) {
				String securityGroupName = namingConvention.create().sharedNameForGroup(group);
				try {
					securityGroupCache
							.get(new RegionSecurityGroupNameAndPorts(region, securityGroupName, inboundPorts));
				} catch (ExecutionException e) {
					throw Throwables.propagate(e.getCause());
				}
				templateOptions.securityGroups(securityGroupName);
				tagsBuilder.add(JCLOUDS_SG);
			}
		}
		templateOptions.userMetadata(ComputeServiceConstants.NODE_GROUP_KEY, group);
		templateOptions.tags(tagsBuilder.build());

		return super.execute(group, count, mutableTemplate, goodNodes, badNodes, customizationResponses);
	}

	@Override
	protected ListenableFuture<AtomicReference<NodeMetadata>> createNodeInGroupWithNameAndTemplate(String group,
			final String name, Template template) {

		ListenableFuture<AtomicReference<NodeMetadata>> future = super.createNodeInGroupWithNameAndTemplate(group, name,
				template);
		final NovaTemplateOptions templateOptions = NovaTemplateOptions.class.cast(template.getOptions());
		if (templateOptions.shouldAutoAssignFloatingIp()) {

			ListenableFuture<AtomicReference<NodeAndNovaTemplateOptions>> nodeAndNovaTemplateOptions = Futures
					.transform(future,
							new Function<AtomicReference<NodeMetadata>, AtomicReference<NodeAndNovaTemplateOptions>>() {

								@Override
								public AtomicReference<NodeAndNovaTemplateOptions> apply(
										AtomicReference<NodeMetadata> input) {
									return NodeAndNovaTemplateOptions.newAtomicReference(input,
											Atomics.newReference(templateOptions));
								}
							});
			return Futures.transform(nodeAndNovaTemplateOptions, createAndAddFloatingIpToNode, userExecutor);
		} else {
			return future;
		}
	}

}
