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
package org.jclouds.softlayer;

import static org.jclouds.reflect.Reflection2.typeToken;
import static org.jclouds.softlayer.reference.SoftLayerConstants.SOFTLAYER_PROVIDER_NAME;
import java.net.URI;
import java.util.Properties;

import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.rest.internal.BaseHttpApiMetadata;
import org.jclouds.softlayer.compute.config.SoftLayerComputeServiceContextModule;
import org.jclouds.softlayer.config.SoftLayerHttpApiModule;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;

/**
 * Implementation of {@link org.jclouds.apis.ApiMetadata} for API
 */
public class SoftLayerApiMetadata extends BaseHttpApiMetadata<SoftLayerApi> {

	@Override
	public Builder toBuilder() {
		return new Builder().fromApiMetadata(this);
	}

	public SoftLayerApiMetadata() {
		this(new Builder());
	}

	protected SoftLayerApiMetadata(Builder builder) {
		super(builder);
	}

	public static Properties defaultProperties() {
		Properties properties = BaseHttpApiMetadata.defaultProperties();
		properties.setProperty("jclouds.ssh.max-retries", "5");
		properties.setProperty("jclouds.ssh.retry-auth", "true");
		return properties;
	}

	public static class Builder extends BaseHttpApiMetadata.Builder<SoftLayerApi, Builder> {

		protected Builder() {
			id(SOFTLAYER_PROVIDER_NAME).name("SoftLayer API").identityName("API Username").credentialName("API Key")
					.documentation(URI.create("http://sldn.softlayer.com/article/REST")).version("3")
					.defaultEndpoint("https://api.softlayer.com/rest")
					.defaultProperties(SoftLayerApiMetadata.defaultProperties())
					.view(typeToken(ComputeServiceContext.class))
					.defaultModules(ImmutableSet.<Class<? extends Module>>of(SoftLayerHttpApiModule.class,
							SoftLayerComputeServiceContextModule.class))
					.build();
		}

		@Override
		public SoftLayerApiMetadata build() {
			return new SoftLayerApiMetadata(this);
		}

		@Override
		protected Builder self() {
			return this;
		}
	}
}
