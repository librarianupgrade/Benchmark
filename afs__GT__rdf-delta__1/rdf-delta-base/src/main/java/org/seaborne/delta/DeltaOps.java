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

package org.seaborne.delta;

import java.io.OutputStream;

import org.apache.jena.rdfpatch.RDFChanges;
import org.apache.jena.rdfpatch.changes.RDFChangesLog;
import org.apache.jena.rdfpatch.changes.RDFChangesN;
import org.apache.jena.rdfpatch.system.Printer;
import org.apache.jena.rdfpatch.text.TokenWriter;
import org.apache.jena.rdfpatch.text.TokenWriterText;
import org.slf4j.Logger;

public class DeltaOps {

	public static void printer(String fmt, Object... args) {
		System.out.printf(fmt, args);
		System.out.println();
	}

	public static Printer printerToLog(Logger log) {
		return (fmt, args) -> log.info(String.format(fmt, args));
	}

	public static String verString(Version version) {
		if (version == null)
			return "<null>";
		if (version.equals(Version.UNSET))
			return "--";
		return Long.toString(version.value());
	}

	/** Validate a name as a {@code DataSource} name */
	public static boolean isValidName(String dsName) {
		if (dsName == null)
			return false;
		return DeltaConst.DataSourceRegex.matcher(dsName).matches();
	}

	/** Add a printer to a {@link RDFChanges} */
	public static RDFChanges print(RDFChanges changes) {
		return RDFChangesN.multi(changes, new RDFChangesLog(DeltaOps::printer));
	}

	/** Create a {@link TokenWriter} */
	public static TokenWriter tokenWriter(OutputStream out) {
		// Placeholder for text/binary choice.
		// IO ops to buffer
		TokenWriter tokenWriter = TokenWriterText.create(out);
		return tokenWriter;
	}
}
