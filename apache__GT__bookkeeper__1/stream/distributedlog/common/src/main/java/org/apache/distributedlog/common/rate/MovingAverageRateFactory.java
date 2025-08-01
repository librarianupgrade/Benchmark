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
package org.apache.distributedlog.common.rate;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Factory to create {@link MovingAverageRate} instances.
 */
public class MovingAverageRateFactory implements Runnable {

	private static final int DEFAULT_INTERVAL_SECS = 1;

	private final ScheduledExecutorService scheduler;
	private final ScheduledFuture<?> scheduledFuture;
	private final CopyOnWriteArrayList<SampledMovingAverageRate> avgs;

	public MovingAverageRateFactory(ScheduledExecutorService scheduler) {
		this.avgs = new CopyOnWriteArrayList<SampledMovingAverageRate>();
		this.scheduler = scheduler;
		this.scheduledFuture = this.scheduler.scheduleAtFixedRate(this, DEFAULT_INTERVAL_SECS, DEFAULT_INTERVAL_SECS,
				TimeUnit.SECONDS);
	}

	public MovingAverageRate create(int intervalSecs) {
		SampledMovingAverageRate avg = new SampledMovingAverageRate(intervalSecs);
		avgs.add(avg);
		return avg;
	}

	public void close() {
		scheduledFuture.cancel(true);
		avgs.clear();
	}

	@Override
	public void run() {
		sampleAll();
	}

	private void sampleAll() {
		avgs.forEach(SampledMovingAverageRate::sample);
	}
}
