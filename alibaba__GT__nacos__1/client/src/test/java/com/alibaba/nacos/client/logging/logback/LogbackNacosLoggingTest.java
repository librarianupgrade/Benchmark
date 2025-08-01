/*
 *
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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
 *
 */

package com.alibaba.nacos.client.logging.logback;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.isA;

public class LogbackNacosLoggingTest {

	@Rule
	public ExpectedException exceptionRule = ExpectedException.none();

	@Test
	public void testLoadConfiguration() {
		exceptionRule.expectCause(isA(ClassCastException.class));
		exceptionRule.expectMessage("Could not initialize Logback Nacos logging from classpath:nacos-logback.xml");
		LogbackNacosLogging logbackNacosLogging = new LogbackNacosLogging();
		logbackNacosLogging.loadConfiguration();
	}
}