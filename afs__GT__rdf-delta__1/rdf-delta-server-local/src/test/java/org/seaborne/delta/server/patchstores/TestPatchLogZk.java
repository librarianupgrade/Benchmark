/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.delta.server.patchstores;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.junit.After;
import org.junit.Before;
import org.seaborne.delta.DataSourceDescription;
import org.seaborne.delta.Id;
import org.seaborne.delta.lib.LogX;
import org.seaborne.delta.server.local.*;
import org.seaborne.delta.server.local.patchstores.zk.PatchStoreProviderZk;

public class TestPatchLogZk extends AbstractTestPatchLog {

	static {
		LogX.setJavaLogging("src/test/resources/logging.properties");
	}

	private TestingServer server;
	private CuratorFramework client;

	//    @BeforeClass public static void beforeClass() { }
	//    @AfterClass public static void afterClass() {}

	@Before
	public void before() {
		try {
			server = new TestingServer();
			server.start();
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}

	@After
	public void after() {
		client.close();
		try {
			server.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}

	@Override
	protected PatchLog patchLog() {
		try {
			String connectString = "localhost:" + server.getPort();
			RetryPolicy policy = new ExponentialBackoffRetry(10000, 5);

			client = CuratorFrameworkFactory.builder().connectString(connectString).retryPolicy(policy).build();
			client.start();
			//client.getConnectionStateListenable().addListener((c, newState)->System.out.println("** STATE CHANGED TO : " + newState));
			client.blockUntilConnected();

			LocalServerConfig config = LocalServers.configZk(connectString);
			PatchStore ps = new PatchStoreProviderZk().create(config);
			ps.initialize(new DataSourceRegistry("TestPatchLogZk"), config);

			DataSourceDescription dsd = new DataSourceDescription(Id.create(), "ABC", "http://example/ABC");
			PatchLog patchLog = ps.createLog(dsd);
			return patchLog;

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

}
