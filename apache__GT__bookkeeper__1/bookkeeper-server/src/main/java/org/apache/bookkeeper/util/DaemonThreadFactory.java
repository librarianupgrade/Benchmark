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

package org.apache.bookkeeper.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Daemon thread factory.
 */
public class DaemonThreadFactory implements ThreadFactory {
	private ThreadFactory defaultThreadFactory = Executors.defaultThreadFactory();
	private int priority = Thread.NORM_PRIORITY;

	public DaemonThreadFactory() {
	}

	public DaemonThreadFactory(int priority) {
		assert priority >= Thread.MIN_PRIORITY && priority <= Thread.MAX_PRIORITY;
		this.priority = priority;
	}

	@Override
	public Thread newThread(Runnable r) {
		Thread thread = defaultThreadFactory.newThread(r);
		thread.setDaemon(true);
		thread.setPriority(priority);
		return thread;
	}
}
