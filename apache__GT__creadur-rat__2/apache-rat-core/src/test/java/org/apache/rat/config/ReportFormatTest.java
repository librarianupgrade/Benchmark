package org.apache.rat.config;

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

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ReportFormatTest {
	@Test
	public void isANullSafe() {
		for (String optionType : Arrays.asList(null, "")) {
			assertFalse("Must not equal PLAIN, was " + optionType, ReportFormat.PLAIN.is(optionType));
		}
	}

	@Test
	public void isAConfigurationOption() {
		for (String optionType : Arrays.asList("PLAIN", "pLain", "plain", ReportFormat.PLAIN.name())) {
			assertTrue("Must equal PLAIN, was " + optionType, ReportFormat.PLAIN.is(optionType));
		}
		assertFalse(ReportFormat.PLAIN.is(ReportFormat.XML.name()));
	}
}
