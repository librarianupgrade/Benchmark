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
package org.apache.directory.api.ldap.model.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

/**
 * Tests the BranchNormalizedVisitor.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@Execution(ExecutionMode.CONCURRENT)
public class BranchNormalizedVisitorTest {
	@Test
	public void testBranchNormalizedVisitor0() throws Exception {
		String filter = "(ou=Human Resources)";

		ExprNode ori = FilterParser.parse(filter);

		ExprNode altered = FilterParser.parse(filter);

		BranchNormalizedVisitor visitor = new BranchNormalizedVisitor();

		visitor.visit(altered);

		assertEquals(ori.toString(), altered.toString());
	}

	@Test
	public void testBranchNormalizedVisitor1() throws Exception {
		String filter = "(&(ou=Human Resources)(uid=akarasulu))";

		ExprNode ori = FilterParser.parse(filter);

		ExprNode altered = FilterParser.parse(filter);

		BranchNormalizedVisitor visitor = new BranchNormalizedVisitor();

		visitor.visit(altered);

		assertEquals(ori.toString(), altered.toString());
	}

	@Test
	public void testBranchNormalizedVisitor2() throws Exception {
		String filter = "(&(uid=akarasulu)(ou=Human Resources)";

		filter += "(|(uid=akarasulu)(ou=Human Resources))) ";

		ExprNode ori = FilterParser.parse(filter);

		ExprNode altered = FilterParser.parse(filter);

		BranchNormalizedVisitor visitor = new BranchNormalizedVisitor();

		visitor.visit(altered);

		assertFalse(ori.toString().equals(altered.toString()));
	}

	@Test
	public void testBranchNormalizedVisitor3() throws Exception {
		String filter = "(&(ou=Human Resources)(uid=akarasulu)";

		filter += "(|(ou=Human Resources)(uid=akarasulu)))";

		ExprNode ori = FilterParser.parse(filter);

		ExprNode altered = FilterParser.parse(filter);

		BranchNormalizedVisitor visitor = new BranchNormalizedVisitor();

		visitor.visit(altered);

		assertTrue(ori.toString().equals(altered.toString()));
	}

	@Test
	public void testBranchNormalizedComplex() throws Exception {
		String filter1 = "(&(a=A)(|(b=B)(c=C)))";

		String filter2 = "(&(a=A)(|(c=C)(b=B)))";

		String normalizedFilter1 = BranchNormalizedVisitor.getNormalizedFilter(null, filter1);

		String normalizedFilter2 = BranchNormalizedVisitor.getNormalizedFilter(null, filter2);

		assertEquals(normalizedFilter1, normalizedFilter2);
	}

	public void testBranchNormalizedVisitor4() throws Exception {
		ExprNode ori = FilterParser.parse("(&(!(sn=Bob))(ou=Human Resources)(uid=akarasulu))");

		ExprNode altered = FilterParser.parse("(&(ou=Human Resources)(uid=akarasulu)(!(sn=Bob)))");

		BranchNormalizedVisitor visitor = new BranchNormalizedVisitor();

		visitor.visit(altered);

		assertTrue(ori.toString().equals(altered.toString()));

	}

}
