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
package org.jclouds.ultradns.ws.parse;

import static org.testng.Assert.assertEquals;

import java.io.InputStream;

import org.jclouds.http.functions.BaseHandlerTest;
import org.jclouds.ultradns.ws.xml.ItemListHandler;
import org.testng.annotations.Test;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;

@Test(testName = "GetAvailableGroupsResponseTest")
public class GetAvailableGroupsResponseTest extends BaseHandlerTest {

	public void test() {
		InputStream is = getClass().getResourceAsStream("/directionalgroup_names.xml");

		FluentIterable<String> expected = expected();

		ItemListHandler handler = injector.getInstance(ItemListHandler.class);
		FluentIterable<String> result = factory.create(handler).parse(is);

		assertEquals(result.toSet().toString(), expected.toSet().toString());
	}

	public FluentIterable<String> expected() {
		return FluentIterable.from(ImmutableSet.of("EU-foo.jclouds.org.", "non-foo.jclouds.org."));
	}
}
