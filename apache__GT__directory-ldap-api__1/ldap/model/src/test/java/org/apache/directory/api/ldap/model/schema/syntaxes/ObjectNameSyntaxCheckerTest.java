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

import org.apache.directory.api.ldap.model.schema.syntaxCheckers.ObjectNameSyntaxChecker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

/**
 * Test cases for ObjectNameSyntaxChecker.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@Execution(ExecutionMode.CONCURRENT)
public class ObjectNameSyntaxCheckerTest {
	ObjectNameSyntaxChecker checker = ObjectNameSyntaxChecker.INSTANCE;

	@Test
	public void testNullString() {
		assertFalse(checker.isValidSyntax(null));
	}

	@Test
	public void testEmptyString() {
		assertFalse(checker.isValidSyntax(""));
	}

	@Test
	public void testName() {
		assertTrue(checker.isValidSyntax("a"));
		assertTrue(checker.isValidSyntax("azerty"));
		assertTrue(checker.isValidSyntax("A"));
		assertTrue(checker.isValidSyntax("AZERTY"));
		assertTrue(checker.isValidSyntax("AzErTy"));
		assertTrue(checker.isValidSyntax("a123;-bcdEf0"));
	}

	@Test
	public void testWrongName() {
		assertFalse(checker.isValidSyntax("1test"));
		assertFalse(checker.isValidSyntax(";test"));
		assertFalse(checker.isValidSyntax("-test"));
		assertFalse(checker.isValidSyntax("tes "));
		assertFalse(checker.isValidSyntax("http://test"));
		assertFalse(checker.isValidSyntax("a name"));
	}
}
