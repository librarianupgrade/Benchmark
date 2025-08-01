/*
 * Copyright (C) 2016-2023 ActionTech.
 * License: http://www.gnu.org/licenses/gpl.html GPL version 2 or higher.
 */

package com.actiontech.dble.net;

public interface SocketConnector {

	void start();

	void postConnect(com.actiontech.dble.net.connection.AbstractConnection c);
}
