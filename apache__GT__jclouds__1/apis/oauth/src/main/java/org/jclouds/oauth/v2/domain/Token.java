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

import org.jclouds.json.SerializedNames;

import com.google.auto.value.AutoValue;

/**
 * The oauth token, obtained upon a successful token request and ready to embed in requests.
 */
@AutoValue
public abstract class Token {
	/** The access token obtained from the OAuth server. */
	public abstract String accessToken();

	/** The type of the token, e.g., {@code Bearer}. */
	public abstract String tokenType();

	/** In how many seconds this token expires. */
	public abstract long expiresIn();

	@SerializedNames({ "access_token", "token_type", "expires_in" })
	public static Token create(String accessToken, String tokenType, long expiresIn) {
		return new AutoValue_Token(accessToken, tokenType, expiresIn);
	}
}
