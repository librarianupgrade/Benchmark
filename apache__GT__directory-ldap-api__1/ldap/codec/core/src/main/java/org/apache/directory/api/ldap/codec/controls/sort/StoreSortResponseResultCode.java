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
package org.apache.directory.api.ldap.codec.controls.sort;

import org.apache.directory.api.asn1.actions.AbstractReadInteger;
import org.apache.directory.api.asn1.ber.Asn1Container;
import org.apache.directory.api.ldap.model.message.controls.SortResultCode;

/**
 * The action used to store the result code of a SortResponseControl
 * 
 * @param <C> The Asn1Container type to use
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class StoreSortResponseResultCode<C extends Asn1Container> extends AbstractReadInteger<C> {

	/**
	 * Instantiates a new StoreSortResponseResultCode action.
	 */
	public StoreSortResponseResultCode() {
		super("SortResponse result code error");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setIntegerValue(int value, Asn1Container container) {
		SortResponseContainer sortRespContainer = (SortResponseContainer) container;

		SortResultCode code = SortResultCode.get(value);
		sortRespContainer.getControl().setSortResult(code);

		sortRespContainer.setGrammarEndAllowed(true);
	}
}
