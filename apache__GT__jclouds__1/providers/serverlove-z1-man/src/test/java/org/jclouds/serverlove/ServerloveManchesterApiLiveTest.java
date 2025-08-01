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
package org.jclouds.serverlove;

import org.jclouds.domain.LoginCredentials;
import org.jclouds.elasticstack.ElasticStackApiLiveTest;
import org.jclouds.elasticstack.domain.Server;
import org.testng.annotations.Test;

@Test(groups = "live", singleThreaded = true, testName = "ServerloveManchesterApiLiveTest")
public class ServerloveManchesterApiLiveTest extends ElasticStackApiLiveTest {
	public ServerloveManchesterApiLiveTest() {
		provider = "serverlove-z1-man";
	}

	@Override
	protected LoginCredentials getSshCredentials(Server server) {
		return LoginCredentials.builder().user("root").password(server.getVnc().getPassword()).build();
	}

	@Override
	@Test(enabled = false, description = "Standard drive API still not supported")
	public void testListStandardDrives() throws Exception {

	}

	@Override
	@Test(enabled = false, description = "Standard drive API still not supported")
	public void testListStandardDriveInfo() throws Exception {

	}

}
