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
package org.jclouds.io.payloads;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import org.jclouds.crypto.Crypto;
import org.jclouds.io.Payload;

import com.google.common.base.Throwables;

public class RSAEncryptingPayload extends BaseCipherPayload {

	public RSAEncryptingPayload(Crypto crypto, Payload delegate, Key key) {
		super(crypto, delegate, key);
	}

	@Override
	public Cipher initializeCipher(Key key) {
		Cipher cipher = null;
		try {
			cipher = crypto.cipher("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, key);
		} catch (NoSuchAlgorithmException e) {
			Throwables.propagate(e);
		} catch (NoSuchPaddingException e) {
			Throwables.propagate(e);
		} catch (InvalidKeyException e) {
			Throwables.propagate(e);
		}
		return cipher;
	}

}
