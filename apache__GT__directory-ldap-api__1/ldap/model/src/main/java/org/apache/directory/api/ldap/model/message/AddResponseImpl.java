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
 * AddResponse implementation.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AddResponseImpl extends AbstractResultResponse implements AddResponse {
	static final long serialVersionUID = 4027132942339551383L;

	/**
	 * Creates an AddResponse as a reply to an AddRequest.
	 */
	public AddResponseImpl() {
		super(-1, MessageTypeEnum.ADD_RESPONSE);
	}

	/**
	 * Creates an AddResponse as a reply to an AddRequest.
	 * 
	 * @param id the session unique message id
	 */
	public AddResponseImpl(final int id) {
		super(id, MessageTypeEnum.ADD_RESPONSE);
	}

	/**
	 * Get a String representation of an AddResponse
	 * 
	 * @return An AddResponse String
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("    Add Response\n");
		sb.append(super.toString());

		return super.toString(sb.toString());
	}
}
