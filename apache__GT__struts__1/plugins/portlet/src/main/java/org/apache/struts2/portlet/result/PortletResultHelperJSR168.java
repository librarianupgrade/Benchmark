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
package org.apache.struts2.portlet.result;

import javax.portlet.*;
import java.io.IOException;

/**
 * PortletResultHelperJSR168 implements PortletResultHelper for Portlet 1.0 API (JSR168).
 *
 * @author Rene Gielen
 */
public class PortletResultHelperJSR168 implements PortletResultHelper {

	/**
	 * Set a render parameter, abstracted from the used Portlet API version. This implementation assumes that the given
	 * response must be a {@link javax.portlet.ActionResponse}, as JSR168 implies.
	 *
	 * @param response The response to set the parameter on.
	 * @param key      The parameter key to set.
	 * @param value    The parameter value to set.
	 */
	public void setRenderParameter(PortletResponse response, String key, String value) {
		((ActionResponse) response).setRenderParameter(key, value);
	}

	/**
	 * Set a portlet mode, abstracted from the used Portlet API version. This implementation assumes that the given
	 * response must be a {@link javax.portlet.ActionResponse}, as JSR168 implies.
	 *
	 * @param response    The response to set the portlet mode on.
	 * @param portletMode The portlet mode to set.
	 */
	public void setPortletMode(PortletResponse response, PortletMode portletMode) throws PortletModeException {
		((ActionResponse) response).setPortletMode(portletMode);
	}

	/**
	 * Call a dispatcher's include method, abstracted from the used Portlet API version. This implementation assumes
	 * that the given the request must be a {@link javax.portlet.RenderRequest} and the response must be a {@link
	 * javax.portlet.RenderResponse}, as JSR168 implies.
	 *
	 * @param dispatcher  The dispatcher to call the include method on.
	 * @param contentType The content type to set for the response.
	 * @param request     The request to use for including
	 * @param response    The response to use for including
	 */
	public void include(PortletRequestDispatcher dispatcher, String contentType, PortletRequest request,
			PortletResponse response) throws IOException, PortletException {
		RenderRequest req = (RenderRequest) request;
		RenderResponse res = (RenderResponse) response;
		res.setContentType(contentType);
		dispatcher.include(req, res);
	}

}