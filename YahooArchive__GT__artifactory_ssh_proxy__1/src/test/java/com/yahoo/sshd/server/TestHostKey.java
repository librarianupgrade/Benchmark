/*
 * Copyright 2014 Yahoo! Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the License); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an AS IS BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.yahoo.sshd.server;

import java.security.KeyPair;

import org.apache.sshd.server.keyprovider.PEMHostKeyProvider;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = "unit")
public class TestHostKey {
	@Test
	public void testHostKey() {
		System.err.println("os.name=" + System.getProperty("os.name"));
		PEMHostKeyProvider keyPairProvider = new PEMHostKeyProvider("src/test/resources/hostkey.pem");
		Iterable<KeyPair> keys = keyPairProvider.loadKeys();
		Assert.assertNotNull(keys);
		// TODO: fix
		// Assert.assertFalse(keys.);
		for (KeyPair k : keys) {
			Assert.assertNotNull(k);
			Assert.assertNotNull(k.getPrivate());
			System.err.println("key: " + k.getPrivate());
		}
	}
}
