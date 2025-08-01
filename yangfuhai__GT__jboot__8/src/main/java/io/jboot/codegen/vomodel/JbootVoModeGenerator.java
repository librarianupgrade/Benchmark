/**
 * Copyright (c) 2015-2017, Michael Yang 杨福海 (fuhai999@gmail.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *  http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jboot.codegen.vomodel;

import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.generator.BaseModelGenerator;
import com.jfinal.plugin.activerecord.generator.TableMeta;
import io.jboot.Jboot;
import io.jboot.codegen.CodeGenHelpler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class JbootVoModeGenerator extends BaseModelGenerator {

	public static void main(String[] args) {

		Jboot.setBootArg("jboot.datasource.url", "jdbc:mysql://127.0.0.1:3306/jbootdemo");
		Jboot.setBootArg("jboot.datasource.user", "root");

		String modelPackage = "io.jboot.codegen.vomodel.test";
		run(modelPackage);

	}

	public static void run(String voModelPackage) {
		new JbootVoModeGenerator(voModelPackage).doGenerate(null);
	}

	public static void run(String voModelPackage, String excludeTables) {
		new JbootVoModeGenerator(voModelPackage).doGenerate(excludeTables);
	}

	public JbootVoModeGenerator(String basePackage) {
		super(basePackage, PathKit.getWebRootPath() + "/src/main/java/" + basePackage.replace(".", "/"));

		this.template = "io/jboot/codegen/vomodel/vomodel_template.jf";

	}

	public void doGenerate(String excludeTables) {

		System.out.println("start generate...");
		List<TableMeta> tableMetaList = CodeGenHelpler.createMetaBuilder().build();
		CodeGenHelpler.excludeTables(tableMetaList, excludeTables);

		generate(tableMetaList);

		System.out.println("generate finished !!!");

	}

	/**
	 * base model 覆盖写入
	 */
	@Override
	protected void writeToFile(TableMeta tableMeta) throws IOException {
		File dir = new File(baseModelOutputDir);
		if (!dir.exists()) {
			dir.mkdirs();
		}

		String target = baseModelOutputDir + File.separator + tableMeta.modelName + "Vo" + ".java";
		FileWriter fw = new FileWriter(target);
		try {
			fw.write(tableMeta.baseModelContent);
		} finally {
			fw.close();
		}
	}
}
