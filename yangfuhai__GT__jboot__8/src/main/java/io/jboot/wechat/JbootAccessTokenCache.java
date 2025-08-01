/**
 * Copyright (c) 2015-2017, Michael Yang 杨福海 (fuhai999@gmail.com).
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
package io.jboot.wechat;

import com.jfinal.weixin.sdk.cache.IAccessTokenCache;
import io.jboot.Jboot;

public class JbootAccessTokenCache implements IAccessTokenCache {

	static final String cache_name = "__jboot_wechat_access_tokens";

	@Override
	public String get(String key) {
		return Jboot.me().getCache().get(cache_name, key);
	}

	@Override
	public void set(String key, String value) {
		Jboot.me().getCache().put(cache_name, key, value);
	}

	@Override
	public void remove(String key) {
		Jboot.me().getCache().remove(cache_name, key);
	}
}
