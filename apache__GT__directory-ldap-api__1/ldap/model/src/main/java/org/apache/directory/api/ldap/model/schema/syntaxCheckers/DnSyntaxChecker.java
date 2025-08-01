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
package org.apache.directory.api.ldap.model.schema.syntaxCheckers;

import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.name.Rdn;
import org.apache.directory.api.ldap.model.schema.SyntaxChecker;
import org.apache.directory.api.util.Strings;

/**
 * A SyntaxChecker which verifies that a value is a valid {@link Dn}. We just check
 * that the {@link Dn} is valid, we don't need to verify each of the {@link Rdn} syntax.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@SuppressWarnings("serial")
public final class DnSyntaxChecker extends SyntaxChecker {
	/**
	 * A static instance of DnSyntaxChecker
	 */
	public static final DnSyntaxChecker INSTANCE = new DnSyntaxChecker(SchemaConstants.DN_SYNTAX);

	/**
	 * A static Builder for this class
	 */
	public static final class Builder extends SCBuilder<DnSyntaxChecker> {
		/**
		 * The Builder constructor
		 */
		private Builder() {
			super(SchemaConstants.DN_SYNTAX);
		}

		/**
		 * Create a new instance of DnSyntaxChecker
		 * @return A new instance of DnSyntaxChecker
		 */
		@Override
		public DnSyntaxChecker build() {
			return new DnSyntaxChecker(oid);
		}
	}

	/**
	 * Creates a new instance of DNSyntaxChecker.
	 * 
	 * @param oid The OID to use for this SyntaxChecker
	 */
	private DnSyntaxChecker(String oid) {
		super(oid);
	}

	/**
	 * @return An instance of the Builder for this class
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isValidSyntax(Object value) {
		String strValue;

		if (value == null) {
			if (LOG.isDebugEnabled()) {
				LOG.debug(I18n.err(I18n.ERR_13210_SYNTAX_INVALID, "null"));
			}

			return false;
		}

		if (value instanceof String) {
			strValue = (String) value;
		} else if (value instanceof byte[]) {
			strValue = Strings.utf8ToString((byte[]) value);
		} else {
			strValue = value.toString();
		}

		if (strValue.length() == 0) {
			// TODO: this should be a false, but for 
			// some reason, the principal is empty in 
			// some cases.
			if (LOG.isDebugEnabled()) {
				LOG.debug(I18n.msg(I18n.MSG_13701_SYNTAX_VALID, value));
			}

			return true;
		}

		// Check that the value is a valid Dn
		boolean result = Dn.isValid(strValue);

		if (LOG.isDebugEnabled()) {
			if (result) {
				LOG.debug(I18n.msg(I18n.MSG_13701_SYNTAX_VALID, value));
			} else {
				LOG.debug(I18n.err(I18n.ERR_13210_SYNTAX_INVALID, value));
			}
		}

		return result;
	}
}
