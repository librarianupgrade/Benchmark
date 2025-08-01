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

package org.apache.servicecomb.config.kie.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import org.apache.servicecomb.config.common.ConfigConverter;
import org.apache.servicecomb.config.kie.client.model.ConfigurationsRequest;
import org.apache.servicecomb.config.kie.client.model.ConfigurationsRequestFactory;
import org.apache.servicecomb.config.kie.client.model.ConfigurationsResponse;
import org.apache.servicecomb.config.kie.client.model.KieAddressManager;
import org.apache.servicecomb.config.kie.client.model.KieConfiguration;
import org.apache.servicecomb.http.client.task.AbstractTask;
import org.apache.servicecomb.http.client.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

public class KieConfigManager extends AbstractTask {

	private static final Logger LOGGER = LoggerFactory.getLogger(KieConfigManager.class);

	private static final long LONG_POLLING_INTERVAL = 1000;

	private final KieConfigOperation configKieClient;

	private final EventBus eventBus;

	private final ConfigConverter configConverter;

	private final List<ConfigurationsRequest> configurationsRequests;

	private final KieConfiguration kieConfiguration;

	private final KieAddressManager kieAddressManager;

	public KieConfigManager(KieConfigOperation configKieClient, EventBus eventBus, KieConfiguration kieConfiguration,
			ConfigConverter configConverter, KieAddressManager kieAddressManager) {
		super("config-center-configuration-task");
		this.configurationsRequests = ConfigurationsRequestFactory.buildConfigurationRequests(kieConfiguration);
		this.configurationsRequests.sort(ConfigurationsRequest::compareTo);
		this.configKieClient = configKieClient;
		this.eventBus = eventBus;
		this.configConverter = configConverter;
		this.kieConfiguration = kieConfiguration;
		this.kieAddressManager = kieAddressManager;
	}

	public void firstPull() {
		Map<String, Object> data = new HashMap<>();
		try {
			firstQueryConfigurations(data);
		} catch (Exception e) {
			if (this.kieConfiguration.isFirstPullRequired()) {
				throw e;
			} else {
				LOGGER.warn("first pull failed!");
			}
		}
	}

	private void firstQueryConfigurations(Map<String, Object> data) {
		for (int i = 0; i < 3;) {
			String address = kieAddressManager.address();
			try {
				this.configurationsRequests.forEach(r -> {
					r.setRevision(ConfigurationsRequest.INITIAL_REVISION);
					ConfigurationsResponse response = configKieClient.queryConfigurations(r, address);
					if (response.isChanged()) {
						r.setRevision(response.getRevision());
						r.setLastRawData(response.getConfigurations());
						data.putAll(response.getConfigurations());
					}
				});
				this.configConverter.updateData(data);
				break;
			} catch (Exception e) {
				if (i == 2) {
					throw e;
				}
				LOGGER.warn("firstQueryConfigurations failed, config address {} and ignore {}", address,
						e.getMessage());
			}
			i++;
		}
	}

	private void onDataChanged() {
		Map<String, Object> latestData = new HashMap<>();
		this.configurationsRequests.forEach(r -> latestData.putAll(r.getLastRawData()));

		Map<String, Object> lastData = configConverter.updateData(latestData);
		KieConfigurationChangedEvent event = KieConfigurationChangedEvent
				.createIncremental(configConverter.getCurrentData(), lastData);
		if (!event.getChanged().isEmpty()) {
			eventBus.post(event);
		}
	}

	@Override
	protected void initTaskPool(String taskName) {
		this.taskPool = Executors.newFixedThreadPool(3, (task) -> new Thread(task, taskName));
	}

	public void startConfigKieManager() {
		this.configurationsRequests.forEach((t) -> this.startTask(new PollConfigurationTask(0, t)));
		schedulerCheckAddressAvailable("kie-addr-check", new CheckKieAddressTask(configurationsRequests.get(0)),
				kieConfiguration.getRefreshIntervalInMillis());
	}

	class PollConfigurationTask implements Task {
		final int failCount;

		ConfigurationsRequest configurationsRequest;

		public PollConfigurationTask(int failCount, ConfigurationsRequest configurationsRequest) {
			this.failCount = failCount;
			this.configurationsRequest = configurationsRequest;
		}

		@Override
		public void execute() {
			try {
				ConfigurationsResponse response = configKieClient.queryConfigurations(configurationsRequest,
						kieAddressManager.address());
				if (response.isChanged()) {
					configurationsRequest.setRevision(response.getRevision());
					configurationsRequest.setLastRawData(response.getConfigurations());
					onDataChanged();
				}
				if (KieConfigManager.this.kieConfiguration.isEnableLongPolling()) {
					startTask(new BackOffSleepTask(LONG_POLLING_INTERVAL,
							new PollConfigurationTask(0, this.configurationsRequest)));
				} else {
					startTask(new BackOffSleepTask(kieConfiguration.getRefreshIntervalInMillis(),
							new PollConfigurationTask(0, this.configurationsRequest)));
				}
			} catch (Exception e) {
				LOGGER.error("get configurations from KieConfigCenter failed, and will try again.", e);
				startTask(new BackOffSleepTask(failCount + 1,
						new PollConfigurationTask(failCount + 1, this.configurationsRequest)));
			}
		}
	}

	class CheckKieAddressTask implements Runnable {
		ConfigurationsRequest configurationsRequest;

		public CheckKieAddressTask(ConfigurationsRequest configurationsRequest) {
			this.configurationsRequest = configurationsRequest;
		}

		@Override
		public void run() {
			List<String> isolationAddresses = kieAddressManager.getIsolationAddresses();
			if (isolationAddresses.isEmpty()) {
				return;
			}
			for (String address : isolationAddresses) {
				configKieClient.checkAddressAvailable(this.configurationsRequest, address);
			}
		}
	}
}
