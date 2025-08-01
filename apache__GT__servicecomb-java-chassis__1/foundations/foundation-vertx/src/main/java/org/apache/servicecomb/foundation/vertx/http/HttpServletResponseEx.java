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

package org.apache.servicecomb.foundation.vertx.http;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.CompletableFuture;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import jakarta.ws.rs.core.Response.StatusType;

public interface HttpServletResponseEx extends HttpServletResponse, BodyBufferSupport {
	StatusType getStatusType();

	void setAttribute(String key, Object value);

	Object getAttribute(String key);

	CompletableFuture<Void> sendPart(Part body);

	CompletableFuture<Void> sendBuffer(Buffer buffer);

	default void setChunked(boolean chunked) {
		setHeader(HttpHeaders.TRANSFER_ENCODING.toString(), HttpHeaders.CHUNKED.toString());
	}

	void endResponse() throws IOException;

	@Override
	default ServletOutputStream getOutputStream() throws IOException {
		throw new IOException("Not allowed");
	}

	@Override
	default PrintWriter getWriter() throws IOException {
		throw new IOException("Not allowed");
	}
}
