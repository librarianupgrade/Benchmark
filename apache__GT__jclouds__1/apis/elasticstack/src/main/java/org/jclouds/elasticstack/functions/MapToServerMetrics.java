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
package org.jclouds.elasticstack.functions;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jclouds.elasticstack.domain.DriveMetrics;
import org.jclouds.elasticstack.domain.ServerMetrics;

import com.google.common.base.Function;

@Singleton
public class MapToServerMetrics implements Function<Map<String, String>, ServerMetrics> {
	private final Function<Map<String, String>, Map<String, ? extends DriveMetrics>> mapToDriveMetrics;

	@Inject
	public MapToServerMetrics(Function<Map<String, String>, Map<String, ? extends DriveMetrics>> mapToDriveMetrics) {
		this.mapToDriveMetrics = mapToDriveMetrics;
	}

	public ServerMetrics apply(Map<String, String> from) {
		ServerMetrics.Builder metricsBuilder = new ServerMetrics.Builder();
		if (from.containsKey("tx:packets"))
			metricsBuilder.txPackets(Long.parseLong(from.get("tx:packets")));
		if (from.containsKey("tx"))
			metricsBuilder.tx(Long.parseLong(from.get("tx")));
		if (from.containsKey("rx:packets"))
			metricsBuilder.rxPackets(Long.parseLong(from.get("rx:packets")));
		if (from.containsKey("rx"))
			metricsBuilder.rx(Long.parseLong(from.get("rx")));
		metricsBuilder.driveMetrics(mapToDriveMetrics.apply(from));

		ServerMetrics metrics = metricsBuilder.build();
		return metrics;
	}
}
