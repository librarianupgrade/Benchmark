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

package org.apache.servicecomb.config.center.client;

import java.util.List;
import java.util.Map;

import org.apache.servicecomb.config.center.client.model.ConfigCenterConfiguration;
import org.apache.servicecomb.config.center.client.model.QueryConfigurationsRequest;
import org.apache.servicecomb.config.center.client.model.QueryConfigurationsResponse;
import org.apache.servicecomb.config.common.ConfigConverter;
import org.apache.servicecomb.http.client.task.AbstractTask;
import org.apache.servicecomb.http.client.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

public class ConfigCenterManager extends AbstractTask {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigCenterManager.class);

	private final ConfigCenterClient configCenterClient;

	private final EventBus eventBus;

	private QueryConfigurationsRequest queryConfigurationsRequest;

	private final ConfigConverter configConverter;

	private final ConfigCenterConfiguration configCenterConfiguration;

	private final ConfigCenterAddressManager configCenterAddressManager;

	public ConfigCenterManager(ConfigCenterClient configCenterClient, EventBus eventBus,
			ConfigConverter configConverter, ConfigCenterConfiguration configCenterConfiguration,
			ConfigCenterAddressManager configCenterAddressManager) {
		super("config-center-configuration-task");
		this.configCenterClient = configCenterClient;
		this.eventBus = eventBus;
		this.configConverter = configConverter;
		this.configCenterConfiguration = configCenterConfiguration;
		this.configCenterAddressManager = configCenterAddressManager;
	}

	public void setQueryConfigurationsRequest(QueryConfigurationsRequest queryConfigurationsRequest) {
		this.queryConfigurationsRequest = queryConfigurationsRequest;
	}

	public void startConfigCenterManager() {
		this.startTask(new PollConfigurationTask(0));
		schedulerCheckAddressAvailable("cc-addr-check", new CheckConfigCenterAddressTask(),
				configCenterConfiguration.getRefreshIntervalInMillis());
	}

	class PollConfigurationTask implements Task {
		int failCount;

		public PollConfigurationTask(int failCount) {
			this.failCount = failCount;
		}

		@Override
		public void execute() {
			try {
				QueryConfigurationsResponse response = configCenterClient
						.queryConfigurations(queryConfigurationsRequest, configCenterAddressManager.address());
				if (response.isChanged()) {
					queryConfigurationsRequest.setRevision(response.getRevision());
					Map<String, Object> lastData = configConverter.updateData(response.getConfigurations());
					ConfigCenterConfigurationChangedEvent event = ConfigCenterConfigurationChangedEvent
							.createIncremental(configConverter.getCurrentData(), lastData);
					if (!event.getChanged().isEmpty()) {
						eventBus.post(event);
					}
				}
				startTask(new BackOffSleepTask(configCenterConfiguration.getRefreshIntervalInMillis(),
						new PollConfigurationTask(0)));
			} catch (Exception e) {
				LOGGER.error("get configurations from ConfigCenter failed, and will try again.", e);
				startTask(new BackOffSleepTask(failCount + 1, new PollConfigurationTask(failCount + 1)));
			}
		}
	}

	class CheckConfigCenterAddressTask implements Runnable {
		@Override
		public void run() {
			List<String> isolationAddresses = configCenterAddressManager.getIsolationAddresses();
			if (isolationAddresses.isEmpty()) {
				return;
			}
			for (String address : isolationAddresses) {
				configCenterClient.checkAddressAvailable(queryConfigurationsRequest, address);
			}
		}
	}
}
