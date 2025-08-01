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

package io.milton.common;

import org.slf4j.Logger;

/**
 * Utils for logging.
 * @author HP
 */
public class LogUtils {

	private LogUtils() {
	}

	/**
	 * Traces the message.
	 * @param log Logger.
	 * @param args Arguments to trace.
	 */
	public static void trace(Logger log, Object... args) {
		if (log.isTraceEnabled()) {
			StringBuilder sb = new StringBuilder();
			for (Object o : args) {
				sb.append(o).append(", ");
			}
			log.trace(sb.toString());
		}
	}

	/**
	 * Debugs the message.
	 * @param log Logger.
	 * @param args Arguments to debug.
	 */
	public static void debug(Logger log, Object... args) {
		if (log.isDebugEnabled()) {
			StringBuilder sb = new StringBuilder();
			for (Object o : args) {
				sb.append(o).append(", ");
			}
			log.debug(sb.toString());
		}
	}

	/**
	 * Warns the message.
	 * @param log Logger.
	 * @param args Arguments to warn.
	 */
	public static void warn(Logger log, Object... args) {
		if (log.isWarnEnabled()) {
			StringBuilder sb = new StringBuilder();
			for (Object o : args) {
				sb.append(o).append(", ");
			}
			log.warn(sb.toString());
		}
	}
}
