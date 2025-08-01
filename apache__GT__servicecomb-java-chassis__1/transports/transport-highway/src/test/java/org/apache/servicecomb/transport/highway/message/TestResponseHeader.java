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

package org.apache.servicecomb.transport.highway.message;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

public class TestResponseHeader {

	private ResponseHeader responseHeader = null;

	Map<String, String> context = new HashMap<>();

	@Before
	public void setUp() throws Exception {
		responseHeader = new ResponseHeader();
	}

	@After
	public void tearDown() throws Exception {
		responseHeader = null;
	}

	@Test
	public void testSetContext() {
		context.put("key1", "v1");
		responseHeader.setContext(context);
		Assertions.assertNotNull(responseHeader.getContext());
		Assertions.assertEquals("v1", responseHeader.getContext().get("key1"));
	}
}
