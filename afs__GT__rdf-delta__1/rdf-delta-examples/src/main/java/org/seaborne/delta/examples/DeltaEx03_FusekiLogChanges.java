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

import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
import org.seaborne.delta.lib.LogX;
import org.apache.jena.rdfpatch.RDFPatchOps;
import org.apache.jena.rdfpatch.text.RDFChangesWriterText;

/**
 * Example of a Fuseki server, with a dataset that writes out changes as they happen.
 */
public class DeltaEx03_FusekiLogChanges {
	static {
		LogX.setJavaLogging();
	}

	public static void main(String... args) {
		try {
			main2(args);
		} finally {
			System.exit(0);
		}
	}

	public static void main2(String... args) {
		int PORT = 2020;
		DatasetGraph dsgBase = DatasetGraphFactory.createTxnMem();
		try (RDFChangesWriterText changeLog = RDFPatchOps.textWriter(System.out)) {

			DatasetGraph dsg = RDFPatchOps.changes(dsgBase, changeLog);

			// Create a server with the changes-enables dataset.
			// Plain server. No other registration necessary.
			FusekiServer server = FusekiServer.create().port(PORT).add("/ds", dsg).build();
			server.start();

			RDFConnection conn = RDFConnection.connect("http://localhost:" + PORT + "/ds");
			UpdateRequest update = UpdateFactory.create("PREFIX : <http://example/> INSERT DATA { :s :p 123 }");
			// Note - no prefix in changes. The SPARQL Update prefix is not a chnage to the dataset prefixes.
			conn.update(update);
			server.stop();
			//        // Server in the background so explicitly exit.
			//        System.exit(0);
		}
	}

}
