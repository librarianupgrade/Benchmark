/**
 * Copyright (c) 2015-2017, Michael Yang 杨福海 (fuhai999@gmail.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *  http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jboot.codegen;

import com.jfinal.plugin.activerecord.dialect.*;
import com.jfinal.plugin.activerecord.generator.MetaBuilder;
import com.jfinal.plugin.activerecord.generator.TableMeta;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.jboot.Jboot;
import io.jboot.db.datasource.DatasourceConfig;
import io.jboot.exception.JbootException;
import io.jboot.utils.StringUtils;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 代码生成工具类
 */
public class CodeGenHelpler {

	/**
	 * 获取数据源
	 *
	 * @return
	 */
	public static DataSource getDatasource() {
		DatasourceConfig datasourceConfig = Jboot.config(DatasourceConfig.class, "jboot.datasource");
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(datasourceConfig.getUrl());
		config.setUsername(datasourceConfig.getUser());
		config.setPassword(datasourceConfig.getPassword());
		config.setDriverClassName(datasourceConfig.getDriverClassName());

		return new HikariDataSource(config);
	}

	public static MetaBuilder createMetaBuilder() {
		MetaBuilder metaBuilder = new MetaBuilder(getDatasource());
		DatasourceConfig datasourceConfig = Jboot.config(DatasourceConfig.class, "jboot.datasource");
		switch (datasourceConfig.getType()) {
		case DatasourceConfig.TYPE_MYSQL:
			metaBuilder.setDialect(new MysqlDialect());
			break;
		case DatasourceConfig.TYPE_ORACLE:
			metaBuilder.setDialect(new OracleDialect());
			break;
		case DatasourceConfig.TYPE_SQLSERVER:
			metaBuilder.setDialect(new SqlServerDialect());
			break;
		case DatasourceConfig.TYPE_SQLITE:
			metaBuilder.setDialect(new Sqlite3Dialect());
			break;
		case DatasourceConfig.TYPE_ANSISQL:
			metaBuilder.setDialect(new AnsiSqlDialect());
			break;
		case DatasourceConfig.TYPE_POSTGRESQL:
			metaBuilder.setDialect(new PostgreSqlDialect());
			break;
		default:
			throw new JbootException("only support datasource type : mysql，orcale，sqlserver，sqlite，ansisql，postgresql");
		}

		return metaBuilder;

	}

	/**
	 * 排除指定的表，有些表不需要生成的
	 *
	 * @param list
	 * @param excludeTables
	 */
	public static void excludeTables(List<TableMeta> list, String excludeTables) {
		if (StringUtils.isNotBlank(excludeTables)) {
			List<TableMeta> newTableMetaList = new ArrayList<>();
			Set<String> excludeTableSet = StringUtils.splitToSet(excludeTables.toLowerCase(), ",");
			for (TableMeta tableMeta : list) {
				if (excludeTableSet.contains(tableMeta.name.toLowerCase())) {
					System.out.println("exclude table : " + tableMeta.name);
					continue;
				}
				newTableMetaList.add(tableMeta);
			}
			list.clear();
			list.addAll(newTableMetaList);
		}
	}

}
