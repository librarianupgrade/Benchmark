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
 * Exception indicates that there is an internal error at distributedlog service side.
 */
public class InternalServerException extends DLException {

	private static final long serialVersionUID = 288438028880978802L;

	public InternalServerException(String msg) {
		super(StatusCode.INTERNAL_SERVER_ERROR, msg);
	}

	public InternalServerException(Throwable t) {
		super(StatusCode.INTERNAL_SERVER_ERROR, t);
	}

	public InternalServerException(String msg, Throwable t) {
		super(StatusCode.INTERNAL_SERVER_ERROR, msg, t);
	}
}
