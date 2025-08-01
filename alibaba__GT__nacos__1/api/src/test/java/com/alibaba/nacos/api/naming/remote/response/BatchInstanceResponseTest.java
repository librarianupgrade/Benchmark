/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.api.naming.remote.response;

import com.alibaba.nacos.api.naming.remote.NamingRemoteConstants;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BatchInstanceResponseTest {

	protected static ObjectMapper mapper;

	@BeforeClass
	public static void setUp() throws Exception {
		mapper = new ObjectMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
	}

	@Test
	public void testSerialize() throws JsonProcessingException {
		BatchInstanceResponse response = new BatchInstanceResponse(NamingRemoteConstants.REGISTER_INSTANCE);
		String json = mapper.writeValueAsString(response);
		assertTrue(json.contains("\"type\":\"" + NamingRemoteConstants.REGISTER_INSTANCE + "\""));
	}

	@Test
	public void testDeserialize() throws JsonProcessingException {
		String json = "{\"resultCode\":200,\"errorCode\":0,\"type\":\"registerInstance\",\"success\":true}";
		BatchInstanceResponse response = mapper.readValue(json, BatchInstanceResponse.class);
		assertEquals(NamingRemoteConstants.REGISTER_INSTANCE, response.getType());
	}
}