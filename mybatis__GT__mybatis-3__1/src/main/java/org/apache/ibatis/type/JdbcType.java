/*
 *    Copyright 2009-2012 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.type;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Clinton Begin
 */
public enum JdbcType {
	/*
	 * This is added to enable basic support for the
	 * ARRAY data type - but a custom type handler is still required
	 */
	ARRAY(Types.ARRAY), BIT(Types.BIT), TINYINT(Types.TINYINT), SMALLINT(Types.SMALLINT), INTEGER(Types.INTEGER),
	BIGINT(Types.BIGINT), FLOAT(Types.FLOAT), REAL(Types.REAL), DOUBLE(Types.DOUBLE), NUMERIC(Types.NUMERIC),
	DECIMAL(Types.DECIMAL), CHAR(Types.CHAR), VARCHAR(Types.VARCHAR), LONGVARCHAR(Types.LONGVARCHAR), DATE(Types.DATE),
	TIME(Types.TIME), TIMESTAMP(Types.TIMESTAMP), BINARY(Types.BINARY), VARBINARY(Types.VARBINARY),
	LONGVARBINARY(Types.LONGVARBINARY), NULL(Types.NULL), OTHER(Types.OTHER), BLOB(Types.BLOB), CLOB(Types.CLOB),
	BOOLEAN(Types.BOOLEAN), CURSOR(-10), // Oracle
	UNDEFINED(Integer.MIN_VALUE + 1000), NVARCHAR(Types.NVARCHAR), // JDK6
	NCHAR(Types.NCHAR), // JDK6
	NCLOB(Types.NCLOB), // JDK6
	STRUCT(Types.STRUCT);

	public final int TYPE_CODE;
	private static Map<Integer, JdbcType> codeLookup = new HashMap<Integer, JdbcType>();

	static {
		for (JdbcType type : JdbcType.values()) {
			codeLookup.put(type.TYPE_CODE, type);
		}
	}

	JdbcType(int code) {
		this.TYPE_CODE = code;
	}

	public static JdbcType forCode(int code) {
		return codeLookup.get(code);
	}

}
