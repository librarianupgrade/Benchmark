/**
 * Copyright (c) 2015-2021, Michael Yang 杨福海 (fuhai999@gmail.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *  http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jboot.components.cache.ehredis;

import java.io.Serializable;

public class JbootEhredisMessage implements Serializable {

	public static final int ACTION_PUT = 1;
	public static final int ACTION_REMOVE = 2;
	public static final int ACTION_REMOVE_ALL = 3;

	private String clientId;
	private int action;
	private String cacheName;
	private Object key;

	public JbootEhredisMessage() {

	}

	public JbootEhredisMessage(String clientId, int action, String cacheName, Object key) {
		this.clientId = clientId;
		this.action = action;
		this.cacheName = cacheName;
		this.key = key;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public int getAction() {
		return action;
	}

	public void setAction(int action) {
		this.action = action;
	}

	public String getCacheName() {
		return cacheName;
	}

	public void setCacheName(String cacheName) {
		this.cacheName = cacheName;
	}

	public Object getKey() {
		return key;
	}

	public void setKey(Object key) {
		this.key = key;
	}

}
