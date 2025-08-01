/**
 * Copyright (c) 2015-2022, Michael Yang 杨福海 (fuhai999@gmail.com).
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
package io.jboot.aop.jfinal;

import com.jfinal.aop.Aop;
import com.jfinal.config.Plugins;
import com.jfinal.plugin.IPlugin;

import java.util.List;

/**
 * Jfinal Plugins 的代理类，方便为 Plugin 插件的自动注入功能
 */
public class JfinalPlugins {

	private final Plugins plugins;

	public JfinalPlugins(Plugins plugins) {
		this.plugins = plugins;
	}

	public JfinalPlugins add(IPlugin plugin) {
		Aop.inject(plugin);
		plugins.add(plugin);
		return this;
	}

	public Plugins getPlugins() {
		return plugins;
	}

	public List<IPlugin> getPluginList() {
		return plugins.getPluginList();
	}
}
