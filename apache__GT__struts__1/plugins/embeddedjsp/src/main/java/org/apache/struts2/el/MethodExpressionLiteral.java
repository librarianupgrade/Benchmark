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
package org.apache.struts2.el;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.MethodExpression;
import javax.el.MethodInfo;

import org.apache.struts2.el.lang.ELSupport;
import org.apache.struts2.el.util.ReflectionUtil;

public class MethodExpressionLiteral extends MethodExpression implements Externalizable {

	private Class expectedType;

	private String expr;

	private Class[] paramTypes;

	public MethodExpressionLiteral() {
		// do nothing
	}

	public MethodExpressionLiteral(String expr, Class expectedType, Class[] paramTypes) {
		this.expr = expr;
		this.expectedType = expectedType;
		this.paramTypes = paramTypes;
	}

	public MethodInfo getMethodInfo(ELContext context) throws ELException {
		return new MethodInfo(this.expr, this.expectedType, this.paramTypes);
	}

	public Object invoke(ELContext context, Object[] params) throws ELException {
		if (this.expectedType != null) {
			return ELSupport.coerceToType(this.expr, this.expectedType);
		} else {
			return this.expr;
		}
	}

	public String getExpressionString() {
		return this.expr;
	}

	public boolean equals(Object obj) {
		return (obj instanceof MethodExpressionLiteral && this.hashCode() == obj.hashCode());
	}

	public int hashCode() {
		return this.expr.hashCode();
	}

	public boolean isLiteralText() {
		return true;
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		this.expr = in.readUTF();
		String type = in.readUTF();
		if (!"".equals(type)) {
			this.expectedType = ReflectionUtil.forName(type);
		}
		this.paramTypes = ReflectionUtil.toTypeArray(((String[]) in.readObject()));
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(this.expr);
		out.writeUTF((this.expectedType != null) ? this.expectedType.getName() : "");
		out.writeObject(ReflectionUtil.toTypeNameArray(this.paramTypes));
	}
}
