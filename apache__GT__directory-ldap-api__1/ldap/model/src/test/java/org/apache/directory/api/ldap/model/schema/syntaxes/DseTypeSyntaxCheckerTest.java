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

import org.apache.directory.api.ldap.model.schema.syntaxCheckers.DseTypeSyntaxChecker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

/**
 * Test cases for DSETypeSyntaxChecker.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@Execution(ExecutionMode.CONCURRENT)
public class DseTypeSyntaxCheckerTest {
	DseTypeSyntaxChecker checker = DseTypeSyntaxChecker.INSTANCE;

	@Test
	public void testNullString() {
		assertFalse(checker.isValidSyntax(null));
	}

	@Test
	public void testEmptyString() {
		assertFalse(checker.isValidSyntax(""));
	}

	@Test
	public void testWrongCase() {
		assertFalse(checker.isValidSyntax("()"));
		assertFalse(checker.isValidSyntax("(xyz)"));
		assertFalse(checker.isValidSyntax("sa"));
		assertFalse(checker.isValidSyntax("(SA)"));
		assertFalse(checker.isValidSyntax(" () "));
		assertFalse(checker.isValidSyntax(" (sa) "));
		assertFalse(checker.isValidSyntax("(sa) "));
		assertFalse(checker.isValidSyntax("(sa$)"));
		assertFalse(checker.isValidSyntax("(sa$ )"));
		assertFalse(checker.isValidSyntax("( sa $ sa )"));
	}

	@Test
	public void testCorrectCase() {
		assertTrue(checker.isValidSyntax("(sa)"));
		assertTrue(checker.isValidSyntax("(  sa   )"));
		assertTrue(checker.isValidSyntax("( root $ glue $ cp $ entry $ alias $ subr $ "
				+ "nssr $ supr $ xr $ admPoint $ subentry $ " + "shadow $ zombie $ immSupr $ rhob $ sa )"));
	}
}
