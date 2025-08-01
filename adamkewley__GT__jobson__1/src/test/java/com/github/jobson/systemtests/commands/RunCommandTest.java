/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.github.jobson.systemtests.commands;

import com.github.jobson.Constants;
import com.github.jobson.Helpers;
import com.github.jobson.TestHelpers;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public final class RunCommandTest {

	@Test
	public void testCanRunAProgramInstalledViaATrivialEndToEndWorkflow() throws IOException, InterruptedException {
		final Path pwd = Files.createTempDirectory(RunCommandTest.class.getSimpleName());

		final int newCommandExitCode = CliHelpers.run(pwd.toFile(), "new");
		assertThat(newCommandExitCode).isEqualTo(0);

		final String installedSpecId = TestHelpers.generateRandomString();

		final int generateExitCode = CliHelpers.run(pwd.toFile(), "generate", "spec", installedSpecId);
		assertThat(generateExitCode).isEqualTo(0);

		final String specText = Helpers.loadResourceFileAsString("fixtures/systemtests/commands/trivial-spec.yml");

		final Path specFilePath = pwd.resolve(Constants.WORKSPACE_SPECS_DIRNAME).resolve(installedSpecId)
				.resolve(Constants.SPEC_DIR_SPEC_FILENAME);

		Files.write(specFilePath, specText.getBytes());

		// TODO: Intercept output from `generate request ${specId}`
	}
}
