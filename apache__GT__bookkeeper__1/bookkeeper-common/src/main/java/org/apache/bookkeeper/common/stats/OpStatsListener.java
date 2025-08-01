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
package org.apache.bookkeeper.common.stats;

import com.google.common.base.Stopwatch;
import java.util.concurrent.TimeUnit;
import org.apache.bookkeeper.common.concurrent.FutureEventListener;
import org.apache.bookkeeper.stats.OpStatsLogger;

/**
 * A {@link FutureEventListener} monitors the stats for a given operation.
 */
public class OpStatsListener<T> implements FutureEventListener<T> {

	private final OpStatsLogger opStatsLogger;
	private final Stopwatch stopwatch;

	public OpStatsListener(OpStatsLogger opStatsLogger, Stopwatch stopwatch) {
		this.opStatsLogger = opStatsLogger;
		if (null == stopwatch) {
			this.stopwatch = Stopwatch.createStarted();
		} else {
			this.stopwatch = stopwatch;
		}
	}

	public OpStatsListener(OpStatsLogger opStatsLogger) {
		this(opStatsLogger, null);
	}

	@Override
	public void onSuccess(T value) {
		opStatsLogger.registerSuccessfulEvent(stopwatch.elapsed(TimeUnit.MICROSECONDS), TimeUnit.MICROSECONDS);
	}

	@Override
	public void onFailure(Throwable cause) {
		opStatsLogger.registerFailedEvent(stopwatch.elapsed(TimeUnit.MICROSECONDS), TimeUnit.MICROSECONDS);
	}
}
