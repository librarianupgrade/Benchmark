// Copyright (c) 2018-2023 Broadcom. All Rights Reserved.
// The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
//
// This software, the RabbitMQ Java client library, is triple-licensed under the
// Mozilla Public License 2.0 ("MPL"), the GNU General Public License version 2
// ("GPL") and the Apache License version 2 ("ASL"). For the MPL, please see
// LICENSE-MPL-RabbitMQ. For the GPL, please see LICENSE-GPL2.  For the ASL,
// please see LICENSE-APACHE2.
//
// This software is distributed on an "AS IS" basis, WITHOUT WARRANTY OF ANY KIND,
// either express or implied. See the LICENSE file for specific language governing
// rights and limitations of this software.
//
// If you have any questions regarding licensing, please contact us at
// info@rabbitmq.com.
package com.rabbitmq.perf.it;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Host {

	public static String capture(InputStream is) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		StringBuilder buff = new StringBuilder();
		while ((line = br.readLine()) != null) {
			buff.append(line).append("\n");
		}
		return buff.toString();
	}

	public static Process executeCommand(String command) throws IOException {
		Process pr = executeCommandProcess(command);

		int ev = waitForExitValue(pr);
		if (ev != 0) {
			String stdout = capture(pr.getInputStream());
			String stderr = capture(pr.getErrorStream());
			throw new IOException("unexpected command exit value: " + ev + "\ncommand: " + command + "\n"
					+ "\nstdout:\n" + stdout + "\nstderr:\n" + stderr + "\n");
		}
		return pr;
	}

	private static int waitForExitValue(Process pr) {
		while (true) {
			try {
				pr.waitFor();
				break;
			} catch (InterruptedException ignored) {
			}
		}
		return pr.exitValue();
	}

	private static Process executeCommandProcess(String command) throws IOException {
		String[] finalCommand;
		if (System.getProperty("os.name").toLowerCase().contains("windows")) {
			finalCommand = new String[4];
			finalCommand[0] = "C:\\winnt\\system32\\cmd.exe";
			finalCommand[1] = "/y";
			finalCommand[2] = "/c";
			finalCommand[3] = command;
		} else {
			finalCommand = new String[3];
			finalCommand[0] = "/bin/sh";
			finalCommand[1] = "-c";
			finalCommand[2] = command;
		}
		return Runtime.getRuntime().exec(finalCommand);
	}

	public static Process rabbitmqctl(String command) throws IOException {
		return executeCommand(rabbitmqctlCommand() + " " + command);
	}

	public static void stopBrokerApp() throws IOException {
		rabbitmqctl("stop_app");
	}

	public static void startBrokerApp() throws IOException {
		rabbitmqctl("start_app");
	}

	public static String rabbitmqctlCommand() {
		String rabbitmqCtl = System.getProperty("rabbitmqctl.bin");
		if (rabbitmqCtl == null) {
			throw new IllegalStateException("Please define the rabbitmqctl.bin system property");
		}
		if (rabbitmqCtl.startsWith("DOCKER:")) {
			String containerId = rabbitmqCtl.split(":")[1];
			return "docker exec " + containerId + " rabbitmqctl";
		} else {
			return rabbitmqCtl;
		}
	}

	private static void closeConnection(String pid) throws IOException {
		rabbitmqctl("close_connection '" + pid + "' 'Closed via rabbitmqctl'");
	}

	public static void closeAllConnections(List<ConnectionInfo> connectionInfos) throws IOException {
		for (ConnectionInfo connectionInfo : connectionInfos) {
			closeConnection(connectionInfo.getPid());
		}
	}

	public static List<ConnectionInfo> listConnections() throws IOException {
		String output = capture(rabbitmqctl("list_connections -q pid peer_port").getInputStream());
		// output (header line presence depends on broker version):
		// pid	peer_port
		// <rabbit@mercurio.1.11491.0>	58713
		String[] allLines = output.split("\n");

		List<ConnectionInfo> result = new ArrayList<>();
		for (String line : allLines) {
			// line: <rabbit@mercurio.1.11491.0>	58713
			String[] columns = line.split("\t");
			// can be also header line, so ignoring NumberFormatException
			try {
				Integer.valueOf(columns[1]);
				result.add(new ConnectionInfo(columns[0]));
			} catch (NumberFormatException e) {
				// OK
			}
		}
		return result;
	}

	public static List<String> listQueues() throws IOException {
		// "messages" column will help ignore the header line
		String output = capture(rabbitmqctl("list_queues -q name messages").getInputStream());
		// output:
		// name	messages
		// amq.gen-R6o236wc_tFFqMd2W2ldGg	0
		List<String> allLines = Arrays.asList(output.split("\n"));

		return allLines.stream().filter(line -> line.trim().length() > 0).map(line -> {
			// line: amq.gen-R6o236wc_tFFqMd2W2ldGg	0
			String[] columns = line.split("\t");
			// can be also header line, so ignoring NumberFormatException
			try {
				Integer.valueOf(columns[1]);
				return columns[0];
			} catch (NumberFormatException e) {
				// OK
				return null;
			}
		}).filter(line -> line != null).collect(Collectors.toList());
	}

	public static class ConnectionInfo {

		private final String pid;

		public ConnectionInfo(String pid) {
			this.pid = pid;
		}

		public String getPid() {
			return pid;
		}
	}
}
