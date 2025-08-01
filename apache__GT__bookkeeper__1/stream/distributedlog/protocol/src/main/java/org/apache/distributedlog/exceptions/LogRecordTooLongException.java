/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.distributedlog.exceptions;

/**
 * Exception is thrown when attempting to write a record whose size is too larger.
 *
 * <p>The size limit of a log record is {@link org.apache.distributedlog.LogRecord#MAX_LOGRECORD_SIZE}.
 */
public class LogRecordTooLongException extends DLException {

	private static final long serialVersionUID = 2788274084603111386L;

	public LogRecordTooLongException(String message) {
		super(StatusCode.TOO_LARGE_RECORD, message);
	}
}
