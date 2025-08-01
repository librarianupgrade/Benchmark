/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.bookkeeper.stats;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.configuration.Configuration;

/**
 * A <i>no-op</i> stats provider implementation.
 */
@SuppressFBWarnings("EI_EXPOSE_REP2")
public class NullStatsProvider implements StatsProvider {

	final StatsLogger nullStatsLogger = new NullStatsLogger();

	@Override
	public void start(Configuration conf) {
		// nop
	}

	@Override
	public void stop() {
		// nop
	}

	@Override
	public StatsLogger getStatsLogger(String scope) {
		return nullStatsLogger;
	}

}
