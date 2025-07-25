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

package de.felixschulze.maven.plugins.xcode;

import java.util.ArrayList;
import java.util.List;

import de.felixschulze.maven.plugins.xcode.AbstractXcodeMojo;
import de.felixschulze.maven.plugins.xcode.CommandExecutor;
import de.felixschulze.maven.plugins.xcode.ExecutionException;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Run the xcodebuild-clean command line program
 * @goal xcodebuild-clean
 * @phase clean
 * @author <a href="mail@felixschulze.de">Felix Schulze</a>
 */
public class XcodeCleanMojo extends AbstractXcodeMojo {

	/**
	 * Execute the xcode command line utility.
	 */
	public void execute() throws MojoExecutionException {
		if (!xcodeCommandLine.exists()) {
			throw new MojoExecutionException("Invalid path for xcodebuild: " + xcodeCommandLine.getAbsolutePath());
		}

		CommandExecutor executor = CommandExecutor.Factory.createDefaultCommmandExecutor();
		executor.setLogger(this.getLog());
		List<String> commands = new ArrayList<String>();
		commands.add("clean");

		if (xcodeProject != null) {
			commands.add("-project");
			commands.add(xcodeProject.getAbsolutePath());
		}
		if (xcodeTarget != null) {
			commands.add("-target");
			commands.add(xcodeTarget);
		}
		if (xcodeConfiguration != null) {
			commands.add("-configuration");
			commands.add(xcodeConfiguration);
		}

		getLog().info(xcodeCommandLine.getAbsolutePath() + " " + commands.toString());

		try {
			executor.executeCommand(xcodeCommandLine.getAbsolutePath(), commands, false);
		} catch (ExecutionException e) {
			getLog().error(executor.getStandardOut());
			getLog().error(executor.getStandardError());
			throw new MojoExecutionException("Error while executing: ", e);
		}
	}
}