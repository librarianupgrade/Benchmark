// Copyright (c) 2023 Broadcom. All Rights Reserved.
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
package com.rabbitmq.perf.metrics;

import static org.apache.commons.lang3.StringUtils.countMatches;
import static org.apache.commons.lang3.StringUtils.splitByWholeSeparatorPreserveAllTokens;
import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class DefaultPerformanceMetricsTest {

	static final String LATENCY_HEADER = "min/median/75th/95th/99th";
	StringWriter csvOut;
	ByteArrayOutputStream defaultConsoleOut, compactConsoleOut;
	String defaultOutput, compactOutput;

	static Configurator configure() {
		return new Configurator();
	}

	static TestConfiguration[] testArguments() {
		return ArrayUtils.addAll(testConfigurationsMicroSeconds(), testConfigurationsMilliSeconds());
	}

	static TestConfiguration[] testConfigurationsMicroSeconds() {
		return new TestConfiguration[] {
				tc("All metrics (sent, received, returned, confirmed, nacked)",
						configure().sent(true).received(true).returned(true).confirmed(true),
						array("sent", "returned", "confirmed", "nacked", "received",
								"min/median/75th/95th/99th consumer latency", "confirm latency"),
						2),
				tc("Sent, received", configure().sent(true).received(true).returned(false).confirmed(false),
						array("sent", "received", "min/median/75th/95th/99th consumer latency"), 1,
						array("returned", "confirmed", "nacked", "confirm latency")),
				tc("Sent, received, confirmed", configure().sent(true).received(true).returned(false).confirmed(true),
						array("sent", "confirmed", "nacked", "received", "min/median/75th/95th/99th consumer latency",
								"confirm latency"),
						2, array("returned")),
				tc("Sent, received, returned", configure().sent(true).received(true).returned(true).confirmed(false),
						array("sent", "returned", "received", "min/median/75th/95th/99th consumer latency"), 1,
						array("confirmed", "nacked", "confirm latency")),
				tc("Sent", configure().sent(true).received(false).returned(false).confirmed(false), array("sent"), 0,
						array("returned", "confirmed", "nacked", "received",
								"min/median/75th/95th/99th consumer latency", "consumer latency", "confirm latency")),
				tc("Sent, returned", configure().sent(true).received(false).returned(true).confirmed(false),
						array("sent", "returned"), 0,
						array("confirmed", "nacked", "received", "min/median/75th/95th/99th consumer latency",
								"consumer latency", "confirm latency")),
				tc("Sent, confirmed", configure().sent(true).received(false).returned(false).confirmed(true),
						array("sent", "confirmed", "nacked", "min/median/75th/95th/99th confirm latency"), 1,
						array("returned", "received", "consumer latency")),
				tc("Sent, returned, confirmed", configure().sent(true).received(false).returned(true).confirmed(true),
						array("sent", "returned", "confirmed", "nacked", "min/median/75th/95th/99th confirm latency"),
						1, array("received", "consumer latency")),
				tc("Sent, received", configure().sent(true).received(true).returned(false).confirmed(false),
						array("sent", "received", "min/median/75th/95th/99th consumer latency"), 1,
						array("returned", "confirmed", "nacked", "confirm latency")),
				tc("Received", configure().sent(false).received(true).returned(false).confirmed(false),
						array("received", "min/median/75th/95th/99th consumer latency"), 1,
						array("sent", "returned", "confirmed", "nacked", "confirm latency")), };
	}

	static TestConfiguration[] testConfigurationsMilliSeconds() {
		return Stream.of(testConfigurationsMicroSeconds()).map(configuration -> {
			configuration.configurator.useMilliseconds(true);
			return configuration;
		}).collect(Collectors.toList()).toArray(new TestConfiguration[] {});
	}

	static String[] array(String... values) {
		return values;
	}

	static TestConfiguration tc(String description, Configurator configurator, String[] expectedSubstringInOutput,
			int unitOccurrences, String... nonExpectedSubstringInOutput) {
		return new TestConfiguration(description, configurator, expectedSubstringInOutput, unitOccurrences,
				nonExpectedSubstringInOutput);
	}

	@BeforeEach
	public void init() {
		csvOut = new StringWriter();
		defaultConsoleOut = new ByteArrayOutputStream();
		compactConsoleOut = new ByteArrayOutputStream();
	}

	@ParameterizedTest
	@MethodSource("testArguments")
	public void stats(TestConfiguration testConfiguration) {
		execute(testConfiguration.configurator);
		checkCsv(testConfiguration.unit());
		assertThat(defaultOutput.split(",")).hasSize(testConfiguration.expectedSubstringInOutput.length);
		assertThatDefaultOutputContains(testConfiguration.expectedSubstringInOutput);
		assertThatDefaultOutputDoesNotContain(testConfiguration.nonExpectedSubstringInOutput);
		assertThat(countMatches(defaultOutput, "0/0/0/0/0 " + testConfiguration.unit().name))
				.isEqualTo(testConfiguration.unitOccurrences);

		String[] lines = compactOutput.split(System.getProperty("line.separator"));
		assertThat(lines).hasSize(2);
		assertThat(Arrays.stream(lines[0].split("  ")) // several spaces between fields
				.map(String::trim).filter(s -> !s.isEmpty())) // remove fields that are empty because of the space split
				.hasSize(testConfiguration.expectedSubstringInOutput.length - 1); // no test ID
		assertThatCompactOutputContains(testConfiguration.expectedSubstringInOutput);
		assertThatCompactOutputDoesNotContain(testConfiguration.nonExpectedSubstringInOutput);
		assertThat(countMatches(compactOutput, "0/0/0/0/0 " + testConfiguration.unit().name))
				.isEqualTo(testConfiguration.unitOccurrences);
	}

	void execute(Configurator configurator) {
		DefaultPerformanceMetrics metrics = metrics(configurator);
		metrics.started(true);
		metrics.metrics(System.nanoTime());
		this.defaultOutput = defaultConsoleOut.toString();
		this.compactOutput = compactConsoleOut.toString();
	}

	void assertThatDefaultOutputContains(String... substrings) {
		assertThat(defaultOutput).contains(substrings);
	}

	void assertThatCompactOutputContains(String... substrings) {
		// compact formatter contains less info, so we relax the expectations
		String[] relaxedSubstrings = new String[substrings.length];
		for (int i = 0; i < substrings.length; i++) {
			relaxedSubstrings[i] = substrings[i].replace(LATENCY_HEADER, "").trim();
		}
		assertThat(compactOutput).contains(Arrays.stream(relaxedSubstrings).filter(s -> !"id".equals(s))
				.filter(s -> !"test".equals(s)).collect(Collectors.toList()));
	}

	void assertThatDefaultOutputDoesNotContain(String... substrings) {
		if (substrings != null && substrings.length > 0) {
			assertThat(defaultOutput).doesNotContain(substrings);
		}
	}

	void assertThatCompactOutputDoesNotContain(String... substrings) {
		if (substrings != null && substrings.length > 0) {
			assertThat(compactOutput).doesNotContain(substrings);
		}
	}

	void checkCsv(Unit unit) {
		String[] lines = csvOut.toString().split(System.getProperty("line.separator"));
		assertThat(lines).hasSize(2);
		for (String line : lines) {
			assertThat(splitByWholeSeparatorPreserveAllTokens(line, ",")).hasSize(17);
		}
		assertThat(countMatches(lines[0], "(" + unit.name + ")")).isEqualTo(5 * 2);
	}

	DefaultPerformanceMetrics metrics(Configurator configurator) {
		return new DefaultPerformanceMetrics(Duration.ofMillis(1000), TimeUnit.NANOSECONDS, new SimpleMeterRegistry(),
				"metrics-prefix",
				new CompositeMetricsFormatter(
						new DefaultPrintStreamMetricsFormatter(new PrintStream(defaultConsoleOut), "test-id",
								configurator.sendStatsEnabled, configurator.recvStatsEnabled,
								configurator.returnStatsEnabled, configurator.confirmStatsEnabled,
								configurator.useMillis ? TimeUnit.MILLISECONDS : TimeUnit.NANOSECONDS),
						new CompactPrintStreamMetricsFormatter(new PrintStream(compactConsoleOut),
								configurator.sendStatsEnabled, configurator.recvStatsEnabled,
								configurator.returnStatsEnabled, configurator.confirmStatsEnabled,
								configurator.useMillis ? TimeUnit.MILLISECONDS : TimeUnit.NANOSECONDS),
						new CsvMetricsFormatter(new PrintWriter(csvOut), "test-id", configurator.sendStatsEnabled,
								configurator.recvStatsEnabled, configurator.returnStatsEnabled,
								configurator.confirmStatsEnabled,
								configurator.useMillis ? TimeUnit.MILLISECONDS : TimeUnit.NANOSECONDS)));
	}

	enum Unit {
		MS("ms"), MICROS("µs");

		private final String name;

		Unit(String unit) {
			this.name = unit;
		}
	}

	static class TestConfiguration {

		Configurator configurator;

		String[] expectedSubstringInOutput, nonExpectedSubstringInOutput;

		int unitOccurrences;

		String description;

		public TestConfiguration(String description, Configurator configurator, String[] expectedSubstringInOutput,
				int unitOccurrences, String... nonExpectedSubstringInOutput) {
			this.description = description;
			this.configurator = configurator;
			this.expectedSubstringInOutput = ArrayUtils.addAll(new String[] { "id", "test" },
					expectedSubstringInOutput);
			this.nonExpectedSubstringInOutput = nonExpectedSubstringInOutput;
			this.unitOccurrences = unitOccurrences;
		}

		Unit unit() {
			return useMilliseconds() ? Unit.MS : Unit.MICROS;
		}

		boolean useMilliseconds() {
			return configurator.useMillis;
		}

		@Override
		public String toString() {
			return description + " (" + unit() + ")";
		}
	}

	static class Configurator {

		boolean sendStatsEnabled;
		boolean recvStatsEnabled;
		boolean returnStatsEnabled;
		boolean confirmStatsEnabled;
		boolean useMillis;

		Configurator sent(boolean enabled) {
			this.sendStatsEnabled = enabled;
			return this;
		}

		Configurator received(boolean enabled) {
			this.recvStatsEnabled = enabled;
			return this;
		}

		Configurator returned(boolean enabled) {
			this.returnStatsEnabled = enabled;
			return this;
		}

		Configurator confirmed(boolean enabled) {
			this.confirmStatsEnabled = enabled;
			return this;
		}

		Configurator useMilliseconds(boolean enabled) {
			this.useMillis = enabled;
			return this;
		}
	}
}
