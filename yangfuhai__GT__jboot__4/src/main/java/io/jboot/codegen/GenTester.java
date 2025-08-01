/**
 * Copyright (c) 2015-2021, Michael Yang 杨福海 (fuhai999@gmail.com).
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
package io.jboot.codegen;

import com.jfinal.kit.PathKit;
import io.jboot.app.JbootApplication;
import io.jboot.codegen.model.JbootBaseModelGenerator;
import io.jboot.codegen.model.JbootModelGenerator;
import io.jboot.codegen.service.JbootServiceImplGenerator;
import io.jboot.codegen.service.JbootServiceInterfaceGenerator;

/**
 * @author Michael Yang 杨福海 （fuhai999@gmail.com）
 * @version V1.0
 */
public class GenTester {

	public static void main(String[] args) {

		JbootApplication.setBootArg("jboot.datasource.url", "jdbc:mysql://127.0.0.1:3306/jbootdemo");
		JbootApplication.setBootArg("jboot.datasource.user", "root");

		String modelPackage = "io.jboot.codegen.test.model";
		String baseModelPackage = modelPackage + ".base";

		String modelDir = PathKit.getWebRootPath() + "/src/main/java/" + modelPackage.replace(".", "/");
		String baseModelDir = PathKit.getWebRootPath() + "/src/main/java/" + baseModelPackage.replace(".", "/");

		System.out.println("start generate...");
		System.out.println("generate dir:" + modelDir);

		new JbootBaseModelGenerator(baseModelPackage, baseModelDir).setGenerateRemarks(true).generate();
		new JbootModelGenerator(modelPackage, baseModelPackage, modelDir).generate();

		String servicePackage = "io.jboot.codegen.test.service";
		new JbootServiceInterfaceGenerator(servicePackage, modelPackage).generate();
		new JbootServiceImplGenerator(servicePackage, modelPackage).setImplName("provider").generate();

	}
}
