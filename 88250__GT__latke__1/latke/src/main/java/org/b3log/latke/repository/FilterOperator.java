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
package org.b3log.latke.repository;

/**
 * Filter operator.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.1, Oct 18, 2011
 * @see Filter
 */
public enum FilterOperator {

	/**
	 * Less than.
	 */
	LESS_THAN,
	/**
	 * Less than or equal.
	 */
	LESS_THAN_OR_EQUAL,
	/**
	 * Greater than.
	 */
	GREATER_THAN,
	/**
	 * Grater than or equal.
	 */
	GREATER_THAN_OR_EQUAL,
	/**
	 * Equal.
	 */
	EQUAL,
	/**
	 * Not equal.
	 */
	NOT_EQUAL,
	/**
	 * In.
	 */
	IN,
}
