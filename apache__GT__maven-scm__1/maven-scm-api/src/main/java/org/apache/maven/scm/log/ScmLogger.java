package org.apache.maven.scm.log;

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

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 *
 */
public interface ScmLogger {
	boolean isDebugEnabled();

	void debug(String content);

	void debug(String content, Throwable error);

	void debug(Throwable error);

	boolean isInfoEnabled();

	void info(String content);

	void info(String content, Throwable error);

	void info(Throwable error);

	boolean isWarnEnabled();

	void warn(String content);

	void warn(String content, Throwable error);

	void warn(Throwable error);

	boolean isErrorEnabled();

	void error(String content);

	void error(String content, Throwable error);

	void error(Throwable error);
}
