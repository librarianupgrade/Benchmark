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
package org.apache.struts2.showcase.actionchaining;

import com.opensymphony.xwork2.ActionSupport;

public class ActionChain3 extends ActionSupport {

	private static final long serialVersionUID = -1456568865075250621L;

	private String actionChain1Property1;
	private String actionChain2Property1;
	private String actionChain3Property1 = "Property set in Action Chain 3";

	public String execute() throws Exception {
		return SUCCESS;
	}

	public String getActionChain1Property1() {
		return actionChain1Property1;
	}

	public void setActionChain1Property1(String actionChain1Property1) {
		this.actionChain1Property1 = actionChain1Property1;
	}

	public String getActionChain2Property1() {
		return actionChain2Property1;
	}

	public void setActionChain2Property1(String actionChain2Property1) {
		this.actionChain2Property1 = actionChain2Property1;
	}

	public String getActionChain3Property1() {
		return actionChain3Property1;
	}

	public void setActionChain3Property1(String actionChain3Property1) {
		this.actionChain3Property1 = actionChain3Property1;
	}
}
