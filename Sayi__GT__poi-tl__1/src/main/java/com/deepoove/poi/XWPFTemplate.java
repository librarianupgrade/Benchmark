/*
 * Copyright 2014-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.deepoove.poi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.deepoove.poi.config.Configure;
import com.deepoove.poi.exception.ResolverException;
import com.deepoove.poi.policy.RenderPolicy;
import com.deepoove.poi.render.RenderAPI;
import com.deepoove.poi.resolver.TemplateVisitor;
import com.deepoove.poi.resolver.Visitor;
import com.deepoove.poi.template.ElementTemplate;

/**
 * 模板
 * 
 * @author Sayi
 * @version 0.0.1
 */
public class XWPFTemplate {
	private static Logger logger = LoggerFactory.getLogger(XWPFTemplate.class);
	private NiceXWPFDocument doc;
	private Configure config;
	private Visitor visitor;

	private List<ElementTemplate> eleTemplates;

	private XWPFTemplate() {
	}

	/**
	 * @version 0.0.4
	 */
	public static XWPFTemplate compile(String filePath) {
		return compile(new File(filePath));
	}

	public static XWPFTemplate compile(File file) {
		return compile(file, Configure.createDefault());
	}

	/**
	 * template file as InputStream
	 * 
	 * @param inputStream
	 * @return
	 * @version 1.2.0
	 */
	public static XWPFTemplate compile(InputStream inputStream) {
		return compile(inputStream, Configure.createDefault());
	}

	/**
	 * @param filePath
	 * @param config
	 * @return
	 * @version 1.0.0
	 */
	public static XWPFTemplate compile(String filePath, Configure config) {
		return compile(new File(filePath), config);
	}

	/**
	 * @param file
	 * @param config
	 * @return
	 * @version 1.0.0
	 */
	public static XWPFTemplate compile(File file, Configure config) {
		try {
			return compile(new FileInputStream(file), config);
		} catch (FileNotFoundException e) {
			logger.error("Cannot find the file", e);
			throw new ResolverException("Cannot find the file [" + file.getPath() + "]");
		}
	}

	/**
	 * template file as InputStream
	 * 
	 * @param inputStream
	 * @param config
	 * @return
	 * @version 1.2.0
	 */
	public static XWPFTemplate compile(InputStream inputStream, Configure config) {
		try {
			XWPFTemplate instance = new XWPFTemplate();
			instance.config = config;
			instance.doc = new NiceXWPFDocument(inputStream);
			instance.visitor = new TemplateVisitor(instance.config);
			instance.eleTemplates = instance.visitor.visitDocument(instance.doc);
			return instance;
		} catch (IOException e) {
			logger.error("Compile template failed", e);
			throw new ResolverException("Compile template failed");
		}
	}

	/**
	 * 重新解析doc
	 * 
	 * @param doc
	 */
	public void reload(NiceXWPFDocument doc) {
		try {
			this.close();
		} catch (IOException e) {
			logger.error("Close failed", e);
		}
		this.doc = doc;
		this.eleTemplates = this.visitor.visitDocument(doc);
	}

	public XWPFTemplate render(Object model) {
		RenderAPI.render(this, model);
		return this;
	}

	/**
	 * 自定义模板对应的策略
	 * 
	 * @param templateName
	 * @param policy
	 */
	@Deprecated
	public void registerPolicy(String templateName, RenderPolicy policy) {
		config.customPolicy(templateName, policy);
	}

	public void write(OutputStream out) throws IOException {
		this.doc.write(out);
	}

	public void close() throws IOException {
		this.doc.close();
	}

	public List<ElementTemplate> getElementTemplates() {
		return eleTemplates;
	}

	public NiceXWPFDocument getXWPFDocument() {
		return this.doc;
	}

	public Configure getConfig() {
		return config;
	}

}
