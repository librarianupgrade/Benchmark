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
package com.opensymphony.xwork2.util;

import com.opensymphony.xwork2.ActionContext;

import java.util.Map;

/**
 * ValueStack allows multiple beans to be pushed in and dynamic EL expressions to be evaluated against it. When
 * evaluating an expression, the stack will be searched down the stack, from the latest objects pushed in to the
 * earliest, looking for a bean with a getter or setter for the given property or a method of the given name (depending
 * on the expression being evaluated).
 */
public interface ValueStack {

	String VALUE_STACK = "com.opensymphony.xwork2.util.ValueStack.ValueStack";

	String REPORT_ERRORS_ON_NO_PROP = "com.opensymphony.xwork2.util.ValueStack.ReportErrorsOnNoProp";

	/**
	 * Gets the context for this value stack. The context holds all the information in the value stack and it's surroundings.
	 *
	 * @return  the context.
	 */
	Map<String, Object> getContext();

	ActionContext getActionContext();

	/**
	 * Sets the default type to convert to if no type is provided when getting a value.
	 *
	 * @param defaultType the new default type
	 */
	void setDefaultType(Class defaultType);

	/**
	 * Set a override map containing <code> key -&gt; values </code> that takes precedent when doing find operations on the ValueStack.
	 * <p>
	 * See the unit test for ValueStackTest for examples.
	 * </p>
	 *
	 * @param overrides  overrides map.
	 */
	void setExprOverrides(Map<Object, Object> overrides);

	/**
	 * Gets the override map if anyone exists.
	 *
	 * @return the override map, <tt>null</tt> if not set.
	 */
	Map<Object, Object> getExprOverrides();

	/**
	 * Get the CompoundRoot which holds the objects pushed onto the stack
	 *
	 * @return the root
	 */
	CompoundRoot getRoot();

	/**
	 * Attempts to set a property on a bean in the stack with the given expression using the default search order.
	 *
	 * @param expr  the expression defining the path to the property to be set.
	 * @param value the value to be set into the named property
	 */
	void setValue(String expr, Object value);

	/**
	 * Attempts to set a property on a bean in the stack with the given expression using the default search order.
	 * N.B.: unlike #setValue(String,Object) it doesn't allow eval expression.
	 * @param expr  the expression defining the path to the property to be set.
	 * @param value the value to be set into the named property
	 */
	void setParameter(String expr, Object value);

	/**
	 * Attempts to set a property on a bean in the stack with the given expression using the default search order.
	 *
	 * @param expr                    the expression defining the path to the property to be set.
	 * @param value                   the value to be set into the named property
	 * @param throwExceptionOnFailure a flag to tell whether an exception should be thrown if there is no property with
	 *                                the given name.
	 */
	void setValue(String expr, Object value, boolean throwExceptionOnFailure);

	String findString(String expr);

	String findString(String expr, boolean throwExceptionOnFailure);

	/**
	 * Find a value by evaluating the given expression against the stack in the default search order.
	 *
	 * @param expr the expression giving the path of properties to navigate to find the property value to return
	 * @return the result of evaluating the expression
	 */
	Object findValue(String expr);

	Object findValue(String expr, boolean throwExceptionOnFailure);

	/**
	 * Find a value by evaluating the given expression against the stack in the default search order.
	 *
	 * @param expr   the expression giving the path of properties to navigate to find the property value to return
	 * @param asType the type to convert the return value to
	 * @return the result of evaluating the expression
	 */
	Object findValue(String expr, Class asType);

	Object findValue(String expr, Class asType, boolean throwExceptionOnFailure);

	/**
	 * Get the object on the top of the stack <b>without</b> changing the stack.
	 *
	 * @return the object on the top.
	 * @see CompoundRoot#peek()
	 */
	Object peek();

	/**
	 * Get the object on the top of the stack and <b>remove</b> it from the stack.
	 *
	 * @return the object on the top of the stack
	 * @see CompoundRoot#pop()
	 */
	Object pop();

	/**
	 * Put this object onto the top of the stack
	 *
	 * @param o the object to be pushed onto the stack
	 * @see CompoundRoot#push(Object)
	 */
	void push(Object o);

	/**
	 * Sets an object on the stack with the given key
	 * so it is retrievable by {@link #findValue(String)}, {@link #findValue(String, Class)}
	 *
	 * @param key  the key
	 * @param o    the object
	 */
	void set(String key, Object o);

	/**
	 * Get the number of objects in the stack
	 *
	 * @return the number of objects in the stack
	 */
	int size();

}