/**
 *
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
 *
 */
package org.apache.bookkeeper.common.util.affinity.impl;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Sets;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import org.junit.Test;

/**
 * Tests for CpuInfo class.
 */
public class CpuInfoTest {

	@Test
	public void testParseCpuInfo() throws Exception {
		try (BufferedReader r = new BufferedReader(
				new InputStreamReader(CpuInfoTest.class.getResourceAsStream("/proc_cpuinfo.txt")))) {
			String text = r.lines().collect(Collectors.joining("\n"));

			ProcessorsInfo pi = ProcessorsInfo.parseCpuInfo(text);

			assertEquals(Sets.newHashSet(0, 12), pi.getCpusOnSameCore(0));
			assertEquals(Sets.newHashSet(0, 12), pi.getCpusOnSameCore(12));

			assertEquals(Sets.newHashSet(8, 20), pi.getCpusOnSameCore(8));
			assertEquals(Sets.newHashSet(8, 20), pi.getCpusOnSameCore(20));
		}
	}
}
