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
package org.jclouds.oauth.v2.config;

import static com.google.common.base.Suppliers.ofInstance;
import static org.testng.Assert.assertNotNull;

import java.io.File;
import java.security.PrivateKey;
import java.util.Properties;

import org.jclouds.domain.Credentials;
import org.jclouds.oauth.v2.OAuthTestUtils;
import org.jclouds.oauth.v2.config.PrivateKeySupplier.PrivateKeyForCredentials;
import org.jclouds.rest.AuthorizationException;
import org.testng.annotations.Test;

import com.google.common.base.Charsets;
import com.google.common.base.Suppliers;
import com.google.common.io.Files;

@Test(groups = "unit")
public class PrivateKeySupplierTest {

	/** Test loading the credentials by extracting a pk from a PKCS12 keystore. */
	public void testLoadPKString() throws Exception {
		assertNotNull(loadPrivateKey());
	}

	@Test(expectedExceptions = AuthorizationException.class)
	public void testAuthorizationExceptionIsThrownOnBadKeys() {
		PrivateKeySupplier supplier = new PrivateKeySupplier(
				Suppliers.ofInstance(new Credentials("MOMMA", "FileNotFoundCredential")),
				new PrivateKeyForCredentials());
		supplier.get();
	}

	public void testCredentialsAreLoadedOnRightAlgoAndCredentials() {
		Properties propertied = OAuthTestUtils.defaultProperties(new Properties());
		Credentials validCredentials = new Credentials(propertied.getProperty("oauth.identity"),
				propertied.getProperty("oauth.credential"));
		PrivateKeySupplier supplier = new PrivateKeySupplier(Suppliers.ofInstance(validCredentials),
				new PrivateKeyForCredentials());
		assertNotNull(supplier.get());
	}

	public static PrivateKey loadPrivateKey() throws Exception {
		PrivateKeySupplier supplier = new PrivateKeySupplier(
				ofInstance(new Credentials("foo",
						Files.asCharSource(new File("src/test/resources/testpk.pem"), Charsets.UTF_8).read())),
				new PrivateKeyForCredentials());
		return supplier.get();
	}
}
