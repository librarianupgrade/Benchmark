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
package org.apache.directory.api.ldap.model.message;

/**
 * CompareResponse implementation.
 * 
 * @author <a href="mailto:dev@directory.apache.org"> Apache Directory Project</a>
 */
public class CompareResponseImpl extends AbstractResultResponse implements CompareResponse {
	static final long serialVersionUID = 6452521899386487731L;

	/**
	 * Creates a CompareResponse as a reply to an CompareRequest.
	 */
	public CompareResponseImpl() {
		super(-1, MessageTypeEnum.COMPARE_RESPONSE);
	}

	/**
	 * Creates a CompareResponse as a reply to an CompareRequest.
	 * 
	 * @param id the session unique message id
	 */
	public CompareResponseImpl(final int id) {
		super(id, MessageTypeEnum.COMPARE_RESPONSE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isTrue() {
		return ldapResult.getResultCode() == ResultCodeEnum.COMPARE_TRUE;
	}

	/**
	 * Get a String representation of an CompareResponse
	 * 
	 * @return An CompareResponse String
	 */
	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();

		sb.append("    Compare Response\n");
		sb.append(super.toString());

		return super.toString(sb.toString());
	}
}
