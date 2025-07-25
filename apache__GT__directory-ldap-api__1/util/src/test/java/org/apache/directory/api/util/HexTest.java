/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    https://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */
package org.apache.directory.api.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import javax.naming.NamingException;

/**
 * Tests the StringTools class methods.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@Execution(ExecutionMode.CONCURRENT)
public class HexTest {
	@Test
	public void testDecodeHexString() throws Exception {
		// weird stuff - corner cases
		try {
			assertEquals("", Hex.decodeHexString(""));
			fail("should not get here");
		} catch (NamingException e) {
		}

		assertEquals("", Hex.decodeHexString("#"));
		assertEquals("F", Hex.decodeHexString("#46"));

		try {
			assertEquals("F", Hex.decodeHexString("46"));
			fail("should not get here");
		} catch (NamingException e) {
		}

		assertEquals("Ferry", Hex.decodeHexString("#4665727279"));
	}
}
