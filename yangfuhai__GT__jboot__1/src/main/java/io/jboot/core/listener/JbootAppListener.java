/**
 * Copyright (c) 2015-2022, Michael Yang 杨福海 (fuhai999@gmail.com).
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
package io.jboot.core.listener;

import com.jfinal.config.Constants;
import com.jfinal.config.Interceptors;
import com.jfinal.config.Routes;
import com.jfinal.template.Engine;
import io.jboot.aop.jfinal.JfinalHandlers;
import io.jboot.aop.jfinal.JfinalPlugins;

public interface JbootAppListener {

	default void onInit() {
	}

	default void onConstantConfigBefore(Constants constants) {
	}

	default void onConstantConfig(Constants constants) {
	}

	default void onRouteConfig(Routes routes) {
	}

	default void onEngineConfig(Engine engine) {
	}

	default void onPluginConfig(JfinalPlugins plugins) {
	}

	default void onInterceptorConfig(Interceptors interceptors) {
	}

	default void onHandlerConfig(JfinalHandlers handlers) {
	}

	default void onStartBefore() {
	}

	default void onStart() {
	}

	default void onStartFinish() {
	}

	default void onStop() {
	}

}
