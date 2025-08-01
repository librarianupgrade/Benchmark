/**
 *
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
 *
 */
package org.apache.bookkeeper.http.servlet;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.Servlet;
import org.apache.bookkeeper.http.NullHttpServiceProvider;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test bookie http service servlet.
 **/
public class TestBookieHttpServiceServlet {

	private JettyHttpServer jettyHttpServer;
	private String host = "localhost";
	private int port = 8080;
	private BookieServletHttpServer bookieServletHttpServer;

	@Before
	public void setUp() throws Exception {
		this.bookieServletHttpServer = new BookieServletHttpServer();
		this.bookieServletHttpServer.initialize(new NullHttpServiceProvider());
		this.jettyHttpServer = new JettyHttpServer(host, port);
		List<Servlet> servlets = new ArrayList<>();
		servlets.add(new BookieHttpServiceServlet());
		jettyHttpServer.addServlet("web/bookie", "/", "/", servlets);
		jettyHttpServer.startServer();
	}

	@Test
	public void testBookieHeartBeat() throws URISyntaxException, IOException {
		assertThat(IOUtils.toString(new URI(String.format("http://%s:%d/heartbeat", host, port)), "UTF-8"),
				containsString("OK"));
	}

	@After
	public void stop() throws Exception {
		jettyHttpServer.stopServer();
	}
}
