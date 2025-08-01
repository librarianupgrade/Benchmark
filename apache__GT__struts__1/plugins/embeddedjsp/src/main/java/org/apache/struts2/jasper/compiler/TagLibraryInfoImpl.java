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
package org.apache.struts2.jasper.compiler;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.servlet.jsp.tagext.FunctionInfo;
import javax.servlet.jsp.tagext.PageData;
import javax.servlet.jsp.tagext.TagAttributeInfo;
import javax.servlet.jsp.tagext.TagExtraInfo;
import javax.servlet.jsp.tagext.TagFileInfo;
import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagLibraryInfo;
import javax.servlet.jsp.tagext.TagLibraryValidator;
import javax.servlet.jsp.tagext.TagVariableInfo;
import javax.servlet.jsp.tagext.ValidationMessage;
import javax.servlet.jsp.tagext.VariableInfo;

import org.apache.struts2.jasper.JasperException;
import org.apache.struts2.jasper.JspCompilationContext;
import org.apache.struts2.jasper.xmlparser.ParserUtils;
import org.apache.struts2.jasper.xmlparser.TreeNode;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

/**
 * Implementation of the TagLibraryInfo class from the JSP spec.
 * 
 * @author Anil K. Vijendran
 * @author Mandar Raje
 * @author Pierre Delisle
 * @author Kin-man Chung
 * @author Jan Luehe
 */
class TagLibraryInfoImpl extends TagLibraryInfo implements TagConstants {

	// Logger
	private Log log = LogFactory.getLog(TagLibraryInfoImpl.class);

	private JspCompilationContext ctxt;

	private PageInfo pi;

	private ErrorDispatcher err;

	private ParserController parserController;

	private final void print(String name, String value, PrintWriter w) {
		if (value != null) {
			w.print(name + " = {\n\t");
			w.print(value);
			w.print("\n}\n");
		}
	}

	public String toString() {
		StringWriter sw = new StringWriter();
		PrintWriter out = new PrintWriter(sw);
		print("tlibversion", tlibversion, out);
		print("jspversion", jspversion, out);
		print("shortname", shortname, out);
		print("urn", urn, out);
		print("info", info, out);
		print("uri", uri, out);
		print("tagLibraryValidator", "" + tagLibraryValidator, out);

		for (int i = 0; i < tags.length; i++)
			out.println(tags[i].toString());

		for (int i = 0; i < tagFiles.length; i++)
			out.println(tagFiles[i].toString());

		for (int i = 0; i < functions.length; i++)
			out.println(functions[i].toString());

		return sw.toString();
	}

	// XXX FIXME
	// resolveRelativeUri and/or getResourceAsStream don't seem to properly
	// handle relative paths when dealing when home and getDocBase are set
	// the following is a workaround until these problems are resolved.
	private InputStream getResourceAsStream(String uri) throws FileNotFoundException {
		try {
			// see if file exists on the filesystem first
			String real = ctxt.getRealPath(uri);
			if (real == null) {
				return ctxt.getResourceAsStream(uri);
			} else {
				return new FileInputStream(real);
			}
		} catch (FileNotFoundException ex) {
			// if file not found on filesystem, get the resource through
			// the context
			return ctxt.getResourceAsStream(uri);
		}

	}

