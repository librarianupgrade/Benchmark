/*
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

package org.apache.bookkeeper.server.service;

import java.io.IOException;
import org.apache.bookkeeper.common.util.ReflectionUtils;
import org.apache.bookkeeper.server.component.ServerLifecycleComponent;
import org.apache.bookkeeper.server.conf.BookieConfiguration;
import org.apache.bookkeeper.stats.NullStatsLogger;
import org.apache.bookkeeper.stats.StatsProvider;

/**
 * A {@link org.apache.bookkeeper.common.component.LifecycleComponent} that runs stats provider.
 */
public class StatsProviderService extends ServerLifecycleComponent {

	public static final String NAME = "stats-provider";

	private final StatsProvider statsProvider;

	public StatsProviderService(BookieConfiguration conf) throws Exception {
		super(NAME, conf, NullStatsLogger.INSTANCE);

		Class<? extends StatsProvider> statsProviderClass = conf.getServerConf().getStatsProviderClass();
		this.statsProvider = ReflectionUtils.newInstance(statsProviderClass);
	}

	public StatsProvider getStatsProvider() {
		return this.statsProvider;
	}

	@Override
	protected void doStart() {
		statsProvider.start(conf);
	}

	@Override
	protected void doStop() {
		statsProvider.stop();
	}

	@Override
	protected void doClose() throws IOException {
		// no-op
	}
}
