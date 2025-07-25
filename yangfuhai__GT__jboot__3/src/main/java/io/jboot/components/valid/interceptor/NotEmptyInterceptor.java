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
package io.jboot.components.valid.interceptor;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import io.jboot.components.valid.ValidUtil;
import io.jboot.utils.ClassUtil;
import io.jboot.utils.StrUtil;

import javax.validation.constraints.NotEmpty;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.Map;

public class NotEmptyInterceptor implements Interceptor {

	@Override
	public void intercept(Invocation inv) {
		Parameter[] parameters = inv.getMethod().getParameters();
		for (int index = 0; index < parameters.length; index++) {
			NotEmpty notEmpty = parameters[index].getAnnotation(NotEmpty.class);
			if (notEmpty != null) {
				Object validObject = inv.getArg(index);
				if (validObject == null || (validObject instanceof String && StrUtil.isBlank((String) validObject))
						|| (validObject instanceof Map && ((Map) validObject).isEmpty())
						|| (validObject instanceof Collection && ((Collection) validObject).isEmpty())
						|| (validObject.getClass().isArray() && ((Object[]) validObject).length == 0)
						|| (validObject.getClass() == int[].class && ((int[]) validObject).length == 0)
						|| (validObject.getClass() == long[].class && ((long[]) validObject).length == 0)
						|| (validObject.getClass() == short[].class && ((short[]) validObject).length == 0)) {
					String reason = parameters[index].getName() + " is null or empty at method: "
							+ ClassUtil.buildMethodString(inv.getMethod());
					ValidUtil.throwValidException(parameters[index].getName(), notEmpty.message(), reason);
				}
			}
		}

		inv.invoke();
	}

}