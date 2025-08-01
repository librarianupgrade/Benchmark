// Copyright (c) 2019-2023 Broadcom. All Rights Reserved.
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
package com.rabbitmq.perf.it;

import static com.rabbitmq.perf.TestUtils.threadFactory;
import static com.rabbitmq.perf.TestUtils.waitAtMost;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.perf.MulticastParams;
import com.rabbitmq.perf.MulticastSet;
import com.rabbitmq.perf.PerformanceMetricsAdapter;
import com.rabbitmq.perf.metrics.PerformanceMetrics;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** */
public class StartUpIT {

	static final Logger LOGGER = LoggerFactory.getLogger(StartUpIT.class);

	static final List<String> URIS = Collections.singletonList("amqp://localhost");

	static final int RATE = 10;

	MulticastParams params;

	ExecutorService executorService;

	ConnectionFactory cf;

	AtomicBoolean testIsDone;
	AtomicBoolean testHasFailed;
	CountDownLatch testLatch;

	AtomicLong msgPublished, msgConsumed;

	PerformanceMetrics performanceMetrics = new PerformanceMetricsAdapter() {
		@Override
		public void published() {
			msgPublished.incrementAndGet();
		}

		@Override
		public void received(long latency) {
			msgConsumed.incrementAndGet();
		}

		@Override
		public Duration interval() {
			return Duration.ofSeconds(1);
		}
	};

	@BeforeEach
	public void init(TestInfo info) throws IOException {
		Host.stopBrokerApp();
		executorService = Executors.newCachedThreadPool(threadFactory(info));
		params = new MulticastParams();
		params.setProducerCount(1);
		params.setConsumerCount(1);
		params.setProducerRateLimit(RATE);
		cf = new ConnectionFactory();
		testIsDone = new AtomicBoolean(false);
		testHasFailed = new AtomicBoolean(false);
		testLatch = new CountDownLatch(1);
		msgConsumed = new AtomicLong(0);
		msgPublished = new AtomicLong(0);
	}

	@AfterEach
	public void tearDown() throws InterruptedException, IOException {
		LOGGER.info("Shutting down test executor");
		executorService.shutdownNow();
		if (!testLatch.await(10, TimeUnit.SECONDS)) {
			LOGGER.warn("PerfTest run didn't shut down properly, run logs may show up during other tests");
		}
		Host.startBrokerApp();
	}

	@Test
	public void shouldFailByDefaultIfBrokerIsDownAtStartup(TestInfo info) throws Exception {
		MulticastSet set = new MulticastSet(performanceMetrics, cf, params, "", URIS, latchCompletionHandler(1, info));
		run(set);
		waitAtMost(10, () -> testHasFailed.get());
	}

	@Test
	public void shouldWaitUntilBrokerIsUpWhenStartUpTimeoutIsSet(TestInfo info) throws Exception {
		AtomicInteger attempts = new AtomicInteger(0);
		// broker app is restarted after a few attempts
		ConnectionFactory factory = new ConnectionFactory() {
			@Override
			public void setUri(String uriString)
					throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException {
				super.setUri(uriString);
				if (attempts.incrementAndGet() == 3) {
					try {
						Host.startBrokerApp();
					} catch (IOException e) {
						LOGGER.warn("Could not start broker");
					}
				}
			}
		};
		params.setServersStartUpTimeout(10);
		MulticastSet.CompletionHandler completionHandler = latchCompletionHandler(1, info);
		MulticastSet set = new MulticastSet(performanceMetrics, factory, params, "", URIS, completionHandler);
		run(set);
		waitAtMost(10, () -> msgConsumed.get() > 10);
		completionHandler.countDown("");
		waitAtMost(10, () -> testIsDone.get());
	}

	@Test
	public void shouldStopGracefullyIfStartUpRetryTimesout(TestInfo info) throws Exception {
		Host.stopBrokerApp();
		params.setServersStartUpTimeout(3);
		MulticastSet set = new MulticastSet(performanceMetrics, cf, params, "", URIS, latchCompletionHandler(1, info));
		run(set);
		waitAtMost(10, () -> testIsDone.get());
	}

	private void run(MulticastSet multicastSet) {
		executorService.submit(() -> {
			try {
				multicastSet.run();
				testIsDone.set(true);
				testLatch.countDown();
			} catch (InterruptedException e) {
				// one of the tests stops the execution, no need to be noisy
				throw new RuntimeException(e);
			} catch (Exception e) {
				testHasFailed.set(true);
				testLatch.countDown();
			}
		});
	}

	private MulticastSet.CompletionHandler latchCompletionHandler(int count, TestInfo info) {
		return new LatchCompletionHandler(new CountDownLatch(count), info);
	}

	private static class LatchCompletionHandler implements MulticastSet.CompletionHandler {

		final CountDownLatch latch;

		final String name;

		private LatchCompletionHandler(CountDownLatch latch, TestInfo info) {
			this.latch = latch;
			this.name = info.getDisplayName();
		}

		@Override
		public void waitForCompletion() {
			LOGGER.info("Waiting completion for test [{}]", name);
			try {
				latch.await();
			} catch (InterruptedException e) {
				LOGGER.info("Completion waiting has been interrupted for test [{}]", name);
			}
		}

		@Override
		public void countDown(String reason) {
			latch.countDown();
		}
	}
}
