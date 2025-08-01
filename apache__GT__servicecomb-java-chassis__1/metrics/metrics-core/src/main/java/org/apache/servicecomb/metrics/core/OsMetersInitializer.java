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
package org.apache.servicecomb.metrics.core;

import org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig;
import org.apache.servicecomb.foundation.metrics.MetricsInitializer;
import org.apache.servicecomb.foundation.metrics.meter.PeriodMeter;
import org.apache.servicecomb.metrics.core.meter.os.OsMeter;

import com.google.common.eventbus.EventBus;

import io.micrometer.core.instrument.MeterRegistry;

public class OsMetersInitializer implements MetricsInitializer, PeriodMeter {
	private OsMeter osMeter;

	@Override
	public void init(MeterRegistry meterRegistry, EventBus eventBus, MetricsBootstrapConfig config) {
		osMeter = new OsMeter(meterRegistry);
	}

	public OsMeter getOsMeter() {
		return osMeter;
	}

	@Override
	public void poll(long msNow, long secondInterval) {
		if (osMeter != null) {
			osMeter.poll(msNow, secondInterval);
		}
	}
}
