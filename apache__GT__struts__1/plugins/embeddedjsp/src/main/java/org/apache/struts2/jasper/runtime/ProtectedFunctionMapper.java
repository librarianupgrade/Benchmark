/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.struts2.jasper.runtime;

import java.util.HashMap;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;
import java.lang.reflect.Method;
import javax.servlet.jsp.el.FunctionMapper;

import org.apache.struts2.jasper.security.SecurityUtil;

/**
 * Maps EL functions to their Java method counterparts. Keeps the actual Method
 * objects protected so that JSP pages can't indirectly do reflection.
 * 
 * @author Mark Roth
 * @author Kin-man Chung
 */
public final class ProtectedFunctionMapper extends javax.el.FunctionMapper implements FunctionMapper {

	/**
	 * Maps "prefix:name" to java.lang.Method objects.
	 */
	private HashMap fnmap = null;

	/**
	 * If there is only one function in the map, this is the Method for it.
	 */
	private Method theMethod = null;

	/**
	 * Constructor has protected access.
	 */
	private ProtectedFunctionMapper() {
	}

	/**
	 * Generated Servlet and Tag Handler implementations call this method to
	 * retrieve an instance of the ProtectedFunctionMapper. This is necessary
	 * since generated code does not have access to create instances of classes
	 * in this package.
	 * 
	 * @return A new protected function mapper.
	 */
	public static ProtectedFunctionMapper getInstance() {
		ProtectedFunctionMapper funcMapper;
		if (SecurityUtil.isPackageProtectionEnabled()) {
			funcMapper = (ProtectedFunctionMapper) AccessController.doPrivileged(new PrivilegedAction() {
				public Object run() {
					return new ProtectedFunctionMapper();
				}
			});
		} else {
			funcMapper = new ProtectedFunctionMapper();
		}
		funcMapper.fnmap = new java.util.HashMap();
		return funcMapper;
	}

	/**
	 * Stores a mapping from the given EL function prefix and name to the given
	 * Java method.
	 * 
	 * @param fnQName
	 *            The EL function qualified name (including prefix)
	 * @param c
	 *            The class containing the Java method
	 * @param methodName
	 *            The name of the Java method
	 * @param args
	 *            The arguments of the Java method
	 * @throws RuntimeException
	 *             if no method with the given signature could be found.
	 */
	public void mapFunction(String fnQName, final Class c, final String methodName, final Class[] args) {
		java.lang.reflect.Method method;
		if (SecurityUtil.isPackageProtectionEnabled()) {
			try {
				method = (java.lang.reflect.Method) AccessController.doPrivileged(new PrivilegedExceptionAction() {

					public Object run() throws Exception {
						return c.getDeclaredMethod(methodName, args);
					}
				});
			} catch (PrivilegedActionException ex) {
				throw new RuntimeException(
						"Invalid function mapping - no such method: " + ex.getException().getMessage());
			}
		} else {
			try {
				method = c.getDeclaredMethod(methodName, args);
			} catch (NoSuchMethodException e) {
				throw new RuntimeException("Invalid function mapping - no such method: " + e.getMessage());
			}
		}

		this.fnmap.put(fnQName, method);
	}

	/**
	 * Creates an instance for this class, and stores the Method for the given
	 * EL function prefix and name. This method is used for the case when there
	 * is only one function in the EL expression.
	 * 
	 * @param fnQName
	 *            The EL function qualified name (including prefix)
	 * @param c
	 *            The class containing the Java method
	 * @param methodName
	 *            The name of the Java method
	 * @param args
	 *            The arguments of the Java method
	 *
	 * @return function mapper
	 *
	 * @throws RuntimeException
	 *             if no method with the given signature could be found.
	 */
	public static ProtectedFunctionMapper getMapForFunction(String fnQName, final Class c, final String methodName,
			final Class[] args) {
		java.lang.reflect.Method method;
		ProtectedFunctionMapper funcMapper;
		if (SecurityUtil.isPackageProtectionEnabled()) {
			funcMapper = (ProtectedFunctionMapper) AccessController.doPrivileged(new PrivilegedAction() {
				public Object run() {
					return new ProtectedFunctionMapper();
				}
			});

			try {
				method = (java.lang.reflect.Method) AccessController.doPrivileged(new PrivilegedExceptionAction() {

					public Object run() throws Exception {
						return c.getDeclaredMethod(methodName, args);
					}
				});
			} catch (PrivilegedActionException ex) {
				throw new RuntimeException(
						"Invalid function mapping - no such method: " + ex.getException().getMessage());
			}
		} else {
			funcMapper = new ProtectedFunctionMapper();
			try {
				method = c.getDeclaredMethod(methodName, args);
			} catch (NoSuchMethodException e) {
				throw new RuntimeException("Invalid function mapping - no such method: " + e.getMessage());
			}
		}
		funcMapper.theMethod = method;
		return funcMapper;
	}

	/**
	 * Resolves the specified local name and prefix into a Java.lang.Method.
	 * Returns null if the prefix and local name are not found.
	 * 
	 * @param prefix
	 *            the prefix of the function
	 * @param localName
	 *            the short name of the function
	 * @return the result of the method mapping. Null means no entry found.
	 */
	public Method resolveFunction(String prefix, String localName) {
		if (this.fnmap != null) {
			return (Method) this.fnmap.get(prefix + ":" + localName);
		}
		return theMethod;
	}
}
