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
package org.apache.directory.api.ldap.model.message.controls;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.directory.api.ldap.model.message.controls.OpaqueControl;
import org.apache.directory.api.util.Strings;
import org.junit.jupiter.api.Test;

/**
 * Test the OpaqueControl class
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpaqueControlTest {
	@Test
	public void testEmptyValue() {
		OpaqueControl control = new OpaqueControl("1.1");

		assertFalse(control.hasEncodedValue());

		control.setEncodedValue(Strings.EMPTY_BYTES);

		assertTrue(control.hasEncodedValue());
	}
}
