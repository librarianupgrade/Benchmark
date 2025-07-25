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
package io.jboot.components.valid.interceptor;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.kit.Ret;
import io.jboot.utils.ClassUtil;

import javax.validation.constraints.Size;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.Map;

import static io.jboot.components.valid.ValidUtil.throwValidException;

public class SizeInterceptor implements Interceptor {

	@Override
	public void intercept(Invocation inv) {
		Parameter[] parameters = inv.getMethod().getParameters();

		for (int index = 0; index < parameters.length; index++) {
			Size size = parameters[index].getAnnotation(Size.class);
			if (size != null) {
				Object validObject = inv.getArg(index);
				if (validObject == null) {
					String reason = parameters[index].getName() + " need size is " + size.min() + " ~ " + size.max()
							+ ", but current value is null at method: " + ClassUtil.buildMethodString(inv.getMethod());
					Ret paras = Ret.by("max", size.max()).set("min", size.min());
					throwValidException(size.message(), paras, reason);
				}

				int len = getObjectLen(validObject);
				if (len < size.min() || len > size.max()) {
					String reason = parameters[index].getName() + " need size is " + size.min() + " ~ " + size.max()
							+ ", but current value size (or length) is " + len + " at method: "
							+ ClassUtil.buildMethodString(inv.getMethod());
					Ret paras = Ret.by("max", size.max()).set("min", size.min());
					throwValidException(size.message(), paras, reason);
				}
			}
		}

		inv.invoke();
	}

	private int getObjectLen(Object validObject) {
		if (validObject instanceof Number) {
			return ((Number) validObject).intValue();
		}
		if (validObject instanceof CharSequence) {
			return ((CharSequence) validObject).length();
		}
		if (validObject instanceof Map) {
			return ((Map<?, ?>) validObject).size();
		}
		if (validObject instanceof Collection) {
			return ((Collection) validObject).size();
		}
		if (validObject.getClass().isArray()) {
			return ((Object[]) validObject).length;
		}

		return -1;
	}

}