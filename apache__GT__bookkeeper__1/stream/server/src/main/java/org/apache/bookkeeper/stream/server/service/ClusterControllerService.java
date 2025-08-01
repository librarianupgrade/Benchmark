/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.bookkeeper.stream.server.service;

import java.io.IOException;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.apache.bookkeeper.common.component.AbstractLifecycleComponent;
import org.apache.bookkeeper.stats.StatsLogger;
import org.apache.bookkeeper.stream.storage.api.cluster.ClusterController;
import org.apache.bookkeeper.stream.storage.conf.StorageConfiguration;

/**
 * A service that runs cluster controller.
 */
@Slf4j
public class ClusterControllerService extends AbstractLifecycleComponent<StorageConfiguration> {

	private final Supplier<ClusterController> controllerSupplier;
	private ClusterController controller;

	public ClusterControllerService(StorageConfiguration conf, Supplier<ClusterController> controllerSupplier,
			StatsLogger statsLogger) {
		super("cluster-controller", conf, statsLogger);
		this.controllerSupplier = controllerSupplier;
	}

	@Override
	protected void doStart() {
		if (null == controller) {
			controller = controllerSupplier.get();
			controller.start();
			log.info("Successfully started the cluster controller.");
		}
	}

	@Override
	protected void doStop() {
		if (null != controller) {
			controller.stop();
		}
	}

	@Override
	protected void doClose() throws IOException {
		// no-op
	}
}
