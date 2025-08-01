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

package io.milton.httpclient;

import java.net.URI;
import java.net.URISyntaxException;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mcevoyb
 */
public class PropFindMethod extends HttpEntityEnclosingRequestBase {

	private static final Logger log = LoggerFactory.getLogger(PropFindMethod.class);

	public PropFindMethod(String sUri) {
		URI uri;
		try {
			uri = new URI(sUri);
		} catch (URISyntaxException ex) {
			throw new RuntimeException(ex);
		}
		this.setURI(uri);
	}

	@Override
	public String getMethod() {
		return "PROPFIND";
	}
}
