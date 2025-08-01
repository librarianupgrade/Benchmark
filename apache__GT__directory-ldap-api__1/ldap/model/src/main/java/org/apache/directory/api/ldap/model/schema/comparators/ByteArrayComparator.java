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
package org.apache.directory.api.ldap.model.schema.comparators;

import org.apache.directory.api.ldap.model.schema.LdapComparator;
import org.apache.directory.api.util.Strings;

/**
 * A comparator for byte[]s.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ByteArrayComparator extends LdapComparator<byte[]> {
	/** The serial version UID */
	private static final long serialVersionUID = 2L;

	/**
	 * The ByteArrayComparator constructor. Its OID is the OctetStringMatch matching
	 * rule OID.
	 * 
	 * @param oid The Comparator's OID
	 */
	public ByteArrayComparator(String oid) {
		super(oid);
	}

	/**
	 * {@inheritDoc}
	 */
	public int compare(byte[] b1, byte[] b2) {
		return Strings.compare(b1, b2);
	}
}
