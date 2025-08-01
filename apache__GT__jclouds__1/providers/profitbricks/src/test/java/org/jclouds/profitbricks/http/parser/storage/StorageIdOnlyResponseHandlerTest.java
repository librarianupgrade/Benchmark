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
package org.jclouds.profitbricks.http.parser.storage;

import static org.testng.Assert.assertEquals;

import org.jclouds.http.functions.ParseSax;
import org.jclouds.profitbricks.http.parser.BaseResponseHandlerTest;
import org.testng.annotations.Test;

@Test(groups = "unit", testName = "StorageIdOnlyResponseHandlerTest")
public class StorageIdOnlyResponseHandlerTest extends BaseResponseHandlerTest<String> {

	@Override
	protected ParseSax<String> createParser() {
		return factory.create(injector.getInstance(StorageIdOnlyResponseHandler.class));
	}

	@Test
	public void testParseResponseFromCreateStorage() {
		ParseSax<String> parser = createParser();

		String storageId = parser.parse(payloadFromResource("/storage/storage-create.xml"));

		assertEquals("qswdefrg-qaws-qaws-defe-rgrgdsvcxbrh", storageId);
	}

}
