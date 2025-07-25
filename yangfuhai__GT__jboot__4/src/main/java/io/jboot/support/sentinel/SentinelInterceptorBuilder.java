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
package io.jboot.support.sentinel;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import io.jboot.aop.InterceptorBuilder;
import io.jboot.aop.Interceptors;
import io.jboot.aop.annotation.AutoLoad;
import io.jboot.utils.ClassUtil;

import java.lang.reflect.Method;

/**
 * @author michael yang (fuhai999@gmail.com)
 */
@AutoLoad
public class SentinelInterceptorBuilder implements InterceptorBuilder {

	private static Boolean hasSentinelDependency = ClassUtil.hasClass("com.alibaba.csp.sentinel.Sph");

	@Override
	public void build(Class<?> serviceClass, Method method, Interceptors interceptors) {

		if (hasSentinelDependency) {
			SentinelResource annotation = method.getAnnotation(SentinelResource.class);
			if (annotation != null) {
				interceptors.add(SentinelInterceptor.class);
			}
		}
	}

}
