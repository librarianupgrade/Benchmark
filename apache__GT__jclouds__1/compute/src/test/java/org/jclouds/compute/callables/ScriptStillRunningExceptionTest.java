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
package org.jclouds.compute.callables;

import static org.testng.Assert.assertEquals;

import java.util.concurrent.TimeUnit;

import org.jclouds.compute.domain.ExecResponse;
import org.testng.annotations.Test;

import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.ListenableFuture;

@Test(groups = "unit", singleThreaded = true, testName = "ScriptStillRunningExceptionTest")
public class ScriptStillRunningExceptionTest {

	public void simpleMessage() {
		ListenableFuture<ExecResponse> future = new AbstractFuture<ExecResponse>() {
			@Override
			public String toString() {
				return "task for foo";
			}

		};

		ScriptStillRunningException testMe = new ScriptStillRunningException(1000, TimeUnit.MILLISECONDS, future);
		assertEquals(testMe.getMessage(),
				"time up waiting 1s for task for foo to complete. call get() on this exception to get access to the task in progress");

	}
}
