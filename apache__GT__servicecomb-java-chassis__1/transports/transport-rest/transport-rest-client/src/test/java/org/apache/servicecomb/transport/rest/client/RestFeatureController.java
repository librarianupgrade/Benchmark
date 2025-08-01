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
package org.apache.servicecomb.transport.rest.client;

import java.io.File;
import java.util.List;

import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

@Path("/")
public class RestFeatureController {
	public static final String SCHEMA_ID = "rest-feature";

	@GET
	@Path("/query")
	public String query(@QueryParam("query") String query) {
		return query;
	}

	@GET
	@Path("/header")
	public String header(@HeaderParam("header") String header) {
		return header;
	}

	@GET
	@Path("/cookie")
	public String cookie(@CookieParam("cookie1") String cookie1, @CookieParam("cookie2") String cookie2) {
		return cookie1 + ":" + cookie2;
	}

	@POST
	@Path("/form")
	public String form(@FormParam("form1") String form1, @FormParam("form2") String form2) {
		return form1 + ":" + form2;
	}

	@POST
	@Path("/formWithUpload")
	public String formWithUpload(@FormParam("form1") String form1, @FormParam("form2") File form2) {
		return form1 + ":" + form2.getName();
	}

	@POST
	@Path("/formWithUploadList")
	public String formWithUploadList(@FormParam("files") List<File> files) {
		return files.toString();
	}

	@POST
	@Path("/body")
	public String body(String body) {
		return body;
	}
}
