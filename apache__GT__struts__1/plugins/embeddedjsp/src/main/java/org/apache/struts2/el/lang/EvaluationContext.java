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
package org.apache.struts2.el.lang;

import java.util.Locale;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.FunctionMapper;
import javax.el.VariableMapper;

public final class EvaluationContext extends ELContext {

	private final ELContext elContext;

	private final FunctionMapper fnMapper;

	private final VariableMapper varMapper;

	public EvaluationContext(ELContext elContext, FunctionMapper fnMapper, VariableMapper varMapper) {
		this.elContext = elContext;
		this.fnMapper = fnMapper;
		this.varMapper = varMapper;
	}

	public ELContext getELContext() {
		return this.elContext;
	}

	public FunctionMapper getFunctionMapper() {
		return this.fnMapper;
	}

	public VariableMapper getVariableMapper() {
		return this.varMapper;
	}

	public Object getContext(Class key) {
		return this.elContext.getContext(key);
	}

	public ELResolver getELResolver() {
		return this.elContext.getELResolver();
	}

	public boolean isPropertyResolved() {
		return this.elContext.isPropertyResolved();
	}

	public void putContext(Class key, Object contextObject) {
		this.elContext.putContext(key, contextObject);
	}

	public void setPropertyResolved(boolean resolved) {
		this.elContext.setPropertyResolved(resolved);
	}

	public Locale getLocale() {
		return this.elContext.getLocale();
	}

	public void setLocale(Locale locale) {
		this.elContext.setLocale(locale);
	}
}
