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

import org.apache.directory.api.ldap.model.schema.syntaxCheckers.ObjectClassDescriptionSyntaxChecker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

/**
 * Test cases for ObjectClassDescriptionSyntaxChecker.
 * 
 * There are also many test cases in SchemaParserObjectClassDescriptionTest.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@Execution(ExecutionMode.CONCURRENT)
public class ObjectClassDescriptionSyntaxCheckerTest {
	private ObjectClassDescriptionSyntaxChecker checker = ObjectClassDescriptionSyntaxChecker.INSTANCE;

	@Test
	public void testValid() {
		assertTrue(checker.isValidSyntax("( 2.5.6.6 )"));
		assertFalse(checker.isValidSyntax("( 2.5.6.6 NAME person )"));
		assertTrue(checker.isValidSyntax("( 2.5.6.6 NAME 'person' )"));
		assertTrue(checker.isValidSyntax("( 2.5.6.6 NAME 'person' DESC 'RFC2256: a person' )"));
		assertTrue(checker.isValidSyntax("( 2.5.6.6 NAME 'person' DESC 'RFC2256: a person' SUP top )"));
		assertTrue(checker.isValidSyntax("( 2.5.6.6 NAME 'person' DESC 'RFC2256: a person' SUP top STRUCTURAL )"));
		assertTrue(checker.isValidSyntax(
				"( 2.5.6.6 NAME 'person' DESC 'RFC2256: a person' SUP top STRUCTURAL MUST ( sn $ cn ) )"));
		assertTrue(checker.isValidSyntax(
				"( 2.5.6.6 NAME 'person' DESC 'RFC2256: a person' SUP top STRUCTURAL MUST ( sn $ cn ) MAY ( userPassword $ telephoneNumber $ seeAlso $ description ) )"));

		assertTrue(checker.isValidSyntax("(2.5.6.6)"));
		assertTrue(checker.isValidSyntax("(      2.5.6.6      NAME      'person'      )"));
	}

	@Test
	public void testInvalid() {
		// null 
		assertFalse(checker.isValidSyntax(null));

		// empty 
		assertFalse(checker.isValidSyntax(""));

		// missing/invalid OID
		assertFalse(checker.isValidSyntax("()"));
		assertFalse(checker.isValidSyntax("(  )"));
		assertFalse(checker.isValidSyntax("( . )"));
		assertFalse(checker.isValidSyntax("( 1 )"));
		assertFalse(checker.isValidSyntax("( 1. )"));
		assertFalse(checker.isValidSyntax("( 1.2. )"));
		assertFalse(checker.isValidSyntax("( 1.A )"));
		assertFalse(checker.isValidSyntax("( A.B )"));

		// missing right parenthesis
		assertFalse(checker.isValidSyntax("( 2.5.6.6 NAME 'person'"));

		// lowercase NAME, DESC, SUP
		assertFalse(checker.isValidSyntax("( 2.5.6.6 name 'person' desc 'RFC2256: a person' sup top "));

	}

}
