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
package org.apache.distributedlog.util;

import com.google.common.annotations.VisibleForTesting;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import org.apache.bookkeeper.feature.Feature;
import org.apache.bookkeeper.stats.Counter;
import org.apache.bookkeeper.stats.Gauge;
import org.apache.bookkeeper.stats.OpStatsLogger;
import org.apache.bookkeeper.stats.StatsLogger;
import org.apache.distributedlog.common.util.PermitLimiter;

/**
 * Simple counter based {@link PermitLimiter}.
 *
 * <h3>Metrics</h3>
 * <ul>
 * <li> `permits`: gauge. how many permits are acquired right now?
 * <li> `permits`/*: opstats. the characteristics about number of permits already acquired on each acquires.
 * <li> `acquireFailure`: counter. how many acquires failed? failure means it already reached maximum permits
 * when trying to acquire.
 * </ul>
 */
public class SimplePermitLimiter implements PermitLimiter {

	final Counter acquireFailureCounter;
	final OpStatsLogger permitsMetric;
	private static final AtomicIntegerFieldUpdater<SimplePermitLimiter> permitsUpdater = AtomicIntegerFieldUpdater
			.newUpdater(SimplePermitLimiter.class, "permits");
	volatile int permits = 0;
	final int permitsMax;
	final boolean darkmode;
	final Feature disableWriteLimitFeature;
	private StatsLogger statsLogger = null;
	private Gauge<Number> permitsGauge = null;
	private String permitsGaugeLabel = "";

	public SimplePermitLimiter(boolean darkmode, int permitsMax, StatsLogger statsLogger, boolean singleton,
			Feature disableWriteLimitFeature) {
		this.permitsMax = permitsMax;
		this.darkmode = darkmode;
		this.disableWriteLimitFeature = disableWriteLimitFeature;

		// stats
		if (singleton) {
			this.statsLogger = statsLogger;
			this.permitsGauge = new Gauge<Number>() {
				@Override
				public Number getDefaultValue() {
					return 0;
				}

				@Override
				public Number getSample() {
					return permitsUpdater.get(SimplePermitLimiter.this);
				}
			};
			this.permitsGaugeLabel = "permits";
			statsLogger.registerGauge(permitsGaugeLabel, permitsGauge);
		}
		acquireFailureCounter = statsLogger.getCounter("acquireFailure");
		permitsMetric = statsLogger.getOpStatsLogger("permits");
	}

	public boolean isDarkmode() {
		return darkmode || disableWriteLimitFeature.isAvailable();
	}

	@Override
	public boolean acquire() {
		permitsMetric.registerSuccessfulValue(permitsUpdater.get(this));
		if (permitsUpdater.incrementAndGet(this) <= permitsMax || isDarkmode()) {
			return true;
		} else {
			acquireFailureCounter.inc();
			permitsUpdater.decrementAndGet(this);
			return false;
		}
	}

	@Override
	public void release(int permitsToRelease) {
		permitsUpdater.addAndGet(this, -permitsToRelease);
	}

	@Override
	public void close() {
		unregisterGauge();
	}

	@VisibleForTesting
	public int getPermits() {
		return permitsUpdater.get(this);
	}

	public void unregisterGauge() {
		if (this.statsLogger != null && this.permitsGauge != null) {
			this.statsLogger.unregisterGauge(permitsGaugeLabel, permitsGauge);
		}
	}
}
