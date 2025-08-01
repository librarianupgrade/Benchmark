package org.apache.maven.scm.provider.perforce.command.diff;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.codehaus.plexus.util.cli.StreamConsumer;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author Mike Perham
 */
public class PerforceDiffConsumer implements StreamConsumer {
	private StringWriter out = new StringWriter();

	private PrintWriter output = new PrintWriter(out);

	/*
	 * I don't see any easy way to distinguish between an error and
	 * normal diff output.  I see two possibilities:
	 *
	 * 1) Use the p4 global "-s" parameter
	 * 2) Check for a non-zero code returned by the p4 process
	 *
	 * We'll do the latter as it's simpler to implement.
	 */
	/** {@inheritDoc} */
	public void consumeLine(String line) {
		output.println(line);
	}

	public String getOutput() {
		output.flush();
		out.flush();
		return out.toString();
	}
}