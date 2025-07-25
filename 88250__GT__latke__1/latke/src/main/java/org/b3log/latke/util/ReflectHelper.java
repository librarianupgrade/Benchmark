/*
 * Copyright (c) 2009, 2010, 2011, 2012, 2013, B3log Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.b3log.latke.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;

/**
 * ReflectHelper while not using java reflect instead of the other class byte tool.
 * 
 * @author <a href="mailto:wmainlove@gmail.com">Love Yao</a>
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.4, Dec 21, 2012
 */
public final class ReflectHelper {

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(ReflectHelper.class.getName());

	/**
	 * the maxFindLength to get the 'this' keyword when resolving the vaibleNames.
	 */
	private static final Integer MAX_FIND_LENGTH = 30;

	/**
	 * The default constructor.
	 */
	private ReflectHelper() {
	}

	/**
	 * getMethodVariableNames in user defined.
	 * @param clazz the specific clazz
	 * @param targetMethodName the targetMethodName
	 * @param types the types of the method parameters
	 * @return the String[] of names
	 */
	public static String[] getMethodVariableNames(final Class<?> clazz, final String targetMethodName,
			final Class<?>[] types) {
		CtClass cc;
		CtMethod cm = null;

		try {
			final ClassPool pool = ClassPool.getDefault();

			pool.insertClassPath(new ClassClassPath(ReflectHelper.class));
			pool.insertClassPath(new ClassClassPath(clazz));
			pool.insertClassPath(new ClassClassPath(Thread.currentThread().getClass()));

			cc = pool.get(clazz.getName());
			final CtClass[] ptypes = new CtClass[types.length];

			for (int i = 0; i < ptypes.length; i++) {
				ptypes[i] = pool.get(types[i].getName());
			}
			cm = cc.getDeclaredMethod(targetMethodName, ptypes);
		} catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Get method variable names failed", e);
		}

		if (null == cm) {
			return new String[types.length];
		}

		final MethodInfo methodInfo = cm.getMethodInfo();
		final CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
		final LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute
				.getAttribute(LocalVariableAttribute.tag);
		String[] variableNames = new String[0];

		try {
			variableNames = new String[cm.getParameterTypes().length];
		} catch (final NotFoundException e) {
			LOGGER.log(Level.SEVERE, "Get method variable names failed", e);
		}

		// final int staticIndex = Modifier.isStatic(cm.getModifiers()) ? 0 : 1;
		int j = -1;
		String variableName = null;
		Boolean ifkill = false;

		while (!"this".equals(variableName)) {
			j++;
			variableName = attr.variableName(j);
			// to prevent heap error when there being some unknown reasons to
			// resolve the VariableNames
			if (j > MAX_FIND_LENGTH) {
				LOGGER.log(Level.WARNING, "maybe resolve to VariableNames error ");
				ifkill = true;
				break;
			}
		}

		if (!ifkill) {
			for (int i = 0; i < variableNames.length; i++) {
				variableNames[i] = attr.variableName(++j);
			}
		}
		return variableNames;
	}
}
