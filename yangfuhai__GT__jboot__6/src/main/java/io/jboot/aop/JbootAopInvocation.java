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
package io.jboot.aop;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import io.jboot.exception.JbootException;

import java.lang.reflect.Method;

public class JbootAopInvocation extends Invocation {

	private Interceptor[] inters;
	private Invocation originInvocation;

	private int index = 0;

	public JbootAopInvocation(Invocation originInvocation, Interceptor[] inters) {
		this.originInvocation = originInvocation;
		this.inters = inters;
	}

	@Override
	public void invoke() {
		if (index < inters.length) {
			inters[index++].intercept(this);
		}
		// index++ ensure invoke action only one time
		else if (index++ == inters.length) {
			try {
				originInvocation.invoke();
			} catch (Throwable throwable) {
				if (throwable instanceof RuntimeException) {
					throw (RuntimeException) throwable;
				} else {
					throw new JbootException(throwable.getMessage(), throwable);
				}
			}
		}
	}

	@Override
	public Method getMethod() {
		return originInvocation.getMethod();
	}

	@Override
	public String getMethodName() {
		return getMethod().getName();
	}

	@Override
	public <T> T getTarget() {
		return (T) originInvocation.getTarget();
	}

	@Override
	public Object getArg(int index) {
		return originInvocation.getArg(index);
	}

	@Override
	public void setArg(int index, Object value) {
		originInvocation.setArg(index, value);
	}

	@Override
	public Object[] getArgs() {
		return originInvocation.getArgs();
	}

	@Override
	public <T> T getReturnValue() {
		return originInvocation.getReturnValue();
	}

	@Override
	public void setReturnValue(Object returnValue) {
		originInvocation.setReturnValue(returnValue);
	}
}
