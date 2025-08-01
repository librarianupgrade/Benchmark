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
package org.jclouds.openstack.keystone.v1_1.suppliers;

import static org.testng.Assert.assertEquals;

import java.net.URI;

import javax.inject.Singleton;

import org.jclouds.location.Provider;
import org.jclouds.location.suppliers.RegionIdToURISupplier;
import org.jclouds.openstack.keystone.v1_1.domain.Auth;
import org.jclouds.openstack.keystone.v1_1.parse.ParseAuthTest;
import org.testng.annotations.Test;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Provides;
import com.google.inject.assistedinject.FactoryModuleBuilder;

@Test(groups = "unit", testName = "RegionIdToURIFromAuthForServiceSupplierTest")
public class RegionIdToURIFromAuthForServiceSupplierTest {
	private final RegionIdToURISupplier.Factory factory = Guice.createInjector(new AbstractModule() {

		@Override
		protected void configure() {
			bindConstant().annotatedWith(Provider.class).to("keystone");
			install(new FactoryModuleBuilder()
					.implement(RegionIdToURISupplier.class, RegionIdToURIFromAuthForServiceSupplier.class)
					.build(RegionIdToURISupplier.Factory.class));
		}

		@Provides
		@Singleton
		public Supplier<Auth> provide() {
			return Suppliers.ofInstance(new ParseAuthTest().expected());
		}
	}).getInstance(RegionIdToURISupplier.Factory.class);

	public void testRegionMatches() {
		assertEquals(
				Maps.transformValues(factory.createForApiTypeAndVersion("cloudFilesCDN", "1.0").get(),
						Suppliers.<URI>supplierFunction()),
				ImmutableMap.of("LON", URI
						.create("https://cdn3.clouddrive.com/v1/MossoCloudFS_83a9d536-2e25-4166-bd3b-a503a934f953")));
	}

	public void testTakesFirstPartOfDNSWhenNoRegion() {
		assertEquals(
				Maps.transformValues(factory.createForApiTypeAndVersion("cloudServers", "1.1").get(),
						Suppliers.<URI>supplierFunction()),
				ImmutableMap.of("lon", URI.create("https://lon.servers.api.rackspacecloud.com/v1.0/10001786")));
	}
}
