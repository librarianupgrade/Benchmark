/*
 * Copyright 2013-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.nacos.registry;

import java.util.Collections;
import java.util.List;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;
import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.alibaba.nacos.api.selector.AbstractSelector;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class MockNamingService implements NamingService {

	@Override
	public void registerInstance(String serviceName, String ip, int port) throws NacosException {

	}

	@Override
	public void registerInstance(String serviceName, String groupName, String ip, int port) throws NacosException {

	}

	@Override
	public void registerInstance(String serviceName, String ip, int port, String clusterName) throws NacosException {

	}

	@Override
	public void registerInstance(String serviceName, String groupName, String ip, int port, String clusterName)
			throws NacosException {

	}

	@Override
	public void registerInstance(String serviceName, Instance instance) throws NacosException {

	}

	@Override
	public void registerInstance(String serviceName, String groupName, Instance instance) throws NacosException {

	}

	@Override
	public void batchRegisterInstance(String serviceName, String groupName, List<Instance> instances)
			throws NacosException {

	}

	@Override
	public void batchDeregisterInstance(String serviceName, String groupName, List<Instance> instances)
			throws NacosException {

	}

	@Override
	public void deregisterInstance(String serviceName, String ip, int port) throws NacosException {

	}

	@Override
	public void deregisterInstance(String serviceName, String groupName, String ip, int port) throws NacosException {

	}

	@Override
	public void deregisterInstance(String serviceName, String ip, int port, String clusterName) throws NacosException {

	}

	@Override
	public void deregisterInstance(String serviceName, String groupName, String ip, int port, String clusterName)
			throws NacosException {

	}

	@Override
	public void deregisterInstance(String serviceName, Instance instance) throws NacosException {

	}

	@Override
	public void deregisterInstance(String serviceName, String groupName, Instance instance) throws NacosException {

	}

	@Override
	public List<Instance> getAllInstances(String serviceName) throws NacosException {
		return Collections.emptyList();
	}

	@Override
	public List<Instance> getAllInstances(String serviceName, String groupName) throws NacosException {
		return Collections.emptyList();
	}

	@Override
	public List<Instance> getAllInstances(String serviceName, boolean subscribe) throws NacosException {
		return Collections.emptyList();
	}

	@Override
	public List<Instance> getAllInstances(String serviceName, String groupName, boolean subscribe)
			throws NacosException {
		return Collections.emptyList();
	}

	@Override
	public List<Instance> getAllInstances(String serviceName, List<String> clusters) throws NacosException {
		return Collections.emptyList();
	}

	@Override
	public List<Instance> getAllInstances(String serviceName, String groupName, List<String> clusters)
			throws NacosException {
		return Collections.emptyList();
	}

	@Override
	public List<Instance> getAllInstances(String serviceName, List<String> clusters, boolean subscribe)
			throws NacosException {
		return Collections.emptyList();
	}

	@Override
	public List<Instance> getAllInstances(String serviceName, String groupName, List<String> clusters,
			boolean subscribe) throws NacosException {
		return Collections.emptyList();
	}

	@Override
	public List<Instance> selectInstances(String serviceName, boolean healthy) throws NacosException {
		return Collections.emptyList();
	}

	@Override
	public List<Instance> selectInstances(String serviceName, String groupName, boolean healthy) throws NacosException {
		return Collections.emptyList();
	}

	@Override
	public List<Instance> selectInstances(String serviceName, boolean healthy, boolean subscribe)
			throws NacosException {
		return Collections.emptyList();
	}

	@Override
	public List<Instance> selectInstances(String serviceName, String groupName, boolean healthy, boolean subscribe)
			throws NacosException {
		return Collections.emptyList();
	}

	@Override
	public List<Instance> selectInstances(String serviceName, List<String> clusters, boolean healthy)
			throws NacosException {
		return Collections.emptyList();
	}

	@Override
	public List<Instance> selectInstances(String serviceName, String groupName, List<String> clusters, boolean healthy)
			throws NacosException {
		return Collections.emptyList();
	}

	@Override
	public List<Instance> selectInstances(String serviceName, List<String> clusters, boolean healthy, boolean subscribe)
			throws NacosException {
		return Collections.emptyList();
	}

	@Override
	public List<Instance> selectInstances(String serviceName, String groupName, List<String> clusters, boolean healthy,
			boolean subscribe) throws NacosException {
		return Collections.emptyList();
	}

	@Override
	public Instance selectOneHealthyInstance(String serviceName) throws NacosException {
		return null;
	}

	@Override
	public Instance selectOneHealthyInstance(String serviceName, String groupName) throws NacosException {
		return null;
	}

	@Override
	public Instance selectOneHealthyInstance(String serviceName, boolean subscribe) throws NacosException {
		return null;
	}

	@Override
	public Instance selectOneHealthyInstance(String serviceName, String groupName, boolean subscribe)
			throws NacosException {
		return null;
	}

	@Override
	public Instance selectOneHealthyInstance(String serviceName, List<String> clusters) throws NacosException {
		return null;
	}

	@Override
	public Instance selectOneHealthyInstance(String serviceName, String groupName, List<String> clusters)
			throws NacosException {
		return null;
	}

	@Override
	public Instance selectOneHealthyInstance(String serviceName, List<String> clusters, boolean subscribe)
			throws NacosException {
		return null;
	}

	@Override
	public Instance selectOneHealthyInstance(String serviceName, String groupName, List<String> clusters,
			boolean subscribe) throws NacosException {
		return null;
	}

	@Override
	public void subscribe(String serviceName, EventListener listener) throws NacosException {

	}

	@Override
	public void subscribe(String serviceName, String groupName, EventListener listener) throws NacosException {

	}

	@Override
	public void subscribe(String serviceName, List<String> clusters, EventListener listener) throws NacosException {

	}

	@Override
	public void subscribe(String serviceName, String groupName, List<String> clusters, EventListener listener)
			throws NacosException {

	}

	@Override
	public void unsubscribe(String serviceName, EventListener listener) throws NacosException {

	}

	@Override
	public void unsubscribe(String serviceName, String groupName, EventListener listener) throws NacosException {

	}

	@Override
	public void unsubscribe(String serviceName, List<String> clusters, EventListener listener) throws NacosException {

	}

	@Override
	public void unsubscribe(String serviceName, String groupName, List<String> clusters, EventListener listener)
			throws NacosException {

	}

	@Override
	public ListView<String> getServicesOfServer(int pageNo, int pageSize) throws NacosException {
		return this.emptyListView();
	}

	@Override
	public ListView<String> getServicesOfServer(int pageNo, int pageSize, String groupName) throws NacosException {
		return this.emptyListView();
	}

	@Override
	public ListView<String> getServicesOfServer(int pageNo, int pageSize, AbstractSelector selector)
			throws NacosException {
		return this.emptyListView();
	}

	@Override
	public ListView<String> getServicesOfServer(int pageNo, int pageSize, String groupName, AbstractSelector selector)
			throws NacosException {
		return this.emptyListView();
	}

	@Override
	public List<ServiceInfo> getSubscribeServices() throws NacosException {
		return Collections.emptyList();
	}

	@Override
	public String getServerStatus() {
		return null;
	}

	@Override
	public void shutDown() throws NacosException {

	}

	private ListView<String> emptyListView() {
		ListView<String> emptyListView = new ListView<>();
		emptyListView.setCount(0);
		emptyListView.setData(Collections.emptyList());
		return emptyListView;
	}
}
