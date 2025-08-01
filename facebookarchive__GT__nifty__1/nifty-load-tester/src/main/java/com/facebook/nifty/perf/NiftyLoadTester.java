/*
 * Copyright (C) 2012-2013 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.nifty.perf;

import com.facebook.nifty.core.NettyConfigBuilderBase;
import com.facebook.nifty.core.NiftyBootstrap;
import com.facebook.nifty.core.ThriftServerDef;
import com.facebook.nifty.core.ThriftServerDefBuilder;
import com.facebook.nifty.guice.NiftyModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.Stage;
import io.airlift.bootstrap.LifeCycleManager;
import io.airlift.bootstrap.LifeCycleModule;
import io.airlift.configuration.Config;
import io.airlift.configuration.ConfigurationFactory;
import io.airlift.configuration.ConfigurationLoader;
import io.airlift.configuration.ConfigurationModule;
import io.airlift.configuration.ValidationErrorModule;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import java.util.concurrent.Executors;

import static io.airlift.configuration.Configuration.processConfiguration;

public class NiftyLoadTester {
	public static void main(String[] args) throws Exception {
		ConfigurationFactory cf = new ConfigurationFactory(new ConfigurationLoader().loadProperties());
		AbstractModule exampleModule = new AbstractModule() {
			@Override
			public void configure() {
				ConfigurationModule.bindConfig(binder()).to(LoadTesterConfig.class);
				binder().bind(NiftyBootstrap.class).in(Singleton.class);
			}
		};
		Guice.createInjector(Stage.PRODUCTION, new ConfigurationModule(cf),
				new ValidationErrorModule(processConfiguration(cf, null, exampleModule)), new LifeCycleModule(),
				exampleModule, new NiftyModule() {
					@Override
					protected void configureNifty() {
						bind().toProvider(LoadTestServerProvider.class);
						withNettyServerConfig(LoadTesterNettyConfigProvider.class);
					}
				}).getInstance(LifeCycleManager.class).start();
	}

	private static class LoadTestServerProvider implements Provider<ThriftServerDef> {
		private final LoadTesterConfig config;
		private final LifeCycleManager lifeCycleManager;

		@Inject
		private LoadTestServerProvider(LoadTesterConfig config, LifeCycleManager lifeCycleManager) {
			this.config = config;
			this.lifeCycleManager = lifeCycleManager;
		}

		@Override
		public ThriftServerDef get() {
			ThriftServerDefBuilder builder = new ThriftServerDefBuilder().listen(config.getServerPort())
					.limitFrameSizeTo(config.getMaxFrameSize())
					.limitQueuedResponsesPerConnection(config.getQueuedResponseLimit())
					.withProcessor(new LoadTest.Processor<LoadTest.Iface>(new LoadTestHandler()));

			if (config.getUseTaskQueue()) {
				builder.using(Executors.newFixedThreadPool(config.getNumTaskThreads()));
			}

			return builder.build();
		}
	}

	public static class LoadTesterConfig {
		private int serverPort = 1234;
		private int maxFrameSize = 1048576;
		private boolean useTaskQueue = false;
		private int numTaskThreads = 8;
		private int queuedResponseLimit = 500;
		private int acceptBacklog = 1024;
		private int numBossThreads = NettyConfigBuilderBase.DEFAULT_BOSS_THREAD_COUNT;
		private int numIoThreads = NettyConfigBuilderBase.DEFAULT_WORKER_THREAD_COUNT;

		@Min(0)
		@Max(65535)
		public int getServerPort() {
			return serverPort;
		}

		@Config("serverPort")
		public void setServerPort(int serverPort) {
			this.serverPort = serverPort;
		}

		public int getMaxFrameSize() {
			return maxFrameSize;
		}

		@Config("maxFrameSize")
		public void setMaxFrameSize(int maxFrameSize) {
			this.maxFrameSize = maxFrameSize;
		}

		public boolean getUseTaskQueue() {
			return useTaskQueue;
		}

		@Config("useTaskQueue")
		public void setUseTaskQueue(boolean useTaskQueue) {
			this.useTaskQueue = useTaskQueue;
		}

		@Min(1)
		public int getNumTaskThreads() {
			return numTaskThreads;
		}

		@Config("numTaskThreads")
		public void setNumTaskThreads(int numTaskThreads) {
			this.numTaskThreads = numTaskThreads;
		}

		@Min(1)
		public int getQueuedResponseLimit() {
			return queuedResponseLimit;
		}

		@Config("queuedResponseLimit")
		public void setQueuedResponseLimit(int queuedResponseLimit) {
			this.queuedResponseLimit = queuedResponseLimit;
		}

		public int getAcceptBacklog() {
			return acceptBacklog;
		}

		@Config("acceptBacklog")
		public void setAcceptBacklog(int acceptBacklog) {
			this.acceptBacklog = acceptBacklog;
		}

		@Min(1)
		public int getNumBossThreads() {
			return numBossThreads;
		}

		@Config("numBossThreads")
		public void setNumBossThreads(int numBossThreads) {
			this.numBossThreads = numBossThreads;
		}

		@Min(1)
		public int getNumIoThreads() {
			return numIoThreads;
		}

		@Config("numIoThreads")
		public void setNumIoThreads(int numIoThreads) {
			this.numIoThreads = numIoThreads;
		}
	}
}
