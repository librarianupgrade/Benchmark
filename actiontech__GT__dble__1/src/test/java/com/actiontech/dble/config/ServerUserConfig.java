/*
 * Copyright (C) 2016-2023 ActionTech.
 * License: http://www.gnu.org/licenses/gpl.html GPL version 2 or higher.
 */

package com.actiontech.dble.config;

import com.alibaba.druid.wall.WallProvider;

/**
 * requires attention:
 * New fields need to consider equals and copyBaseInfo methods
 * http://10.186.18.11/jira/browse/DBLE0REQ-1793?focusedCommentId=99601&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment-99601
 */
public abstract class ServerUserConfig extends UserConfig {
	private String tenant;
	private WallProvider blacklist;

}
