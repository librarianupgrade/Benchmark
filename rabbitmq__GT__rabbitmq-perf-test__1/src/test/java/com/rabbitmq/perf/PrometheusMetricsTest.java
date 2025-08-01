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

import static com.rabbitmq.perf.TestUtils.randomNetworkPort;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.rabbitmq.perf.Metrics.ConfigurationContext;
import com.rabbitmq.perf.TestUtils.DisabledOnJavaSemeru;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.cli.*;
import org.junit.jupiter.api.Test;

@DisabledOnJavaSemeru
public class PrometheusMetricsTest {

	@Test
	public void prometheusHttpEndpointExposed() throws Exception {
		PrometheusMetrics metrics = new PrometheusMetrics();
		Options options = metrics.options();
		int port = randomNetworkPort();
		CommandLineParser parser = new DefaultParser();
		CommandLine rawCmd = parser.parse(options, ("--metrics-prometheus-port " + port).split(" "));
		CommandLineProxy cmd = new CommandLineProxy(options, rawCmd, name -> null);
		CompositeMeterRegistry registry = new CompositeMeterRegistry();
		AtomicInteger metric = registry.gauge("dummy", new AtomicInteger(0));
		metric.set(42);
		metrics.configure(new ConfigurationContext(cmd, registry, null, null, null, null));
		metrics.start();

		URL url = new URI("http://localhost:" + port + "/metrics").toURL();
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		assertEquals(200, con.getResponseCode());
		String content = response(con);
		con.disconnect();
		assertTrue(content.contains("dummy 42.0"));

		metrics.close();
	}

	private String response(HttpURLConnection con) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuilder content = new StringBuilder();
		while ((inputLine = in.readLine()) != null) {
			content.append(inputLine);
		}
		in.close();
		return content.toString();
	}
}
