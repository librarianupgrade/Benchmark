/*
 *    Copyright 2009-2012 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.submitted.complex_property;

public class EncryptedString {
	private String encrypted;

	public EncryptedString() {
		setEncrypted(null);
	}

	public EncryptedString(String message) {
		this();

		//encrypt the message.
		setEncrypted(message);
	}

	public String decrypt() {
		return encrypted;
	}

	public String getEncrypted() {
		return encrypted;
	}

	public void setEncrypted(String arg) {
		this.encrypted = arg;
	}

}
