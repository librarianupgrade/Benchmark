/**
 * Copyright (c) 2015-2017, Michael Yang 杨福海 (fuhai999@gmail.com).
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
package io.jboot.db.datasource;

import com.google.common.collect.Maps;
import io.jboot.Jboot;
import io.jboot.config.JbootConfigManager;
import io.jboot.utils.StringUtils;

import java.util.*;

public class DatasourceConfigManager {

	private static DatasourceConfigManager manager = new DatasourceConfigManager();

	public static DatasourceConfigManager me() {
		return manager;
	}

	private Map<String, DatasourceConfig> datasourceConfigs = Maps.newHashMap();
	private Map<String, DatasourceConfig> shardingDatasourceConfigs = Maps.newHashMap();

	private DatasourceConfigManager() {

		DatasourceConfig datasourceConfig = Jboot.config(DatasourceConfig.class, "jboot.datasource");
		datasourceConfig.setName(DatasourceConfig.NAME_MAIN);
		if (datasourceConfig.isConfigOk()) {
			datasourceConfigs.put(datasourceConfig.getName(), datasourceConfig);
		}
		if (datasourceConfig.isShardingEnable()) {
			shardingDatasourceConfigs.put(datasourceConfig.getName(), datasourceConfig);
		}

		Properties prop = JbootConfigManager.me().getProperties();
		Set<String> datasourceNames = new HashSet<>();
		for (Map.Entry<Object, Object> entry : prop.entrySet()) {
			String key = entry.getKey().toString();
			if (key.startsWith("jboot.datasource.") && entry.getValue() != null) {
				String[] keySplits = key.split("\\.");
				if (keySplits.length == 4) {
					datasourceNames.add(keySplits[2]);
				}
			}
		}

		for (String name : datasourceNames) {
			DatasourceConfig dsc = Jboot.config(DatasourceConfig.class, "jboot.datasource." + name);
			if (StringUtils.isBlank(dsc.getName())) {
				dsc.setName(name);
			}
			if (dsc.isConfigOk()) {
				datasourceConfigs.put(name, dsc);
			}
			if (dsc.isShardingEnable()) {
				shardingDatasourceConfigs.put(name, dsc);
			}
		}
	}

	public Map<String, DatasourceConfig> getDatasourceConfigs() {
		return datasourceConfigs;
	}

	public Map<String, DatasourceConfig> getShardingDatasourceConfigs() {
		return shardingDatasourceConfigs;
	}

}
