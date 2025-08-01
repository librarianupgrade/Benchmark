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
package com.opensymphony.xwork2.security;

import com.opensymphony.xwork2.XWorkTestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class DefaultExcludedPatternsCheckerTest extends XWorkTestCase {

	public void testHardcodedPatterns() throws Exception {
		// given
		List<String> params = new ArrayList<String>() {
			{
				add("%{#application['test']}");
				add("%{#application.test}");
				add("%{#Application['test']}");
				add("%{#Application.test}");
				add("%{#session['test']}");
				add("%{#session.test}");
				add("%{#Session['test']}");
				add("%{#Session.test}");
				add("%{#struts['test']}");
				add("%{#struts.test}");
				add("%{#Struts['test']}");
				add("%{#Struts.test}");
				add("%{#request['test']}");
				add("%{#request.test}");
				add("%{#Request['test']}");
				add("%{#Request.test}");
				add("%{#servletRequest['test']}");
				add("%{#servletRequest.test}");
				add("%{#ServletRequest['test']}");
				add("%{#ServletRequest.test}");
				add("%{#servletResponse['test']}");
				add("%{#servletResponse.test}");
				add("%{#ServletResponse['test']}");
				add("%{#ServletResponse.test}");
				add("%{#servletContext['test']}");
				add("%{#servletContext.test}");
				add("%{#ServletContext['test']}");
				add("%{#ServletContext.test}");
				add("%{#parameters['test']}");
				add("%{#parameters.test}");
				add("%{#Parameters['test']}");
				add("%{#Parameters.test}");
				add("#context.get('com.opensymphony.xwork2.dispatcher.HttpServletResponse')");
				add("%{#context.get('com.opensymphony.xwork2.dispatcher.HttpServletResponse')}");
				add("#_memberAccess[\"allowStaticMethodAccess\"]= new java.lang.Boolean(true)");
				add("%{#_memberAccess[\"allowStaticMethodAccess\"]= new java.lang.Boolean(true)}");
				add("form.class.classLoader");
				add("form[\"class\"][\"classLoader\"]");
				add("form['class']['classLoader']");
				add("class['classLoader']");
				add("class[\"classLoader\"]");
				add("class.classLoader.resources.dirContext.docBase=tttt");
				add("Class.classLoader.resources.dirContext.docBase=tttt");
			}
		};

		DefaultExcludedPatternsChecker checker = new DefaultExcludedPatternsChecker();
		checker.setAdditionalExcludePatterns(".*(^|\\.|\\[|'|\")class(\\.|\\[|'|\").*");

		for (String param : params) {
			// when
			ExcludedPatternsChecker.IsExcluded actual = checker.isExcluded(param);

			// then
			assertTrue("Access to " + param + " is possible!", actual.isExcluded());
		}
	}

	public void testDefaultExcludePatterns() throws Exception {
		// given
		List<String> prefixes = Arrays.asList("#[0].%s", "[0].%s", "top.%s", "%{[0].%s}", "%{#[0].%s}", "%{top.%s}",
				"%{#top.%s}", "%{#%s}", "%{%s}", "#%s");
		List<String> inners = Arrays.asList("servletRequest", "servletResponse", "servletContext", "application",
				"session", "struts", "request", "response", "dojo", "parameters");
		List<String> suffixes = Arrays.asList("['test']", "[\"test\"]", ".test");

		DefaultExcludedPatternsChecker checker = new DefaultExcludedPatternsChecker();
		checker.setAdditionalExcludePatterns(".*(^|\\.|\\[|'|\")class(\\.|\\[|'|\").*");

		List<String> params = new ArrayList<String>();
		for (String prefix : prefixes) {
			for (String inner : inners) {
				String innerUp = inner.substring(0, 1).toUpperCase() + inner.substring(1);
				for (String suffix : suffixes) {
					params.add(prefix.replace("%s", inner + suffix));
					params.add(prefix.replace("%s", innerUp + suffix));
				}
			}
		}

		for (String param : params) {
			// when
			ExcludedPatternsChecker.IsExcluded actual = checker.isExcluded(param);

			// then
			assertTrue("Access to " + param + " is possible!", actual.isExcluded());
		}
	}

	public void testParamWithClassInName() throws Exception {
		// given
		List<String> properParams = new ArrayList<>();
		properParams.add("eventClass");
		properParams.add("form.eventClass");
		properParams.add("form[\"eventClass\"]");
		properParams.add("form['eventClass']");
		properParams.add("class.super@demo.com");
		properParams.add("super.class@demo.com");

		ExcludedPatternsChecker checker = new DefaultExcludedPatternsChecker();

		for (String properParam : properParams) {
			// when
			ExcludedPatternsChecker.IsExcluded actual = checker.isExcluded(properParam);

			// then
			assertFalse("Param '" + properParam + "' is excluded!", actual.isExcluded());
		}
	}

	public void testStrutsTokenIsExcluded() throws Exception {
		// given
		List<String> tokens = new ArrayList<>();
		tokens.add("struts.token.name");
		tokens.add("struts.token");

		ExcludedPatternsChecker checker = new DefaultExcludedPatternsChecker();

		for (String token : tokens) {
			// when
			ExcludedPatternsChecker.IsExcluded actual = checker.isExcluded(token);

			// then
			assertTrue("Param '" + token + "' is not excluded!", actual.isExcluded());
		}
	}

	public void testExcludedPatternsImmutable() throws Exception {
		ExcludedPatternsChecker checker = new DefaultExcludedPatternsChecker();

		Set<Pattern> excludedPatternSet = checker.getExcludedPatterns();
		assertNotNull("default excluded patterns null?", excludedPatternSet);
		assertFalse("default excluded patterns empty?", excludedPatternSet.isEmpty());
		try {
			excludedPatternSet.add(Pattern.compile("SomeRegexPattern"));
			fail("excluded patterns modifiable?");
		} catch (UnsupportedOperationException uoe) {
			// Expected result
		}
		try {
			excludedPatternSet.clear();
			fail("excluded patterns modifiable?");
		} catch (UnsupportedOperationException uoe) {
			// Expected result
		}

		checker.setExcludedPatterns(DefaultExcludedPatternsChecker.EXCLUDED_PATTERNS);
		excludedPatternSet = checker.getExcludedPatterns();
		assertNotNull("default excluded patterns null?", excludedPatternSet);
		assertFalse("default excluded patterns empty?", excludedPatternSet.isEmpty());
		try {
			excludedPatternSet.add(Pattern.compile("SomeRegexPattern"));
			fail("excluded patterns modifiable?");
		} catch (UnsupportedOperationException uoe) {
			// Expected result
		}
		try {
			excludedPatternSet.clear();
			fail("excluded patterns modifiable?");
		} catch (UnsupportedOperationException uoe) {
			// Expected result
		}

		String[] testPatternArray = { "exactmatch1", "exactmatch2", "exactmatch3", "exactmatch4" };
		checker.setExcludedPatterns(testPatternArray);
		excludedPatternSet = checker.getExcludedPatterns();
		assertNotNull("default excluded patterns null?", excludedPatternSet);
		assertFalse("default excluded patterns empty?", excludedPatternSet.isEmpty());
		assertTrue("replaced default accepted patterns not size " + testPatternArray.length + "?",
				excludedPatternSet.size() == testPatternArray.length);
		for (String testPatternArray1 : testPatternArray) {
			assertTrue(testPatternArray1 + " not excluded?", checker.isExcluded(testPatternArray1).isExcluded());
		}
		try {
			excludedPatternSet.add(Pattern.compile("SomeRegexPattern"));
			fail("excluded patterns modifiable?");
		} catch (UnsupportedOperationException uoe) {
			// Expected result
		}
		try {
			excludedPatternSet.clear();
			fail("excluded patterns modifiable?");
		} catch (UnsupportedOperationException uoe) {
			// Expected result
		}
	}

	public static final ExcludedPatternsChecker NO_EXCLUSION_PATTERNS_CHECKER = new ExcludedPatternsChecker() {
		@Override
		public IsExcluded isExcluded(String value) {
			return IsExcluded.no(new HashSet<>());
		}

		@Override
		public void setExcludedPatterns(String commaDelimitedPatterns) {

		}

		@Override
		public void setExcludedPatterns(String[] patterns) {

		}

		@Override
		public void setExcludedPatterns(Set<String> patterns) {

		}

		@Override
		public Set<Pattern> getExcludedPatterns() {
			return null;
		}
	};
}
