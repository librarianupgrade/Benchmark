/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    https://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */
package org.apache.directory.api.ldap.model.schema.syntaxes;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.directory.api.ldap.model.schema.syntaxCheckers.GeneralizedTimeSyntaxChecker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

/**
 * Test cases for GeneralizedTimeSyntaxChecker.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@Execution(ExecutionMode.CONCURRENT)
public class GeneralizedTimeSyntaxCheckerTest {
	GeneralizedTimeSyntaxChecker checker = GeneralizedTimeSyntaxChecker.INSTANCE;

	@Test
	public void testNullString() {
		assertFalse(checker.isValidSyntax(null));
	}

	@Test
	public void testEmptyString() {
		assertFalse(checker.isValidSyntax(""));
	}

	@Test
	public void testOneCharString() {
		assertFalse(checker.isValidSyntax("0"));
		assertFalse(checker.isValidSyntax("'"));
		assertFalse(checker.isValidSyntax("1"));
		assertFalse(checker.isValidSyntax("B"));
	}

	@Test
	public void testErrorCase() {
		assertFalse(checker.isValidSyntax("20060005184527Z"));
		assertFalse(checker.isValidSyntax("20061305184527Z"));
		assertFalse(checker.isValidSyntax("20062205184527Z"));
		assertFalse(checker.isValidSyntax("20061200184527Z"));
		assertFalse(checker.isValidSyntax("20061235184527Z"));
		assertFalse(checker.isValidSyntax("20061205604527Z"));
		assertFalse(checker.isValidSyntax("20061205186027Z"));
		assertFalse(checker.isValidSyntax("20061205184561Z"));
		assertFalse(checker.isValidSyntax("20061205184527Z+"));
		assertFalse(checker.isValidSyntax("20061205184527+2400"));
		assertFalse(checker.isValidSyntax("20061205184527+9900"));
		assertFalse(checker.isValidSyntax("20061205184527+1260"));
		assertFalse(checker.isValidSyntax("20061205184527+1299"));
	}

	@Test
	public void testCorrectCase() {
		assertTrue(checker.isValidSyntax("20061205184527Z"));
		assertTrue(checker.isValidSyntax("20061205184527+0500"));
		assertTrue(checker.isValidSyntax("20061205184527-1234"));
		assertTrue(checker.isValidSyntax("20061205184527.123Z"));
		assertTrue(checker.isValidSyntax("20061205184527,123+0100"));
		assertTrue(checker.isValidSyntax("2006120519Z"));
	}
}
