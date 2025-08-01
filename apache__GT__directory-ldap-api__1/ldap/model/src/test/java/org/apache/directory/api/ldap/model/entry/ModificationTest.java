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
package org.apache.directory.api.ldap.model.entry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.DefaultAttribute;
import org.apache.directory.api.ldap.model.entry.DefaultModification;
import org.apache.directory.api.ldap.model.entry.Modification;
import org.apache.directory.api.ldap.model.entry.ModificationOperation;
import org.apache.directory.api.ldap.model.exception.LdapException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

/**
 * Test the DefaultModification class
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@Execution(ExecutionMode.CONCURRENT)
public class ModificationTest {
	/**
	 * Serialize a DefaultModification
	 */
	private ByteArrayOutputStream serializeValue(Modification modification) throws IOException {
		ObjectOutputStream oOut = null;
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		try {
			oOut = new ObjectOutputStream(out);
			modification.writeExternal(oOut);
		} catch (IOException ioe) {
			throw ioe;
		} finally {
			try {
				if (oOut != null) {
					oOut.flush();
					oOut.close();
				}
			} catch (IOException ioe) {
				throw ioe;
			}
		}

		return out;
	}

	/**
	 * Deserialize a DefaultModification
	 */
	private Modification deserializeValue(ByteArrayOutputStream out) throws IOException, ClassNotFoundException {
		ObjectInputStream oIn = null;
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

		try {
			Modification modification = new DefaultModification();

			oIn = new ObjectInputStream(in);

			modification.readExternal(oIn);

			return modification;
		} catch (IOException ioe) {
			throw ioe;
		} finally {
			try {
				if (oIn != null) {
					oIn.close();
				}
			} catch (IOException ioe) {
				throw ioe;
			}
		}
	}

	@Test
	public void testCreateServerModification() throws LdapException {
		Attribute attribute = new DefaultAttribute("cn");
		attribute.add("test1", "test2");

		Modification mod = new DefaultModification(ModificationOperation.ADD_ATTRIBUTE, attribute);
		Modification clone = mod.clone();

		attribute.remove("test2");

		Attribute clonedAttribute = clone.getAttribute();

		assertEquals(1, mod.getAttribute().size());
		assertTrue(mod.getAttribute().contains("test1"));

		assertEquals(2, clonedAttribute.size());
		assertTrue(clone.getAttribute().contains("test1"));
		assertTrue(clone.getAttribute().contains("test2"));
	}

	@Test
	public void testSerializationModificationADD() throws ClassNotFoundException, IOException, LdapException {
		Attribute attribute = new DefaultAttribute("cn");
		attribute.add("test1", "test2");

		DefaultModification mod = new DefaultModification(ModificationOperation.ADD_ATTRIBUTE, attribute);

		Modification modSer = deserializeValue(serializeValue(mod));

		assertEquals(mod, modSer);
	}

	@Test
	public void testSerializationModificationREPLACE() throws ClassNotFoundException, IOException, LdapException {
		Attribute attribute = new DefaultAttribute("cn");
		attribute.add("test1", "test2");

		DefaultModification mod = new DefaultModification(ModificationOperation.REPLACE_ATTRIBUTE, attribute);

		Modification modSer = deserializeValue(serializeValue(mod));

		assertEquals(mod, modSer);
	}

	@Test
	public void testSerializationModificationREMOVE() throws ClassNotFoundException, IOException, LdapException {
		Attribute attribute = new DefaultAttribute("cn");
		attribute.add("test1", "test2");

		DefaultModification mod = new DefaultModification(ModificationOperation.REMOVE_ATTRIBUTE, attribute);

		Modification modSer = deserializeValue(serializeValue(mod));

		assertEquals(mod, modSer);
	}

	@Test
	public void testSerializationModificationNoAttribute() throws ClassNotFoundException, IOException {
		DefaultModification mod = new DefaultModification();

		mod.setOperation(ModificationOperation.ADD_ATTRIBUTE);

		Modification modSer = deserializeValue(serializeValue(mod));

		assertEquals(mod, modSer);
	}
}
