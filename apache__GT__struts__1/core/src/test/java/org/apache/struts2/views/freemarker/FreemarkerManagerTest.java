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
package org.apache.struts2.views.freemarker;

import com.opensymphony.xwork2.util.fs.DefaultFileManagerFactory;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.Version;
import org.apache.commons.io.FileUtils;
import org.apache.struts2.StrutsInternalTestCase;
import org.apache.struts2.views.jsp.StrutsMockServletContext;

import javax.servlet.ServletContext;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * Test case for FreemarkerManager
 */
public class FreemarkerManagerTest extends StrutsInternalTestCase {

	public void testIfStrutsEncodingIsSetProperty() throws Exception {
		FreemarkerManager mgr = new FreemarkerManager();
		mgr.setEncoding("UTF-8");
		DefaultFileManagerFactory factory = new DefaultFileManagerFactory();
		container.inject(factory);
		mgr.setFileManagerFactory(factory);
		mgr.setThemeTemplateLoader(new FreemarkerThemeTemplateLoader());
		StrutsMockServletContext servletContext = new StrutsMockServletContext();
		servletContext.setAttribute(FreemarkerManager.CONFIG_SERVLET_CONTEXT_KEY, null);
		freemarker.template.Configuration conf = mgr.getConfiguration(servletContext);
		assertEquals(conf.getDefaultEncoding(), "UTF-8");
	}

	public void testTemplateLoaderBaseOnFile() throws Exception {
		// given
		DummyFreemarkerManager manager = new DummyFreemarkerManager();
		StrutsMockServletContext servletContext = new StrutsMockServletContext();
		servletContext.setAttribute(FreemarkerManager.CONFIG_SERVLET_CONTEXT_KEY, null);

		String tmpPath = "file://" + FileUtils.getTempDirectoryPath();

		// when
		manager.load(servletContext, tmpPath);

		// then
		assertTrue(true); // should pass
	}

	public void testIncompatibleImprovementsByOverriding() throws Exception {
		// given
		FreemarkerManager manager = new FreemarkerManager() {
			@Override
			protected Version getFreemarkerVersion(ServletContext servletContext) {
				return Configuration.VERSION_2_3_0;
			}
		};
		container.inject(manager);

		// when
		manager.init(servletContext);

		// then
		assertEquals(Configuration.VERSION_2_3_0, manager.config.getIncompatibleImprovements());
	}

	public void testIncompatibleImprovementsWithTemplate() throws Exception {
		// given
		FreemarkerManager manager = new FreemarkerManager();
		container.inject(manager);
		Configuration configuration = manager.getConfiguration(servletContext);
		Template tpl = configuration.getTemplate("org/apache/struts2/views/freemarker/incompatible-improvements.ftl");

		// when
		Writer out = new StringWriter();
		Map<String, String> model = new HashMap<>();
		model.put("error", "It's an error message");

		tpl.process(model, out);

		// then
		assertEquals("<input type=\"text\" onclick=\"this.alert('It&#39;s an error message')\"/>", out.toString());
	}

	public void testIncompatibleImprovementsByServletContext() throws Exception {
		// given
		servletContext.setInitParameter("freemarker.incompatible_improvements", "2.3.0");
		FreemarkerManager manager = new FreemarkerManager();
		container.inject(manager);

		// when
		manager.init(servletContext);

		// then
		assertEquals(Configuration.VERSION_2_3_0, manager.config.getIncompatibleImprovements());
	}
}

class DummyFreemarkerManager extends FreemarkerManager {

	public void load(StrutsMockServletContext servletContext, String path) {
		createTemplateLoader(servletContext, path);
	}

}
