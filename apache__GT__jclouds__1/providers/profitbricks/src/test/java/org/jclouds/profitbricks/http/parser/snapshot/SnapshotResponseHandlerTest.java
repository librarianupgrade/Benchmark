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
package org.jclouds.profitbricks.http.parser.snapshot;

import org.jclouds.http.functions.ParseSax;
import org.jclouds.profitbricks.domain.Location;
import org.jclouds.profitbricks.domain.OsType;
import org.jclouds.profitbricks.domain.ProvisioningState;
import org.jclouds.profitbricks.domain.Snapshot;
import org.jclouds.profitbricks.http.parser.BaseResponseHandlerTest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import org.jclouds.date.DateService;
import org.testng.annotations.Test;

@Test(groups = "unit", testName = "ServerResponseHandlerTest")
public class SnapshotResponseHandlerTest extends BaseResponseHandlerTest<Snapshot> {

	@Override
	protected ParseSax<Snapshot> createParser() {
		return factory.create(injector.getInstance(SnapshotResponseHandler.class));
	}

	protected DateService createDateParser() {
		return injector.getInstance(DateService.class);
	}

	@Test
	public void testParseResponseFromGetSnapshot() {
		ParseSax<Snapshot> parser = createParser();

		Snapshot actual = parser.parse(payloadFromResource("/snapshot/snapshot.xml"));
		assertNotNull(actual, "Parsed content returned null");

		DateService dateParser = createDateParser();

		Snapshot expected = Snapshot.builder().id("qswdefrg-qaws-qaws-defe-rgrgdsvcxbrh").description("description")
				.size(1024f).name("snapshot01").state(ProvisioningState.AVAILABLE).isBootable(true).osType(OsType.LINUX)
				.isCpuHotPlug(true).isCpuHotUnPlug(true).isDiscVirtioHotPlug(true).isDiscVirtioHotUnPlug(true)
				.isRamHotPlug(true).isRamHotUnPlug(true).isNicHotPlug(true).isNicHotUnPlug(true)
				.location(Location.US_LAS)
				.creationTime(dateParser.iso8601DateOrSecondsDateParse("2015-01-26T07:09:23.138Z"))
				.lastModificationTime(dateParser.iso8601DateOrSecondsDateParse("2015-01-26T07:09:23.138Z")).build();

		assertEquals(actual, expected);

	}
}
