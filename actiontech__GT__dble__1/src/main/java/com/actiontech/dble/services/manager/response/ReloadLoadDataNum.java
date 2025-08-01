/*
 * Copyright (C) 2016-2023 ActionTech.
 * License: http://www.gnu.org/licenses/gpl.html GPL version 2 or higher.
 */

package com.actiontech.dble.services.manager.response;

import com.actiontech.dble.config.ErrorCode;
import com.actiontech.dble.net.mysql.OkPacket;
import com.actiontech.dble.server.status.LoadDataBatch;
import com.actiontech.dble.services.manager.ManagerService;
import com.actiontech.dble.services.manager.handler.WriteDynamicBootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public final class ReloadLoadDataNum {
	private ReloadLoadDataNum() {
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(ReloadLoadDataNum.class);

	public static void execute(ManagerService service, int num) {
		if (num < 1) {
			service.writeErrMessage(ErrorCode.ER_UNKNOWN_ERROR,
					"must be of numeric type and the value must be greater than 0");
			return;
		}
		try {
			WriteDynamicBootstrap.getInstance().changeValue("maxRowSizeToFile", String.valueOf(num));
		} catch (IOException e) {
			String msg = " reload @@load_data.num failed";
			LOGGER.warn(service + " " + msg, e);
			service.writeErrMessage(ErrorCode.ER_YES, msg);
			return;
		}
		LoadDataBatch.getInstance().setSize(num);
		LOGGER.info(service + " reload @@load_data.num=" + num + " success by manager");

		OkPacket ok = new OkPacket();
		ok.setPacketId(1);
		ok.setAffectedRows(1);
		ok.setServerStatus(2);
		ok.setMessage("reload @@load_data.num success".getBytes());
		ok.write(service.getConnection());
	}

}
