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
package org.apache.directory.api.ldap.entry;

import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.LdapComparator;
import org.apache.directory.api.ldap.model.schema.LdapSyntax;
import org.apache.directory.api.ldap.model.schema.MatchingRule;
import org.apache.directory.api.ldap.model.schema.Normalizer;
import org.apache.directory.api.ldap.model.schema.PrepareString;
import org.apache.directory.api.ldap.model.schema.PrepareString.AssertionType;
import org.apache.directory.api.ldap.model.schema.SyntaxChecker;
import org.apache.directory.api.ldap.model.schema.comparators.ByteArrayComparator;
import org.apache.directory.api.ldap.model.schema.normalizers.DeepTrimToLowerNormalizer;
import org.apache.directory.api.util.Strings;

/**
 * Some common declaration used by the serverEntry tests.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class TestEntryUtils {
	/**
	 * A local Syntax class for tests
	 */
	static class AT extends AttributeType {
		private static final long serialVersionUID = 0L;

		protected AT(String oid) {
			super(oid);
		}
	}

	public static MatchingRule matchingRuleFactory(String oid) {
		MatchingRule matchingRule = new MatchingRule(oid);

		return matchingRule;
	}

	/**
	 * A local MatchingRule class for tests
	 */
	static class MR extends MatchingRule {
		/** The mandatory serialVersionUID field */
		public static final long serialVersionUID = 1L;

		protected MR(String oid) {
			super(oid);
		}
	}

	/**
	 * A local Syntax class used for the tests
	 */
	public static LdapSyntax syntaxFactory(String oid, boolean humanReadable) {
		LdapSyntax ldapSyntax = new LdapSyntax(oid);

		ldapSyntax.setHumanReadable(humanReadable);

		return ldapSyntax;
	}

	static class S extends LdapSyntax {
		private static final long serialVersionUID = 0L;

		public S(String oid, boolean humanReadible) {
			super(oid, "", humanReadible);
		}
	}

	/* no protection*/
	static AttributeType getCaseIgnoringAttributeNoNumbersType() {
		AttributeType attributeType = new AttributeType("1.1.3.1");
		LdapSyntax syntax = new LdapSyntax("1.1.1.1", "", true);

		syntax.setSyntaxChecker(new SyntaxChecker("1.1.2.1") {
			/** The mandatory serialVersionUID field */
			public static final long serialVersionUID = 1L;

			public boolean isValidSyntax(Object value) {
				if (!(value instanceof String)) {
					return false;
				}

				String strval = (String) value;

				for (char c : strval.toCharArray()) {
					if (Character.isDigit(c)) {
						return false;
					}
				}
				return true;
			}
		});

		MatchingRule matchingRule = new MatchingRule("1.1.2.1");
		matchingRule.setSyntax(syntax);

		matchingRule.setLdapComparator(new LdapComparator<String>(matchingRule.getOid()) {
			/** The mandatory serialVersionUID field */
			public static final long serialVersionUID = 1L;

			public int compare(String o1, String o2) {
				return (o1 == null ? (o2 == null ? 0 : -1) : (o2 == null ? 1 : o1.compareTo(o2)));
			}
		});

		Normalizer normalizer = new Normalizer("1.1.1") {
			/** The mandatory serialVersionUID field */
			public static final long serialVersionUID = 1L;

			public String normalize(String value) throws LdapException {
				return normalize(value, AssertionType.ATTRIBUTE_VALUE);
			}

			public String normalize(String value, PrepareString.AssertionType assertionType) throws LdapException {
				return Strings.toLowerCaseAscii(value);
			}
		};

		matchingRule.setNormalizer(normalizer);

		attributeType.setEquality(matchingRule);
		attributeType.setSyntax(syntax);

		return attributeType;
	}

	/* no protection*/static AttributeType getIA5StringAttributeType() {
		AttributeType attributeType = new AttributeType("1.1");
		attributeType.addName("1.1");
		LdapSyntax syntax = new LdapSyntax("1.1.1", "", true);

		syntax.setSyntaxChecker(new SyntaxChecker("1.1.2") {
			/** The mandatory serialVersionUID field */
			public static final long serialVersionUID = 1L;

			public boolean isValidSyntax(Object value) {
				String trimmedValue = Strings.deepTrim((String) value);

				return (trimmedValue == null) || (trimmedValue.length() < 7);
			}
		});

		MatchingRule matchingRule = new MatchingRule("1.1.2");
		matchingRule.setSyntax(syntax);

		matchingRule.setLdapComparator(new LdapComparator<String>(matchingRule.getOid()) {
			/** The mandatory serialVersionUID field */
			public static final long serialVersionUID = 1L;

			public int compare(String o1, String o2) {
				return ((o1 == null) ? (o2 == null ? 0 : -1) : (o2 == null ? 1 : o1.compareTo(o2)));
			}
		});

		matchingRule.setNormalizer(new DeepTrimToLowerNormalizer(matchingRule.getOid()));

		attributeType.setEquality(matchingRule);
		attributeType.setSyntax(syntax);

		return attributeType;
	}

	/* No protection */static AttributeType getBytesAttributeType() {
		AttributeType attributeType = new AttributeType("1.2");
		LdapSyntax syntax = new LdapSyntax("1.2.1", "", true);

		syntax.setSyntaxChecker(new SyntaxChecker("1.2.1") {
			/** The mandatory serialVersionUID field */
			public static final long serialVersionUID = 1L;

			public boolean isValidSyntax(Object value) {
				return (value == null) || (((byte[]) value).length < 5);
			}
		});

		MatchingRule matchingRule = new MatchingRule("1.2.2");
		matchingRule.setSyntax(syntax);

		matchingRule.setLdapComparator(new ByteArrayComparator("1.2.2"));

		matchingRule.setNormalizer(new Normalizer("1.1.1") {
			/** The mandatory serialVersionUID field */
			public static final long serialVersionUID = 1L;

			public String normalize(String value) throws LdapException {
				return normalize(value, AssertionType.ATTRIBUTE_VALUE);
			}

			public String normalize(String value, PrepareString.AssertionType assertionType) throws LdapException {
				byte[] val = Strings.getBytesUtf8(value);

				// each byte will be changed to be > 0, and spaces will be trimmed
				byte[] newVal = new byte[val.length];

				int i = 0;

				for (byte b : val) {
					newVal[i++] = (byte) (b & 0x007F);
				}

				return Strings.utf8ToString(Strings.trim(newVal));
			}
		});

		attributeType.setEquality(matchingRule);
		attributeType.setSyntax(syntax);

		return attributeType;
	}

	private TestEntryUtils() {
	}
}
