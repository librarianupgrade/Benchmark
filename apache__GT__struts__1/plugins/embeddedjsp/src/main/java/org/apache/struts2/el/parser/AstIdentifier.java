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
package org.apache.struts2.el.parser;

import javax.el.ELException;
import javax.el.MethodExpression;
import javax.el.MethodInfo;
import javax.el.MethodNotFoundException;
import javax.el.ValueExpression;
import javax.el.VariableMapper;

import org.apache.struts2.el.lang.EvaluationContext;

/**
 * @author Jacob Hookom [jacob@hookom.net]
 * @version $Change: 181177 $$DateTime: 2001/06/26 08:45:09 $$Author: markt $
 */
public final class AstIdentifier extends SimpleNode {
	public AstIdentifier(int id) {
		super(id);
	}

	public Class getType(EvaluationContext ctx) throws ELException {
		VariableMapper varMapper = ctx.getVariableMapper();
		if (varMapper != null) {
			ValueExpression expr = varMapper.resolveVariable(this.image);
			if (expr != null) {
				return expr.getType(ctx.getELContext());
			}
		}
		ctx.setPropertyResolved(false);
		return ctx.getELResolver().getType(ctx, null, this.image);
	}

	public Object getValue(EvaluationContext ctx) throws ELException {
		VariableMapper varMapper = ctx.getVariableMapper();
		if (varMapper != null) {
			ValueExpression expr = varMapper.resolveVariable(this.image);
			if (expr != null) {
				return expr.getValue(ctx.getELContext());
			}
		}
		ctx.setPropertyResolved(false);
		return ctx.getELResolver().getValue(ctx, null, this.image);
	}

	public boolean isReadOnly(EvaluationContext ctx) throws ELException {
		VariableMapper varMapper = ctx.getVariableMapper();
		if (varMapper != null) {
			ValueExpression expr = varMapper.resolveVariable(this.image);
			if (expr != null) {
				return expr.isReadOnly(ctx.getELContext());
			}
		}
		ctx.setPropertyResolved(false);
		return ctx.getELResolver().isReadOnly(ctx, null, this.image);
	}

	public void setValue(EvaluationContext ctx, Object value) throws ELException {
		VariableMapper varMapper = ctx.getVariableMapper();
		if (varMapper != null) {
			ValueExpression expr = varMapper.resolveVariable(this.image);
			if (expr != null) {
				expr.setValue(ctx.getELContext(), value);
				return;
			}
		}
		ctx.setPropertyResolved(false);
		ctx.getELResolver().setValue(ctx, null, this.image, value);
	}

	public Object invoke(EvaluationContext ctx, Class[] paramTypes, Object[] paramValues) throws ELException {
		return this.getMethodExpression(ctx).invoke(ctx.getELContext(), paramValues);
	}

	public MethodInfo getMethodInfo(EvaluationContext ctx, Class[] paramTypes) throws ELException {
		return this.getMethodExpression(ctx).getMethodInfo(ctx.getELContext());
	}

	private final MethodExpression getMethodExpression(EvaluationContext ctx) throws ELException {
		Object obj = null;

		// case A: ValueExpression exists, getValue which must
		// be a MethodExpression
		VariableMapper varMapper = ctx.getVariableMapper();
		ValueExpression ve = null;
		if (varMapper != null) {
			ve = varMapper.resolveVariable(this.image);
			if (ve != null) {
				obj = ve.getValue(ctx);
			}
		}

		// case B: evaluate the identity against the ELResolver, again, must be
		// a MethodExpression to be able to invoke
		if (ve == null) {
			ctx.setPropertyResolved(false);
			obj = ctx.getELResolver().getValue(ctx, null, this.image);
		}

		// finally provide helpful hints
		if (obj instanceof MethodExpression) {
			return (MethodExpression) obj;
		} else if (obj == null) {
			throw new MethodNotFoundException("Identity '" + this.image + "' was null and was unable to invoke");
		} else {
			throw new ELException("Identity '" + this.image
					+ "' does not reference a MethodExpression instance, returned type: " + obj.getClass().getName());
		}
	}
}
