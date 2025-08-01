/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    https://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */
package org.apache.directory.api.util.exception;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class MultiExceptionTest {
	private PrintStream originalOut;
	private PrintStream originalErr;

	@BeforeEach
	public void setUp() {
		originalOut = System.out;
		originalErr = System.err;
	}

	@AfterEach
	public void tearDown() {
		System.setOut(originalOut);
		System.setErr(originalErr);
	}

	@Test
	public void testPrintStacktracePrintWriterDoesNotWriteToSystemOutErr() {
		ByteArrayOutputStream systemOut = new ByteArrayOutputStream();
		PrintStream systemPrintStream = new PrintStream(systemOut);
		System.setOut(systemPrintStream);
		System.setErr(systemPrintStream);

		StringWriter customOut = new StringWriter();
		PrintWriter customPrintWriter = new PrintWriter(customOut);
		MultiException multiException = new MultiException("multi");
		multiException.addThrowable(new Exception("nested1"));
		multiException.addThrowable(new Exception("nested2"));
		multiException.printStackTrace(customPrintWriter);

		assertThat(customOut.toString(), containsString("multi"));
		assertThat(customOut.toString(), containsString("nested1"));
		assertThat(customOut.toString(), containsString("nested2"));
		assertThat(systemOut.size(), equalTo(0));
	}

	@Test
	public void testPrintStacktraceToPrintStreamDoesNotWriteToSystemOutErr() {
		ByteArrayOutputStream systemOut = new ByteArrayOutputStream();
		PrintStream systemPrintStream = new PrintStream(systemOut);
		System.setOut(systemPrintStream);
		System.setErr(systemPrintStream);

		ByteArrayOutputStream customOut = new ByteArrayOutputStream();
		PrintStream customPrintWriter = new PrintStream(customOut);
		MultiException multiException = new MultiException("multi");
		multiException.addThrowable(new Exception("nested1"));
		multiException.addThrowable(new Exception("nested2"));
		multiException.printStackTrace(customPrintWriter);

		assertThat(customOut.toString(), containsString("multi"));
		assertThat(customOut.toString(), containsString("nested1"));
		assertThat(customOut.toString(), containsString("nested2"));
		assertThat(systemOut.size(), equalTo(0));
	}
}
