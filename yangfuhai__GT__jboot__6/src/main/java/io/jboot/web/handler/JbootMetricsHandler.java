/**
 * Copyright (c) 2015-2020, Michael Yang 杨福海 (fuhai999@gmail.com).
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
package io.jboot.web.handler;

import com.jfinal.handler.Handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class JbootMetricsHandler extends Handler {

	private String metricsReadUrl;

	public JbootMetricsHandler(String metricsReadUrl) {
		this.metricsReadUrl = metricsReadUrl;
	}

	@Override
	public void handle(String target, HttpServletRequest request, HttpServletResponse response, boolean[] isHandled) {

		//static files
		if (target.lastIndexOf('.') != -1) {
			return;
		}

		// metrics servlet 处理，此处如果是 tomcat，需要在 web.xml 配置 metrics 的相关  servlet
		if (target.startsWith(metricsReadUrl)) {
			return;
		}

		next.handle(target, request, response, isHandled);
	}

}
