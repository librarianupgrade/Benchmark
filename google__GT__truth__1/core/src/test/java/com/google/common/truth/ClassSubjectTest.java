/*
 * Copyright (c) 2011 Google, Inc.
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
package com.google.common.truth;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for introspective Subject behaviour.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class ClassSubjectTest extends BaseSubjectTestCase {
	@Test
	public void testIsAssignableTo_same() {
		assertThat(String.class).isAssignableTo(String.class);
	}

	@Test
	public void testIsAssignableTo_parent() {
		assertThat(String.class).isAssignableTo(Object.class);
		assertThat(NullPointerException.class).isAssignableTo(Exception.class);
	}

	@Test
	public void testIsAssignableTo_reversed() {
		expectFailureWhenTestingThat(Object.class).isAssignableTo(String.class);
		assertFailureValue("expected to be assignable to", "java.lang.String");
	}

	@Test
	public void testIsAssignableTo_differentTypes() {
		expectFailureWhenTestingThat(String.class).isAssignableTo(Exception.class);
		assertFailureValue("expected to be assignable to", "java.lang.Exception");
	}

	private ClassSubject expectFailureWhenTestingThat(Class<?> actual) {
		return expectFailure.whenTesting().that(actual);
	}
}
