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
package org.jclouds.glesys.compute;

import static org.testng.Assert.assertEquals;

import org.jclouds.glesys.compute.internal.BaseGleSYSComputeServiceExpectTest;
import org.testng.annotations.Test;

import com.google.common.collect.Iterables;

@Test(groups = "live", singleThreaded = true, testName = "GleSYSComputeServiceAdapterExpectTest")
public class GleSYSComputeServiceAdapterExpectTest extends BaseGleSYSComputeServiceExpectTest {

	@Test
	public void testListImages() {

		GleSYSComputeServiceAdapter adapter = injectorForKnownArgumentsAndConstantPassword()
				.getInstance(GleSYSComputeServiceAdapter.class);

		assertEquals(Iterables.size(adapter.listImages()), 34);

	}

	//TODO: most importantly createServer and listHardwareProfiles tests
}
