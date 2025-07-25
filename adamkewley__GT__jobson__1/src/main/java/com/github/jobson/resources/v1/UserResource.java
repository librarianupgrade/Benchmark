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

package com.github.jobson.resources.v1;

import com.github.jobson.api.v1.APIUserDetails;
import com.github.jobson.api.v1.UserId;
import io.swagger.annotations.Api;

import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

import static com.github.jobson.Constants.HTTP_USERS_PATH;

@Api(description = "Operations related to users")
@Path(HTTP_USERS_PATH)
@Produces("application/json")
public final class UserResource {
	@GET
	@Path("current")
	@PermitAll
	public APIUserDetails fetchCurrentUserDetails(@Context SecurityContext context) {
		return new APIUserDetails(new UserId(context.getUserPrincipal().getName()));
	}
}
