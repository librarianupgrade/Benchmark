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

package org.apache.servicecomb.samples;

import org.apache.servicecomb.demo.TestMgr;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TestClientApplication.class)
public class ZookeeperIT {
	private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperIT.class);

	@BeforeEach
	public void setUp() {
		TestMgr.errors().clear();
	}

	@Test
	public void clientGetsNoError() throws Exception {
		try {
			TestClientApplication.run();
		} catch (Exception e) {
			TestMgr.failed("test case run failed", e);
			LOGGER.error("-------------- test failed -------------");
			LOGGER.error("", e);
			LOGGER.error("-------------- test failed -------------");
		}
		TestMgr.summary();
		Assertions.assertTrue(TestMgr.errors().isEmpty());
	}
}
