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
package org.apache.directory.api.ldap.extras.extended.ads_impl.pwdModify;

import org.apache.directory.api.asn1.ber.grammar.States;

/**
 * This class store the PasswordModifyResponse's grammar constants. It is also used
 * for debugging purposes.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum PasswordModifyResponseStatesEnum implements States {

	/** The END_STATE */
	END_STATE,

	/** start state*/
	START_STATE,

	/** sequence*/
	PASSWORD_MODIFY_RESPONSE_SEQUENCE_STATE,

	/** the generated password */
	GEN_PASSWORD_STATE,

	/** Last state */
	LAST_PASSWORD_MODIFY_RESPONSE_STATE;

	/**
	 * Get the grammar name
	 * 
	 * @return The grammar name
	 */
	public String getGrammarName() {
		return "PASSWORD_MODIFY_RESPONSE_GRAMMER";
	}

	/**
	 * Get the string representing the state
	 * 
	 * @param state The state number
	 * @return The String representing the state
	 */
	public String getState(int state) {
		return (state == END_STATE.ordinal()) ? "PASSWORD_MODIFY_RESPONSE_GRAMMER" : name();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEndState() {
		return this == END_STATE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PasswordModifyResponseStatesEnum getStartState() {
		return START_STATE;
	}
}
