/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.config.ability;

import java.io.Serializable;
import java.util.Objects;

/**
 * config abilities of nacos server.
 *
 * @author liuzunfei
 * @version $Id: ServerConfigAbility.java, v 0.1 2021年01月24日 00:09 AM liuzunfei Exp $
 */
public class ServerConfigAbility implements Serializable {

	private static final long serialVersionUID = -4976152499731684230L;

	/**
	 * support remote metrics get.
	 */
	private boolean supportRemoteMetrics;

	public boolean isSupportRemoteMetrics() {
		return supportRemoteMetrics;
	}

	public void setSupportRemoteMetrics(boolean supportRemoteMetrics) {
		this.supportRemoteMetrics = supportRemoteMetrics;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ServerConfigAbility that = (ServerConfigAbility) o;
		return supportRemoteMetrics == that.supportRemoteMetrics;
	}

	@Override
	public int hashCode() {
		return Objects.hash(supportRemoteMetrics);
	}
}
