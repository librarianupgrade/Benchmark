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
package org.jclouds.chef.binders;

import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;

import org.jclouds.http.HttpRequest;
import org.jclouds.rest.binders.BindToStringPayload;

@Singleton
public class BindGenerateKeyForClientToJsonPayload extends BindToStringPayload {

	@Override
	public <R extends HttpRequest> R bindToRequest(R request, Object payload) {
		super.bindToRequest(request, String.format("{\"name\":\"%s\", \"private_key\": true}", payload));
		request.getPayload().getContentMetadata().setContentType(MediaType.APPLICATION_JSON);
		return request;
	}

}
