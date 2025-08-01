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
package org.jclouds.http.internal;

import org.jclouds.http.HttpRequest;
import org.jclouds.io.PayloadEnclosing;
import org.jclouds.io.payloads.StringPayload;
import org.jclouds.logging.Logger;
import org.jclouds.util.Strings2;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

@Test(groups = "unit", sequential = true)
public class WireTest {

	static class BufferLogger implements Logger {
		StringBuilder buff = new StringBuilder();

		public void debug(String message, Object... args) {
			buff.append(message);
		}

		public void error(String message, Object... args) {
		}

		public void error(Throwable throwable, String message, Object... args) {
		}

		public String getCategory() {
			return null;
		}

		public void info(String message, Object... args) {
		}

		public boolean isDebugEnabled() {
			return true;
		}

		public boolean isErrorEnabled() {
			return false;
		}

		public boolean isInfoEnabled() {
			return false;
		}

		public boolean isTraceEnabled() {
			return false;
		}

		public boolean isWarnEnabled() {
			return false;
		}

		public void trace(String message, Object... args) {
		}

		public void warn(String message, Object... args) {
		}

		public void warn(Throwable throwable, String message, Object... args) {
		}

	}

	public HttpWire setUp() throws Exception {
		BufferLogger bufferLogger = new BufferLogger();
		HttpWire wire = new HttpWire();
		wire.wireLog = bufferLogger;
		return wire;
	}

	public HttpWire setUpSynch() throws Exception {
		BufferLogger bufferLogger = new BufferLogger();
		HttpWire wire = new HttpWire();
		wire.wireLog = bufferLogger;
		return wire;
	}

	public void testInputInputStream() throws Exception {
		HttpWire wire = setUp();
		InputStream in = wire.input(new ByteArrayInputStream("foo".getBytes()));
		String compare = Strings2.toStringAndClose(in);
		Thread.sleep(100);
		assertEquals(compare, "foo");
		assertEquals(((BufferLogger) wire.getWireLog()).buff.toString(), "<< \"foo\"");
	}

	public void testInputInputStreamSynch() throws Exception {
		HttpWire wire = setUpSynch();
		InputStream in = wire.input(new ByteArrayInputStream("foo".getBytes()));
		String compare = Strings2.toStringAndClose(in);
		assertEquals(compare, "foo");
		assertEquals(((BufferLogger) wire.getWireLog()).buff.toString(), "<< \"foo\"");
	}

	public void testOutputInputStream() throws Exception {
		HttpWire wire = setUp();
		InputStream in = wire.output(new ByteArrayInputStream("foo".getBytes()));
		String compare = Strings2.toStringAndClose(in);
		Thread.sleep(100);
		assertEquals(compare, "foo");
		assertEquals(((BufferLogger) wire.getWireLog()).buff.toString(), ">> \"foo\"");
	}

	public void testOutputBytes() throws Exception {
		HttpWire wire = setUp();
		wire.output("foo".getBytes());
		assertEquals(((BufferLogger) wire.getWireLog()).buff.toString(), ">> \"foo\"");
	}

	public void testOutputString() throws Exception {
		HttpWire wire = setUp();
		wire.output("foo");
		assertEquals(((BufferLogger) wire.getWireLog()).buff.toString(), ">> \"foo\"");
	}

	@Test
	public void testInputPayload() throws Exception {
		HttpWire wire = setUp();
		StringPayload payload = new StringPayload("foo");
		PayloadEnclosing request = HttpRequest.builder().method("foo").endpoint("http://foo").payload(payload).build();
		wire.input(request);
		BufferLogger wireLog = (BufferLogger) wire.getWireLog();
		assertEquals(wireLog.buff.toString(), "<< \"foo\"", "Expected payload to be printed in logs");
		wireLog.buff.setLength(0);

		payload.setSensitive(true);
		request = HttpRequest.builder().method("foo").endpoint("http://foo").payload(payload).build();
		wire.input(request);
		assertNotEquals(wireLog.buff.toString(), "<< \"foo\"", "Expected payload to NOT be printed in logs");
		wireLog.buff.setLength(0);

		wire.logSensitiveInformation = true;
		request = HttpRequest.builder().method("foo").endpoint("http://foo").payload(payload).build();
		wire.input(request);
		assertEquals(wireLog.buff.toString(), "<< \"foo\"", "Expected payload to be printed in logs");
	}

	@Test
	public void testOutputPayload() throws Exception {
		HttpWire wire = setUp();
		StringPayload payload = new StringPayload("foo");
		PayloadEnclosing request = HttpRequest.builder().method("foo").endpoint("http://foo").payload(payload).build();
		wire.output(request);
		BufferLogger wireLog = (BufferLogger) wire.getWireLog();
		assertEquals(wireLog.buff.toString(), ">> \"foo\"", "Expected payload to be printed in logs");
		wireLog.buff.setLength(0);

		payload.setSensitive(true);
		request = HttpRequest.builder().method("foo").endpoint("http://foo").payload(payload).build();
		wire.output(request);
		assertNotEquals(wireLog.buff.toString(), ">> \"foo\"", "Expected payload to NOT be printed in logs");
		wireLog.buff.setLength(0);

		wire.logSensitiveInformation = true;
		request = HttpRequest.builder().method("foo").endpoint("http://foo").payload(payload).build();
		wire.output(request);
		assertEquals(wireLog.buff.toString(), ">> \"foo\"", "Expected payload to be printed in logs");
	}
}
