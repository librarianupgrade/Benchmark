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
package io.jboot.aop.interceptor.cache;

import com.jfinal.log.Log;
import com.jfinal.plugin.ehcache.IDataLoader;
import io.jboot.Jboot;
import io.jboot.core.cache.annotation.Cacheable;
import io.jboot.exception.JbootAssert;
import io.jboot.utils.StringUtils;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;

/**
 * 缓存操作的拦截器
 */
public class JbootCacheInterceptor implements MethodInterceptor {

	static final Log LOG = Log.getLog(JbootCacheInterceptor.class);

	@Override
	public Object invoke(MethodInvocation methodInvocation) throws Throwable {

		Class targetClass = methodInvocation.getThis().getClass();
		Method method = methodInvocation.getMethod();

		Cacheable cacheable = method.getAnnotation(Cacheable.class);
		if (cacheable == null) {
			return methodInvocation.proceed();
		}

		String unlessString = cacheable.unless();
		if (StringUtils.isNotBlank(unlessString)) {
			unlessString = String.format("#(%s)", unlessString);
			String unlessBoolString = Kits.engineRender(unlessString, method, methodInvocation.getArguments());
			if ("true".equals(unlessBoolString)) {
				return methodInvocation.proceed();
			}
		}

		String cacheName = cacheable.name();
		JbootAssert.assertTrue(StringUtils.isNotBlank(cacheName), String.format(
				"Cacheable.name()  must not empty in method [%s]!!!", targetClass.getName() + "#" + method.getName()));

		String cacheKey = Kits.buildCacheKey(cacheable.key(), targetClass, method, methodInvocation.getArguments());

		IDataLoader dataLoader = new IDataLoader() {
			@Override
			public Object load() {
				Object r = null;
				try {
					r = methodInvocation.proceed();
				} catch (Throwable e) {
					LOG.error(e.toString(), e);
				}

				if (r != null) {
					return r;
				}

				return Cacheable.DEFAULT_NULL_VALUE.equals(cacheable.nullValue()) ? null : cacheable.nullValue();
			}
		};

		if (cacheable.liveSeconds() > 0) {
			return Jboot.me().getCache().get(cacheName, cacheKey, dataLoader, cacheable.liveSeconds());
		} else {
			return Jboot.me().getCache().get(cacheName, cacheKey, dataLoader);
		}

	}

}
