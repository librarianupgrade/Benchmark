package org.apache.maven.scm.provider.starteam.util;

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

import org.apache.maven.scm.providers.starteam.settings.Settings;
import org.apache.maven.scm.providers.starteam.settings.io.xpp3.StarteamXpp3Reader;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author <a href="mailto:dantran@apache.org">Dan T. Tran</a>
 */
public final class StarteamUtil {

	protected static final String STARTEAM_SETTINGS_FILENAME = "starteam-settings.xml";

	public static final File DEFAULT_SETTINGS_DIRECTORY = new File(System.getProperty("user.home"), ".scm");

	private static File settingsDirectory = DEFAULT_SETTINGS_DIRECTORY;

	private static Settings settings;

	private StarteamUtil() {
	}

	public static Settings getSettings() {
		if (settings == null) {
			settings = readSettings();
		}
		return settings;
	}

	public static Settings readSettings() {
		File settingsFile = getSettingsFile();

		if (settingsFile.exists()) {
			StarteamXpp3Reader reader = new StarteamXpp3Reader();
			try {
				return reader.read(ReaderFactory.newXmlReader(settingsFile));
			} catch (FileNotFoundException e) {
				// nop
			} catch (IOException e) {
				// nop
			} catch (XmlPullParserException e) {
				String message = settingsFile.getAbsolutePath() + " isn't well formed. SKIPPED." + e.getMessage();

				System.err.println(message);
			}
		}

		return new Settings();
	}

	public static File getSettingsFile() {
		return new File(settingsDirectory, STARTEAM_SETTINGS_FILENAME);
	}

	public static void setSettingsDirectory(File directory) {
		settingsDirectory = directory;
	}
}
