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

import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.api.ldap.model.schema.SyntaxChecker;

/**
 * A SyntaxChecker which verifies that a value is a DataQualitySyntax.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@SuppressWarnings("serial")
public final class DataQualitySyntaxSyntaxChecker extends SyntaxChecker {
	/**
	 * A static instance of DataQualitySyntaxSyntaxChecker
	 */
	public static final DataQualitySyntaxSyntaxChecker INSTANCE = new DataQualitySyntaxSyntaxChecker(
			SchemaConstants.DATA_QUALITY_SYNTAX);

	/**
	 * A static Builder for this class
	 */
	public static final class Builder extends SCBuilder<DataQualitySyntaxSyntaxChecker> {
		/**
		 * The Builder constructor
		 */
		private Builder() {
			super(SchemaConstants.DATA_QUALITY_SYNTAX);
		}

		/**
		 * Create a new instance of DataQualitySyntaxSyntaxChecker
		 * @return A new instance of DataQualitySyntaxSyntaxChecker
		 */
		@Override
		public DataQualitySyntaxSyntaxChecker build() {
			return new DataQualitySyntaxSyntaxChecker(oid);
		}
	}

	/**
	 * Creates a new instance of DataQualitySyntaxSyntaxChecker.
	 * 
	 * @param oid The OID to use for this SyntaxChecker
	 */
	private DataQualitySyntaxSyntaxChecker(String oid) {
		super(oid);
	}

	/**
	 * @return An instance of the Builder for this class
	 */
	public static Builder builder() {
		return new Builder();
	}
}
