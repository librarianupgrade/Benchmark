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
package org.jclouds.openstack.keystone.v2_0.handlers;

import com.google.inject.Inject;
import org.jclouds.Constants;
import org.jclouds.http.HttpCommand;
import org.jclouds.http.HttpErrorHandler;
import org.jclouds.http.HttpResponse;
import org.jclouds.http.HttpResponseException;
import org.jclouds.rest.AuthorizationException;
import org.jclouds.rest.ResourceNotFoundException;

import javax.inject.Named;
import javax.inject.Singleton;

import static org.jclouds.http.HttpUtils.closeClientButKeepContentStream;

/**
 * This will parse and set an appropriate exception on the command object.
 */
// TODO: is there error spec someplace? let's type errors, etc.
@Singleton
public class KeystoneErrorHandler implements HttpErrorHandler {

	@Inject(optional = true)
	@Named(Constants.PROPERTY_LOGGER_WIRE_LOG_SENSITIVE_INFO)
	private boolean logSensitiveInformation = false;

	public void handleError(HttpCommand command, HttpResponse response) {
		// it is important to always read fully and close streams
		byte[] data = closeClientButKeepContentStream(response);
		String message = data != null ? new String(data) : null;

		Exception exception = message != null ? new HttpResponseException(command, response, message)
				: new HttpResponseException(command, response, logSensitiveInformation);
		message = message != null ? message
				: String.format("%s -> %s", command.getCurrentRequest().getRequestLine(), response.getStatusLine());
		switch (response.getStatusCode()) {
		case 400:
			break;
		case 401:
		case 403:
			exception = new AuthorizationException(message, exception);
			break;
		case 404:
			if (!command.getCurrentRequest().getMethod().equals("DELETE")) {
				exception = new ResourceNotFoundException(message, exception);
			}
			break;
		}
		command.setException(exception);
	}
}
