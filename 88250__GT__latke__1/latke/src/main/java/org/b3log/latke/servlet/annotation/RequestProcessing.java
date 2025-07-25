/*
 * Copyright (c) 2009, 2010, 2011, 2012, 2013, B3log Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.b3log.latke.servlet.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.URIPatternMode;
import org.b3log.latke.servlet.converter.ConvertSupport;

/**
 * Indicates that an annotated method for HTTP servlet request processing.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @author <a href="mailto:wmainlove@gmail.com">Love Yao</a>
 * @version 1.0.0.4, May 1, 2012
 * @see RequestProcessor
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestProcessing {

	/**
	 * The dispatching URI path patterns of a request.
	 * 
	 * <p>
	 * Semantics of these values adapting to the URL patterns 
	 * (&lt;url-pattern/&gt;) configures in 
	 * web application descriptor (web.xml) of a servlet. Ant-style path 
	 * pattern and regular expression pattern are also supported.
	 * </p>
	 */
	String[] value() default {};

	/**
	 * The URI patterns mode.
	 */
	URIPatternMode uriPatternsMode() default URIPatternMode.ANT_PATH;

	/**
	 * The HTTP request methods the annotated method should process.
	 */
	HTTPRequestMethod[] method() default { HTTPRequestMethod.GET };

	/**
	 * Checks dose whether the URI patterns with context path.
	 * 
	 * <p>
	 * For example, the context path is /blog, and the annotation
	 * <pre>{@code @RequestProcessing(value = "/index")}</pre>
	 * means to serve /blog/index.
	 * </p>
	 * 
	 * <p>
	 * If the annotation 
	 * <pre>{@code @RequestProcessing(value = "/index", isWithContextPath=false)}</pre>
	 * means to serve /index.
	 * </p>
	 */
	boolean isWithContextPath() default true;

	/**
	 * User customized data convert class.
	 */
	Class<? extends ConvertSupport> convertClass() default ConvertSupport.class;
}
