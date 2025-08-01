/*
 * Copyright (C) 2016-2023 ActionTech.
 * License: http://www.gnu.org/licenses/gpl.html GPL version 2 or higher.
 */

package com.actiontech.dble.route.handler;

import com.actiontech.dble.DbleServer;
import com.actiontech.dble.backend.datasource.ShardingNode;
import com.actiontech.dble.route.RouteResultset;
import com.actiontech.dble.route.util.RouterUtil;
import com.actiontech.dble.server.parser.ServerParse;
import com.actiontech.dble.services.mysqlsharding.ShardingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLNonTransientException;

/**
 * HintShardingNodeHandler
 *
 * @author zhuam
 */
public final class HintShardingNodeHandler {

	private HintShardingNodeHandler() {
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(HintShardingNodeHandler.class);

	public static RouteResultset route(String hintSQLValue, int sqlType, String realSQL, ShardingService service)
			throws SQLNonTransientException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("route shardingNode sql hint from " + realSQL);
		}

		RouteResultset rrs = new RouteResultset(realSQL, sqlType);
		if (ServerParse.CALL == sqlType) {
			rrs.setCallStatement(true);
		}
		ShardingNode shardingNode = DbleServer.getInstance().getConfig().getShardingNodes().get(hintSQLValue);
		if (shardingNode != null) {
			rrs = RouterUtil.routeToSingleNode(rrs, shardingNode.getName(), null);
		} else {
			String msg = "can't find hint sharding node:" + hintSQLValue;
			LOGGER.info(msg);
			throw new SQLNonTransientException(msg);
		}

		service.getSession2().endParse();
		return rrs;
	}

}
