/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.bookkeeper.common.conf.validators;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit test {@link RangeValidator} validator.
 */
public class RangeValidatorTest {

	@Test
	public void testAtLeastRangeValidator() {
		RangeValidator range = RangeValidator.atLeast(1234L);
		assertTrue(range.validate("test-0", 1235L));
		assertTrue(range.validate("test-1", 1234L));
		assertFalse(range.validate("test-2", 1233L));
	}

	@Test
	public void testAtMostRangeValidator() {
		RangeValidator range = RangeValidator.atMost(1234L);
		assertFalse(range.validate("test-0", 1235L));
		assertTrue(range.validate("test-1", 1234L));
		assertTrue(range.validate("test-2", 1233L));
	}

	@Test
	public void testBetweenRangeValidator() {
		RangeValidator range = RangeValidator.between(1230L, 1240L);
		assertTrue(range.validate("test-0", 1230L));
		assertTrue(range.validate("test-1", 1235L));
		assertTrue(range.validate("test-2", 1240L));
		assertFalse(range.validate("test-3", 1229L));
		assertFalse(range.validate("test-4", 1241L));
	}

}
