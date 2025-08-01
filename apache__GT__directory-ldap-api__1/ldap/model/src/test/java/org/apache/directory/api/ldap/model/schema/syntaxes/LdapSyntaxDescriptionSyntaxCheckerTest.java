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

import org.apache.directory.api.ldap.model.schema.syntaxCheckers.LdapSyntaxDescriptionSyntaxChecker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

/**
 * Test cases for LdapSyntaxDescriptionSyntaxChecker.
 * 
 * There are also many test cases in SchemaParserLdapSyntaxDescriptionTest.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@Execution(ExecutionMode.CONCURRENT)
public class LdapSyntaxDescriptionSyntaxCheckerTest {
	private LdapSyntaxDescriptionSyntaxChecker checker = LdapSyntaxDescriptionSyntaxChecker.INSTANCE;

	@Test
	public void testValid() {
		assertTrue(checker.isValidSyntax(("( 1.3.6.1.4.1.1466.115.121.1.15 )")));
		assertTrue(checker.isValidSyntax(("( 1.3.6.1.4.1.1466.115.121.1.15 DESC 'Directory String' )")));
		assertTrue(
				checker.isValidSyntax(("( 1.3.6.1.4.1.1466.115.121.1.15 DESC 'Directory String' X-ABC-DEF 'test' )")));

		// spaces
		assertTrue(checker.isValidSyntax("(1.3.6.1.4.1.1466.115.121.1.15)"));
		assertTrue(checker.isValidSyntax(
				"(      1.3.6.1.4.1.1466.115.121.1.15        DESC 'Directory String' X-ABC-DEF     'test'     )"));

		// lowercase DESC
		assertTrue(checker.isValidSyntax("( 1.3.6.1.4.1.1466.115.121.1.15 desc 'Directory String' )"));
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
		assertFalse(checker.isValidSyntax("( 1.3.6.1.4.1.1466.115.121.1.15 "));

		// missing quotes
		assertFalse(checker.isValidSyntax("( 1.3.6.1.4.1.1466.115.121.1.15 DESC Directory String )"));

		// invalid extension
		assertFalse(checker.isValidSyntax("( 1.3.6.1.4.1.1466.115.121.1.15 DESC 'Directory String' X-ABC-DEF )"));
		assertFalse(
				checker.isValidSyntax("( 1.3.6.1.4.1.1466.115.121.1.15 DESC 'Directory String' X-ABC-123 'test' )"));

	}

}
