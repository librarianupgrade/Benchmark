/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.opensymphony.xwork2.config.providers;

import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookupFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EnvsValueSubstitutor implements ValueSubstitutor {

	private static final Logger LOG = LogManager.getLogger(EnvsValueSubstitutor.class);

	protected StringSubstitutor envStrSubstitutor;
	protected StringSubstitutor sysStrSubstitutor;

	public EnvsValueSubstitutor() {
		envStrSubstitutor = new StringSubstitutor(System.getenv());
		envStrSubstitutor.setVariablePrefix("${env.");
		envStrSubstitutor.setVariableSuffix('}');
		envStrSubstitutor.setValueDelimiter(':');

		sysStrSubstitutor = new StringSubstitutor(StringLookupFactory.INSTANCE.systemPropertyStringLookup());
		sysStrSubstitutor.setVariablePrefix("${");
		sysStrSubstitutor.setVariableSuffix('}');
		sysStrSubstitutor.setValueDelimiter(':');
	}

	@Override
	public String substitute(String value) {
		LOG.debug("Substituting value {} with proper System variable or environment variable", value);

		String substituted = sysStrSubstitutor.replace(value);
		return envStrSubstitutor.replace(substituted);
	}
}
