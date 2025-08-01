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
package org.jclouds.serverlove;

import java.net.URI;
import java.util.Properties;

import org.jclouds.elasticstack.ElasticStackApiMetadata;
import org.jclouds.providers.ProviderMetadata;
import org.jclouds.providers.internal.BaseProviderMetadata;
import org.jclouds.serverlove.config.ServerloveImagesModule;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;

/**
 * Implementation of {@link ProviderMetadata} for Serverlove Manchester.
 */
@AutoService(ProviderMetadata.class)
public class ServerloveManchesterProviderMetadata extends BaseProviderMetadata {

	public static Builder builder() {
		return new Builder();
	}

	@Override
	public Builder toBuilder() {
		return builder().fromProviderMetadata(this);
	}

	public ServerloveManchesterProviderMetadata() {
		super(builder());
	}

	public ServerloveManchesterProviderMetadata(Builder builder) {
		super(builder);
	}

	public static Properties defaultProperties() {
		Properties properties = new Properties();
		return properties;
	}

	public static class Builder extends BaseProviderMetadata.Builder {

		protected Builder() {
			ElasticStackApiMetadata apiMedatada = new ElasticStackApiMetadata();

			ImmutableSet.Builder<Class<? extends Module>> modules = ImmutableSet.builder();
			modules.addAll(apiMedatada.getDefaultModules());
			modules.add(ServerloveImagesModule.class); // Custom image supplier binding

			id("serverlove-z1-man").name("Serverlove Manchester")
					.apiMetadata(apiMedatada.toBuilder().version("2.0").defaultModules(modules.build()).build())
					.homepage(URI.create("http://www.serverlove.com"))
					.console(URI.create("http://www.serverlove.com/accounts")).iso3166Codes("GB-MAN")
					.endpoint("https://api.z1-man.serverlove.com")
					.defaultProperties(ServerloveManchesterProviderMetadata.defaultProperties());
		}

		@Override
		public ServerloveManchesterProviderMetadata build() {
			return new ServerloveManchesterProviderMetadata(this);
		}

		@Override
		public Builder fromProviderMetadata(ProviderMetadata in) {
			super.fromProviderMetadata(in);
			return this;
		}

	}
}
