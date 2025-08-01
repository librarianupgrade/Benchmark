/*
 * Copyright (C) 2016-2023 ActionTech.
 * based on code by MyCATCopyrightHolder Copyright (c) 2013, OpenCloudDB/MyCAT.
 * License: http://www.gnu.org/licenses/gpl.html GPL version 2 or higher.
 */
package com.actiontech.dble.server.handler;

import com.actiontech.dble.backend.mysql.nio.handler.transaction.savepoint.SavePointHandler;
import com.actiontech.dble.config.ErrorCode;
import com.actiontech.dble.route.parser.util.DruidUtil;
import com.actiontech.dble.services.mysqlsharding.ShardingService;
import com.alibaba.druid.sql.ast.statement.SQLReleaseSavePointStatement;
import com.alibaba.druid.sql.ast.statement.SQLRollbackStatement;
import com.alibaba.druid.sql.ast.statement.SQLSavePointStatement;

/**
 * @author collapsar
 */
public final class SavepointHandler {
	private SavepointHandler() {
	}

	public static void save(String stmt, ShardingService service) {
		try {
			SQLSavePointStatement statement = (SQLSavePointStatement) DruidUtil.parseMultiSQL(stmt);
			String spName = statement.getName().toString();
			service.performSavePoint(spName, SavePointHandler.Type.SAVE);
		} catch (Exception e) {
			service.writeErrMessage(ErrorCode.ER_PARSE_ERROR, e.toString());
		}
	}

	public static void rollback(String stmt, ShardingService service) {
		try {
			SQLRollbackStatement statement = (SQLRollbackStatement) DruidUtil.parseMultiSQL(stmt);
			String spName = statement.getTo().toString();
			service.performSavePoint(spName, SavePointHandler.Type.ROLLBACK);
		} catch (Exception e) {
			service.writeErrMessage(ErrorCode.ER_PARSE_ERROR, e.toString());
		}
	}

	public static void release(String stmt, ShardingService service) {
		try {
			SQLReleaseSavePointStatement statement = (SQLReleaseSavePointStatement) DruidUtil.parseMultiSQL(stmt);
			String spName = statement.getName().toString();
			service.performSavePoint(spName, SavePointHandler.Type.RELEASE);
		} catch (Exception e) {
			service.writeErrMessage(ErrorCode.ER_PARSE_ERROR, e.toString());
		}
	}

}
