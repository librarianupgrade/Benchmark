// Copyright (c) 2018-2023 Broadcom. All Rights Reserved.
// The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
//
// This software, the RabbitMQ Java client library, is triple-licensed under the
// Mozilla Public License 2.0 ("MPL"), the GNU General Public License version 2
// ("GPL") and the Apache License version 2 ("ASL"). For the MPL, please see
// LICENSE-MPL-RabbitMQ. For the GPL, please see LICENSE-GPL2.  For the ASL,
// please see LICENSE-APACHE2.
//
// This software is distributed on an "AS IS" basis, WITHOUT WARRANTY OF ANY KIND,
// either express or implied. See the LICENSE file for specific language governing
// rights and limitations of this software.
//
// If you have any questions regarding licensing, please contact us at
// info@rabbitmq.com.
package com.rabbitmq.perf;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.jmx.JmxConfig;
import io.micrometer.jmx.JmxMeterRegistry;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/** */
public class JmxMetrics implements Metrics {

	private volatile MeterRegistry registry;

	public Options options() {
		Options options = new Options();
		options.addOption(new Option("mjx", "metrics-jmx", false, "enable JMX metrics"));
		return options;
	}

	public void configure(ConfigurationContext context) throws Exception {
		CommandLineProxy cmd = context.cmd();
		CompositeMeterRegistry meterRegistry = context.meterRegistry();
		if (isEnabled(cmd)) {
			registry = new JmxMeterRegistry(JmxConfig.DEFAULT, Clock.SYSTEM);
			meterRegistry.add(registry);
		}
	}

	public void close() {
		if (registry != null) {
			registry.close();
		}
	}

	@Override
	public String toString() {
		return "JMX Metrics";
	}
}
