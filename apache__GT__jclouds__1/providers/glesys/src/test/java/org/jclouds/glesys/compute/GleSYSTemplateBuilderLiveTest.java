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

import static org.jclouds.compute.util.ComputeServiceUtils.getCores;
import static org.jclouds.compute.util.ComputeServiceUtils.getSpace;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.Set;

import org.jclouds.compute.domain.OsFamily;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.domain.Volume;
import org.jclouds.compute.internal.BaseTemplateBuilderLiveTest;
import org.jclouds.glesys.compute.options.GleSYSTemplateOptions;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;

@Test(groups = "live", testName = "GleSYSTemplateBuilderLiveTest")
public class GleSYSTemplateBuilderLiveTest extends BaseTemplateBuilderLiveTest {

	public GleSYSTemplateBuilderLiveTest() {
		provider = "glesys";
	}

	@Test
	public void testDefaultTemplateBuilder() throws IOException {
		Template defaultTemplate = view.getComputeService().templateBuilder().build();
		assertEquals(defaultTemplate.getImage().getId(), "Ubuntu 12.04 x64");
		assertEquals(defaultTemplate.getImage().getOperatingSystem().getVersion(), "12.04");
		assertEquals(defaultTemplate.getImage().getOperatingSystem().is64Bit(), true);
		assertEquals(defaultTemplate.getImage().getOperatingSystem().getFamily(), OsFamily.UBUNTU);
		assertEquals(getCores(defaultTemplate.getHardware()), 1.0d);
		assertEquals(defaultTemplate.getHardware().getRam(), 768);
		assertEquals(defaultTemplate.getHardware().getHypervisor(), "Xen");
		assertEquals(getSpace(defaultTemplate.getHardware()), 5.0d);
		assertEquals(defaultTemplate.getHardware().getVolumes().get(0).getType(), Volume.Type.LOCAL);
		// test that we bound the correct templateoptions in guice
		assertEquals(defaultTemplate.getOptions().getClass(), GleSYSTemplateOptions.class);
	}

	@Override
	protected Set<String> getIso3166Codes() {
		return ImmutableSet.of("NL-NH", "SE-N", "US-NY", "SE-AB");
	}
}
