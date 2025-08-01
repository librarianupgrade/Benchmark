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
package org.apache.servicecomb.registry.discovery;

import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.servicecomb.foundation.common.net.IpPort;
import org.apache.servicecomb.foundation.common.net.NetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

public class TelnetInstancePing implements InstancePing {
	private static final Logger LOGGER = LoggerFactory.getLogger(TelnetInstancePing.class);

	@Override
	public boolean ping(StatefulDiscoveryInstance instance) {
		if (CollectionUtils.isEmpty(instance.getEndpoints())) {
			return false;
		}

		for (String endpoint : instance.getEndpoints()) {
			IpPort ipPort = NetUtils.parseIpPortFromURI(endpoint);
			try (Socket s = new Socket()) {
				s.connect(new InetSocketAddress(ipPort.getHostOrIp(), ipPort.getPort()), 3000);
				return true;
			} catch (Exception e) {
				LOGGER.warn("ping instance {}/{}/{}/{} endpoint {} failed", instance.getApplication(),
						instance.getServiceName(), instance.getRegistryName(), instance.getInstanceId(), endpoint);
			}
		}

		return false;
	}

	@Override
	public int getOrder() {
		return 10000;
	}
}
