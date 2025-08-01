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
package org.jclouds.events.handlers;

import javax.annotation.Resource;
import javax.inject.Singleton;

import org.jclouds.logging.Logger;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.Subscribe;

/**
 * Default handler for dead events.
 * <p>
 * It simply logs all dead events to allow debugging and troubleshooting.
 */
@Singleton
public class DeadEventLoggingHandler {
	@Resource
	private Logger logger = Logger.NULL;

	@Subscribe
	public void handleDeadEvent(DeadEvent deadEvent) { // NO_UCD
		logger.trace("detected dead event %s", deadEvent.getEvent());
	}
}
