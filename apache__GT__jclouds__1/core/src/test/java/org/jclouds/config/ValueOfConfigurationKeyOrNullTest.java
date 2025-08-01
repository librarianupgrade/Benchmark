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
package org.jclouds.config;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.name.Names;

/**
 * Tests behavior of ValueOfConfigurationKeyOrNull
 */
@Test(groups = "unit")
public class ValueOfConfigurationKeyOrNullTest {

	@Test
	public void testNotThere() {
		assertEquals(Guice.createInjector().getInstance(ValueOfConfigurationKeyOrNull.class).apply("foo"), null);
	}

	@Test
	public void testThere() {
		assertEquals(Guice.createInjector(new AbstractModule() {

			@Override
			protected void configure() {
				bindConstant().annotatedWith(Names.named("foo")).to("bar");
			}

		}).getInstance(ValueOfConfigurationKeyOrNull.class).apply("foo"), "bar");
	}

	@Test
	public void testEmptyIsThere() {
		assertEquals(Guice.createInjector(new AbstractModule() {

			@Override
			protected void configure() {
				bindConstant().annotatedWith(Names.named("foo")).to("");
			}

		}).getInstance(ValueOfConfigurationKeyOrNull.class).apply("foo"), "");
	}
}
