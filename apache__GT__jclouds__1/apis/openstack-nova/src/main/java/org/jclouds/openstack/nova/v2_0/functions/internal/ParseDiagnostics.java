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
package org.jclouds.openstack.nova.v2_0.functions.internal;

import static com.google.common.base.Preconditions.checkNotNull;
import org.jclouds.http.HttpResponse;
import org.jclouds.http.functions.ParseJson;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.inject.Inject;
import java.util.Map;

public class ParseDiagnostics implements Function<HttpResponse, Optional<Map<String, String>>> {

	private final ParseJson<Optional<Map<String, String>>> parser;

	@Inject
	public ParseDiagnostics(ParseJson<Optional<Map<String, String>>> parser) {
		this.parser = parser;
	}

	@Override
	public Optional<Map<String, String>> apply(HttpResponse response) {
		checkNotNull(response, "response");
		return parser.apply(response);
	}

}
