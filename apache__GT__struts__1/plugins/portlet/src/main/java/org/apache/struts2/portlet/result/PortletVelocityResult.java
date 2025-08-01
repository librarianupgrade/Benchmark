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

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.inject.Inject;
import com.opensymphony.xwork2.util.ValueStack;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.StrutsConstants;
import org.apache.struts2.result.StrutsResultSupport;
import org.apache.struts2.portlet.PortletConstants;
import org.apache.struts2.portlet.PortletPhase;
import org.apache.struts2.portlet.context.PortletActionContext;
import org.apache.struts2.views.JspSupportServlet;
import org.apache.struts2.views.velocity.VelocityManager;
import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;

import javax.portlet.ActionResponse;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.PageContext;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * <!-- START SNIPPET: description -->
 *
 * Using the Servlet container's {@link JspFactory}, this result mocks a JSP
 * execution environment and then displays a Velocity template that will be
 * streamed directly to the servlet output.
 *
 * <!-- END SNIPPET: description -->
 * <p><b>This result type takes the following parameters: </b></p>
 *
 * <!-- START SNIPPET: params -->
 *
 * <ul>
 *
 * <li><b>location (default) </b>- the location of the template to process.
 * </li>
 *
 * <li><b>parse </b>- true by default. If set to false, the location param
 * will not be parsed for Ognl expressions.</li>
 *
 * </ul>
 * <p>
 * This result follows the same rules from {@link StrutsResultSupport}.
 * </p>
 *
 * <!-- END SNIPPET: params -->
 *
 * <p><b>Example: </b></p>
 *
 * <pre>
 * &lt;!-- START SNIPPET: example --&gt;
 *  &lt;result name=&quot;success&quot; type=&quot;velocity&quot;&gt;
 *    &lt;param name=&quot;location&quot;&gt;foo.vm&lt;/param&gt;
 *  &lt;/result&gt;
 *  &lt;!-- END SNIPPET: example --&gt;
 * </pre>
 *
 */
public class PortletVelocityResult extends StrutsResultSupport {

	private static final long serialVersionUID = -8241086555872212274L;

	private static final Logger LOG = LogManager.getLogger(PortletVelocityResult.class);

	private String defaultEncoding;
	private VelocityManager velocityManager;
	private JspFactory jspFactory = JspFactory.getDefaultFactory();

	public PortletVelocityResult() {
		super();
	}

	public PortletVelocityResult(String location) {
		super(location);
	}

	@Inject
	public void setVelocityManager(VelocityManager mgr) {
		this.velocityManager = mgr;
	}

	@Inject(StrutsConstants.STRUTS_I18N_ENCODING)
	public void setDefaultEncoding(String encoding) {
		this.defaultEncoding = encoding;
	}

	/* (non-Javadoc)
	 * @see org.apache.struts2.result.StrutsResultSupport#doExecute(java.lang.String, com.opensymphony.xwork2.ActionInvocation)
	 */
	public void doExecute(String location, ActionInvocation invocation) throws Exception {
		PortletPhase phase = PortletActionContext.getPhase();
		if (phase.isAction()) {
			executeActionResult(location, invocation);
		} else if (phase.isRender()) {
			executeRenderResult(location, invocation);
		}
	}

	/**
	 * Executes the result
	 *
	 * @param location The location string
	 * @param invocation The action invocation
	 */
	private void executeActionResult(String location, ActionInvocation invocation) {
		ActionResponse res = PortletActionContext.getActionResponse();
		// View is rendered outside an action...uh oh...
		res.setRenderParameter(PortletConstants.ACTION_PARAM, "freemarkerDirect");
		res.setRenderParameter("location", location);
		res.setRenderParameter(PortletConstants.MODE_PARAM,
				PortletActionContext.getRequest().getPortletMode().toString());
	}

