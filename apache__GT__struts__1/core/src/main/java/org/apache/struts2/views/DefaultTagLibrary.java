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
package org.apache.struts2.views;

import com.opensymphony.xwork2.util.ValueStack;
import org.apache.struts2.views.freemarker.tags.StrutsModels;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The default Struts tag library
 */
public class DefaultTagLibrary implements TagLibraryModelProvider {

	public Object getModels(ValueStack stack, HttpServletRequest req, HttpServletResponse res) {

		return new StrutsModels(stack, req, res);
	}

	public Object getFreemarkerModels(ValueStack stack, HttpServletRequest req, HttpServletResponse res) {
		return getModels(stack, req, res);
	}

}
