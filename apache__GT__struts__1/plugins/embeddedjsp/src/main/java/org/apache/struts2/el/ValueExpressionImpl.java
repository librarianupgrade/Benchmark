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
import javax.el.ELResolver;
import javax.el.Expression;
import javax.el.ExpressionFactory;
import javax.el.FunctionMapper;
import javax.el.PropertyNotFoundException;
import javax.el.PropertyNotWritableException;
import javax.el.ValueExpression;
import javax.el.VariableMapper;

import org.apache.struts2.el.lang.ELSupport;
import org.apache.struts2.el.lang.EvaluationContext;
import org.apache.struts2.el.lang.ExpressionBuilder;
import org.apache.struts2.el.parser.AstLiteralExpression;
import org.apache.struts2.el.parser.Node;
import org.apache.struts2.el.util.ReflectionUtil;

/**
 * An <code>Expression</code> that can get or set a value.
 * 
 * <p>
 * In previous incarnations of this API, expressions could only be read.
 * <code>ValueExpression</code> objects can now be used both to retrieve a
 * value and to set a value. Expressions that can have a value set on them are
 * referred to as l-value expressions. Those that cannot are referred to as
 * r-value expressions. Not all r-value expressions can be used as l-value
 * expressions (e.g. <code>"${1+1}"</code> or
 * <code>"${firstName} ${lastName}"</code>). See the EL Specification for
 * details. Expressions that cannot be used as l-values must always return
 * <code>true</code> from <code>isReadOnly()</code>.
 * </p>
 * 
 * <p>
 * The {@link ExpressionFactory#createValueExpression} method
 * can be used to parse an expression string and return a concrete instance
 * of <code>ValueExpression</code> that encapsulates the parsed expression.
 * The {@link FunctionMapper} is used at parse time, not evaluation time, 
 * so one is not needed to evaluate an expression using this class.  
 * However, the {@link ELContext} is needed at evaluation time.</p>
 *
 * <p>The {@link #getValue}, {@link #setValue}, {@link #isReadOnly} and
 * {@link #getType} methods will evaluate the expression each time they are
 * called. The {@link ELResolver} in the <code>ELContext</code> is used to 
 * resolve the top-level variables and to determine the behavior of the
 * <code>.</code> and <code>[]</code> operators. For any of the four methods,
 * the {@link ELResolver#getValue} method is used to resolve all properties 
 * up to but excluding the last one. This provides the <code>base</code> 
 * object. At the last resolution, the <code>ValueExpression</code> will 
 * call the corresponding {@link ELResolver#getValue}, 
 * {@link ELResolver#setValue}, {@link ELResolver#isReadOnly} or 
 * {@link ELResolver#getType} method, depending on which was called on 
 * the <code>ValueExpression</code>.
 * </p>
 *
 * <p>See the notes about comparison, serialization and immutability in 
 * the {@link Expression} javadocs.
 *
 * @see javax.el.ELResolver
 * @see javax.el.Expression
 * @see javax.el.ExpressionFactory
 * @see javax.el.ValueExpression
 * 
 * @author Jacob Hookom [jacob@hookom.net]
 * @version $Change: 181177 $$DateTime: 2001/06/26 08:45:09 $$Author: markt $
 */
public final class ValueExpressionImpl extends ValueExpression implements Externalizable {

	private Class expectedType;

	private String expr;

	private FunctionMapper fnMapper;

	private VariableMapper varMapper;

	private transient Node node;

	public ValueExpressionImpl() {

	}

	/**
	 * @param expr expression
	 * @param node node
	 * @param fnMapper function mapper
	 * @param varMapper variable mapper
	 * @param expectedType expected type
	 */
	public ValueExpressionImpl(String expr, Node node, FunctionMapper fnMapper, VariableMapper varMapper,
			Class expectedType) {
		this.expr = expr;
		this.node = node;
		this.fnMapper = fnMapper;
		this.varMapper = varMapper;
		this.expectedType = expectedType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return (obj instanceof ValueExpressionImpl && obj.hashCode() == this.hashCode());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.el.ValueExpression#getExpectedType()
	 */
	public Class getExpectedType() {
		return this.expectedType;
	}

	/**
	 * Returns the type the result of the expression will be coerced to after
	 * evaluation.
	 * 
	 * @return the <code>expectedType</code> passed to the
	 *         <code>ExpressionFactory.createValueExpression</code> method
	 *         that created this <code>ValueExpression</code>.
	 * 
	 * @see javax.el.Expression#getExpressionString()
	 */
	public String getExpressionString() {
		return this.expr;
	}

	/**
	 * @return
	 * @throws ELException
	 */
	private Node getNode() throws ELException {
		if (this.node == null) {
			this.node = ExpressionBuilder.createNode(this.expr);
		}
		return this.node;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.el.ValueExpression#getType(javax.el.ELContext)
	 */
	public Class getType(ELContext context) throws PropertyNotFoundException, ELException {
		EvaluationContext ctx = new EvaluationContext(context, this.fnMapper, this.varMapper);
		return this.getNode().getType(ctx);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.el.ValueExpression#getValue(javax.el.ELContext)
	 */
	public Object getValue(ELContext context) throws PropertyNotFoundException, ELException {
		EvaluationContext ctx = new EvaluationContext(context, this.fnMapper, this.varMapper);
		Object value = this.getNode().getValue(ctx);
		if (this.expectedType != null) {
			return ELSupport.coerceToType(value, this.expectedType);
		}
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return this.expr.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.el.ValueExpression#isLiteralText()
	 */
	public boolean isLiteralText() {
		try {
			return this.getNode() instanceof AstLiteralExpression;
		} catch (ELException ele) {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.el.ValueExpression#isReadOnly(javax.el.ELContext)
	 */
	public boolean isReadOnly(ELContext context) throws PropertyNotFoundException, ELException {
		EvaluationContext ctx = new EvaluationContext(context, this.fnMapper, this.varMapper);
		return this.getNode().isReadOnly(ctx);
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		this.expr = in.readUTF();
		String type = in.readUTF();
		if (!"".equals(type)) {
			this.expectedType = ReflectionUtil.forName(type);
		}
		this.fnMapper = (FunctionMapper) in.readObject();
		this.varMapper = (VariableMapper) in.readObject();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.el.ValueExpression#setValue(javax.el.ELContext,
	 *      java.lang.Object)
	 */
	public void setValue(ELContext context, Object value)
			throws PropertyNotFoundException, PropertyNotWritableException, ELException {
		EvaluationContext ctx = new EvaluationContext(context, this.fnMapper, this.varMapper);
		this.getNode().setValue(ctx, value);
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(this.expr);
		out.writeUTF((this.expectedType != null) ? this.expectedType.getName() : "");
		out.writeObject(this.fnMapper);
		out.writeObject(this.varMapper);
	}

	public String toString() {
		return "ValueExpression[" + this.expr + "]";
	}
}
