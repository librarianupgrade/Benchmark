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
package io.jboot.app;

import com.jfinal.server.undertow.UndertowConfig;
import com.jfinal.server.undertow.UndertowServer;
import com.jfinal.server.undertow.WebBuilder;
import io.jboot.app.config.JbootConfigManager;
import io.jboot.app.undertow.JbootUndertowConfig;
import io.jboot.app.undertow.JbootUndertowServer;

import javax.servlet.DispatcherType;

public class JbootApplication {

	public static void main(String[] args) {
		run(args);
	}

	public static void run(String[] args) {
		start(createServer(args));
	}

	public static void run(String[] args, JbootWebBuilderConfiger configer) {
		start(createServer(args, configer));
	}

	public static void start(UndertowServer server) {
		server.start();
		if (ApplicationUtil.isDevMode()) {
			new JbootResourceLoader().start();
		}
	}

	public static void setBootArg(String key, Object value) {
		JbootConfigManager.setBootArg(key, value);
	}

	/**
	 * 创建 Undertow 服务器，public 用于可以给第三方创建创建着急的 Server
	 *
	 * @param args
	 * @return 返回 UndertowServer
	 */
	public static UndertowServer createServer(String[] args) {
		JbootApplicationConfig appConfig = ApplicationUtil.getAppConfig(args);
		return createServer(appConfig, createUndertowConfig(appConfig), null);
	}

	/**
	 * 创建 Undertow 服务器，public 用于可以给第三方创建创建着急的 Server
	 * <p>
	 * JbootApplication.start(JbootApplication.createServer(args,new MyWebBuilderConfiger()))
	 *
	 * @param args
	 * @param configer 可以通过 Configer 来进行自定义配置
	 * @return
	 */
	public static UndertowServer createServer(String[] args, JbootWebBuilderConfiger configer) {
		JbootApplicationConfig appConfig = ApplicationUtil.getAppConfig(args);
		return createServer(appConfig, createUndertowConfig(appConfig), configer);
	}

	public static UndertowServer createServer(JbootApplicationConfig appConfig, UndertowConfig undertowConfig,
			JbootWebBuilderConfiger configer) {

		ApplicationUtil.printBannerInfo(appConfig);
		ApplicationUtil.printApplicationInfo(appConfig);
		ApplicationUtil.printClassPath();

		return new JbootUndertowServer(undertowConfig).setDevMode(ApplicationUtil.isDevMode()).configWeb(webBuilder -> {
			tryAddMetricsSupport(webBuilder);
			tryAddShiroSupport(webBuilder);
			tryAddWebSocketSupport(webBuilder);
			if (configer != null) {
				configer.onConfig(webBuilder);
			}
		});
	}

	public static UndertowConfig createUndertowConfig(JbootApplicationConfig appConfig) {
		UndertowConfig undertowConfig = new JbootUndertowConfig(appConfig.getJfinalConfig());
		undertowConfig.addSystemClassPrefix("io.jboot.app");
		undertowConfig.addHotSwapClassPrefix("io.jboot");
		return undertowConfig;
	}

	private static void tryAddMetricsSupport(WebBuilder webBuilder) {
		String url = ApplicationUtil.getConfigValue("jboot.metric.url");
		String reporter = ApplicationUtil.getConfigValue("jboot.metric.reporter");
		if (url != null && reporter != null) {
			webBuilder.addServlet("MetricsAdminServlet", "com.codahale.metrics.servlets.AdminServlet")
					.addServletMapping("MetricsAdminServlet", url.endsWith("/*") ? url : url + "/*");
			webBuilder.addListener("io.jboot.support.metric.JbootMetricServletContextListener");
			webBuilder.addListener("io.jboot.support.metric.JbootHealthCheckServletContextListener");
		}
	}

	private static void tryAddShiroSupport(WebBuilder webBuilder) {
		String iniConfig = ApplicationUtil.getConfigValue("jboot.shiro.ini");
		if (iniConfig != null) {
			String urlMapping = ApplicationUtil.getConfigValue("jboot.shiro.urlMapping");
			if (urlMapping == null) {
				urlMapping = "/*";
			}
			webBuilder.addListener("org.apache.shiro.web.env.EnvironmentLoaderListener");
			webBuilder.addFilter("shiro", "io.jboot.support.shiro.JbootShiroFilter").addFilterUrlMapping("shiro",
					urlMapping, DispatcherType.REQUEST);

		}
	}

	private static void tryAddWebSocketSupport(WebBuilder webBuilder) {
		String websocketEndpoint = ApplicationUtil.getConfigValue("jboot.web.webSocketEndpoint");
		if (websocketEndpoint != null && websocketEndpoint.trim().length() > 0) {
			String[] classStrings = websocketEndpoint.split(",");
			for (String c : classStrings) {
				webBuilder.addWebSocketEndpoint(c.trim());
			}
		}
	}

}
