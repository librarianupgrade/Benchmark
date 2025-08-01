/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.naming.healthcheck.heartbeat;

import com.alibaba.nacos.common.utils.ConvertUtils;
import com.alibaba.nacos.naming.core.v2.metadata.InstanceMetadata;
import com.alibaba.nacos.naming.core.v2.metadata.NamingMetadataManager;
import com.alibaba.nacos.naming.core.v2.pojo.HealthCheckInstancePublishInfo;
import com.alibaba.nacos.naming.misc.UtilsAndCommons;
import com.alibaba.nacos.sys.utils.ApplicationUtils;

import java.util.Optional;

/**
 * Instance enable beat check interceptor.
 *
 * @author xiweng.yy
 */
public class InstanceEnableBeatCheckInterceptor extends AbstractBeatCheckInterceptor {

	@Override
	public boolean intercept(InstanceBeatCheckTask object) {
		NamingMetadataManager metadataManager = ApplicationUtils.getBean(NamingMetadataManager.class);
		HealthCheckInstancePublishInfo instance = object.getInstancePublishInfo();
		Optional<InstanceMetadata> metadata = metadataManager.getInstanceMetadata(object.getService(),
				instance.getMetadataId());
		if (metadata.isPresent() && metadata.get().getExtendData().containsKey(UtilsAndCommons.ENABLE_CLIENT_BEAT)) {
			return ConvertUtils
					.toBoolean(metadata.get().getExtendData().get(UtilsAndCommons.ENABLE_CLIENT_BEAT).toString());
		}
		if (instance.getExtendDatum().containsKey(UtilsAndCommons.ENABLE_CLIENT_BEAT)) {
			return ConvertUtils.toBoolean(instance.getExtendDatum().get(UtilsAndCommons.ENABLE_CLIENT_BEAT).toString());
		}
		return false;
	}

	@Override
	public int order() {
		return Integer.MIN_VALUE + 1;
	}
}
