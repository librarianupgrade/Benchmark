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
package io.jboot.support.jwt;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import io.jboot.web.controller.JbootController;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author Michael Yang 杨福海 （fuhai999@gmail.com）
 * @version V1.0
 * @Title: 用于对Jwt的设置
 */
public class JwtInterceptor implements Interceptor {

	public static final String ISUUED_AT = "isuuedAt";

	@Override
	public void intercept(Invocation inv) {
		try {
			inv.invoke();
		} finally {

			JbootController jbootController = (JbootController) inv.getController();
			Map<String, Object> jwtMap = jbootController.getJwtAttrs();

			if (jwtMap == null || jwtMap.isEmpty()) {
				refreshIfNecessary(jbootController, jbootController.getJwtParas());
			} else {
				String token = JwtManager.me().createJwtToken(jwtMap);
				HttpServletResponse response = inv.getController().getResponse();
				response.addHeader(JwtManager.me().getHttpHeaderName(), token);
			}
		}
	}

	private void refreshIfNecessary(Controller ctr, Map oldData) {
		if (oldData == null || oldData.isEmpty()) {
			return;
		}

		// Jwt token 的发布时间
		Long isuuedAtMillis = (Long) oldData.get(ISUUED_AT);
		if (isuuedAtMillis == null || JwtManager.me().getConfig().getValidityPeriod() <= 0) {
			return;
		}

		Long nowMillis = System.currentTimeMillis();
		long savedMillis = nowMillis - isuuedAtMillis;

		if (savedMillis > JwtManager.me().getConfig().getValidityPeriod() / 2) {
			String token = JwtManager.me().createJwtToken(oldData);
			HttpServletResponse response = ctr.getResponse();
			response.addHeader(JwtManager.me().getHttpHeaderName(), token);
		}

	}
}
