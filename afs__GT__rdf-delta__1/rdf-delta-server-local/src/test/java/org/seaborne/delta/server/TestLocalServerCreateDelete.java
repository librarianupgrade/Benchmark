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

package org.seaborne.delta.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.dboe.base.file.Location;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.seaborne.delta.DeltaException;
import org.seaborne.delta.Id;
import org.apache.jena.atlas.io.IOX;
import org.seaborne.delta.lib.LogX;
import org.seaborne.delta.server.local.DPS;
import org.seaborne.delta.server.local.LocalServer;
import org.seaborne.delta.server.local.LocalServers;
import org.seaborne.delta.server.local.PatchStoreProvider;

/**
 * Tests of {@link LocalServer} for creating and
 * deleting a {@link LocalServer} area using a file {@link PatchStoreProvider}.
 *
 * See {@link TestLocalServer} for tests involving
 * a static setup of data sources.
 */

public class TestLocalServerCreateDelete {
	// Testing area that is created and modified by tests.
	private static String DIR = "target/testing/delta";

	@BeforeClass
	public static void beforeClass() {
		LogX.setJavaLogging("src/test/resources/logging.properties");
	}

	private static void initialize() {
		Location loc = Location.create(DIR);
		FileOps.clearAll(DIR);
		// Copy in test setup.
		try {
			FileUtils.copyDirectory(new File(TestLocalServer.SERVER_DIR), new File(DIR));
		} catch (IOException ex) {
			throw IOX.exception(ex);
		}

		DPS.resetSystem();
	}

	@AfterClass
	public static void afterClass() {
		DPS.resetSystem();
	}

	@Before
	public void beforeTest() {
		initialize();
	}

	@Test
	public void local_server_create_01() {
		LocalServer server = LocalServers.createFile(DIR);
	}

	//    @Test public void local_server_create_02() {
	//        LocalServer server1 = LocalServer.attach(loc);
	//        LocalServer server2 = LocalServer.attach(loc);
	//        assertEquals(server1, server2);
	//    }

	@Test
	public void datasource_create_01() {
		LocalServer server = LocalServers.createFile(DIR);
		Id newId = server.createDataSource("XYZ", "http://example/xyz");
		assertNotNull(newId);
	}

	// Create does not overwrite
	@Test
	public void datasource_create_02() {
		LocalServer server = LocalServers.createFile(DIR);

		Id newId1 = server.createDataSource("XYZ", "http://example/xyz");
		try {
			Id newId2 = server.createDataSource("XYZ", "http://example/xyz");
			fail("Expected createDataSource to fail");
		} catch (DeltaException ex) {
		}
	}

	// Create does not overwrite persistent state.
	@Test
	public void local_server_create_03() {
		Location loc = Location.create(DIR);
		// Finds previous.
		LocalServer server1 = LocalServers.createFile(DIR);
		Id newId1 = server1.createDataSource("XYZ", "http://example/xyz");
		LocalServer.release(server1);
		LocalServer server2 = LocalServers.createFile(DIR);
		try {
			Id newId2 = server2.createDataSource("XYZ", "http://example/xyz");
			fail("Expected createDataSource to fail");
		} catch (DeltaException ex) {
		}
	}

	// "Restart" test.
	@Test
	public void local_server_restart_01() {
		Location loc = Location.create(DIR);

		LocalServer server1 = LocalServers.createFile(DIR);
		assertEquals(2, server1.listDataSources().size());

		Id newId1 = server1.createDataSource("AXYZ", "http://example/axyz");
		LocalServer.release(server1);

		LocalServer server2 = LocalServers.createFile(DIR);
		// 3 - data1, data2 and the new XYZ.

		assertEquals(3, server2.listDataSources().size());
		assertEquals(3, server2.listDataSourcesIds().size());

		long z = server2.listDataSourcesIds().stream().filter(id -> id.equals(newId1)).count();
		assertEquals("Count of newId occurences", 1, z);

		Id id = server2.listDataSourcesIds().stream().filter(_id -> _id.equals(newId1)).findFirst().get();
		assertEquals(newId1, id);

		List<Id> ids = server2.listDataSources().stream().map(dss -> dss.getId()).collect(Collectors.toList());
		assertTrue(ids.contains(newId1));
	}

}
