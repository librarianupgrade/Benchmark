/*
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */

package org.apache.directory.ldap.client.api;

import org.apache.directory.api.ldap.model.constants.SupportedSaslMechanisms;

/**
 * Holds the data required to complete the EXTERNAL SASL operation
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SaslExternalRequest extends AbstractSaslRequest {
	/**
	 * Creates a new instance of SaslExternalRequest.
	 */
	public SaslExternalRequest() {
		super(SupportedSaslMechanisms.EXTERNAL);
	}

	/**
	 * Creates a new instance of SaslExternalRequest.
	 * 
	 * @param authzId The authorization ID
	 */
	public SaslExternalRequest(String authzId) {
		super(SupportedSaslMechanisms.EXTERNAL);
		this.authorizationId = authzId;
	}
}
