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
package org.apache.struts2.jasper.servlet;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.SingleThreadModel;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.tagext.TagInfo;

import org.apache.struts2.jasper.JasperException;
import org.apache.struts2.jasper.JspCompilationContext;
import org.apache.struts2.jasper.Options;
import org.apache.struts2.jasper.compiler.ErrorDispatcher;
import org.apache.struts2.jasper.compiler.JavacErrorDetail;
import org.apache.struts2.jasper.compiler.JspRuntimeContext;
import org.apache.struts2.jasper.compiler.Localizer;
import org.apache.struts2.jasper.runtime.JspSourceDependent;
import org.apache.struts2.jasper.runtime.InstanceHelper;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.InstanceManager;

/**
 * The JSP engine (a.k.a Jasper).
 *
 * The servlet container is responsible for providing a
 * URLClassLoader for the web application context Jasper
 * is being used in. Jasper will try get the Tomcat
 * ServletContext attribute for its ServletContext class
 * loader, if that fails, it uses the parent class loader.
 * In either case, it must be a URLClassLoader.
 *
 * @author Anil K. Vijendran
 * @author Harish Prabandham
 * @author Remy Maucherat
 * @author Kin-man Chung
 * @author Glenn Nielsen
 * @author Tim Fennell
 */

public class JspServletWrapper {

	// Logger
	private Log log = LogFactory.getLog(JspServletWrapper.class);

	private Servlet theServlet;
	private String jspUri;
	private Class servletClass;
	private Class tagHandlerClass;
	private JspCompilationContext ctxt;
	private long available = 0L;
	private ServletConfig config;
	private Options options;
	private boolean firstTime = true;
	private boolean reload = true;
	private boolean isTagFile;
	private int tripCount;
	private JasperException compileException;
	private long servletClassLastModifiedTime;
	private long lastModificationTest = 0L;

	/*
	 * JspServletWrapper for JSP pages.
	 */
	public JspServletWrapper(ServletConfig config, Options options, String jspUri, boolean isErrorPage,
			JspRuntimeContext rctxt) throws JasperException {

		this.isTagFile = false;
		this.config = config;
		this.options = options;
		this.jspUri = jspUri;
		ctxt = new JspCompilationContext(jspUri, isErrorPage, options, config.getServletContext(), this, rctxt, null);
	}

	/*
	 * JspServletWrapper for tag files.
	 */
	public JspServletWrapper(ServletContext servletContext, Options options, String tagFilePath, TagInfo tagInfo,
			JspRuntimeContext rctxt, URL tagFileJarUrl) throws JasperException {

		this.isTagFile = true;
		this.config = null; // not used
		this.options = options;
		this.jspUri = tagFilePath;
		this.tripCount = 0;
		ctxt = new JspCompilationContext(jspUri, tagInfo, options, servletContext, this, rctxt, tagFileJarUrl);
	}

	public JspCompilationContext getJspEngineContext() {
		return ctxt;
	}

	public void setReload(boolean reload) {
		this.reload = reload;
	}

	public Servlet getServlet() throws ServletException, IOException, FileNotFoundException {
		if (reload) {
			synchronized (this) {
				// Synchronizing on jsw enables simultaneous loading
				// of different pages, but not the same page.
				if (reload) {
					// This is to maintain the original protocol.
					destroy();

					Servlet servlet = null;

					try {
						servletClass = ctxt.load();
						servlet = (Servlet) servletClass.newInstance();
						final InstanceManager instanceManager = InstanceHelper.getServletInstanceManager(config);
						if (instanceManager != null) {
							InstanceHelper.postConstruct(instanceManager, servlet);
						}
					} catch (IllegalAccessException e) {
						throw new JasperException(e);
					} catch (InstantiationException e) {
						throw new JasperException(e);
					} catch (Exception e) {
						throw new JasperException(e);
					}

					servlet.init(config);

					if (!firstTime) {
						ctxt.getRuntimeContext().incrementJspReloadCount();
					}

					theServlet = servlet;
					reload = false;
				}
			}
		}
		return theServlet;
	}

	public ServletContext getServletContext() {
		return config.getServletContext();
	}

	/**
	 * Sets the compilation exception for this JspServletWrapper.
	 *
	 * @param je The compilation exception
	 */
	public void setCompilationException(JasperException je) {
		this.compileException = je;
	}

