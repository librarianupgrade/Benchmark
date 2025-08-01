/*
 * Copyright (C) 2016-2023 ActionTech.
 * based on code by MyCATCopyrightHolder Copyright (c) 2013, OpenCloudDB/MyCAT.
 * License: http://www.gnu.org/licenses/gpl.html GPL version 2 or higher.
 */
package com.actiontech.dble.net.mysql;

import com.actiontech.dble.net.service.AbstractService;

/**
 * @author mycat
 */
public class PingPacket extends MySQLPacket {
	public static final byte[] PING = new byte[] { 1, 0, 0, 0, 14 };

	@Override
	public int calcPacketSize() {
		return 1;
	}

	@Override
	protected String getPacketInfo() {
		return "MySQL Ping Packet";
	}

	public static void response(AbstractService service) {
		service.write(OkPacket.getDefault());
	}

	@Override
	public boolean isEndOfQuery() {
		return true;
	}
}
