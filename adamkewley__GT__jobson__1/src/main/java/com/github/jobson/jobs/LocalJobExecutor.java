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

package com.github.jobson.jobs;

import com.github.jobson.Constants;
import com.github.jobson.Helpers;
import com.github.jobson.jobinputs.JobExpectedInputId;
import com.github.jobson.jobs.jobstates.PersistedJob;
import com.github.jobson.scripting.functions.ToFileFunction;
import com.github.jobson.scripting.functions.ToJSONFunction;
import com.github.jobson.specs.*;
import com.github.jobson.utils.BinaryData;
import com.github.jobson.utils.CancelablePromise;
import com.github.jobson.utils.SimpleCancelablePromise;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.github.jobson.Helpers.*;
import static com.github.jobson.jobs.JobStatus.FINISHED;
import static java.util.Objects.requireNonNull;

/**
 * Executes a job submission as a local subprocess.
 */
public final class LocalJobExecutor implements JobExecutor {

	private static final Logger log = Logger.getLogger(LocalJobExecutor.class);

	private static String resolveArg(PersistedJob persistedJob, Path jobWorkingDir, RawTemplateString arg) {
		final Map<String, Object> environment = new HashMap<>();

		environment.put("toJSON", new ToJSONFunction());
		environment.put("toFile", new ToFileFunction(jobWorkingDir));
		environment.put("request", persistedJob);
		environment.put("inputs", mapKeys(persistedJob.getInputs(), JobExpectedInputId::toString));

		return arg.tryEvaluate(environment);
	}

	private static void copyJobDependency(JobDependencyConfiguration jobDependencyConfiguration, Path workingDir) {
		final Path source = Paths.get(jobDependencyConfiguration.getSource());
		final Path target = workingDir.resolve(Paths.get(jobDependencyConfiguration.getTarget()));

		try {
			if (source.toFile().isDirectory()) {
				log.debug("copy dependency: " + source.toString() + " -> " + target.toString());
				FileUtils.copyDirectory(source.toFile(), target.toFile());
			} else {
				log.debug("copy dependency: " + source.toString() + " -> " + target.toString());
				Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException ex) {
			log.error(source.toString() + ": cannot copy: " + ex.toString());
			throw new RuntimeException(ex);
		}
	}

	private final Path workingDirs;
	private final long delayBeforeForciblyKillingJobs;

	public LocalJobExecutor(Path workingDirs, long delayBeforeForciblyKillingJobs) throws FileNotFoundException {
		requireNonNull(workingDirs);
		if (!workingDirs.toFile().exists())
			throw new FileNotFoundException(workingDirs + ": does not exist");
		if (delayBeforeForciblyKillingJobs < 0)
			throw new IllegalArgumentException(
					delayBeforeForciblyKillingJobs + ": delay before killing jobs must be positive");

		this.workingDirs = workingDirs;
		this.delayBeforeForciblyKillingJobs = delayBeforeForciblyKillingJobs;
	}

	@Override
	public CancelablePromise<JobExecutionResult> execute(PersistedJob req, JobEventListeners jobEventListeners) {
		final ExecutionConfiguration executionConfiguration = req.getSpec().getExecution();

		try {
			final Path workingDir = workingDirs.resolve(req.getId().toString());
			Files.createDirectory(workingDir);
			log.debug(req.getId() + ": created working directory: " + workingDir.toString());

			executionConfiguration.getDependencies()
					.ifPresent(deps -> deps.forEach(dep -> copyJobDependency(dep, workingDir)));

			final String application = executionConfiguration.getApplication();
			final List<String> argList = new ArrayList<>();
			argList.add(application);

			log.debug(req.getId() + ": resolving args");

			executionConfiguration.getArguments().ifPresent(
					args -> args.stream().map(arg -> resolveArg(req, workingDir, arg)).forEach(argList::add));

			final ProcessBuilder processBuilder = new ProcessBuilder(argList);

			processBuilder.directory(workingDir.toFile());

			log.debug(req.getId() + ": launch subprocess: " + String.join(" ", argList));

			final Process runningProcess = processBuilder.start();

			log.info(req.getId() + ": launched: " + String.join(" ", argList));

			final SimpleCancelablePromise<JobExecutionResult> ret = new SimpleCancelablePromise<>();
			ret.onCancel(() -> abort(runningProcess));

			attachTo(runningProcess, jobEventListeners.getOnStdoutListener(), jobEventListeners.getOnStderrListener(),
					exitCode -> onProcessExit(req, workingDir, ret, exitCode));

			return ret;

		} catch (Exception ex) {
			log.error(req.getId() + ": cannot start: " + ex.toString());
			throw new RuntimeException(ex);
		}
	}

	private void onProcessExit(PersistedJob req, Path workingDir, SimpleCancelablePromise<JobExecutionResult> promise,
			int exitCode) {

		final JobStatus exitStatus = JobStatus.fromExitCode(exitCode);

		final JobExecutionResult jobExecutionResult;
		if (exitStatus == FINISHED) {
			final List<JobOutput> outputs = tryResolveJobOutputs(req, workingDir, req.getSpec().getExpectedOutputs());

			jobExecutionResult = new JobExecutionResult(exitStatus, outputs);
		} else {
			jobExecutionResult = new JobExecutionResult(exitStatus);
		}

		promise.complete(jobExecutionResult);
	}

	private List<JobOutput> tryResolveJobOutputs(PersistedJob req, Path workingDir,
			List<JobExpectedOutput> expectedOutputs) {

		return expectedOutputs.stream().map(e -> {
			final JobOutputId jobOutputId = new JobOutputId(resolveArg(req, workingDir, e.getId()));
			return tryGetJobOutput(workingDir, jobOutputId, e);
		}).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
	}

	private Optional<JobOutput> tryGetJobOutput(Path workingDir, JobOutputId outputId,
			JobExpectedOutput expectedOutput) {
		final Path expectedOutputFile = workingDir.resolve(expectedOutput.getPath());

		if (expectedOutputFile.toFile().exists()) {
			final String mimeType = establishMimeType(expectedOutput, expectedOutputFile);
			final BinaryData data = streamBinaryData(expectedOutputFile, mimeType);
			final JobOutput output = new JobOutput(outputId, data, expectedOutput.getName(),
					expectedOutput.getDescription(), expectedOutput.getMetadata());
			return Optional.of(output);
		} else {
			return Optional.empty();
		}
	}

	private String establishMimeType(JobExpectedOutput jobExpectedOutput, Path p) {
		if (jobExpectedOutput.getMimeType().isPresent()) {
			return jobExpectedOutput.getMimeType().get();
		} else {
			try {
				return Helpers.getMimeType(Files.newInputStream(p), jobExpectedOutput.getPath());
			} catch (IOException ex) {
				log.warn("Encountered IO error when determining an output's MIME type. Skipping MIME type detection");
				return Constants.DEFAULT_BINARY_MIME_TYPE;
			}
		}
	}

	private void abort(Process process) {
		log.debug("Aborting process: " + process);
		process.destroy();
		try {
			final boolean terminated = process.waitFor(delayBeforeForciblyKillingJobs, TimeUnit.MILLISECONDS);
			if (!terminated) {
				log.warn(process + " did not abort within " + delayBeforeForciblyKillingJobs
						+ " seconds, aborting forcibly (SIGKILL)");
				process.destroyForcibly();
			}
		} catch (InterruptedException e) {
			log.error("Abortion interrupted while waiting on process (this shouldn't happen)");
		}
	}
}
