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

import org.apache.directory.api.ldap.model.schema.syntaxCheckers.NameAndOptionalUIDSyntaxChecker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

/**
 * Test cases for NameAndOptionalUIDSyntaxChecker.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@Execution(ExecutionMode.CONCURRENT)
public class NameAndOptionalUIDSyntaxCheckerTest {
	NameAndOptionalUIDSyntaxChecker checker = NameAndOptionalUIDSyntaxChecker.INSTANCE;

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
		assertFalse(checker.isValidSyntax("#"));
	}

	@Test
	public void testWrongDN() {
		assertFalse(checker.isValidSyntax("a=b,"));
		assertFalse(checker.isValidSyntax("a=#0101'B"));
		assertFalse(checker.isValidSyntax("a=b+"));
		assertFalse(checker.isValidSyntax("a=b,c=d,"));
	}

	@Test
	public void testWrongUID() {
		assertFalse(checker.isValidSyntax("#'0101'B"));
		assertFalse(checker.isValidSyntax("a=\\#,e=f#'1010'B"));
		assertFalse(checker.isValidSyntax("a=b##'0101'B"));
		assertFalse(checker.isValidSyntax("a=b#'0101'C"));
	}

	@Test
	public void testCorrectDN() {
		assertTrue(checker.isValidSyntax("a=b"));
		assertTrue(checker.isValidSyntax("a = b"));
		assertTrue(checker.isValidSyntax("a=b + c=d"));
		assertTrue(checker.isValidSyntax("a=b,c=d"));
		assertTrue(checker.isValidSyntax("a=b\\,c \\= d, e=f"));
	}

	@Test
	public void testCorrectDNAndUID() {
		assertTrue(checker.isValidSyntax("a=b#'1010'B"));
		assertTrue(checker.isValidSyntax("a = b#'1010'B"));
		assertTrue(checker.isValidSyntax("a=b + c=d#'1010'B"));
		assertTrue(checker.isValidSyntax("a=b,c=d#'1010'B"));
		assertTrue(checker.isValidSyntax("a=b\\,c \\= d, e=f#'1010'B"));
	}
}
