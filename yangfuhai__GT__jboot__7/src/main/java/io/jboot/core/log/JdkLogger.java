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
package io.jboot.core.log;

import com.jfinal.log.Log;
import com.jfinal.log.LogInfo;
import io.jboot.exception.JbootExceptionHolder;

import java.util.logging.Level;

/**
 * JdkLogger from JdkLog.
 */
public class JdkLogger extends Log {

	private java.util.logging.Logger log;
	private String clazzName;

	JdkLogger(Class<?> clazz) {
		log = java.util.logging.Logger.getLogger(clazz.getName());
		clazzName = clazz.getName();
	}

	JdkLogger(String name) {
		log = java.util.logging.Logger.getLogger(name);
		clazzName = name;
	}

	@Override
	public void trace(String message) {
		log.logp(Level.FINEST, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), message);
	}

	@Override
	public void trace(String message, Throwable t) {
		log.logp(Level.FINEST, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), message, t);
	}

	@Override
	public void debug(String message) {
		log.logp(Level.FINE, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), message);
	}

	@Override
	public void debug(String message, Throwable t) {
		log.logp(Level.FINE, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), message, t);
	}

	@Override
	public void info(String message) {
		log.logp(Level.INFO, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), message);
	}

	@Override
	public void info(String message, Throwable t) {
		log.logp(Level.INFO, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), message, t);
	}

	@Override
	public void warn(String message) {
		log.logp(Level.WARNING, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), message);
	}

	@Override
	public void warn(String message, Throwable t) {
		log.logp(Level.WARNING, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), message, t);
	}

	@Override
	public void error(String message) {
		log.logp(Level.SEVERE, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), message);
	}

	@Override
	public void error(String message, Throwable t) {
		JbootExceptionHolder.hold(message, t);
		log.logp(Level.SEVERE, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), message, t);
	}

	/**
	 * JdkLog fatal is the same as the error.
	 */
	@Override
	public void fatal(String message) {
		log.logp(Level.SEVERE, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), message);
	}

	/**
	 * JdkLog fatal is the same as the error.
	 */
	@Override
	public void fatal(String message, Throwable t) {
		JbootExceptionHolder.hold(message, t);
		log.logp(Level.SEVERE, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), message, t);
	}

	@Override
	public boolean isTraceEnabled() {
		return log.isLoggable(Level.FINEST);
	}

	@Override
	public boolean isDebugEnabled() {
		return log.isLoggable(Level.FINE);
	}

	@Override
	public boolean isInfoEnabled() {
		return log.isLoggable(Level.INFO);
	}

	@Override
	public boolean isWarnEnabled() {
		return log.isLoggable(Level.WARNING);
	}

	@Override
	public boolean isErrorEnabled() {
		return log.isLoggable(Level.SEVERE);
	}

	@Override
	public boolean isFatalEnabled() {
		return log.isLoggable(Level.SEVERE);
	}

	// -------------------------------------------------------

	/*
	 * 以下方法与前面的两个 trace 方法必须覆盖父类中的实现，否则日志中的类名为
	 * com.jfinal.log.Log 而非所需要的日志发生地点的类名
	 */

	@Override
	public void trace(String format, Object... args) {
		if (isTraceEnabled()) {
			if (endsWithThrowable(args)) {
				LogInfo li = parse(format, args);
				trace(li.message, li.throwable);
			} else {
				trace(String.format(format, args));
			}
		}
	}

	@Override
	public void debug(String format, Object... args) {
		if (isDebugEnabled()) {
			if (endsWithThrowable(args)) {
				LogInfo li = parse(format, args);
				debug(li.message, li.throwable);
			} else {
				debug(String.format(format, args));
			}
		}
	}

	@Override
	public void info(String format, Object... args) {
		if (isInfoEnabled()) {
			if (endsWithThrowable(args)) {
				LogInfo li = parse(format, args);
				info(li.message, li.throwable);
			} else {
				info(String.format(format, args));
			}
		}
	}

	@Override
	public void warn(String format, Object... args) {
		if (isWarnEnabled()) {
			if (endsWithThrowable(args)) {
				LogInfo li = parse(format, args);
				warn(li.message, li.throwable);
			} else {
				warn(String.format(format, args));
			}
		}
	}

	@Override
	public void error(String format, Object... args) {
		if (isErrorEnabled()) {
			if (endsWithThrowable(args)) {
				LogInfo li = parse(format, args);
				error(li.message, li.throwable);
			} else {
				error(String.format(format, args));
			}
		}
	}

	@Override
	public void fatal(String format, Object... args) {
		if (isFatalEnabled()) {
			if (endsWithThrowable(args)) {
				LogInfo li = parse(format, args);
				fatal(li.message, li.throwable);
			} else {
				fatal(String.format(format, args));
			}
		}
	}
}
