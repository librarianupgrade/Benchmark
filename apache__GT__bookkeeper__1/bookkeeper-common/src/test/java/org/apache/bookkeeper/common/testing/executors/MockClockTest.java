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
package org.apache.bookkeeper.common.testing.executors;

import static org.junit.Assert.assertEquals;

import java.time.Duration;
import org.junit.Before;
import org.junit.Test;

/**
 * Test {@link MockClock}.
 */
public class MockClockTest {

	private MockClock clock;

	@Before
	public void setup() {
		this.clock = new MockClock();
	}

	@Test
	public void testAdvance() {
		assertEquals(0L, clock.millis());
		clock.advance(Duration.ofMillis(10));
		assertEquals(10L, clock.millis());
	}

}
