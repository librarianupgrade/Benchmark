/*
 * Copyright (C) 2016-2023 ActionTech.
 * License: http://www.gnu.org/licenses/gpl.html GPL version 2 or higher.
 */

package com.actiontech.dble.route.parser.druid.impl.ddl;

import com.actiontech.dble.DbleServer;
import com.actiontech.dble.config.model.sharding.SchemaConfig;
import com.actiontech.dble.route.RouteResultset;
import com.actiontech.dble.route.parser.druid.ServerSchemaStatVisitor;
import com.actiontech.dble.route.parser.druid.impl.DruidImplicitCommitParser;
import com.actiontech.dble.services.mysqlsharding.ShardingService;
import com.actiontech.dble.util.StringUtil;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLCreateDatabaseStatement;

import java.sql.SQLException;
import java.sql.SQLNonTransientException;

public class DruidCreateDatabaseParser extends DruidImplicitCommitParser {

	public SchemaConfig doVisitorParse(SchemaConfig schema, RouteResultset rrs, SQLStatement stmt,
			ServerSchemaStatVisitor visitor, ShardingService service, boolean isExplain) throws SQLException {
		SQLCreateDatabaseStatement statement = (SQLCreateDatabaseStatement) stmt;
		String createSchema = StringUtil.removeBackQuote(statement.getName().getSimpleName());
		SchemaConfig sc = DbleServer.getInstance().getConfig().getSchemas().get(createSchema);
		if (sc != null) {
			if (!sc.isLogicalCreateADrop()) {
				String msg = "THE DDL is not supported :" + statement;
				throw new SQLNonTransientException(msg);
			}
			rrs.setFinishedExecute(true);
		} else {
			throw new SQLException("Can't create database '" + createSchema + "' that doesn't exists in config");
		}
		return schema;
	}

}
