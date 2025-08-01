/*
 * Copyright (C) 2016-2023 ActionTech.
 * License: http://www.gnu.org/licenses/gpl.html GPL version 2 or higher.
 */

package com.actiontech.dble.backend.heartbeat;

public enum MySQLHeartbeatStatus {
	INIT(), OK(), ERROR(), TIMEOUT(), STOP();

	@Override
	public String toString() {
		return super.toString().toLowerCase();
	}

}
