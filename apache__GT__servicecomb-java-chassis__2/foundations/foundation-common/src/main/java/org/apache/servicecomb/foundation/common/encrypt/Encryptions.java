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

package org.apache.servicecomb.foundation.common.encrypt;

import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;

import com.google.common.annotations.VisibleForTesting;

public class Encryptions {
	private static Encryption encryption = SPIServiceUtils.getPriorityHighestService(Encryption.class);

	@VisibleForTesting
	static void setEncryption(Encryption encryption) {
		Encryptions.encryption = encryption;
	}

	@VisibleForTesting
	static Encryption getEncryption() {
		return encryption;
	}

	public static String decode(String encrypted, String tags) {
		if (encrypted == null) {
			return null;
		}
		char[] result = decode(encrypted.toCharArray(), tags);
		if (result == null) {
			return null;
		}
		return new String(result);
	}

	public static char[] decode(char[] encrypted, String tags) {
		return encryption.decode(encrypted, tags);
	}

	public static String encode(String plain, String tags) {
		if (plain == null) {
			return null;
		}
		char[] result = encode(plain.toCharArray(), tags);
		if (result == null) {
			return null;
		}
		return new String(result);
	}

	public static char[] encode(char[] plain, String tags) {
		return encryption.encode(plain, tags);
	}
}
