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
package org.apache.struts2.views.jsp.ui;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.components.Component;
import org.apache.struts2.components.Label;

import com.opensymphony.xwork2.util.ValueStack;

/**
 * @see Label
 */
public class LabelTag extends AbstractUITag {

	private static final long serialVersionUID = 4008321310097730458L;

	protected String forAttr;

	@Override
	public Component getBean(ValueStack stack, HttpServletRequest req, HttpServletResponse res) {
		return new Label(stack, req, res);
	}

	@Override
	protected void populateParams() {
		super.populateParams();

		((Label) component).setFor(forAttr);
	}

	public void setFor(String aFor) {
		this.forAttr = aFor;
	}

	@Override
	/**
	 * Must declare the setter at the descendant Tag class level in order for the tag handler to locate the method.
	 */
	public void setPerformClearTagStateForTagPoolingServers(boolean performClearTagStateForTagPoolingServers) {
		super.setPerformClearTagStateForTagPoolingServers(performClearTagStateForTagPoolingServers);
	}

	@Override
	protected void clearTagStateForTagPoolingServers() {
		if (getPerformClearTagStateForTagPoolingServers() == false) {
			return; // If flag is false (default setting), do not perform any state clearing.
		}
		super.clearTagStateForTagPoolingServers();
		this.forAttr = null;
	}

}
