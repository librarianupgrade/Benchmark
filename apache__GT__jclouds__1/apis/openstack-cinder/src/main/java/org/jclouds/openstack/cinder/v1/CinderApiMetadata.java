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
package org.jclouds.openstack.cinder.v1;

import static org.jclouds.openstack.keystone.v2_0.config.KeystoneProperties.CREDENTIAL_TYPE;
import static org.jclouds.openstack.keystone.v2_0.config.KeystoneProperties.SERVICE_TYPE;

import java.net.URI;
import java.util.Properties;

import org.jclouds.apis.ApiMetadata;
import org.jclouds.openstack.cinder.v1.config.CinderHttpApiModule;
import org.jclouds.openstack.cinder.v1.config.CinderParserModule;
import org.jclouds.openstack.keystone.v2_0.config.AuthenticationApiModule;
import org.jclouds.openstack.keystone.v2_0.config.CredentialTypes;
import org.jclouds.openstack.keystone.v2_0.config.KeystoneAuthenticationModule;
import org.jclouds.openstack.keystone.v2_0.config.KeystoneAuthenticationModule.RegionModule;
import org.jclouds.openstack.v2_0.ServiceType;
import org.jclouds.rest.internal.BaseHttpApiMetadata;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;

/**
 * Implementation of {@link ApiMetadata} for Cinder v1 API
 */
@AutoService(ApiMetadata.class)
public class CinderApiMetadata extends BaseHttpApiMetadata<CinderApi> {

	@Override
	public Builder toBuilder() {
		return new Builder().fromApiMetadata(this);
	}

	public CinderApiMetadata() {
		this(new Builder());
	}

	protected CinderApiMetadata(Builder builder) {
		super(builder);
	}

	public static Properties defaultProperties() {
		Properties properties = BaseHttpApiMetadata.defaultProperties();
		properties.setProperty(SERVICE_TYPE, ServiceType.BLOCK_STORAGE);
		properties.setProperty(CREDENTIAL_TYPE, CredentialTypes.PASSWORD_CREDENTIALS);
		return properties;
	}

	public static class Builder extends BaseHttpApiMetadata.Builder<CinderApi, Builder> {

		protected Builder() {
			id("openstack-cinder").name("OpenStack Cinder Folsom API")
					.identityName(
							"${tenantName}:${userName} or ${userName}, if your keystone supports a default tenant")
					.credentialName("${password}").endpointName("Keystone base URL ending in /v2.0/")
					.documentation(URI.create("http://api.openstack.org/")).version("1")
					.defaultEndpoint("http://localhost:5000/v2.0/")
					.defaultProperties(CinderApiMetadata.defaultProperties())
					.defaultModules(ImmutableSet.<Class<? extends Module>>builder().add(AuthenticationApiModule.class)
							.add(KeystoneAuthenticationModule.class).add(RegionModule.class)
							.add(CinderParserModule.class).add(CinderHttpApiModule.class).build());
		}

		@Override
		public CinderApiMetadata build() {
			return new CinderApiMetadata(this);
		}

		@Override
		protected Builder self() {
			return this;
		}
	}
}
