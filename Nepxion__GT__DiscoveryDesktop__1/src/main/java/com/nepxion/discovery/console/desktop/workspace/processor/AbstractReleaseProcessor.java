package com.nepxion.discovery.console.desktop.workspace.processor;

/**
 * <p>Title: Nepxion Discovery</p>
 * <p>Description: Nepxion Discovery</p>
 * <p>Copyright: Copyright (c) 2017-2050</p>
 * <p>Company: Nepxion</p>
 * @author Haojun Ren
 * @version 1.0
 */

import com.nepxion.discovery.common.entity.FormatType;
import com.nepxion.discovery.common.entity.RuleEntity;
import com.nepxion.discovery.console.controller.ConsoleController;

public abstract class AbstractReleaseProcessor implements ReleaseProcessor {
	@Override
	public RuleEntity parseConfig(String config) {
		return ReleaseProcessorFactory.getXmlConfigParser().parse(config);
	}

	@Override
	public String deparseConfig(RuleEntity ruleEntity) {
		return ReleaseProcessorFactory.getXmlConfigDeparser().deparse(ruleEntity);
	}

	@Override
	public String getConfig(String group, String serviceId) {
		return ConsoleController.remoteConfigView(group, serviceId);
	}

	@Override
	public String saveConfig(String group, String serviceId, String config) {
		return ConsoleController.remoteConfigUpdate(group, serviceId, config, FormatType.XML_FORMAT);
	}
}