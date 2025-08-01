/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.servicecomb.transport.rest.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rest Servlet Server, load by web container
 */
public class RestServlet extends HttpServlet {
	private static final long serialVersionUID = 5797523329773923112L;

	private static final Logger LOGGER = LoggerFactory.getLogger(RestServlet.class);

	private final ServletRestDispatcher servletRestServer = new ServletRestDispatcher();

	@Override
	public void init() throws ServletException {
		super.init();

		LOGGER.info("Rest Servlet inited");
	}

	@Override
	public void service(final HttpServletRequest request, final HttpServletResponse response) {
		servletRestServer.service(request, response);
	}
}
