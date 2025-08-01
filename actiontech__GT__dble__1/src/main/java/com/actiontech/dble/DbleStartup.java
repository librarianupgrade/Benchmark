/*
 * Copyright (C) 2016-2023 ActionTech.
 * based on code by MyCATCopyrightHolder Copyright (c) 2013, OpenCloudDB/MyCAT.
 * License: http://www.gnu.org/licenses/gpl.html GPL version 2 or higher.
 */
package com.actiontech.dble;

import com.actiontech.dble.cluster.ClusterController;
import com.actiontech.dble.config.Versions;
import com.actiontech.dble.config.loader.SystemConfigLoader;
import com.actiontech.dble.config.util.StartProblemReporter;
import com.actiontech.dble.singleton.CustomMySQLHa;
import com.actiontech.dble.singleton.OnlineStatus;
import com.actiontech.dble.util.CheckConfigurationUtil;
import com.alibaba.druid.sql.SQLUtils;

public final class DbleStartup {
	static {
		SQLUtils.DEFAULT_FORMAT_OPTION.setPrettyFormat(false);
	}

	private DbleStartup() {
	}

	public static void main(String[] args) {
		try {
			CheckConfigurationUtil.checkConfiguration();
			ClusterController.loadClusterProperties();
			// load system properties
			SystemConfigLoader.initSystemConfig();
			// load system other properties
			SystemConfigLoader.verifyOtherParam();
			if (StartProblemReporter.getInstance().getErrorConfigs().size() > 0) {
				for (String errInfo : StartProblemReporter.getInstance().getErrorConfigs()) {
					System.out.println(errInfo);
				}
				System.exit(-1);
			}
			if (!ClusterController.tryServerStartDuringInitClusterData()) {
				initClusterAndServerStart();
			}
			System.out.println("Server startup successfully. dble version is ["
					+ new String(Versions.getServerVersion()) + "]. Please see logs in logs/dble.log");
		} catch (Throwable e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	 * may not public ,now no better solution
	 *
	 * @throws Exception
	 */
	public static void initClusterAndServerStart() throws Exception {
		ClusterController.init();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("Server execute ShutdownHook.");
			OnlineStatus.getInstance().shutdownClear();
			CustomMySQLHa.getInstance().stop(true);
		}));
		// startup
		DbleServer.getInstance().startup();
	}
}