	/**
	 * Creates a Velocity context from the action, loads a Velocity template and
	 * executes the template. Output is written to the servlet output stream.
	 *
	 * @param finalLocation the location of the Velocity template
	 * @param invocation an encapsulation of the action execution state.
	 * @throws Exception if an error occurs when creating the Velocity context,
	 *         loading or executing the template or writing output to the
	 *         servlet response stream.
	 */
	public void executeRenderResult(String finalLocation, ActionInvocation invocation) throws Exception {
		ValueStack stack = ActionContext.getContext().getValueStack();

		HttpServletRequest request = ServletActionContext.getRequest();
		HttpServletResponse response = ServletActionContext.getResponse();
		ServletContext servletContext = ServletActionContext.getServletContext();
		Servlet servlet = JspSupportServlet.jspSupportServlet;

		velocityManager.init(servletContext);

		boolean usedJspFactory = false;
		PageContext pageContext = ActionContext.getContext().getPageContext();

		if (pageContext == null && servlet != null) {
			pageContext = jspFactory.getPageContext(servlet, request, response, null, true, 8192, true);
			ActionContext.getContext().withPageContext(pageContext);
			usedJspFactory = true;
		}

		try {
			String encoding = getEncoding(finalLocation);
			String contentType = getContentType(finalLocation);

			if (encoding != null) {
				contentType = contentType + ";charset=" + encoding;
			}
			response.setContentType(contentType);
			Template t = getTemplate(stack, velocityManager.getVelocityEngine(), invocation, finalLocation, encoding);

			Context context = createContext(velocityManager, stack, request, response, finalLocation);
			Writer writer = new OutputStreamWriter(response.getOutputStream(), encoding);

			t.merge(context, writer);

			// always flush the writer (we used to only flush it if this was a
			// jspWriter, but someone asked
			// to do it all the time (WW-829). Since Velocity support is being
			// deprecated, we'll oblige :)
			writer.flush();
		} catch (Exception e) {
			LOG.error("Unable to render Velocity Template, '" + finalLocation + "'", e);
			throw e;
		} finally {
			if (usedJspFactory) {
				jspFactory.releasePageContext(pageContext);
			}
		}
	}

	/**
	 * Retrieve the content type for this template. <br>People can override
	 * this method if they want to provide specific content types for specific
	 * templates (eg text/xml).
	 *
	 * @param templateLocation location of templates
	 *
	 * @return The content type associated with this template (default
	 *         "text/html")
	 */
	protected String getContentType(String templateLocation) {
		return "text/html";
	}

	/**
	 * Retrieve the encoding for this template. <br>People can override this
	 * method if they want to provide specific encodings for specific templates.
	 *
	 * @param templateLocation location of templates
	 *
	 * @return The encoding associated with this template (defaults to the value
	 *         of 'struts.i18n.encoding' property)
	 */
	protected String getEncoding(String templateLocation) {
		String encoding = defaultEncoding;
		if (encoding == null) {
			encoding = System.getProperty("file.encoding");
		}
		if (encoding == null) {
			encoding = "UTF-8";
		}
		return encoding;
	}

	/**
	 * Given a value stack, a Velocity engine, and an action invocation, this
	 * method returns the appropriate Velocity template to render.
	 *
	 * @param stack the value stack to resolve the location again (when parse
	 *        equals true)
	 * @param velocity the velocity engine to process the request against
	 * @param invocation an encapsulation of the action execution state.
	 * @param location the location of the template
	 * @param encoding the charset encoding of the template
	 * @return the template to render
	 * @throws Exception when the requested template could not be found
	 */
	protected Template getTemplate(ValueStack stack, VelocityEngine velocity, ActionInvocation invocation,
			String location, String encoding) throws Exception {
		if (!location.startsWith("/")) {
			location = invocation.getProxy().getNamespace() + "/" + location;
		}
		return velocity.getTemplate(location, encoding);
	}

	/**
	 * Creates the VelocityContext that we'll use to render this page.
	 *
	 * @param velocityManager a reference to the velocityManager to use
	 * @param stack the value stack to resolve the location against (when parse
	 *        equals true)
	 * @param request servlet request
	 * @param response servlet response
	 * @param location the name of the template that is being used
	 * @return the a minted Velocity context.
	 */
	protected Context createContext(VelocityManager velocityManager, ValueStack stack, HttpServletRequest request,
			HttpServletResponse response, String location) {
		return velocityManager.createContext(stack, request, response);
	}
}
