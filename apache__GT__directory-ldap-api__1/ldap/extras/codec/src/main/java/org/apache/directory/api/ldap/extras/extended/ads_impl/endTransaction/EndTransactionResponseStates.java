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
package org.apache.directory.api.ldap.extras.extended.ads_impl.endTransaction;

import org.apache.directory.api.asn1.ber.grammar.States;

/**
 * This class store the EndTransactionResponse's grammar constants. It is also used
 * for debugging purposes.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum EndTransactionResponseStates implements States {
	/** The END_STATE */
	END_STATE,

	/** start state*/
	START_STATE,

	/** The initial SEQUENCE */
	END_TRANSACTION_SEQUENCE_STATE,

	/** The failed message ID */
	FAILED_MESSAGE_ID_STATE,

	/** The update controls SEQ */
	UPDATE_CONTROLS_SEQ_STATE,

	/** The update control SEQ */
	UPDATE_CONTROL_SEQ_STATE,

	/** THe control's message ID state */
	CONTROL_MESSAGE_ID_STATE,

	/** The control's state */
	CONTROLS_STATE,

	/** Last state */
	LAST_STATE;

	/**
	 * Get the grammar name
	 * 
	 * @return The grammar name
	 */
	public String getGrammarName() {
		return "END_TRANSACTION_RESPONSE_GRAMMER";
	}

	/**
	 * Get the string representing the state
	 * 
	 * @param state The state number
	 * @return The String representing the state
	 */
	public String getState(int state) {
		return (state == END_STATE.ordinal()) ? "END_TRANSACTION_RESPONSE_GRAMMER" : name();
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
	public EndTransactionResponseStates getStartState() {
		return START_STATE;
	}
}