	/**
	 * Constructor.
	 */
	public TagLibraryInfoImpl(JspCompilationContext ctxt, ParserController pc, PageInfo pi, String prefix, String uriIn,
			String[] location, ErrorDispatcher err) throws JasperException {
		super(prefix, uriIn);

		this.ctxt = ctxt;
		this.parserController = pc;
		this.pi = pi;
		this.err = err;
		InputStream in = null;
		JarFile jarFile = null;

		if (location == null) {
			// The URI points to the TLD itself or to a JAR file in which the
			// TLD is stored
			location = generateTLDLocation(uri, ctxt);
		}

		try {
			if (!location[0].endsWith("jar")) {
				// Location points to TLD file
				try {
					in = getResourceAsStream(location[0]);
					if (in == null) {
						throw new FileNotFoundException(location[0]);
					}
				} catch (FileNotFoundException ex) {
					err.jspError("jsp.error.file.not.found", location[0]);
				}

				parseTLD(ctxt, location[0], in, null);
				// Add TLD to dependency list
				PageInfo pageInfo = ctxt.createCompiler().getPageInfo();
				if (pageInfo != null) {
					pageInfo.addDependant(location[0]);
				}
			} else {
				// Tag library is packaged in JAR file
				try {
					URL jarFileUrl = new URL("jar:" + location[0] + "!/");
					JarURLConnection conn = (JarURLConnection) jarFileUrl.openConnection();
					conn.setUseCaches(false);
					conn.connect();
					jarFile = conn.getJarFile();
					ZipEntry jarEntry = jarFile.getEntry(location[1]);
					in = jarFile.getInputStream(jarEntry);
					parseTLD(ctxt, location[0], in, jarFileUrl);
				} catch (Exception ex) {
					err.jspError("jsp.error.tld.unable_to_read", location[0], location[1], ex.toString());
				}
			}
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Throwable t) {
				}
			}
			if (jarFile != null) {
				try {
					jarFile.close();
				} catch (Throwable t) {
				}
			}
		}

	}

	public TagLibraryInfo[] getTagLibraryInfos() {
		Collection coll = pi.getTaglibs();
		return (TagLibraryInfo[]) coll.toArray(new TagLibraryInfo[0]);
	}

	/*
	 * @param ctxt The JSP compilation context @param uri The TLD's uri @param
	 * in The TLD's input stream @param jarFileUrl The JAR file containing the
	 * TLD, or null if the tag library is not packaged in a JAR
	 */
	private void parseTLD(JspCompilationContext ctxt, String uri, InputStream in, URL jarFileUrl)
			throws JasperException {
		Vector tagVector = new Vector();
		Vector tagFileVector = new Vector();
		Hashtable functionTable = new Hashtable();

		// Create an iterator over the child elements of our <taglib> element
		ParserUtils pu = new ParserUtils();
		TreeNode tld = pu.parseXMLDocument(uri, in);

		// Check to see if the <taglib> root element contains a 'version'
		// attribute, which was added in JSP 2.0 to replace the <jsp-version>
		// subelement
		this.jspversion = tld.findAttribute("version");

		// Process each child element of our <taglib> element
		Iterator list = tld.findChildren();

		while (list.hasNext()) {
			TreeNode element = (TreeNode) list.next();
			String tname = element.getName();

			if ("tlibversion".equals(tname) // JSP 1.1
					|| "tlib-version".equals(tname)) { // JSP 1.2
				this.tlibversion = element.getBody();
			} else if ("jspversion".equals(tname) || "jsp-version".equals(tname)) {
				this.jspversion = element.getBody();
			} else if ("shortname".equals(tname) || "short-name".equals(tname))
				this.shortname = element.getBody();
			else if ("uri".equals(tname))
				this.urn = element.getBody();
			else if ("info".equals(tname) || "description".equals(tname))
				this.info = element.getBody();
			else if ("validator".equals(tname))
				this.tagLibraryValidator = createValidator(element);
			else if ("tag".equals(tname))
				tagVector.addElement(createTagInfo(element, jspversion));
			else if ("tag-file".equals(tname)) {
				TagFileInfo tagFileInfo = createTagFileInfo(element, uri, jarFileUrl);
				tagFileVector.addElement(tagFileInfo);
			} else if ("function".equals(tname)) { // JSP2.0
				FunctionInfo funcInfo = createFunctionInfo(element);
				String funcName = funcInfo.getName();
				if (functionTable.containsKey(funcName)) {
					err.jspError("jsp.error.tld.fn.duplicate.name", funcName, uri);

				}
				functionTable.put(funcName, funcInfo);
			} else if ("display-name".equals(tname) || // Ignored elements
					"small-icon".equals(tname) || "large-icon".equals(tname) || "listener".equals(tname)) {
				;
			} else if ("taglib-extension".equals(tname)) {
				// Recognized but ignored
			} else {
				if (log.isWarnEnabled()) {
					log.warn(Localizer.getMessage("jsp.warning.unknown.element.in.taglib", tname));
				}
			}

		}

		if (tlibversion == null) {
			err.jspError("jsp.error.tld.mandatory.element.missing", "tlib-version");
		}
		if (jspversion == null) {
			err.jspError("jsp.error.tld.mandatory.element.missing", "jsp-version");
		}

		this.tags = new TagInfo[tagVector.size()];
		tagVector.copyInto(this.tags);

		this.tagFiles = new TagFileInfo[tagFileVector.size()];
		tagFileVector.copyInto(this.tagFiles);

		this.functions = new FunctionInfo[functionTable.size()];
		int i = 0;
		Enumeration enumeration = functionTable.elements();
		while (enumeration.hasMoreElements()) {
			this.functions[i++] = (FunctionInfo) enumeration.nextElement();
		}
	}

	/*
	 * @param uri The uri of the TLD @param ctxt The compilation context
	 * 
	 * @return String array whose first element denotes the path to the TLD. If
	 * the path to the TLD points to a jar file, then the second element denotes
	 * the name of the TLD entry in the jar file, which is hardcoded to
	 * META-INF/taglib.tld.
	 */
	private String[] generateTLDLocation(String uri, JspCompilationContext ctxt) throws JasperException {

		int uriType = TldLocationsCache.uriType(uri);
		if (uriType == TldLocationsCache.ABS_URI) {
			err.jspError("jsp.error.taglibDirective.absUriCannotBeResolved", uri);
		} else if (uriType == TldLocationsCache.NOROOT_REL_URI) {
			uri = ctxt.resolveRelativeUri(uri);
		}

		String[] location = new String[2];
		location[0] = uri;
		if (location[0].endsWith("jar")) {
			URL url = null;
			try {
				url = ctxt.getResource(location[0]);
			} catch (Exception ex) {
				err.jspError("jsp.error.tld.unable_to_get_jar", location[0], ex.toString());
			}
			if (url == null) {
				err.jspError("jsp.error.tld.missing_jar", location[0]);
			}
			location[0] = url.toString();
			location[1] = "META-INF/taglib.tld";
		}

		return location;
	}

	private TagInfo createTagInfo(TreeNode elem, String jspVersion) throws JasperException {

		String tagName = null;
		String tagClassName = null;
		String teiClassName = null;

		/*
		 * Default body content for JSP 1.2 tag handlers (<body-content> has
		 * become mandatory in JSP 2.0, because the default would be invalid for
		 * simple tag handlers)
		 */
		String bodycontent = "JSP";

		String info = null;
		String displayName = null;
		String smallIcon = null;
		String largeIcon = null;
		boolean dynamicAttributes = false;

		Vector attributeVector = new Vector();
		Vector variableVector = new Vector();
		Iterator list = elem.findChildren();
		while (list.hasNext()) {
			TreeNode element = (TreeNode) list.next();
			String tname = element.getName();

			if ("name".equals(tname)) {
				tagName = element.getBody();
			} else if ("tagclass".equals(tname) || "tag-class".equals(tname)) {
				tagClassName = element.getBody();
			} else if ("teiclass".equals(tname) || "tei-class".equals(tname)) {
				teiClassName = element.getBody();
			} else if ("bodycontent".equals(tname) || "body-content".equals(tname)) {
				bodycontent = element.getBody();
			} else if ("display-name".equals(tname)) {
				displayName = element.getBody();
			} else if ("small-icon".equals(tname)) {
				smallIcon = element.getBody();
			} else if ("large-icon".equals(tname)) {
				largeIcon = element.getBody();
			} else if ("icon".equals(tname)) {
				TreeNode icon = element.findChild("small-icon");
				if (icon != null) {
					smallIcon = icon.getBody();
				}
				icon = element.findChild("large-icon");
				if (icon != null) {
					largeIcon = icon.getBody();
				}
			} else if ("info".equals(tname) || "description".equals(tname)) {
				info = element.getBody();
			} else if ("variable".equals(tname)) {
				variableVector.addElement(createVariable(element));
			} else if ("attribute".equals(tname)) {
				attributeVector.addElement(createAttribute(element, jspVersion));
			} else if ("dynamic-attributes".equals(tname)) {
				dynamicAttributes = JspUtil.booleanValue(element.getBody());
			} else if ("example".equals(tname)) {
				// Ignored elements
			} else if ("tag-extension".equals(tname)) {
				// Ignored
			} else {
				if (log.isWarnEnabled()) {
					log.warn(Localizer.getMessage("jsp.warning.unknown.element.in.tag", tname));
				}
			}
		}

		TagExtraInfo tei = null;
		if (teiClassName != null && !teiClassName.equals("")) {
			try {
				Class teiClass = ctxt.getClassLoader().loadClass(teiClassName);
				tei = (TagExtraInfo) teiClass.newInstance();
			} catch (Exception e) {
				err.jspError("jsp.error.teiclass.instantiation", teiClassName, e);
			}
		}

		TagAttributeInfo[] tagAttributeInfo = new TagAttributeInfo[attributeVector.size()];
		attributeVector.copyInto(tagAttributeInfo);

		TagVariableInfo[] tagVariableInfos = new TagVariableInfo[variableVector.size()];
		variableVector.copyInto(tagVariableInfos);

		TagInfo taginfo = new TagInfo(tagName, tagClassName, bodycontent, info, this, tei, tagAttributeInfo,
				displayName, smallIcon, largeIcon, tagVariableInfos, dynamicAttributes);
		return taginfo;
	}

	/*
	 * Parses the tag file directives of the given TagFile and turns them into a
	 * TagInfo.
	 * 
	 * @param elem The <tag-file> element in the TLD @param uri The location of
	 * the TLD, in case the tag file is specified relative to it @param jarFile
	 * The JAR file, in case the tag file is packaged in a JAR
	 * 
	 * @return TagInfo correspoding to tag file directives
	 */
	private TagFileInfo createTagFileInfo(TreeNode elem, String uri, URL jarFileUrl) throws JasperException {

		String name = null;
		String path = null;

		Iterator list = elem.findChildren();
		while (list.hasNext()) {
			TreeNode child = (TreeNode) list.next();
			String tname = child.getName();
			if ("name".equals(tname)) {
				name = child.getBody();
			} else if ("path".equals(tname)) {
				path = child.getBody();
			} else if ("example".equals(tname)) {
				// Ignore <example> element: Bugzilla 33538
			} else if ("tag-extension".equals(tname)) {
				// Ignore <tag-extension> element: Bugzilla 33538
			} else if ("icon".equals(tname) || "display-name".equals(tname) || "description".equals(tname)) {
				// Ignore these elements: Bugzilla 38015
			} else {
				if (log.isWarnEnabled()) {
					log.warn(Localizer.getMessage("jsp.warning.unknown.element.in.tagfile", tname));
				}
			}
		}

		if (path.startsWith("/META-INF/tags")) {
			// Tag file packaged in JAR
			// See https://issues.apache.org/bugzilla/show_bug.cgi?id=46471
			// This needs to be removed once all the broken code that depends on
			// it has been removed
			ctxt.setTagFileJarUrl(path, jarFileUrl);
		} else if (!path.startsWith("/WEB-INF/tags")) {
			err.jspError("jsp.error.tagfile.illegalPath", path);
		}

		TagInfo tagInfo = TagFileProcessor.parseTagFileDirectives(parserController, name, path, jarFileUrl, this);
		return new TagFileInfo(name, path, tagInfo);
	}

	TagAttributeInfo createAttribute(TreeNode elem, String jspVersion) {
		String name = null;
		String type = null;
		String expectedType = null;
		String methodSignature = null;
		boolean required = false, rtexprvalue = false, reqTime = false, isFragment = false, deferredValue = false,
				deferredMethod = false;

		Iterator list = elem.findChildren();
		while (list.hasNext()) {
			TreeNode element = (TreeNode) list.next();
			String tname = element.getName();

			if ("name".equals(tname)) {
				name = element.getBody();
			} else if ("required".equals(tname)) {
				String s = element.getBody();
				if (s != null)
					required = JspUtil.booleanValue(s);
			} else if ("rtexprvalue".equals(tname)) {
				String s = element.getBody();
				if (s != null)
					rtexprvalue = JspUtil.booleanValue(s);
			} else if ("type".equals(tname)) {
				type = element.getBody();
				if ("1.2".equals(jspVersion) && (type.equals("Boolean") || type.equals("Byte")
						|| type.equals("Character") || type.equals("Double") || type.equals("Float")
						|| type.equals("Integer") || type.equals("Long") || type.equals("Object")
						|| type.equals("Short") || type.equals("String"))) {
					type = "java.lang." + type;
				}
			} else if ("fragment".equals(tname)) {
				String s = element.getBody();
				if (s != null) {
					isFragment = JspUtil.booleanValue(s);
				}
			} else if ("deferred-value".equals(tname)) {
				deferredValue = true;
				type = "javax.el.ValueExpression";
				TreeNode child = element.findChild("type");
				if (child != null) {
					expectedType = child.getBody();
					if (expectedType != null) {
						expectedType = expectedType.trim();
					}
				} else {
					expectedType = "java.lang.Object";
				}
			} else if ("deferred-method".equals(tname)) {
				deferredMethod = true;
				type = "javax.el.MethodExpression";
				TreeNode child = element.findChild("method-signature");
				if (child != null) {
					methodSignature = child.getBody();
					if (methodSignature != null) {
						methodSignature = methodSignature.trim();
					}
				} else {
					methodSignature = "java.lang.Object method()";
				}
			} else if ("description".equals(tname) || // Ignored elements
					false) {
				;
			} else {
				if (log.isWarnEnabled()) {
					log.warn(Localizer.getMessage("jsp.warning.unknown.element.in.attribute", tname));
				}
			}
		}

		if (isFragment) {
			/*
			 * According to JSP.C-3 ("TLD Schema Element Structure - tag"),
			 * 'type' and 'rtexprvalue' must not be specified if 'fragment' has
			 * been specified (this will be enforced by validating parser).
			 * Also, if 'fragment' is TRUE, 'type' is fixed at
			 * javax.servlet.jsp.tagext.JspFragment, and 'rtexprvalue' is fixed
			 * at true. See also JSP.8.5.2.
			 */
			type = "javax.servlet.jsp.tagext.JspFragment";
			rtexprvalue = true;
		}

		if (!rtexprvalue && type == null) {
			// According to JSP spec, for static values (those determined at
			// translation time) the type is fixed at java.lang.String.
			type = "java.lang.String";
		}

		return new TagAttributeInfo(name, required, type, rtexprvalue, isFragment, null, deferredValue, deferredMethod,
				expectedType, methodSignature);
	}

	TagVariableInfo createVariable(TreeNode elem) {
		String nameGiven = null;
		String nameFromAttribute = null;
		String className = "java.lang.String";
		boolean declare = true;
		int scope = VariableInfo.NESTED;

		Iterator list = elem.findChildren();
		while (list.hasNext()) {
			TreeNode element = (TreeNode) list.next();
			String tname = element.getName();
			if ("name-given".equals(tname))
				nameGiven = element.getBody();
			else if ("name-from-attribute".equals(tname))
				nameFromAttribute = element.getBody();
			else if ("variable-class".equals(tname))
				className = element.getBody();
			else if ("declare".equals(tname)) {
				String s = element.getBody();
				if (s != null)
					declare = JspUtil.booleanValue(s);
			} else if ("scope".equals(tname)) {
				String s = element.getBody();
				if (s != null) {
					if ("NESTED".equals(s)) {
						scope = VariableInfo.NESTED;
					} else if ("AT_BEGIN".equals(s)) {
						scope = VariableInfo.AT_BEGIN;
					} else if ("AT_END".equals(s)) {
						scope = VariableInfo.AT_END;
					}
				}
			} else if ("description".equals(tname) || // Ignored elements
					false) {
			} else {
				if (log.isWarnEnabled()) {
					log.warn(Localizer.getMessage("jsp.warning.unknown.element.in.variable", tname));
				}
			}
		}
		return new TagVariableInfo(nameGiven, nameFromAttribute, className, declare, scope);
	}

	private TagLibraryValidator createValidator(TreeNode elem) throws JasperException {

		String validatorClass = null;
		Map initParams = new Hashtable();

		Iterator list = elem.findChildren();
		while (list.hasNext()) {
			TreeNode element = (TreeNode) list.next();
			String tname = element.getName();
			if ("validator-class".equals(tname))
				validatorClass = element.getBody();
			else if ("init-param".equals(tname)) {
				String[] initParam = createInitParam(element);
				initParams.put(initParam[0], initParam[1]);
			} else if ("description".equals(tname) || // Ignored elements
					false) {
			} else {
				if (log.isWarnEnabled()) {
					log.warn(Localizer.getMessage("jsp.warning.unknown.element.in.validator", tname));
				}
			}
		}

		TagLibraryValidator tlv = null;
		if (validatorClass != null && !validatorClass.equals("")) {
			try {
				Class tlvClass = ctxt.getClassLoader().loadClass(validatorClass);
				tlv = (TagLibraryValidator) tlvClass.newInstance();
			} catch (Exception e) {
				err.jspError("jsp.error.tlvclass.instantiation", validatorClass, e);
			}
		}
		if (tlv != null) {
			tlv.setInitParameters(initParams);
		}
		return tlv;
	}

	String[] createInitParam(TreeNode elem) {
		String[] initParam = new String[2];

		Iterator list = elem.findChildren();
		while (list.hasNext()) {
			TreeNode element = (TreeNode) list.next();
			String tname = element.getName();
			if ("param-name".equals(tname)) {
				initParam[0] = element.getBody();
			} else if ("param-value".equals(tname)) {
				initParam[1] = element.getBody();
			} else if ("description".equals(tname)) {
				// Do nothing
			} else {
				if (log.isWarnEnabled()) {
					log.warn(Localizer.getMessage("jsp.warning.unknown.element.in.initParam", tname));
				}
			}
		}
		return initParam;
	}

	FunctionInfo createFunctionInfo(TreeNode elem) {

		String name = null;
		String klass = null;
		String signature = null;

		Iterator list = elem.findChildren();
		while (list.hasNext()) {
			TreeNode element = (TreeNode) list.next();
			String tname = element.getName();

			if ("name".equals(tname)) {
				name = element.getBody();
			} else if ("function-class".equals(tname)) {
				klass = element.getBody();
			} else if ("function-signature".equals(tname)) {
				signature = element.getBody();
			} else if ("display-name".equals(tname) || // Ignored elements
					"small-icon".equals(tname) || "large-icon".equals(tname) || "description".equals(tname)
					|| "example".equals(tname)) {
			} else {
				if (log.isWarnEnabled()) {
					log.warn(Localizer.getMessage("jsp.warning.unknown.element.in.function", tname));
				}
			}
		}

		return new FunctionInfo(name, klass, signature);
	}

	// *********************************************************************
	// Until javax.servlet.jsp.tagext.TagLibraryInfo is fixed

	/**
	 * The instance (if any) for the TagLibraryValidator class.
	 * 
	 * @return The TagLibraryValidator instance, if any.
	 */
	public TagLibraryValidator getTagLibraryValidator() {
		return tagLibraryValidator;
	}

	/**
	 * Translation-time validation of the XML document associated with the JSP
	 * page. This is a convenience method on the associated TagLibraryValidator
	 * class.
	 * 
	 * @param thePage
	 *            The JSP page object
	 * @return A string indicating whether the page is valid or not.
	 */
	public ValidationMessage[] validate(PageData thePage) {
		TagLibraryValidator tlv = getTagLibraryValidator();
		if (tlv == null)
			return null;

		String uri = getURI();
		if (uri.startsWith("/")) {
			uri = URN_JSPTLD + uri;
		}

		return tlv.validate(getPrefixString(), uri, thePage);
	}

	protected TagLibraryValidator tagLibraryValidator;
}
