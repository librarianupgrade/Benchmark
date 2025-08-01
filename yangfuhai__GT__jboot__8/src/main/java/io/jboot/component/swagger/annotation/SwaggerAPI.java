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
package io.jboot.component.swagger.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface SwaggerAPI {

	public static final String TYPE_JSON = "application/json";
	public static final String TYPE_XML = "application/xml";
	public static final String TYPE_FORM_DATA = "multipart/form-data";
	public static final String TYPE_NORMAL = "application/x-www-form-urlencoded";

	public static final String METHOD_GET = "get";
	public static final String METHOD_POST = "post";

	String path() default "";

	String apisName() default "";

	String summary() default "";

	String description() default "";

	String operationId() default "";

	String method() default METHOD_GET;

	String contentType() default TYPE_NORMAL;

	SwaggerParam[] params();

	SwaggerResponse response() default @SwaggerResponse;

}
