/**
 * Copyright (c) 2015-2017, Michael Yang 杨福海 (fuhai999@gmail.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *  http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jboot.component.log;

import com.jfinal.log.Log;
import io.jboot.exception.JbootExceptionHolder;

import java.util.logging.Level;

/**
 * 系统的logger
 */
public class JdkLogger extends Log {

	private java.util.logging.Logger log;
	private String clazzName;

	public JdkLogger(Class<?> clazz) {
		log = java.util.logging.Logger.getLogger(clazz.getName());
		clazzName = clazz.getName();
	}

	public JdkLogger(String name) {
		log = java.util.logging.Logger.getLogger(name);
		clazzName = name;
	}

	public void debug(String message) {
		log.logp(Level.FINE, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), message);
	}

	public void debug(String message, Throwable t) {
		JbootExceptionHolder.hold(t);
		log.logp(Level.FINE, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), message, t);
	}

	public void info(String message) {
		log.logp(Level.INFO, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), message);
	}

	public void info(String message, Throwable t) {
		JbootExceptionHolder.hold(t);
		log.logp(Level.INFO, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), message, t);
	}

	public void warn(String message) {
		log.logp(Level.WARNING, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), message);
	}

	public void warn(String message, Throwable t) {
		JbootExceptionHolder.hold(t);
		log.logp(Level.WARNING, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), message, t);
	}

	public void error(String message) {
		log.logp(Level.SEVERE, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), message);
	}

	public void error(String message, Throwable t) {
		JbootExceptionHolder.hold(t);
		log.logp(Level.SEVERE, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), message, t);
	}

	/**
	 * JdkLog fatal is the same as the error.
	 */
	public void fatal(String message) {
		log.logp(Level.SEVERE, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), message);
	}

	/**
	 * JdkLog fatal is the same as the error.
	 */
	public void fatal(String message, Throwable t) {
		JbootExceptionHolder.hold(t);
		log.logp(Level.SEVERE, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), message, t);
	}

	public boolean isDebugEnabled() {
		return log.isLoggable(Level.FINE);
	}

	public boolean isInfoEnabled() {
		return log.isLoggable(Level.INFO);
	}

	public boolean isWarnEnabled() {
		return log.isLoggable(Level.WARNING);
	}

	public boolean isErrorEnabled() {
		return log.isLoggable(Level.SEVERE);
	}

	public boolean isFatalEnabled() {
		return log.isLoggable(Level.SEVERE);
	}
}
