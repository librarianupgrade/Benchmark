/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.address.controller;

import com.alibaba.nacos.address.component.AddressServerGeneratorManager;
import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.alibaba.nacos.api.naming.utils.NamingUtils;
import com.alibaba.nacos.naming.core.v2.ServiceManager;
import com.alibaba.nacos.naming.core.v2.index.ServiceStorage;
import com.alibaba.nacos.naming.core.v2.metadata.ClusterMetadata;
import com.alibaba.nacos.naming.core.v2.metadata.NamingMetadataManager;
import com.alibaba.nacos.naming.core.v2.metadata.ServiceMetadata;
import com.alibaba.nacos.naming.core.v2.pojo.Service;
import com.alibaba.nacos.naming.utils.ServiceUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * Server list controller.
 *
 * @author pbting
 * @since 1.1.0
 */
@RestController
public class ServerListController {

	private final AddressServerGeneratorManager addressServerBuilderManager;

	private final NamingMetadataManager metadataManager;

	private final ServiceStorage serviceStorage;

	public ServerListController(AddressServerGeneratorManager addressServerBuilderManager,
			NamingMetadataManager metadataManager, ServiceStorage serviceStorage) {
		this.addressServerBuilderManager = addressServerBuilderManager;
		this.metadataManager = metadataManager;
		this.serviceStorage = serviceStorage;
	}

	/**
	 * Get cluster.
	 *
	 * @param product will get Ip list of that products to be associated
	 * @param cluster will get Ip list of that product cluster to be associated
	 * @return result of get
	 */
	@RequestMapping(value = "/{product}/{cluster}", method = RequestMethod.GET)
	public ResponseEntity<String> getCluster(@PathVariable String product, @PathVariable String cluster) {

		String productName = addressServerBuilderManager.generateProductName(product);
		String serviceName = addressServerBuilderManager.generateNacosServiceName(productName);
		String serviceWithoutGroup = NamingUtils.getServiceName(serviceName);
		String groupName = NamingUtils.getGroupName(serviceName);
		Optional<Service> service = ServiceManager.getInstance().getSingletonIfExist(Constants.DEFAULT_NAMESPACE_ID,
				groupName, serviceWithoutGroup);
		if (!service.isPresent()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("product=" + product + " not found.");
		}
		ClusterMetadata metadata = metadataManager.getServiceMetadata(service.get()).orElse(new ServiceMetadata())
				.getClusters().get(cluster);
		if (null == metadata) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body("product=" + product + ",cluster=" + cluster + " not found.");
		}
		ServiceInfo serviceInfo = serviceStorage.getData(service.get());
		serviceInfo = ServiceUtil.selectInstances(serviceInfo, cluster, false);
		return ResponseEntity.status(HttpStatus.OK)
				.body(addressServerBuilderManager.generateResponseIps(serviceInfo.getHosts()));
	}
}
