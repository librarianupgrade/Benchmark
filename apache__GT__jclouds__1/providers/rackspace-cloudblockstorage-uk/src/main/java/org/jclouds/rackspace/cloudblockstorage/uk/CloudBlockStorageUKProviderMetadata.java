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
package org.jclouds.rackspace.cloudblockstorage.uk;

import static org.jclouds.location.reference.LocationConstants.ISO3166_CODES;
import static org.jclouds.location.reference.LocationConstants.PROPERTY_REGION;
import static org.jclouds.location.reference.LocationConstants.PROPERTY_REGIONS;
import static org.jclouds.openstack.keystone.v2_0.config.KeystoneProperties.CREDENTIAL_TYPE;

import java.net.URI;
import java.util.Properties;

import org.jclouds.openstack.cinder.v1.CinderApiMetadata;
import org.jclouds.openstack.cinder.v1.config.CinderHttpApiModule;
import org.jclouds.openstack.cinder.v1.config.CinderParserModule;
import org.jclouds.openstack.keystone.v2_0.config.KeystoneAuthenticationModule.RegionModule;
import org.jclouds.providers.ProviderMetadata;
import org.jclouds.providers.internal.BaseProviderMetadata;
import org.jclouds.rackspace.cloudidentity.v2_0.config.CloudIdentityAuthenticationApiModule;
import org.jclouds.rackspace.cloudidentity.v2_0.config.CloudIdentityAuthenticationModule;
import org.jclouds.rackspace.cloudidentity.v2_0.config.CloudIdentityCredentialTypes;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;

/**
 * Implementation of {@link ProviderMetadata} for Rackspace Next Generation Cloud Block Storage.
 */
@AutoService(ProviderMetadata.class)
public class CloudBlockStorageUKProviderMetadata extends BaseProviderMetadata {

	public static Builder builder() {
		return new Builder();
	}

	@Override
	public Builder toBuilder() {
		return builder().fromProviderMetadata(this);
	}

	public CloudBlockStorageUKProviderMetadata() {
		super(builder());
	}

	public CloudBlockStorageUKProviderMetadata(Builder builder) {
		super(builder);
	}

	public static Properties defaultProperties() {
		Properties properties = new Properties();
		properties.setProperty(CREDENTIAL_TYPE, CloudIdentityCredentialTypes.API_KEY_CREDENTIALS);
		properties.setProperty(PROPERTY_REGIONS, "LON");
		properties.setProperty(PROPERTY_REGION + ".LON." + ISO3166_CODES, "GB-SLG");
		return properties;
	}

	public static class Builder extends BaseProviderMetadata.Builder {

		protected Builder() {
			id("rackspace-cloudblockstorage-uk").name("Rackspace Next Generation Cloud Block Storage UK")
					.apiMetadata(new CinderApiMetadata().toBuilder().identityName("${userName}")
							.credentialName("${apiKey}")
							.defaultEndpoint("https://lon.identity.api.rackspacecloud.com/v2.0/")
							.endpointName("identity service url ending in /v2.0/")
							.documentation(URI.create(
									"http://docs.rackspace.com/cbs/api/v1.0/cbs-devguide/content/overview.html"))
							.defaultModules(ImmutableSet.<Class<? extends Module>>builder()
									.add(CloudIdentityAuthenticationApiModule.class)
									.add(CloudIdentityAuthenticationModule.class).add(RegionModule.class)
									.add(CinderParserModule.class).add(CinderHttpApiModule.class).build())
							.build())
					.homepage(
							URI.create("http://www.rackspace.co.uk/cloud-hosting/cloud-products/cloud-block-storage/"))
					.console(URI.create("https://mycloud.rackspace.co.uk"))
					.linkedServices("rackspace-cloudservers-uk", "cloudfiles-uk").iso3166Codes("GB-SLG")
					.endpoint("https://lon.identity.api.rackspacecloud.com/v2.0/")
					.defaultProperties(CloudBlockStorageUKProviderMetadata.defaultProperties());
		}

		@Override
		public CloudBlockStorageUKProviderMetadata build() {
			return new CloudBlockStorageUKProviderMetadata(this);
		}

		@Override
		public Builder fromProviderMetadata(ProviderMetadata in) {
			super.fromProviderMetadata(in);
			return this;
		}
	}

}
