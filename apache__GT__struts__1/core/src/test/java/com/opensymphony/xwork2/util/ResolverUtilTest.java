/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.opensymphony.xwork2.util;

import com.opensymphony.xwork2.ObjectFactory;
import junit.framework.TestCase;

import java.net.URL;
import java.util.Set;

public class ResolverUtilTest extends TestCase {

	public void testSimpleFind() throws Exception {
		ResolverUtil<ObjectFactory> resolver = new ResolverUtil<>();
		resolver.findImplementations(ObjectFactory.class, "com");
		Set<Class<? extends ObjectFactory>> impls = resolver.getClasses();

		assertTrue(impls.contains(ObjectFactory.class));
		assertTrue(impls.contains(DummyObjectFactory.class));
	}

	public void testMissingSomeFind() throws Exception {
		ResolverUtil<ObjectFactory> resolver = new ResolverUtil<>();
		resolver.findImplementations(ObjectFactory.class, "com.opensymphony.xwork2.util");
		Set<Class<? extends ObjectFactory>> impls = resolver.getClasses();

		assertFalse(impls.contains(ObjectFactory.class));
		assertTrue(impls.contains(DummyObjectFactory.class));
	}

	public void testFindNamedResource() throws Exception {
		ResolverUtil resolver = new ResolverUtil();
		resolver.findNamedResource("xwork-default.xml", "");
		Set<URL> impls = resolver.getResources();

		assertTrue(impls.size() > 0);
	}

	public void testFindNamedResourceInDir() throws Exception {
		ResolverUtil resolver = new ResolverUtil();
		resolver.findNamedResource("SimpleAction.properties", "com/opensymphony");
		Set<URL> impls = resolver.getResources();

		assertTrue(impls.size() > 0);
	}

}
