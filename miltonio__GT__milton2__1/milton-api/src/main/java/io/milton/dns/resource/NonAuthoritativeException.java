/*
 *
 * Copyright 2014 McEvoy Software Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.milton.dns.resource;

public class NonAuthoritativeException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = 3054727106748339165L;

	private final String domainName;

	public NonAuthoritativeException(String domainName) {
		this(domainName, "No authoritative data for " + domainName);
	}

	public NonAuthoritativeException(String domainName, String msg) {
		super(msg);
		this.domainName = domainName;
	}

	public String getDomainName() {
		return domainName;
	}
}
