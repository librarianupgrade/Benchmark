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
 *  Latke runtime JDBC database.
 * 
 * <p>
 * If Latke runs on local environment, Latke will read database configurations from file "local.properties".
 * </p>
 *  
 * @author <a href="mailto:wmainlove@gmail.com">Love Yao</a>
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.3, Dec 27, 2012
 * @see Latkes#getRuntimeDatabase() 
 */
public enum RuntimeDatabase {

	/**
	 * Oracle.
	 */
	ORACLE,
	/**
	 * MySQL.
	 */
	MYSQL,
	/**
	 * H2.
	 */
	H2,
	/**
	 * SYBASE.
	 */
	SYBASE,
	/**
	 * MSSQL.
	 */
	MSSQL,
	/**
	 * DB2.
	 */
	DB2
}
