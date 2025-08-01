package org.apache.maven.scm.provider.bazaar;

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

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.log.DefaultLog;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.bazaar.command.BazaarConstants;
import org.apache.maven.scm.provider.bazaar.command.BazaarConsumer;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Common code for executing bazaar commands.
 *
 * @author <a href="mailto:torbjorn@smorgrav.org">Torbjorn Eikli Smorgrav</a>
 *
 */
public final class BazaarUtils {

	private BazaarUtils() {
	}

	/**
	 * Map between command and its valid exit codes
	 */
	private static final Map<String, List<Integer>> EXITCODEMAP = new HashMap<String, List<Integer>>();

	/**
	 * Default exit codes for entries not in exitCodeMap
	 */
	private static final List<Integer> DEFAULTEEXITCODES = new ArrayList<Integer>();

	/** Setup exit codes*/
	static {
		DEFAULTEEXITCODES.add(Integer.valueOf(0));

		//Diff is different
		List<Integer> diffExitCodes = new ArrayList<Integer>();
		diffExitCodes.add(Integer.valueOf(0)); //No difference
		diffExitCodes.add(Integer.valueOf(1)); //Conflicts in merge-like or changes in diff-like
		diffExitCodes.add(Integer.valueOf(2)); //Unrepresentable diff changes
		EXITCODEMAP.put(BazaarConstants.DIFF_CMD, diffExitCodes);
	}

	public static ScmResult execute(BazaarConsumer consumer, ScmLogger logger, File workingDir, String[] cmdAndArgs)
			throws ScmException {
		try {
			//Build commandline
			Commandline cmd = buildCmd(workingDir, cmdAndArgs);
			if (logger.isInfoEnabled()) {
				logger.info("EXECUTING: " + cmd);
			}

			//Execute command
			int exitCode = executeCmd(consumer, cmd);

			//Return result
			List<Integer> exitCodes = DEFAULTEEXITCODES;
			if (EXITCODEMAP.containsKey(cmdAndArgs[0])) {
				exitCodes = EXITCODEMAP.get(cmdAndArgs[0]);
			}
			boolean success = exitCodes.contains(Integer.valueOf(exitCode));

			//On failure (and not due to exceptions) - run diagnostics
			String providerMsg = "Execution of bazaar command succeded";
			if (!success) {
				BazaarConfig config = new BazaarConfig(workingDir);
				providerMsg = "\nEXECUTION FAILED" + "\n  Execution of cmd : " + cmdAndArgs[0]
						+ " failed with exit code: " + exitCode + "." + "\n  Working directory was: " + "\n    "
						+ workingDir.getAbsolutePath() + config.toString(workingDir) + "\n";
				if (logger.isErrorEnabled()) {
					logger.error(providerMsg);
				}
			}

			return new ScmResult(cmd.toString(), providerMsg, consumer.getStdErr(), success);
		} catch (ScmException se) {
			String msg = "EXECUTION FAILED\n  Execution failed before invoking the Bazaar command. Last exception:"
					+ "\n    " + se.getMessage();

			//Add nested cause if any
			if (se.getCause() != null) {
				msg += "\n  Nested exception:" + "\n    " + se.getCause().getMessage();
			}

			//log and return
			if (logger.isErrorEnabled()) {
				logger.error(msg);
			}
			throw se;
		}
	}

	static Commandline buildCmd(File workingDir, String[] cmdAndArgs) throws ScmException {
		Commandline cmd = new Commandline();
		cmd.setExecutable(BazaarConstants.EXEC);
		cmd.setWorkingDirectory(workingDir.getAbsolutePath());
		cmd.addArguments(cmdAndArgs);

		if (!workingDir.exists()) {
			boolean success = workingDir.mkdirs();
			if (!success) {
				String msg = "Working directory did not exist" + " and it couldn't be created: " + workingDir;
				throw new ScmException(msg);
			}
		}
		return cmd;
	}

	static int executeCmd(BazaarConsumer consumer, Commandline cmd) throws ScmException {
		final int exitCode;
		try {
			exitCode = CommandLineUtils.executeCommandLine(cmd, consumer, consumer);
		} catch (CommandLineException ex) {
			throw new ScmException("Command could not be executed: " + cmd, ex);
		}
		return exitCode;
	}

	public static ScmResult execute(File workingDir, String[] cmdAndArgs) throws ScmException {
		ScmLogger logger = new DefaultLog();
		return execute(new BazaarConsumer(logger), logger, workingDir, cmdAndArgs);
	}

	public static String[] expandCommandLine(String[] cmdAndArgs, ScmFileSet additionalFiles) {
		List<File> files = additionalFiles.getFileList();
		String[] cmd = new String[files.size() + cmdAndArgs.length];

		// Copy command into array
		System.arraycopy(cmdAndArgs, 0, cmd, 0, cmdAndArgs.length);

		// Add files as additional parameter into the array
		for (int i = 0; i < files.size(); i++) {
			String file = files.get(i).getPath().replace('\\', File.separatorChar);
			cmd[i + cmdAndArgs.length] = file;
		}

		return cmd;
	}

	public static int getCurrentRevisionNumber(ScmLogger logger, File workingDir) throws ScmException {

		String[] revCmd = new String[] { BazaarConstants.REVNO_CMD };
		BazaarRevNoConsumer consumer = new BazaarRevNoConsumer(logger);
		BazaarUtils.execute(consumer, logger, workingDir, revCmd);

		return consumer.getCurrentRevisionNumber();
	}

	/**
	 * Get current (working) revision.
	 * <p/>
	 * Resolve revision to the last integer found in the command output.
	 */
	private static class BazaarRevNoConsumer extends BazaarConsumer {

		private int revNo;

		BazaarRevNoConsumer(ScmLogger logger) {
			super(logger);
		}

		public void doConsume(ScmFileStatus status, String line) {
			try {
				revNo = Integer.valueOf(line).intValue();
			} catch (NumberFormatException e) {
				// ignore
			}
		}

		int getCurrentRevisionNumber() {
			return revNo;
		}
	}
}
