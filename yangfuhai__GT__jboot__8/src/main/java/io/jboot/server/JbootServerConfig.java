/**
 * Copyright (c) 2015-2017, Michael Yang 杨福海 (fuhai999@gmail.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jboot.server;

import io.jboot.config.annotation.PropertieConfig;

@PropertieConfig(prefix = "jboot.server")
public class JbootServerConfig {

	public static final String TYPE_UNDERTOW = "undertow";
	public static final String TYPE_TOMCAT = "tomcat";
	public static final String TYPE_JETTY = "jetty";

	private String type = TYPE_UNDERTOW;
	private String host = "0.0.0.0";
	private int port = 8080;
	private String contextPath = "/";

	//websocket 的相关配置
	//具体使用请参考：https://github.com/undertow-io/undertow/tree/master/examples/src/main/java/io/undertow/examples/jsrwebsockets
	private boolean websocketEnable = false;
	private int websocketBufferPoolSize = 100;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getContextPath() {
		return contextPath;
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	public boolean isWebsocketEnable() {
		return websocketEnable;
	}

	public void setWebsocketEnable(boolean websocketEnable) {
		this.websocketEnable = websocketEnable;
	}

	public int getWebsocketBufferPoolSize() {
		return websocketBufferPoolSize;
	}

	public void setWebsocketBufferPoolSize(int websocketBufferPoolSize) {
		this.websocketBufferPoolSize = websocketBufferPoolSize;
	}

	@Override
	public String toString() {
		return "JbootServerConfig {" + "type='" + type + '\'' + ", host='" + host + '\'' + ", port=" + port
				+ ", contextPath='" + contextPath + '\'' + ", websocketEnable=" + websocketEnable
				+ ", websocketBufferPoolSize=" + websocketBufferPoolSize + '}';
	}
}
