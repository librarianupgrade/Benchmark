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
package org.apache.directory.api.ldap.codec.controls.search.persistentSearch;

import org.apache.directory.api.asn1.ber.grammar.States;

/**
 * This class store the PSearchControl's grammar constants. It is also used for
 * debugging purposes.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum PersistentSearchStates implements States {
	// ~ Static fields/initializers
	// -----------------------------------------------------------------

	/** The END_STATE */
	END_STATE,

	// =========================================================================
	// Persistent search control grammar states
	// =========================================================================
	/** Initial state */
	START_STATE,

	/** Sequence Value */
	PSEARCH_SEQUENCE_STATE,

	/** changeTypes Value */
	CHANGE_TYPES_STATE,

	/** changesOnly Value */
	CHANGES_ONLY_STATE,

	/** returnECs Value */
	RETURN_ECS_STATE,

	/** terminal state */
	LAST_PSEARCH_STATE;

	/**
	 * Get the grammar name
	 * 
	 * @return The grammar name
	 */
	public String getGrammarName() {
		return "PSEARCH_GRAMMAR";
	}

	/**
	 * Get the string representing the state
	 * 
	 * @param state The state number
	 * @return The String representing the state
	 */
	public String getState(int state) {
		return (state == END_STATE.ordinal()) ? "PSEARCH_END_STATE" : name();
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
	public PersistentSearchStates getStartState() {
		return START_STATE;
	}
}