	/**
	 * Sets the last-modified time of the servlet class file associated with
	 * this JspServletWrapper.
	 *
	 * @param lastModified Last-modified time of servlet class
	 */
	public void setServletClassLastModifiedTime(long lastModified) {
		if (this.servletClassLastModifiedTime < lastModified) {
			synchronized (this) {
				if (this.servletClassLastModifiedTime < lastModified) {
					this.servletClassLastModifiedTime = lastModified;
					reload = true;
				}
			}
		}
	}

	/**
	 * Compile (if needed) and load a tag file
	 */
	public Class loadTagFile() throws JasperException {

		try {
			if (ctxt.isRemoved()) {
				throw new FileNotFoundException(jspUri);
			}
			if (options.getDevelopment() || firstTime) {
				synchronized (this) {
					firstTime = false;
					ctxt.compile();
				}
			} else {
				if (compileException != null) {
					throw compileException;
				}
			}

			if (reload) {
				tagHandlerClass = ctxt.load();
				reload = false;
			}
		} catch (FileNotFoundException ex) {
			throw new JasperException(ex);
		}

		return tagHandlerClass;
	}

	/**
	 * Compile and load a prototype for the Tag file.  This is needed
	 * when compiling tag files with circular dependencies.  A prototpe
	 * (skeleton) with no dependencies on other other tag files is
	 * generated and compiled.
	 */
	public Class loadTagFilePrototype() throws JasperException {

		ctxt.setPrototypeMode(true);
		try {
			return loadTagFile();
		} finally {
			ctxt.setPrototypeMode(false);
		}
	}

	/**
	 * Get a list of files that the current page has source dependency on.
	 */
	public java.util.List getDependants() {
		try {
			Object target;
			if (isTagFile) {
				if (reload) {
					tagHandlerClass = ctxt.load();
					reload = false;
				}
				target = tagHandlerClass.newInstance();
			} else {
				target = getServlet();
			}
			if (target != null && target instanceof JspSourceDependent) {
				return ((java.util.List) ((JspSourceDependent) target).getDependants());
			}
		} catch (Throwable ex) {
		}
		return null;
	}

	public boolean isTagFile() {
		return this.isTagFile;
	}

	public int incTripCount() {
		return tripCount++;
	}

	public int decTripCount() {
		return tripCount--;
	}

	public void service(HttpServletRequest request, HttpServletResponse response, boolean precompile)
			throws ServletException, IOException, FileNotFoundException {

		try {

			if (ctxt.isRemoved()) {
				throw new FileNotFoundException(jspUri);
			}

			if ((available > 0L) && (available < Long.MAX_VALUE)) {
				if (available > System.currentTimeMillis()) {
					response.setDateHeader("Retry-After", available);
					response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
							Localizer.getMessage("jsp.error.unavailable"));
					return;
				} else {
					// Wait period has expired. Reset.
					available = 0;
				}
			}

			/*
			 * (1) Compile
			 */
			if (options.getDevelopment() || firstTime) {
				synchronized (this) {
					firstTime = false;

					// The following sets reload to true, if necessary
					ctxt.compile();
				}
			} else {
				if (compileException != null) {
					// Throw cached compilation exception
					throw compileException;
				}
			}

			/*
			 * (2) (Re)load servlet class file
			 */
			getServlet();

			// If a page is to be precompiled only, return.
			if (precompile) {
				return;
			}

		} catch (ServletException ex) {
			if (options.getDevelopment()) {
				throw handleJspException(ex);
			} else {
				throw ex;
			}
		} catch (IOException ex) {
			if (options.getDevelopment()) {
				throw handleJspException(ex);
			} else {
				throw ex;
			}
		} catch (IllegalStateException ex) {
			if (options.getDevelopment()) {
				throw handleJspException(ex);
			} else {
				throw ex;
			}
		} catch (Exception ex) {
			if (options.getDevelopment()) {
				throw handleJspException(ex);
			} else {
				throw new JasperException(ex);
			}
		}

