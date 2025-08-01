/**
 * Copyright (c) 2015-2022, Michael Yang 杨福海 (fuhai999@gmail.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jboot.components.gateway.discovery;

import java.util.List;

/**
 * GatewayDiscovery.
 */
public interface GatewayDiscovery {

	void registerInstance(GatewayInstance instance);

	void deregisterInstance(GatewayInstance instance);

	List<GatewayInstance> getAllInstances(String serviceName);

	List<GatewayInstance> selectInstances(String serviceName, boolean healthy);

	void subscribe(String serviceName, GatewayDiscoveryListener listener);

}
