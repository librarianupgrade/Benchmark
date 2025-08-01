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
package org.jclouds.loadbalancer.internal;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jclouds.Context;
import org.jclouds.internal.BaseView;
import org.jclouds.loadbalancer.LoadBalancerService;
import org.jclouds.loadbalancer.LoadBalancerServiceContext;
import org.jclouds.location.Provider;
import org.jclouds.rest.Utils;
import org.jclouds.util.Closeables2;

import com.google.common.reflect.TypeToken;

@Singleton
public class LoadBalancerServiceContextImpl extends BaseView implements LoadBalancerServiceContext {
	private final LoadBalancerService loadBalancerService;
	private final Utils utils;

	@Inject
	public LoadBalancerServiceContextImpl(@Provider Context backend, @Provider TypeToken<? extends Context> backendType,
			LoadBalancerService loadBalancerService, Utils utils) {
		super(backend, backendType);
		this.utils = utils;
		this.loadBalancerService = checkNotNull(loadBalancerService, "loadBalancerService");
	}

	@Override
	public LoadBalancerService getLoadBalancerService() {
		return loadBalancerService;
	}

	@Override
	public Utils utils() {
		return utils;
	}

	@Override
	public void close() {
		Closeables2.closeQuietly(delegate());
	}

	public int hashCode() {
		return delegate().hashCode();
	}

	@Override
	public String toString() {
		return delegate().toString();
	}

	@Override
	public boolean equals(Object obj) {
		return delegate().equals(obj);
	}

}
