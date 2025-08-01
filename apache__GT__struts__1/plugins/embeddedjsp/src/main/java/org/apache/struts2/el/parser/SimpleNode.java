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
import javax.el.MethodInfo;
import javax.el.PropertyNotWritableException;

import org.apache.struts2.el.lang.ELSupport;
import org.apache.struts2.el.lang.EvaluationContext;
import org.apache.struts2.el.util.MessageFactory;

/**
 * @author Jacob Hookom [jacob@hookom.net]
 * @version $Change: 181177 $$DateTime: 2001/06/26 08:45:09 $$Author: markt $
 */
public abstract class SimpleNode extends ELSupport implements Node {
	protected Node parent;

	protected Node[] children;

	protected int id;

	protected String image;

	public SimpleNode(int i) {
		id = i;
	}

	public void jjtOpen() {
	}

	public void jjtClose() {
	}

	public void jjtSetParent(Node n) {
		parent = n;
	}

	public Node jjtGetParent() {
		return parent;
	}

	public void jjtAddChild(Node n, int i) {
		if (children == null) {
			children = new Node[i + 1];
		} else if (i >= children.length) {
			Node c[] = new Node[i + 1];
			System.arraycopy(children, 0, c, 0, children.length);
			children = c;
		}
		children[i] = n;
	}

	public Node jjtGetChild(int i) {
		return children[i];
	}

	public int jjtGetNumChildren() {
		return (children == null) ? 0 : children.length;
	}

	/*
	 * You can override these two methods in subclasses of SimpleNode to
	 * customize the way the node appears when the tree is dumped. If your
	 * output uses more than one line you should override toString(String),
	 * otherwise overriding toString() is probably all you need to do.
	 */

	public String toString() {
		if (this.image != null) {
			return ELParserTreeConstants.jjtNodeName[id] + "[" + this.image + "]";
		}
		return ELParserTreeConstants.jjtNodeName[id];
	}

	public String toString(String prefix) {
		return prefix + toString();
	}

	/*
	 * Override this method if you want to customize how the node dumps out its
	 * children.
	 */

	public void dump(String prefix) {
		System.out.println(toString(prefix));
		if (children != null) {
			for (int i = 0; i < children.length; ++i) {
				SimpleNode n = (SimpleNode) children[i];
				if (n != null) {
					n.dump(prefix + " ");
				}
			}
		}
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public Class getType(EvaluationContext ctx) throws ELException {
		throw new UnsupportedOperationException();
	}

	public Object getValue(EvaluationContext ctx) throws ELException {
		throw new UnsupportedOperationException();
	}

	public boolean isReadOnly(EvaluationContext ctx) throws ELException {
		return true;
	}

	public void setValue(EvaluationContext ctx, Object value) throws ELException {
		throw new PropertyNotWritableException(MessageFactory.get("error.syntax.set"));
	}

	public void accept(NodeVisitor visitor) throws Exception {
		visitor.visit(this);
		if (this.children != null && this.children.length > 0) {
			for (int i = 0; i < this.children.length; i++) {
				this.children[i].accept(visitor);
			}
		}
	}

	public Object invoke(EvaluationContext ctx, Class[] paramTypes, Object[] paramValues) throws ELException {
		throw new UnsupportedOperationException();
	}

	public MethodInfo getMethodInfo(EvaluationContext ctx, Class[] paramTypes) throws ELException {
		throw new UnsupportedOperationException();
	}
}
