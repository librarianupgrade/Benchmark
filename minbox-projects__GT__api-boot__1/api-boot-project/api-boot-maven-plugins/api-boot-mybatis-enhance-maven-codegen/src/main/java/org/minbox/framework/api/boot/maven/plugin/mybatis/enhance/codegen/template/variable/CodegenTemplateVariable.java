/*
 * Copyright [2019] [恒宇少年 - 于起宇]
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 *
 */

package org.minbox.framework.api.boot.maven.plugin.mybatis.enhance.codegen.template.variable;

import java.util.HashMap;
import java.util.Map;

/**
 * codegen template variable
 *
 * @author 恒宇少年 - 于起宇
 * <p>
 * DateTime：2019-06-03 16:19
 * Blog：http://blog.yuqiyu.com
 * WebSite：http://www.jianshu.com/u/092df3f77bca
 * Gitee：https://gitee.com/hengboy
 * GitHub：https://github.com/hengboy
 */
public class CodegenTemplateVariable {
	/**
	 * #now variable
	 */
	public static String NOW = "#now";
	/**
	 * #desc variable
	 */
	public static String DESC = "#desc";
	/**
	 * #entity.name variable
	 */
	public static String ENTITY_NAME = "#entity.name";
	/**
	 * #entity.position variable
	 */
	public static String ENTITY_POSITION = "#entity.position";
	/**
	 * #entity.idType variable
	 */
	public static String ENTITY_ID_TYPE = "#entity.idType";

	/**
	 * codegen template variable map
	 */
	public static final Map<String, String> VARIABLES = new HashMap();
}
