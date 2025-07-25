/**
 * Copyright (c) 2015-2020, Michael Yang 杨福海 (fuhai999@gmail.com).
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
package io.jboot.db;

import com.jfinal.ext.kit.DateKit;
import com.jfinal.plugin.activerecord.Config;
import io.jboot.Jboot;
import io.jboot.utils.StrUtil;

import java.util.Date;
import java.util.regex.Matcher;

/**
 * @author michael yang (fuhai999@gmail.com)
 * @Date: 2019/12/12
 */
public class SqlDebugger {

	private static SqlDebugPrinter defaultPrinter = new SqlDebugPrinter() {

		@Override
		public boolean isPrint(Config config, String sql, Object... paras) {
			return Jboot.isDevMode();
		}

		@Override
		public void print(String sql) {
			System.out.println("\r\njboot exec sql >>> " + sql);
		}
	};

	private static SqlDebugPrinter printer = defaultPrinter;

	public static SqlDebugPrinter getPrinter() {
		return printer;
	}

	public static void setPrinter(SqlDebugPrinter printer) {
		SqlDebugger.printer = printer;
	}

	public static void debug(Config config, String sql, Object... paras) {

		if (!printer.isPrint(config, sql, paras)) {
			return;
		}

		if (paras != null) {
			for (Object value : paras) {
				// null
				if (value == null) {
					sql = sql.replaceFirst("\\?", "null");
				}
				// number
				else if (value instanceof Number) {
					sql = sql.replaceFirst("\\?", value.toString());
				}
				// numeric
				else if (value instanceof String && StrUtil.isNumeric((String) value)) {
					sql = sql.replaceFirst("\\?", (String) value);
				}
				// other
				else {
					StringBuilder sb = new StringBuilder();
					sb.append("'");
					if (value instanceof Date) {
						sb.append(DateKit.toStr((Date) value, DateKit.timeStampPattern));
					} else {
						sb.append(value.toString());
					}
					sb.append("'");
					sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(sb.toString()));
				}
			}
		}

		printer.print(sql);
	}

	public static interface SqlDebugPrinter {

		public boolean isPrint(Config config, String sql, Object... paras);

		public void print(String sql);
	}

}
