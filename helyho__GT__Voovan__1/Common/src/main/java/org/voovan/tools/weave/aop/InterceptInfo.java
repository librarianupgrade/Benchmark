package org.voovan.tools.weave.aop;

import org.voovan.tools.reflect.TReflect;
import org.voovan.tools.reflect.annotation.NotSerialization;

import java.lang.reflect.Method;

/**
 * 切面调用信息
 *
 * @author: helyho
 * DBase Framework.
 * WebSite: https://github.com/helyho/DBase
 * Licence: Apache v2 License
 */
public class InterceptInfo {
	private Class clazz;
	private String methodName;
	@NotSerialization
	private Object originObject;
	private Class[] argTypes;
	private Object[] args;
	private Class returnType;
	private Object result;
	private Exception exception;

	public InterceptInfo(Class clazz, String methodName, Object originObject, Class[] argTypes, Object[] args,
			Class returnType, Object result, Exception exception) {
		this.clazz = clazz;
		this.methodName = methodName;
		this.originObject = originObject;
		this.argTypes = argTypes;
		this.args = args;
		this.returnType = returnType;
		this.result = result;
		this.exception = exception;
	}

	public Class getClazz() {
		return clazz;
	}

	public void setClazz(Class clazz) {
		this.clazz = clazz;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public Object getOriginObject() {
		return originObject;
	}

	public void setOriginObject(Object originObject) {
		this.originObject = originObject;
	}

	public Class[] getArgTypes() {
		return argTypes;
	}

	public void setArgTypes(Class[] argTypes) {
		this.argTypes = argTypes;
	}

	public Object[] getArgs() {
		return args;
	}

	public void setArgs(Object[] args) {
		this.args = args;
	}

	public Class getReturnType() {
		return returnType;
	}

	public void setReturnType(Class returnType) {
		this.returnType = returnType;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

	public Object process() throws Throwable {
		try {
			Method originMethod = TReflect.findMethod(clazz, methodName + "$origin", argTypes);
			if (originMethod == null) {
				throw new NoSuchMethodException("[AOP] Method \"methodName\" not found or the cut point isn't around");
			}

			return TReflect.invokeMethod(originObject, originMethod, args);
		} catch (ReflectiveOperationException e) {

			Throwable exception = e;

			do {
				exception = exception.getCause();

			} while (exception.getCause() != null && exception instanceof ReflectiveOperationException);

			if (exception == null) {
				throw e;
			} else {
				throw exception;
			}
		}
	}

}
