/**
 * Copyright (c) 2015-2020, Michael Yang 杨福海 (fuhai999@gmail.com).
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

import com.alibaba.druid.pool.DruidDataSource;
import com.google.common.collect.Sets;
import com.jfinal.log.Log;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * @author Michael Yang 杨福海 （fuhai999@gmail.com）
 * @version V1.0
 */
public class DruidDataSourceFactory implements DataSourceFactory {

	static Log log = Log.getLog(DruidDataSourceFactory.class);

	@Override
	public DataSource createDataSource(DataSourceConfig config) {

		DruidDataSource druidDataSource = new DruidDataSource();
		druidDataSource.setUrl(config.getUrl());
		druidDataSource.setUsername(config.getUser());
		druidDataSource.setPassword(config.getPassword());
		druidDataSource.setDriverClassName(config.getDriverClassName());
		druidDataSource.setMaxActive(config.getMaximumPoolSize());
		druidDataSource.setMaxWait(config.getMaxWait());
		druidDataSource.setTimeBetweenEvictionRunsMillis(config.getTimeBetweenEvictionRunsMillis());
		druidDataSource.setMinEvictableIdleTimeMillis(config.getMinEvictableIdleTimeMillis());
		druidDataSource.setTimeBetweenConnectErrorMillis(config.getTimeBetweenConnectErrorMillis());
		druidDataSource.setValidationQuery(config.getValidationQuery());
		druidDataSource.setTestWhileIdle(config.isTestWhileIdle());
		druidDataSource.setTestOnBorrow(config.isTestOnBorrow());
		druidDataSource.setTestOnReturn(config.isTestOnReturn());
		if (config.getMinimumIdle() != null) {
			druidDataSource.setMinIdle(config.getMinimumIdle());
		}

		if (config.getConnectionInitSql() != null) {
			druidDataSource.setConnectionInitSqls(Sets.newHashSet(config.getConnectionInitSql()));
		}

		try {
			druidDataSource.setFilters("stat");
		} catch (SQLException e) {
			log.error("DruidDataSourceFactory is error", e);
		}

		return druidDataSource;
	}
}
