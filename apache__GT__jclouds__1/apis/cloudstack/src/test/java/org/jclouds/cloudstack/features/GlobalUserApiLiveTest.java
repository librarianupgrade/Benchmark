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
package org.jclouds.cloudstack.features;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.hash.Hashing.md5;
import static com.google.common.io.BaseEncoding.base16;
import static org.jclouds.cloudstack.features.GlobalAccountApiLiveTest.createTestAccount;
import static org.jclouds.cloudstack.options.UpdateUserOptions.Builder.userName;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.Properties;
import java.util.Set;

import org.jclouds.cloudstack.CloudStackApi;
import org.jclouds.cloudstack.CloudStackContext;
import org.jclouds.cloudstack.CloudStackGlobalApi;
import org.jclouds.cloudstack.domain.Account;
import org.jclouds.cloudstack.domain.ApiKeyPair;
import org.jclouds.cloudstack.domain.User;
import org.jclouds.cloudstack.internal.BaseCloudStackApiLiveTest;
import org.testng.annotations.Test;

/**
 * Tests behavior of {@code GlobaUserClient}
 */
@Test(groups = "live", singleThreaded = true, testName = "GlobalUserApiLiveTest")
public class GlobalUserApiLiveTest extends BaseCloudStackApiLiveTest {

	public static User createTestUser(CloudStackGlobalApi client, Account account, String prefix) {
		return client.getUserClient().createUser(prefix + "-user", account.getName(), "dummy2@example.com",
				base16().lowerCase().encode(md5().hashString("password", UTF_8).asBytes()), "First", "Last");
	}

	@Test
	public void testCreateUser() {
		skipIfNotGlobalAdmin();

		Account testAccount = createTestAccount(globalAdminClient, prefix);
		User testUser = null;
		try {
			testUser = createTestUser(globalAdminClient, testAccount, prefix);

			assertNotNull(testUser);
			assertEquals(testUser.getName(), prefix + "-user");
			assertEquals(testUser.getAccount(), prefix + "-account");

			User updatedUser = globalAdminClient.getUserClient().updateUser(testUser.getId(),
					userName(prefix + "-user-2"));

			assertNotNull(updatedUser);
			assertEquals(updatedUser.getName(), prefix + "-user-2");

			ApiKeyPair apiKeys = globalAdminClient.getUserClient().registerUserKeys(updatedUser.getId());

			assertNotNull(apiKeys.getApiKey());
			assertNotNull(apiKeys.getSecretKey());

			checkAuthAsUser(apiKeys);

		} finally {
			if (testUser != null) {
				globalAdminClient.getUserClient().deleteUser(testUser.getId());
			}
			globalAdminClient.getAccountApi().deleteAccount(testAccount.getId());
		}
	}

	private void checkAuthAsUser(ApiKeyPair keyPair) {
		CloudStackContext context = createView(credentialsAsProperties(keyPair), setupModules());

		CloudStackApi client = context.getApi();
		Set<Account> accounts = client.getAccountApi().listAccounts();

		assert !accounts.isEmpty();

		context.close();
	}

	private Properties credentialsAsProperties(ApiKeyPair keyPair) {
		Properties overrides = setupProperties();
		overrides.put(provider + ".identity", checkNotNull(keyPair.getApiKey()));
		overrides.put(provider + ".credential", checkNotNull(keyPair.getSecretKey()));
		return overrides;
	}

}
