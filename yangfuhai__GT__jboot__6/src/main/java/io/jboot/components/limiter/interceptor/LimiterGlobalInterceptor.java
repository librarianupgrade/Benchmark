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
package io.jboot.components.limiter.interceptor;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import io.jboot.components.limiter.LimitType;
import io.jboot.components.limiter.LimiterManager;

public class LimiterGlobalInterceptor extends BaseLimiterInterceptor implements Interceptor {

	@Override
	public void intercept(Invocation inv) {
		String packageOrTarget = getPackageOrTarget(inv);
		LimiterManager.TypeAndRate typeAndRate = LimiterManager.me().matchConfig(packageOrTarget);

		if (typeAndRate != null) {
			doInterceptByTypeAndRate(typeAndRate, packageOrTarget, inv);
			return;
		}

		inv.invoke();
	}

	private void doInterceptByTypeAndRate(LimiterManager.TypeAndRate typeAndRate, String resource, Invocation inv) {
		switch (typeAndRate.getType()) {
		case LimitType.CONCURRENCY:
			doInterceptForConcurrency(typeAndRate.getRate(), resource, null, inv);
			break;
		case LimitType.TOKEN_BUCKET:
			doInterceptForTokenBucket(typeAndRate.getRate(), resource, null, inv);
			break;
		}
	}

}
