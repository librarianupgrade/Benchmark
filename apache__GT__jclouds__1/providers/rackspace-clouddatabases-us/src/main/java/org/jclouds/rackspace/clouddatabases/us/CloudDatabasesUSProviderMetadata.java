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
package org.jclouds.rackspace.clouddatabases.us;

import static org.jclouds.location.reference.LocationConstants.ISO3166_CODES;
import static org.jclouds.location.reference.LocationConstants.PROPERTY_REGION;
import static org.jclouds.location.reference.LocationConstants.PROPERTY_REGIONS;
import static org.jclouds.openstack.keystone.v2_0.config.KeystoneProperties.CREDENTIAL_TYPE;
import static org.jclouds.openstack.keystone.v2_0.config.KeystoneProperties.SERVICE_TYPE;

import java.net.URI;
import java.util.Properties;

import org.jclouds.openstack.keystone.v2_0.config.KeystoneAuthenticationModule.RegionModule;
import org.jclouds.openstack.trove.v1.TroveApiMetadata;
import org.jclouds.openstack.trove.v1.config.TroveHttpApiModule;
import org.jclouds.openstack.trove.v1.config.TroveParserModule;
import org.jclouds.providers.ProviderMetadata;
import org.jclouds.providers.internal.BaseProviderMetadata;
import org.jclouds.rackspace.cloudidentity.v2_0.ServiceType;
import org.jclouds.rackspace.cloudidentity.v2_0.config.CloudIdentityAuthenticationApiModule;
import org.jclouds.rackspace.cloudidentity.v2_0.config.CloudIdentityAuthenticationModule;
import org.jclouds.rackspace.cloudidentity.v2_0.config.CloudIdentityCredentialTypes;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;

/**
 * Implementation of {@link ProviderMetadata} for Rackspace Cloud Databases.
 */
@AutoService(ProviderMetadata.class)
public class CloudDatabasesUSProviderMetadata extends BaseProviderMetadata {

	public static Builder builder() {
		return new Builder();
	}

	@Override
	public Builder toBuilder() {
		return builder().fromProviderMetadata(this);
	}

	public CloudDatabasesUSProviderMetadata() {
		super(builder());
	}

	public CloudDatabasesUSProviderMetadata(Builder builder) {
		super(builder);
	}

	public static Properties defaultProperties() {
		Properties properties = new Properties();
		properties.setProperty(CREDENTIAL_TYPE, CloudIdentityCredentialTypes.API_KEY_CREDENTIALS);
		properties.setProperty(SERVICE_TYPE, ServiceType.DATABASES);
		properties.setProperty(PROPERTY_REGIONS, "ORD,DFW,IAD,SYD,HKG");
		properties.setProperty(PROPERTY_REGION + ".ORD." + ISO3166_CODES, "US-IL");
		properties.setProperty(PROPERTY_REGION + ".DFW." + ISO3166_CODES, "US-TX");
		properties.setProperty(PROPERTY_REGION + ".IAD." + ISO3166_CODES, "US-VA");
		properties.setProperty(PROPERTY_REGION + ".SYD." + ISO3166_CODES, "AU-NSW");
		properties.setProperty(PROPERTY_REGION + ".HKG." + ISO3166_CODES, "HK");
		return properties;
	}

	public static class Builder extends BaseProviderMetadata.Builder {

		protected Builder() {
			id("rackspace-clouddatabases-us").name("Rackspace Clouddatabases US").apiMetadata(new TroveApiMetadata()
					.toBuilder().identityName("${userName}").credentialName("${apiKey}")
					.defaultEndpoint("https://identity.api.rackspacecloud.com/v2.0/")
					.endpointName("identity service url ending in /v2.0/")
					.documentation(
							URI.create("http://docs.rackspace.com/cbs/api/v1.0/cbs-devguide/content/overview.html"))
					.defaultModules(ImmutableSet.<Class<? extends Module>>builder()
							.add(CloudIdentityAuthenticationApiModule.class)
							.add(CloudIdentityAuthenticationModule.class).add(RegionModule.class)
							.add(TroveParserModule.class).add(TroveHttpApiModule.class).build())
					.build()).homepage(URI.create("http://www.rackspace.com/cloud/public/databases/"))
					.console(URI.create("https://mycloud.rackspace.com"))
					.linkedServices("rackspace-cloudservers-us", "cloudfiles-us").iso3166Codes("US-IL", "US-TX")
					.endpoint("https://identity.api.rackspacecloud.com/v2.0/")
					.defaultProperties(CloudDatabasesUSProviderMetadata.defaultProperties());
		}

		@Override
		public CloudDatabasesUSProviderMetadata build() {
			return new CloudDatabasesUSProviderMetadata(this);
		}

		@Override
		public Builder fromProviderMetadata(ProviderMetadata in) {
			super.fromProviderMetadata(in);
			return this;
		}
	}

}
