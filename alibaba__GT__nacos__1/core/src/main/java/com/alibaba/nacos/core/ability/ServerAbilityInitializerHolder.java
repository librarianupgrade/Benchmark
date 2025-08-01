/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.core.ability;

import com.alibaba.nacos.common.spi.NacosServiceLoader;
import com.alibaba.nacos.core.utils.Loggers;

import java.util.Collection;
import java.util.HashSet;

/**
 * Nacos server ability initializer holder.
 *
 * @author xiweng.yy
 */
public class ServerAbilityInitializerHolder {

	private static final ServerAbilityInitializerHolder INSTANCE = new ServerAbilityInitializerHolder();

	private final Collection<ServerAbilityInitializer> initializers;

	private ServerAbilityInitializerHolder() {
		initializers = new HashSet<>();
		for (ServerAbilityInitializer each : NacosServiceLoader.load(ServerAbilityInitializer.class)) {
			Loggers.CORE.info("Load {} for ServerAbilityInitializer", each.getClass().getCanonicalName());
			initializers.add(each);
		}
	}

	public static ServerAbilityInitializerHolder getInstance() {
		return INSTANCE;
	}

	public Collection<ServerAbilityInitializer> getInitializers() {
		return initializers;
	}
}
