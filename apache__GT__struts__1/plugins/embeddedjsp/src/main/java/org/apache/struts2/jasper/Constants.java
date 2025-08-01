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
package org.apache.struts2.jasper;

/**
 * Some constants and other global data that are used by the compiler and the runtime.
 *
 * @author Anil K. Vijendran
 * @author Harish Prabandham
 * @author Shawn Bayern
 * @author Mark Roth
 */
public class Constants {

	/**
	 * The base class of the generated servlets. 
	 */
	public static final String JSP_SERVLET_BASE = System.getProperty(
			"org.apache.struts2.jasper.Constants.JSP_SERVLET_BASE", "org.apache.struts2.jasper.runtime.HttpJspBase");

	/**
	 * _jspService is the name of the method that is called by 
	 * HttpJspBase.service(). This is where most of the code generated
	 * from JSPs go.
	 */
	public static final String SERVICE_METHOD_NAME = System
			.getProperty("org.apache.struts2.jasper.Constants.SERVICE_METHOD_NAME", "_jspService");

	/**
	 * Default servlet content type.
	 */
	public static final String SERVLET_CONTENT_TYPE = "text/html";

	/**
	 * These classes/packages are automatically imported by the
	 * generated code. 
	 */
	public static final String[] STANDARD_IMPORTS = { "javax.servlet.*", "javax.servlet.http.*",
			"javax.servlet.jsp.*" };

	/**
	 * ServletContext attribute for classpath. This is tomcat specific. 
	 * Other servlet engines may choose to support this attribute if they 
	 * want to have this JSP engine running on them. 
	 */
	public static final String SERVLET_CLASSPATH = System
			.getProperty("org.apache.struts2.jasper.Constants.SERVLET_CLASSPATH", "org.apache.catalina.jsp_classpath");

	/**
	 * Request attribute for <code>&lt;jsp-file&gt;</code> element of a
	 * servlet definition.  If present on a request, this overrides the
	 * value returned by <code>request.getServletPath()</code> to select
	 * the JSP page to be executed.
	 */
	public static final String JSP_FILE = System.getProperty("org.apache.struts2.jasper.Constants.JSP_FILE",
			"org.apache.catalina.jsp_file");

	/**
	 * Default size of the JSP buffer.
	 */
	public static final int DEFAULT_BUFFER_SIZE = 8 * 1024;

	/**
	 * Default size for the tag buffers.
	 */
	public static final int DEFAULT_TAG_BUFFER_SIZE = 512;

	/**
	 * Default tag handler pool size.
	 */
	public static final int MAX_POOL_SIZE = 5;

	/**
	 * The query parameter that causes the JSP engine to just
	 * pregenerated the servlet but not invoke it. 
	 */
	public static final String PRECOMPILE = System.getProperty("org.apache.struts2.jasper.Constants.PRECOMPILE",
			"jsp_precompile");

	/**
	 * The default package name for compiled jsp pages.
	 */
	public static final String JSP_PACKAGE_NAME = System
			.getProperty("org.apache.struts2.jasper.Constants.JSP_PACKAGE_NAME", "org.apache.jsp");

	/**
	 * The default package name for tag handlers generated from tag files
	 */
	public static final String TAG_FILE_PACKAGE_NAME = System
			.getProperty("org.apache.struts2.jasper.Constants.TAG_FILE_PACKAGE_NAME", "org.apache.jsp.tag");

	/**
	 * Servlet context and request attributes that the JSP engine
	 * uses. 
	 */
	public static final String INC_SERVLET_PATH = "javax.servlet.include.servlet_path";
	public static final String TMP_DIR = "javax.servlet.context.tempdir";

	// Must be kept in sync with org/apache/catalina/Globals.java
	public static final String ALT_DD_ATTR = System.getProperty("org.apache.struts2.jasper.Constants.ALT_DD_ATTR",
			"org.apache.catalina.deploy.alt_dd");

	/**
	 * Public Id and the Resource path (of the cached copy) 
	 * of the DTDs for tag library descriptors. 
	 */
	public static final String TAGLIB_DTD_PUBLIC_ID_11 = "-//Sun Microsystems, Inc.//DTD JSP Tag Library 1.1//EN";
	public static final String TAGLIB_DTD_RESOURCE_PATH_11 = "/javax/servlet/jsp/resources/web-jsptaglibrary_1_1.dtd";
	public static final String TAGLIB_DTD_PUBLIC_ID_12 = "-//Sun Microsystems, Inc.//DTD JSP Tag Library 1.2//EN";
	public static final String TAGLIB_DTD_RESOURCE_PATH_12 = "/javax/servlet/jsp/resources/web-jsptaglibrary_1_2.dtd";

	/**
	 * Public Id and the Resource path (of the cached copy) 
	 * of the DTDs for web application deployment descriptors
	 */
	public static final String WEBAPP_DTD_PUBLIC_ID_22 = "-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN";
	public static final String WEBAPP_DTD_RESOURCE_PATH_22 = "/javax/servlet/resources/web-app_2_2.dtd";
	public static final String WEBAPP_DTD_PUBLIC_ID_23 = "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN";
	public static final String WEBAPP_DTD_RESOURCE_PATH_23 = "/javax/servlet/resources/web-app_2_3.dtd";

	/**
	 * List of the Public IDs that we cache, and their
	 * associated location. This is used by 
	 * an EntityResolver to return the location of the
	 * cached copy of a DTD.
	 */
	public static final String[] CACHED_DTD_PUBLIC_IDS = { TAGLIB_DTD_PUBLIC_ID_11, TAGLIB_DTD_PUBLIC_ID_12,
			WEBAPP_DTD_PUBLIC_ID_22, WEBAPP_DTD_PUBLIC_ID_23, };
	public static final String[] CACHED_DTD_RESOURCE_PATHS = { TAGLIB_DTD_RESOURCE_PATH_11, TAGLIB_DTD_RESOURCE_PATH_12,
			WEBAPP_DTD_RESOURCE_PATH_22, WEBAPP_DTD_RESOURCE_PATH_23, };

	/**
	 * Default URLs to download the pluging for Netscape and IE.
	 */
	public static final String NS_PLUGIN_URL = "http://java.sun.com/products/plugin/";

	public static final String IE_PLUGIN_URL = "http://java.sun.com/products/plugin/1.2.2/jinstall-1_2_2-win.cab#Version=1,2,2,0";

	/**
	 * Prefix to use for generated temporary variable names
	 */
	public static final String TEMP_VARIABLE_NAME_PREFIX = System
			.getProperty("org.apache.struts2.jasper.Constants.TEMP_VARIABLE_NAME_PREFIX", "_jspx_temp");

	/**
	 * A replacement char for "\$".
	 * XXX This is a hack to avoid changing EL interpreter to recognize "\$"
	 * @deprecated
	 */
	public static final char ESC = '\u001b';
	/**
	 * @deprecated
	 */
	public static final String ESCStr = "'\\u001b'";

	/**
	 * Has security been turned on?
	 */
	public static final boolean IS_SECURITY_ENABLED = (System.getSecurityManager() != null);

	/**
	 * The name of the path parameter used to pass the session identifier
	 * back and forth with the client.
	 */
	public static final String SESSION_PARAMETER_NAME = System.getProperty("org.apache.catalina.SESSION_PARAMETER_NAME",
			"jsessionid");

}
