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
package org.jclouds.cloudstack.parse;

import java.util.Set;

import org.jclouds.cloudstack.domain.NetworkType;
import org.jclouds.cloudstack.domain.Tag;
import org.jclouds.cloudstack.domain.Zone;
import org.jclouds.json.BaseSetParserTest;
import org.jclouds.rest.annotations.SelectJson;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;

@Test(groups = "unit")
public class ListZonesResponseTest extends BaseSetParserTest<Zone> {

	@Override
	public String resource() {
		return "/listzonesresponse.json";
	}

	@Override
	@SelectJson("zone")
	public Set<Zone> expected() {
		return ImmutableSet.of(
				Zone.builder().id("1").name("San Jose 1").networkType(NetworkType.ADVANCED).securityGroupsEnabled(false)
						.build(),
				Zone.builder().id("2").name("Chicago").networkType(NetworkType.ADVANCED).securityGroupsEnabled(true)
						.tags(Tag.builder().account("1").domain("ROOT").domainId("1").key("some-tag").resourceId("2")
								.resourceType(Tag.ResourceType.ZONE).value("some-value").build())
						.build());
	}
}
