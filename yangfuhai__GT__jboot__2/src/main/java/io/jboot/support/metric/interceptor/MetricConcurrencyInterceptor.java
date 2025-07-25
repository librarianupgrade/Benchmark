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
package io.jboot.support.metric.interceptor;

import com.codahale.metrics.Counter;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import io.jboot.Jboot;
import io.jboot.support.metric.annotation.EnableMetricConcurrency;
import io.jboot.utils.AnnotationUtil;
import io.jboot.utils.StrUtil;

public class MetricConcurrencyInterceptor implements Interceptor {

	@Override
	public void intercept(Invocation inv) {

		Counter concurrencyRecord = null;
		EnableMetricConcurrency concurrencyAnnotation = inv.getMethod().getAnnotation(EnableMetricConcurrency.class);
		if (concurrencyAnnotation != null) {
			String value = AnnotationUtil.get(concurrencyAnnotation.value());
			String name = StrUtil.isBlank(value)
					? inv.getController().getClass().getName() + "." + inv.getMethodName() + ".concurrency"
					: value;

			concurrencyRecord = Jboot.getMetric().counter(name);
			concurrencyRecord.inc();
		}

		try {
			inv.invoke();
		} finally {
			if (concurrencyRecord != null) {
				concurrencyRecord.dec();
			}
		}
	}

}
