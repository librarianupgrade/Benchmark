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
package org.b3log.latke;

/**
 * Latke runtime environment.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.2, Aug 27, 2011
 * @see Latkes#getRuntimeEnv() 
 */
public enum RuntimeEnv {

	/**
	 * Indicates Latke runs on local (standard Servlet container).
	 */
	LOCAL,
	/**
	 * Indicates Latke runs on <a href="http://code.google.com/appengine">
	 * Google App Engine</a>.
	 */
	GAE,
	/**
	 * Indicates Latke runs on <a href="http://developer.baidu.com/bae">Baidu App Engine</a>.
	 */
	BAE,
}
