/*
 * Copyright (c) 2019 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.common.truth.refactorings;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.CompilationTestHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** @author cpovirk@google.com (Chris Povirk) */
@RunWith(JUnit4.class)
public class CorrespondenceSubclassToFactoryCallTest {
	private BugCheckerRefactoringTestHelper refactoringHelper;
	private CompilationTestHelper compilationHelper;

	@Before
	public void setUp() {
		compilationHelper = CompilationTestHelper.newInstance(CorrespondenceSubclassToFactoryCall.class, getClass());
		refactoringHelper = BugCheckerRefactoringTestHelper.newInstance(CorrespondenceSubclassToFactoryCall.class,
				getClass());
	}

	@Test
	public void testPositiveCase() {
		compilationHelper.addSourceFile("CorrespondenceSubclassToFactoryCallPositiveCases.java").doTest();
	}

	@Test
	public void testPositiveCase2() {
		compilationHelper.addSourceFile("CorrespondenceSubclassToFactoryCallPositiveCases2.java").doTest();
	}

	@Test
	public void testNegativeCase() {
		compilationHelper.addSourceFile("CorrespondenceSubclassToFactoryCallNegativeCases.java").doTest();
	}

	@Test
	public void refactoring() {
		refactoringHelper.addInput("CorrespondenceSubclassToFactoryCallPositiveCases.java")
				.addOutput("CorrespondenceSubclassToFactoryCallPositiveCases_expected.java").doTest();
	}

	@Test
	public void refactoring2() {
		refactoringHelper.addInput("CorrespondenceSubclassToFactoryCallPositiveCases2.java")
				.addOutput("CorrespondenceSubclassToFactoryCallPositiveCases2_expected.java").doTest();
	}
}
