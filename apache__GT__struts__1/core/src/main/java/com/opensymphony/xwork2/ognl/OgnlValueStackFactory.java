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
package com.opensymphony.xwork2.ognl;

import com.opensymphony.xwork2.TextProvider;
import com.opensymphony.xwork2.conversion.NullHandler;
import com.opensymphony.xwork2.conversion.impl.XWorkConverter;
import com.opensymphony.xwork2.inject.Container;
import com.opensymphony.xwork2.inject.Inject;
import com.opensymphony.xwork2.ognl.accessor.CompoundRootAccessor;
import com.opensymphony.xwork2.util.CompoundRoot;
import com.opensymphony.xwork2.util.ValueStack;
import com.opensymphony.xwork2.util.ValueStackFactory;
import ognl.MethodAccessor;
import ognl.OgnlRuntime;
import ognl.PropertyAccessor;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.StrutsConstants;

import java.util.Map;
import java.util.Set;

/**
 * Creates an Ognl value stack
 */
public class OgnlValueStackFactory implements ValueStackFactory {

	protected XWorkConverter xworkConverter;
	protected CompoundRootAccessor compoundRootAccessor;
	protected TextProvider textProvider;
	protected Container container;

	@Inject
	protected void setXWorkConverter(XWorkConverter converter) {
		this.xworkConverter = converter;
	}

	@Inject("system")
	protected void setTextProvider(TextProvider textProvider) {
		this.textProvider = textProvider;
	}

	public ValueStack createValueStack() {
		ValueStack stack = new OgnlValueStack(xworkConverter, compoundRootAccessor, textProvider,
				containerAllowsStaticFieldAccess());
		container.inject(stack);
		return stack.getActionContext().withContainer(container).withValueStack(stack).getValueStack();
	}

	public ValueStack createValueStack(ValueStack stack) {
		ValueStack result = new OgnlValueStack(stack, xworkConverter, compoundRootAccessor,
				containerAllowsStaticFieldAccess());
		container.inject(result);
		return result.getActionContext().withContainer(container).withValueStack(result).getValueStack();
	}

	@Inject
	protected void setContainer(Container container) throws ClassNotFoundException {
		Set<String> names = container.getInstanceNames(PropertyAccessor.class);
		for (String name : names) {
			Class<?> cls = Class.forName(name);
			OgnlRuntime.setPropertyAccessor(cls, container.getInstance(PropertyAccessor.class, name));
			if (compoundRootAccessor == null && CompoundRoot.class.isAssignableFrom(cls)) {
				compoundRootAccessor = (CompoundRootAccessor) container.getInstance(PropertyAccessor.class, name);
			}
		}

		names = container.getInstanceNames(MethodAccessor.class);
		for (String name : names) {
			Class<?> cls = Class.forName(name);
			OgnlRuntime.setMethodAccessor(cls, container.getInstance(MethodAccessor.class, name));
		}

		names = container.getInstanceNames(NullHandler.class);
		for (String name : names) {
			Class<?> cls = Class.forName(name);
			OgnlRuntime.setNullHandler(cls, new OgnlNullHandlerWrapper(container.getInstance(NullHandler.class, name)));
		}
		if (compoundRootAccessor == null) {
			throw new IllegalStateException("Couldn't find the compound root accessor");
		}
		this.container = container;
	}

	/**
	 * Retrieve allowStaticFieldAccess state from the container (allows for lazy fetching)
	 */
	protected boolean containerAllowsStaticFieldAccess() {
		return BooleanUtils
				.toBoolean(container.getInstance(String.class, StrutsConstants.STRUTS_ALLOW_STATIC_FIELD_ACCESS));
	}

}
