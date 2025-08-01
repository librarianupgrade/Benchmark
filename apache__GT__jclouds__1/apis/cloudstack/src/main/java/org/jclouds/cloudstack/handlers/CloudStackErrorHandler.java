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
package org.jclouds.cloudstack.handlers;

import java.io.IOException;

import javax.annotation.Resource;
import javax.inject.Singleton;

import org.jclouds.http.HttpCommand;
import org.jclouds.http.HttpErrorHandler;
import org.jclouds.http.HttpResponse;
import org.jclouds.http.HttpResponseException;
import org.jclouds.logging.Logger;
import org.jclouds.rest.AuthorizationException;
import org.jclouds.rest.InsufficientResourcesException;
import org.jclouds.rest.ResourceNotFoundException;
import org.jclouds.util.Closeables2;
import org.jclouds.util.Strings2;

import com.google.common.base.Throwables;

@Singleton
public class CloudStackErrorHandler implements HttpErrorHandler {
	@Resource
	protected Logger logger = Logger.NULL;

	public void handleError(HttpCommand command, HttpResponse response) {
		// it is important to always read fully and close streams
		String message = parseMessage(response);
		Exception exception = message != null ? new HttpResponseException(command, response, message)
				: new HttpResponseException(command, response);
		try {
			message = message != null ? message
					: String.format("%s -> %s", command.getCurrentRequest().getRequestLine(), response.getStatusLine());
			switch (response.getStatusCode()) {
			case 400:
				exception = new IllegalArgumentException(message, exception);
				break;
			case 531:
			case 401:
				exception = new AuthorizationException(message, exception);
				break;
			case 404:
				if (!command.getCurrentRequest().getMethod().equals("DELETE")) {
					exception = new ResourceNotFoundException(message, exception);
				}
				break;
			case 405:
				exception = new IllegalArgumentException(message, exception);
				break;
			case 409:
			case 431:
				if (message.contains("does not exist")) {
					exception = new ResourceNotFoundException(message, exception);
				} else {
					exception = new IllegalStateException(message, exception);
				}
				break;
			case 534:
				if (message.contains("Maximum number of resources of type")) {
					exception = new InsufficientResourcesException(message, exception);
				}
				break;
			case 537:
				exception = new IllegalStateException(message, exception);
				break;
			}
		} finally {
			Closeables2.closeQuietly(response.getPayload());
			command.setException(exception);
		}
	}

	public String parseMessage(HttpResponse response) {
		if (response.getPayload() == null)
			return null;
		try {
			return Strings2.toStringAndClose(response.getPayload().openStream());
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				response.getPayload().getInput().close();
			} catch (IOException e) {
				Throwables.propagate(e);
			}
		}
	}
}
