/*
 * Copyright (C) 2016-2023 ActionTech.
 * License: http://www.gnu.org/licenses/gpl.html GPL version 2 or higher.
 */

package com.actiontech.dble.config.model.user;

import com.alibaba.druid.wall.WallProvider;

public class SingleDbGroupUserConfig extends ServerUserConfig {
	protected final String dbGroup;

	SingleDbGroupUserConfig(UserConfig user, String tenant, WallProvider blacklist, String dbGroup) {
		super(user, tenant, blacklist);
		this.dbGroup = dbGroup;
	}

	public String getDbGroup() {
		return dbGroup;
	}
}
