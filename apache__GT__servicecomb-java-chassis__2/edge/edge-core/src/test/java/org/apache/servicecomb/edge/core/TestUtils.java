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

package org.apache.servicecomb.edge.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestUtils {
	@Test
	public void testUtils() {
		Assertions.assertEquals("/a/b/c", Utils.findActualPath("/a/b/c", -1));
		Assertions.assertEquals("/a/b/c", Utils.findActualPath("/a/b/c", 0));
		Assertions.assertEquals("/b/c", Utils.findActualPath("/a/b/c", 1));
		Assertions.assertEquals("/c", Utils.findActualPath("/a/b/c", 2));
		Assertions.assertEquals("", Utils.findActualPath("/a/b/c", 3));
		Assertions.assertEquals("", Utils.findActualPath("/a/b/c", 100));
	}
}
