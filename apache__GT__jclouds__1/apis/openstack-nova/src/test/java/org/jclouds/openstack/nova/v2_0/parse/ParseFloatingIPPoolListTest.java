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
package org.jclouds.openstack.nova.v2_0.parse;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.jclouds.json.BaseSetParserTest;
import org.jclouds.json.config.GsonModule;
import org.jclouds.openstack.nova.v2_0.config.NovaParserModule;
import org.jclouds.openstack.nova.v2_0.domain.FloatingIPPool;
import org.jclouds.rest.annotations.SelectJson;
import org.testng.annotations.Test;

import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import java.util.Set;

@Test(groups = "unit", testName = "ParseFloatingIPPoolListTest")
public class ParseFloatingIPPoolListTest extends BaseSetParserTest<FloatingIPPool> {

	@Override
	public String resource() {
		return "/floatingippool_list.json";
	}

	@Override
	@SelectJson("floating_ip_pools")
	@Consumes(MediaType.APPLICATION_JSON)
	public Set<FloatingIPPool> expected() {
		return ImmutableSet.of(FloatingIPPool.builder().name("VLAN867").build());
	}

	protected Injector injector() {
		return Guice.createInjector(new NovaParserModule(), new GsonModule());
	}
}