		try {

			/*
			 * (3) Service request
			 */
			if (theServlet instanceof SingleThreadModel) {
				// sync on the wrapper so that the freshness
				// of the page is determined right before servicing
				synchronized (this) {
					theServlet.service(request, response);
				}
			} else {
				theServlet.service(request, response);
			}

		} catch (UnavailableException ex) {
			String includeRequestUri = (String) request.getAttribute("javax.servlet.include.request_uri");
			if (includeRequestUri != null) {
				// This file was included. Throw an exception as
				// a response.sendError() will be ignored by the
				// servlet engine.
				throw ex;
			} else {
				int unavailableSeconds = ex.getUnavailableSeconds();
				if (unavailableSeconds <= 0) {
					unavailableSeconds = 60; // Arbitrary default
				}
				available = System.currentTimeMillis() + (unavailableSeconds * 1000L);
				response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, ex.getMessage());
			}
		} catch (ServletException ex) {
			if (options.getDevelopment()) {
				throw handleJspException(ex);
			} else {
				throw ex;
			}
		} catch (IOException ex) {
			if (options.getDevelopment()) {
				throw handleJspException(ex);
			} else {
				throw ex;
			}
		} catch (IllegalStateException ex) {
			if (options.getDevelopment()) {
				throw handleJspException(ex);
			} else {
				throw ex;
			}
		} catch (Exception ex) {
			if (options.getDevelopment()) {
				throw handleJspException(ex);
			} else {
				throw new JasperException(ex);
			}
		}
	}

	public void destroy() {
		if (theServlet != null) {
			theServlet.destroy();
			final InstanceManager instanceManager = InstanceHelper.getServletInstanceManager(config);
			if (instanceManager != null) {
				try {
					InstanceHelper.preDestroy(instanceManager, theServlet);
				} catch (Exception e) {
					// Log any exception, since it can't be passed along
					log.error(Localizer.getMessage("jsp.error.file.not.found", e.getMessage()), e);
				}
			}
		}
	}

	/**
	 * @return Returns the lastModificationTest.
	 */
	public long getLastModificationTest() {
		return lastModificationTest;
	}

	/**
	 * @param lastModificationTest The lastModificationTest to set.
	 */
	public void setLastModificationTest(long lastModificationTest) {
		this.lastModificationTest = lastModificationTest;
	}

	/**
	 * <p>Attempts to construct a JasperException that contains helpful information
	 * about what went wrong. Uses the JSP compiler system to translate the line
	 * number in the generated servlet that originated the exception to a line
	 * number in the JSP.  Then constructs an exception containing that
	 * information, and a snippet of the JSP to help debugging.
	 * Please see http://issues.apache.org/bugzilla/show_bug.cgi?id=37062 and
	 * http://www.tfenne.com/jasper/ for more details.
	 *</p>
	 *
	 * @param ex the exception that was the cause of the problem.
	 * @return a JasperException with more detailed information
	 */
	protected JasperException handleJspException(Exception ex) {
		try {
			Throwable realException = ex;
			if (ex instanceof ServletException) {
				realException = ((ServletException) ex).getRootCause();
			}

			// First identify the stack frame in the trace that represents the JSP
			StackTraceElement[] frames = realException.getStackTrace();
			StackTraceElement jspFrame = null;

			for (int i = 0; i < frames.length; ++i) {
				if (frames[i].getClassName().equals(this.getServlet().getClass().getName())) {
					jspFrame = frames[i];
					break;
				}
			}

			if (jspFrame == null) {
				// If we couldn't find a frame in the stack trace corresponding
				// to the generated servlet class, we can't really add anything
				return new JasperException(ex);
			} else {
				int javaLineNumber = jspFrame.getLineNumber();
				JavacErrorDetail detail = ErrorDispatcher.createJavacError(jspFrame.getMethodName(),
						this.ctxt.getCompiler().getPageNodes(), null, javaLineNumber, ctxt);

				// If the line number is less than one we couldn't find out
				// where in the JSP things went wrong
				int jspLineNumber = detail.getJspBeginLineNumber();
				if (jspLineNumber < 1) {
					throw new JasperException(ex);
				}

				if (options.getDisplaySourceFragment()) {
					return new JasperException(
							Localizer.getMessage("jsp.exception", detail.getJspFileName(), "" + jspLineNumber) + "\n\n"
									+ detail.getJspExtract() + "\n\nStacktrace:",
							ex);

				} else {
					return new JasperException(
							Localizer.getMessage("jsp.exception", detail.getJspFileName(), "" + jspLineNumber), ex);
				}
			}
		} catch (Exception je) {
			// If anything goes wrong, just revert to the original behaviour
			if (ex instanceof JasperException) {
				return (JasperException) ex;
			} else {
				return new JasperException(ex);
			}
		}
	}

}
