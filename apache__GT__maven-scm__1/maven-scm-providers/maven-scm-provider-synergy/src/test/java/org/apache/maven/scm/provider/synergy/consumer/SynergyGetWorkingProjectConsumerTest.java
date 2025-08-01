package org.apache.maven.scm.provider.synergy.consumer;

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

import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.log.DefaultLog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author <a href="mailto:julien.henry@capgemini.com">Julien Henry</a>
 */
public class SynergyGetWorkingProjectConsumerTest extends ScmTestCase {
	public void testSynergyGetTaskObjectsConsumer() throws IOException {
		InputStream inputStream = getResourceAsStream("/synergy/consumer/getWorkingProject.txt");

		BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));

		String s = in.readLine();

		SynergyGetWorkingProjectConsumer consumer = new SynergyGetWorkingProjectConsumer(new DefaultLog());

		while (s != null) {
			consumer.consumeLine(s);

			s = in.readLine();
		}

		assertEquals("GesdemDT~ccm_root", consumer.getProjectSpec());
	}

	public void testSynergyGetTaskObjectsConsumerEmpty() throws IOException {
		InputStream inputStream = getResourceAsStream("/synergy/consumer/getWorkingProjectEmpty.txt");

		BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));

		String s = in.readLine();

		SynergyGetWorkingProjectConsumer consumer = new SynergyGetWorkingProjectConsumer(new DefaultLog());

		while (s != null) {
			consumer.consumeLine(s);

			s = in.readLine();
		}

		assertNull(consumer.getProjectSpec());
	}
}
