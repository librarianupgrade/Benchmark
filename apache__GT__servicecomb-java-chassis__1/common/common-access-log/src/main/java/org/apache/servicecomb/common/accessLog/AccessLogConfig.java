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

package org.apache.servicecomb.common.accessLog;

import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;

public class AccessLogConfig {

	public static final String SERVER_BASE = "servicecomb.accesslog.";

	public static final String CLIENT_BASE = "servicecomb.accesslog.request.";

	public static final String SERVER_LOG_ENABLED = SERVER_BASE + "enabled";

	public static final String SERVER_LOG_PATTERN = SERVER_BASE + "pattern";

	public static final String CLIENT_LOG_ENABLED = CLIENT_BASE + "enabled";

	public static final String CLIENT_LOG_PATTERN = CLIENT_BASE + "pattern";

	public static final String DEFAULT_SERVER_PATTERN = "%h - - %t %r %s %B %D";

	public static final String DEFAULT_CLIENT_PATTERN = "%h %SCB-transport - - %t %r %s %D";

	public static final AccessLogConfig INSTANCE = new AccessLogConfig();

	private boolean serverLogEnabled;

	private boolean clientLogEnabled;

	private String serverLogPattern;

	private String clientLogPattern;

	private AccessLogConfig() {
		init();
	}

	private void init() {
		clientLogEnabled = LegacyPropertyFactory.getBooleanProperty(CLIENT_LOG_ENABLED, false);
		serverLogEnabled = LegacyPropertyFactory.getBooleanProperty(SERVER_LOG_ENABLED, false);
		clientLogPattern = LegacyPropertyFactory.getStringProperty(CLIENT_LOG_PATTERN, DEFAULT_CLIENT_PATTERN);
		serverLogPattern = LegacyPropertyFactory.getStringProperty(SERVER_LOG_PATTERN, DEFAULT_SERVER_PATTERN);
	}

	public boolean isServerLogEnabled() {
		return serverLogEnabled;
	}

	public boolean isClientLogEnabled() {
		return clientLogEnabled;
	}

	public String getServerLogPattern() {
		return serverLogPattern;
	}

	public String getClientLogPattern() {
		return clientLogPattern;
	}
}
