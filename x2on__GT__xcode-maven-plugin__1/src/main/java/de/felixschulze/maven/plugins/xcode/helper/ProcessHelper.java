/*
 * Copyright (C) 2012 Felix Schulze
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.felixschulze.maven.plugins.xcode.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Helper for shutdown iPhone Simulator
 *
 * @author <a href="mail@felixschulze.de">Felix Schulze</a>
 */
public class ProcessHelper {

	private static final String EMULATOR_COMMAND = "ps axo pid,command | grep 'iPhone Simulator'";
	private static final String KILL = "killall 'iPhone Simulator'";

	public static boolean isProcessRunning() throws IOException {

		String[] commands = new String[] { "/bin/sh", "-c", EMULATOR_COMMAND };
		Process p = Runtime.getRuntime().exec(commands);
		BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

		String line;
		while ((line = reader.readLine()) != null) {
			if (line.contains("iPhone Simulator.app")) {
				return true;
			}
		}
		return false;
	}

	public static void killSimulatorProcess() throws IOException {
		String[] commands = new String[] { "/bin/sh", "-c", KILL };
		Runtime.getRuntime().exec(commands);
	}
}
