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
package org.jclouds.softlayer.features;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import java.util.Set;

import org.jclouds.softlayer.domain.SoftwareDescription;
import org.testng.annotations.Test;

/**
 * Tests behavior of {@code SoftwareDescriptionApi}
 */
@Test(groups = "live")
public class SoftwareDescriptionApiLiveTest extends BaseSoftLayerApiLiveTest {

	@Test
	public void testGetAllObjects() {
		Set<SoftwareDescription> response = api().getAllObjects();
		assertNotNull(response);
		assertTrue(response.size() > 0);
	}

	private SoftwareDescriptionApi api() {
		return api.getSoftwareDescriptionApi();
	}

}
