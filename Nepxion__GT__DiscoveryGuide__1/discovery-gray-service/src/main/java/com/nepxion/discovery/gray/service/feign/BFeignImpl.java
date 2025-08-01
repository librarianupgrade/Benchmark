package com.nepxion.discovery.gray.service.feign;

/**
 * <p>Title: Nepxion Discovery</p>
 * <p>Description: Nepxion Discovery</p>
 * <p>Copyright: Copyright (c) 2017-2050</p>
 * <p>Company: Nepxion</p>
 * @author Haojun Ren
 * @version 1.0
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.nepxion.discovery.common.constant.DiscoveryConstant;

@RestController
@ConditionalOnProperty(name = DiscoveryConstant.SPRING_APPLICATION_NAME, havingValue = "discovery-gray-service-b")
public class BFeignImpl extends AbstractFeignImpl implements BFeign {
	private static final Logger LOG = LoggerFactory.getLogger(BFeignImpl.class);

	@Override
	public String invoke(@PathVariable(value = "value") String value) {
		value = doInvoke(value);

		LOG.info("调用路径：{}", value);

		return value;
	}
}