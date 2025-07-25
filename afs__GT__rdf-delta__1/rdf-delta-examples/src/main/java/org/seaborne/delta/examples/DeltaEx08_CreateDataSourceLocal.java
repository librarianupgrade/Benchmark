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

package org.seaborne.delta.examples;

import java.util.List;

import org.apache.jena.dboe.base.file.Location;
import org.seaborne.delta.Id;
import org.seaborne.delta.lib.LogX;
import org.seaborne.delta.server.local.DataSource;
import org.seaborne.delta.server.local.LocalServer;
import org.seaborne.delta.server.local.LocalServers;

/** Locally, create a new DataSource, remove it. */
public class DeltaEx08_CreateDataSourceLocal {
	static {
		LogX.setJavaLogging();
	}

	public static void main(String... args) {
		Location loc = Location.create("DeltaServer");
		// LocalServer is the engine part of the patch log server.
		LocalServer server = LocalServers.createFile(loc.getDirectoryPath());
		state(server.listDataSources());
		System.out.println();

		// Correctly fails if exists
		Id newId = server.createDataSource("XYZ", "http://example/xyz");
		DataSource dSrc = server.getDataSource(newId);
		List<DataSource> x = server.listDataSources();
		state(server.listDataSources());
		System.out.println();

		server.removeDataSource(newId);
		state(server.listDataSources());
		// Can not create again "remove" means "disable".
		System.out.println("DONE");
	}

	public static void state(List<DataSource> x) {
		x.forEach((ds) -> {
			System.out.println(ds);
			System.out.println("    " + ds.getURI());
			System.out.println("    " + ds.getId());
			System.out.println("    " + ds.getPatchLog());
		});
	}
}
