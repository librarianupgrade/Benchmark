/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jclouds.oauth.v2.domain;

import org.jclouds.javax.annotation.Nullable;
import org.jclouds.json.SerializedNames;

import com.google.auto.value.AutoValue;

/**
 * Details corresponding the a client_credential Azure AD Oauth request
 */
@AutoValue
public abstract class ClientCredentialsAuthArgs {
	/** The ID of the client. **/
	public abstract String clientId();

	/** The claims for the JWT. */
	public abstract ClientCredentialsClaims claims();

	/** The resource to authorize against. **/
	public abstract String resource();

	/** The scope(s) to authorize against. **/
	@Nullable
	public abstract String scope();

	@SerializedNames({ "client_id", "claims", "resource", "scope" })
	public static ClientCredentialsAuthArgs create(String clientId, ClientCredentialsClaims claims, String resource,
			String scope) {
		return new AutoValue_ClientCredentialsAuthArgs(clientId, claims, resource, scope);
	}

	ClientCredentialsAuthArgs() {
	}
}
