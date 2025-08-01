package com.nepxion.discovery.console.desktop;

/**
 * <p>Title: Nepxion Discovery</p>
 * <p>Description: Nepxion Discovery</p>
 * <p>Copyright: Copyright (c) 2017-2050</p>
 * <p>Company: Nepxion</p>
 * @author Haojun Ren
 * @version 1.0
 */

import java.awt.Dimension;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nepxion.discovery.console.cache.ConsoleCache;
import com.nepxion.discovery.console.desktop.common.context.ConsoleConstant;
import com.nepxion.discovery.console.desktop.common.icon.ConsoleIconFactory;
import com.nepxion.discovery.console.desktop.common.locale.ConsoleLocaleFactory;
import com.nepxion.swing.frame.JBasicFrame;

public class ConsoleFrame extends JBasicFrame {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(ConsoleFrame.class);

	public ConsoleFrame() {
		super(ConsoleLocaleFactory.getString("title") + " " + getSubTitle(),
				ConsoleIconFactory.getContextIcon("logo.png"), new Dimension(1630, 1030));
	}

	public void launch() {
		ConsoleHierarchy consoleHierarchy = new ConsoleHierarchy();
		getContentPane().add(consoleHierarchy);

		Boolean fullscreenEnabled = Boolean
				.valueOf(System.getProperty(ConsoleConstant.FULLSCREEN_ENABLED, Boolean.TRUE.toString()));
		if (fullscreenEnabled) {
			setExtendedState(ConsoleFrame.MAXIMIZED_BOTH);
		}
		setVisible(true);
		toFront();
	}

	private static String getSubTitle() {
		try {
			return "【" + ConsoleCache.getDiscoveryType() + " " + ConsoleLocaleFactory.getString("discovery_center")
					+ "】【" + ConsoleCache.getConfigType() + " " + ConsoleLocaleFactory.getString("config_center") + "】";
		} catch (Exception e) {
			LOG.error("Not connnect to Discovery Console", e);

			return "【" + ConsoleLocaleFactory.getString("not_connnect_to_console") + "】";
		}
	}
}