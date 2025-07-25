/**
 * Copyright (c) 2015-2022, Michael Yang 杨福海 (fuhai999@gmail.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jboot.codegen.service;

import com.jfinal.core.JFinal;
import com.jfinal.kit.JavaKeyword;
import com.jfinal.kit.Kv;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.generator.MetaBuilder;
import com.jfinal.plugin.activerecord.generator.TableMeta;
import com.jfinal.template.Engine;
import com.jfinal.template.source.ClassPathSourceFactory;
import io.jboot.codegen.CodeGenHelpler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JbootServiceImplGenerator {

	private String basePackage;
	private String implPackage;
	private String modelPackage;

	private MetaBuilder metaBuilder;

	private String template;
	private String implName = "impl";
	private String outputDir;

	public JbootServiceImplGenerator(String basePackage, String modelPackage) {

		this.basePackage = basePackage;
		this.modelPackage = modelPackage;
		this.template = "io/jboot/codegen/service/service_impl_template.tp";
		this.metaBuilder = CodeGenHelpler.createMetaBuilder();
		this.outputDir = buildOutPutDir();

	}

	public JbootServiceImplGenerator(String basePackage, String implPackage, String outputDir, String modelPackage) {

		this.basePackage = basePackage;
		this.implPackage = implPackage;
		this.modelPackage = modelPackage;
		this.template = "io/jboot/codegen/service/service_impl_template.tp";
		this.metaBuilder = CodeGenHelpler.createMetaBuilder();
		this.outputDir = outputDir;

	}

	private String buildOutPutDir() {
		return CodeGenHelpler.getUserDir() + "/src/main/java/" + (basePackage + "." + implName).replace(".", "/");
	}

	public void generate() {
		generate(metaBuilder.build());
	}

	/**
	 * 设置需要被移除的表名前缀
	 * 例如表名  "tb_account"，移除前缀 "tb_" 后变为 "account"
	 */
	public JbootServiceImplGenerator setRemovedTableNamePrefixes(String... prefixes) {
		metaBuilder.setRemovedTableNamePrefixes(prefixes);
		return this;
	}

	public JbootServiceImplGenerator addExcludedTable(String... excludedTables) {
		metaBuilder.addExcludedTable(excludedTables);
		return this;
	}

	public JbootServiceImplGenerator addWhitelist(String... tableNames) {
		if (tableNames != null) {
			this.metaBuilder.addWhitelist(tableNames);
		}
		return this;
	}

	public JbootServiceImplGenerator setGenerateRemarks(boolean generateRemarks) {
		metaBuilder.setGenerateRemarks(generateRemarks);
		return this;
	}

	public JbootServiceImplGenerator setImplName(String implName) {
		this.implName = implName;
		return this;
	}

	public void generate(List<TableMeta> tableMetas) {
		System.out.println("Generate Service Impl ...");
		System.out.println("Service Impl Output Dir: " + outputDir);

		Engine engine = Engine.create("forServiceImpl");
		engine.setSourceFactory(new ClassPathSourceFactory());
		engine.addSharedMethod(new StrKit());
		engine.addSharedObject("getterTypeMap", getterTypeMap);
		engine.addSharedObject("javaKeyword", JavaKeyword.me);

		for (TableMeta tableMeta : tableMetas) {
			genBaseModelContent(tableMeta);
		}
		writeToFile(tableMetas);
	}

	protected void genBaseModelContent(TableMeta tableMeta) {
		Kv data = Kv.by("serviceImplPackageName", implPackage == null ? (basePackage + "." + implName) : implPackage);
		//        data.set("generateChainSetter", generateChainSetter);
		data.set("tableMeta", tableMeta);
		data.set("basePackage", basePackage);
		data.set("modelPackage", modelPackage);
		data.set("implName", implName);

		Engine engine = Engine.use("forServiceImpl");
		tableMeta.baseModelContent = engine.getTemplate(template).renderToString(data);
	}

	protected void writeToFile(List<TableMeta> tableMetas) {
		try {
			for (TableMeta tableMeta : tableMetas) {
				writeToFile(tableMeta);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * base model 覆盖写入
	 */
	protected void writeToFile(TableMeta tableMeta) throws IOException {
		File dir = new File(outputDir);
		if (!dir.exists()) {
			dir.mkdirs();
		}

		String target = outputDir + File.separator + tableMeta.modelName + "Service"
				+ StrKit.firstCharToUpperCase(implName) + ".java";

		File targetFile = new File(target);
		if (targetFile.exists()) {
			return;
		}

		try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(target),
				JFinal.me().getConstants().getEncoding())) {
			osw.write(tableMeta.baseModelContent);
		}

	}

	protected Map<String, String> getterTypeMap = new HashMap<String, String>() {
		{
			put("java.lang.String", "getStr");
			put("java.lang.Integer", "getInt");
			put("java.lang.Long", "getLong");
			put("java.lang.Double", "getDouble");
			put("java.lang.Float", "getFloat");
			put("java.lang.Short", "getShort");
			put("java.lang.Byte", "getByte");
		}
	};
}
