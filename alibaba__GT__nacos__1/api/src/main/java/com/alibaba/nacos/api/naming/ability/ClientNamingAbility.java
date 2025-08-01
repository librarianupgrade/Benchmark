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

package com.alibaba.nacos.api.naming.ability;

import java.io.Serializable;

/**
 * naming abilities of nacos client.
 *
 * @author liuzunfei
 * @version $Id: ClientNamingAbility.java, v 0.1 2021年01月24日 00:09 AM liuzunfei Exp $
 */
public class ClientNamingAbility implements Serializable {

	private static final long serialVersionUID = 7643941846828882862L;

	private boolean supportDeltaPush;

	private boolean supportRemoteMetric;

	public boolean isSupportDeltaPush() {
		return supportDeltaPush;
	}

	public void setSupportDeltaPush(boolean supportDeltaPush) {
		this.supportDeltaPush = supportDeltaPush;
	}

	public boolean isSupportRemoteMetric() {
		return supportRemoteMetric;
	}

	public void setSupportRemoteMetric(boolean supportRemoteMetric) {
		this.supportRemoteMetric = supportRemoteMetric;
	}
}
