package io.jboot.test.aop;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;

public class Name1Interceptor implements Interceptor {
	private String name;

	public Name1Interceptor(String name) {
		this.name = name;
	}

	@Override
	public void intercept(Invocation inv) {

	}

	@Override
	public String toString() {
		return "NameInterceptor{" + "name='" + name + '\'' + '}';
	}
}
