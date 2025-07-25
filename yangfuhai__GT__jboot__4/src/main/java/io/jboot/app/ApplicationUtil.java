/**
 * Copyright (c) 2015-2021, Michael Yang 杨福海 (fuhai999@gmail.com).
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

import io.jboot.app.config.JbootConfigManager;

import java.net.URISyntaxException;
import java.net.URL;

public class ApplicationUtil {

	static JbootApplicationConfig getAppConfig(String[] args) {
		JbootConfigManager.parseArgs(args);
		return getConfig(JbootApplicationConfig.class);
	}

	static void printBannerInfo(JbootApplicationConfig appConfig) {
		if (appConfig.isBannerEnable()) {
			System.out.println();
			System.out.println(Banner.getText(appConfig.getBannerFile()));
			System.out.println();
		}
	}

	static void printApplicationInfo(JbootApplicationConfig appConfig) {
		System.out.println(appConfig.toString());
	}

	public static boolean runInFatjar() {
		URL url = Thread.currentThread().getContextClassLoader().getResource("");
		return url == null || url.toString().toLowerCase().endsWith(".jar!/");
	}

	static void printClassPath() {
		try {
			if (runInFatjar()) {
				System.out.println("JbootApplication is running in fatjar.");
			} else {
				System.out.println(
						"JbootApplication ClassPath: " + ApplicationUtil.class.getResource("/").toURI().getPath());
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	static <T> T getConfig(Class<T> clazz) {
		return JbootConfigManager.me().get(clazz);
	}

	static String getConfigValue(String key) {
		return JbootConfigManager.me().getConfigValue(key);
	}

	static boolean isDevMode() {
		return JbootConfigManager.me().isDevMode();
	}

}
