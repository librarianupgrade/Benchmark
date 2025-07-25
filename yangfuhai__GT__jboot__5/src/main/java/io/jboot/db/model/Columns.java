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
package io.jboot.db.model;

import io.jboot.db.dialect.JbootMysqlDialect;
import io.jboot.db.dialect.JbootSqlServerDialect;
import io.jboot.utils.StrUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Column 的工具类，用于方便组装sql
 */
public class Columns implements Serializable {

	public static final Columns EMPTY = Columns.create();

	private List<Column> cols;

	/**
	 * 在很多场景下，只会根据字段来查询，如果字段值为 null 的情况，Columns 会直接忽略 null 值，此时会造成结果不准确的情况
	 * <p>
	 * 比如 ：
	 * ```
	 * public ShopInfo findFirstByAccountId(BigInteger accountId) {
	 * return findFirstByColumns(Columns.create("account_id", accountId));
	 * }
	 * ```
	 * 根据账户 id 来查询账户的 门店，此时 如果传入 null 值，则返回了 第一个门店，和我们想要的结果集是不同的。
	 * <p>
	 * 准确的结果，应该是当用户传入 null 值的时候，应该直接 返回 null 。
	 * <p>
	 * 此时，我们可以使用如下代码进行查询。
	 * <p>
	 * ```
	 * public ShopInfo findFirstByAccountId(BigInteger accountId) {
	 * return findFirstByColumns(Columns.safeMode().eq("account_id", accountId));
	 * }
	 * ```
	 * <p>
	 * 使用 safeMode 的时候，默认传入的值必须全部不为空，才能返回结果，否则直接返回 null 。
	 */
	private boolean useSafeMode = false;

	public static Columns create() {
		return new Columns();
	}

	public static Columns create(Column column) {
		Columns that = new Columns();
		that.add(column);
		return that;

	}

	public static Columns create(List<Column> columns) {
		Columns that = new Columns();
		that.cols = columns;
		return that;

	}

	public static Columns create(String name, Object value) {
		return create().eq(name, value);
	}

	public static Columns safeMode() {
		return new Columns().useSafeMode();
	}

	public static Columns safeCreate(String name, Object value) {
		return safeMode().eq(name, value);
	}

	/**
	 * add new column in Columns
	 *
	 * @param column
	 */
	public Columns add(Column column) {

		//do not add null value column
		if (column.hasPara() && column.getValue() == null) {
			return this;
		}

		if (this.cols == null) {
			this.cols = new LinkedList<>();
		}

		this.cols.add(column);
		return this;
	}

	/**
	 * add Columns
	 * @param columns
	 * @return
	 */
	public Columns add(Columns columns) {
		return append(columns);
	}

	/**
	 * equals
	 *
	 * @param name
	 * @param value
	 * @return
	 */
	public Columns eq(String name, Object value) {
		Util.checkNullParas(this, name, value);
		return add(Column.create(name, value));
	}

	/**
	 * not equals !=
	 *
	 * @param name
	 * @param value
	 * @return
	 */
	public Columns ne(String name, Object value) {
		Util.checkNullParas(this, name, value);
		return add(Column.create(name, value, Column.LOGIC_NOT_EQUALS));
	}

	/**
	 * like
	 *
	 * @param name
	 * @param value
	 * @return
	 */
	public Columns like(String name, Object value) {
		Util.checkNullParas(this, name, value);
		return add(Column.create(name, value, Column.LOGIC_LIKE));
	}

	/**
	 * 自动添加两边 % 的like
	 *
	 * @param name
	 * @param value
	 * @return
	 */
	public Columns likeAppendPercent(String name, Object value) {
		Util.checkNullParas(this, name, value);
		if (value == null || (value instanceof String && StrUtil.isBlank((String) value))) {
			return this;
		}
		return add(Column.create(name, "%" + value + "%", Column.LOGIC_LIKE));
	}

	/**
	 * 大于 great than
	 *
	 * @param name
	 * @param value
	 * @return
	 */
	public Columns gt(String name, Object value) {
		Util.checkNullParas(this, name, value);
		return add(Column.create(name, value, Column.LOGIC_GT));
	}

	/**
	 * 大于等于 great or equal
	 *
	 * @param name
	 * @param value
	 * @return
	 */
	public Columns ge(String name, Object value) {
		Util.checkNullParas(this, name, value);
		return add(Column.create(name, value, Column.LOGIC_GE));
	}

	/**
	 * 小于 less than
	 *
	 * @param name
	 * @param value
	 * @return
	 */
	public Columns lt(String name, Object value) {
		Util.checkNullParas(this, name, value);
		return add(Column.create(name, value, Column.LOGIC_LT));
	}

	/**
	 * 小于等于 less or equal
	 *
	 * @param name
	 * @param value
	 * @return
	 */
	public Columns le(String name, Object value) {
		Util.checkNullParas(this, name, value);
		return add(Column.create(name, value, Column.LOGIC_LE));
	}

	/**
	 * IS NULL
	 *
	 * @param name
	 * @return
	 */
	public Columns isNull(String name) {
		return add(Column.create(name, null, Column.LOGIC_IS_NULL));
	}

	/**
	 * @param name
	 * @param condition
	 * @return
	 */
	public Columns isNullIf(String name, boolean condition) {
		if (condition) {
			add(Column.create(name, null, Column.LOGIC_IS_NULL));
		}
		return this;
	}

	/**
	 * IS NOT NULL
	 *
	 * @param name
	 * @return
	 */
	public Columns isNotNull(String name) {
		return add(Column.create(name, null, Column.LOGIC_IS_NOT_NULL));
	}

	/**
	 * IS NOT NULL
	 *
	 * @param name
	 * @param condition
	 * @return
	 */
	public Columns isNotNullIf(String name, boolean condition) {
		if (condition) {
			add(Column.create(name, null, Column.LOGIC_IS_NOT_NULL));
		}
		return this;
	}

	/**
	 * in arrays
	 *
	 * @param name
	 * @param arrays
	 * @return
	 */
	public Columns in(String name, Object... arrays) {
		Util.checkNullParas(this, name, arrays);
		return add(Column.create(name, arrays, Column.LOGIC_IN));
	}

	/**
	 * in list
	 *
	 * @param name
	 * @param list
	 * @return
	 */
	public Columns in(String name, List list) {
		if (list != null && !list.isEmpty()) {
			in(name, list.toArray());
		}
		return this;
	}

	/**
	 * not int arrays
	 *
	 * @param name
	 * @param arrays
	 * @return
	 */
	public Columns notIn(String name, Object... arrays) {
		Util.checkNullParas(this, name, arrays);
		return add(Column.create(name, arrays, Column.LOGIC_NOT_IN));
	}

	/**
	 * not in list
	 *
	 * @param name
	 * @param list
	 * @return
	 */
	public Columns notIn(String name, List list) {
		if (list != null && !list.isEmpty()) {
			notIn(name, list.toArray());
		}
		return this;
	}

	/**
	 * between
	 *
	 * @param name
	 * @param start
	 * @param end
	 * @return
	 */
	public Columns between(String name, Object start, Object end) {
		Util.checkNullParas(this, name, start, end);
		return add(Column.create(name, new Object[] { start, end }, Column.LOGIC_BETWEEN));
	}

	/**
	 * not between
	 *
	 * @param name
	 * @param start
	 * @param end
	 * @return
	 */
	public Columns notBetween(String name, Object start, Object end) {
		Util.checkNullParas(this, name, start, end);
		return add(Column.create(name, new Object[] { start, end }, Column.LOGIC_NOT_BETWEEN));
	}

	/**
	 * group
	 *
	 * @param columns
	 * @return
	 */
	public Columns group(Columns columns) {
		if (columns == this) {
			throw new IllegalArgumentException("Columns.group(...) need a new Columns");
		}
		if (!columns.isEmpty()) {
			add(new Group(columns));
		}
		return this;
	}

	/**
	 * @param columns
	 * @param conditon
	 * @return
	 */
	public Columns groupIf(Columns columns, boolean conditon) {
		if (columns == this) {
			throw new IllegalArgumentException("Columns.group(...) need a new Columns");
		}
		if (conditon && !columns.isEmpty()) {
			add(new Group(columns));
		}
		return this;
	}

	/**
	 * customize string sql
	 *
	 * @param sql
	 * @return
	 */
	public Columns sqlPart(String sql) {
		if (StrUtil.isNotBlank(sql)) {
			add(new SqlPart(sql));
		}
		return this;
	}

	/**
	 * customize string sql
	 *
	 * @param sql
	 * @param paras
	 * @return
	 */
	public Columns sqlPart(String sql, Object... paras) {
		Util.checkNullParas(this, paras);
		if (StrUtil.isNotBlank(sql)) {
			add(new SqlPart(sql, paras));
		}
		return this;
	}

	/**
	 * customize string sql
	 *
	 * @param sql
	 * @param condition
	 * @return
	 */
	public Columns sqlPartIf(String sql, boolean condition) {
		if (condition && StrUtil.isNotBlank(sql)) {
			add(new SqlPart(sql));
		}
		return this;
	}

	/**
	 * customize string sql
	 *
	 * @param sql
	 * @param condition
	 * @param paras
	 * @return
	 */
	public Columns sqlPartIf(String sql, boolean condition, Object... paras) {
		Util.checkNullParas(this, paras);
		if (condition && StrUtil.isNotBlank(sql)) {
			add(new SqlPart(sql, paras));
		}
		return this;
	}

	/**
	 * customize string sql
	 *
	 * @param sql
	 * @return
	 */
	public Columns sqlPartWithoutLink(String sql) {
		if (StrUtil.isNotBlank(sql)) {
			add(new SqlPart(sql, true));
		}
		return this;
	}

	/**
	 * customize string sql
	 *
	 * @param sql
	 * @param paras
	 * @return
	 */
	public Columns sqlPartWithoutLink(String sql, Object... paras) {
		Util.checkNullParas(this, paras);
		if (StrUtil.isNotBlank(sql)) {
			add(new SqlPart(sql, paras, true));
		}
		return this;
	}

	/**
	 * customize string sql
	 *
	 * @param sql
	 * @param condition
	 * @return
	 */
	public Columns sqlPartWithoutLinkIf(String sql, boolean condition) {
		if (condition && StrUtil.isNotBlank(sql)) {
			add(new SqlPart(sql, true));
		}
		return this;
	}

	/**
	 * customize string sql
	 *
	 * @param sql
	 * @param condition
	 * @param paras
	 * @return
	 */
	public Columns sqlPartWithoutLinkIf(String sql, boolean condition, Object... paras) {
		Util.checkNullParas(this, paras);
		if (condition && StrUtil.isNotBlank(sql)) {
			add(new SqlPart(sql, paras, true));
		}
		return this;
	}

	public Columns or() {
		add(new Or());
		return this;
	}

	public Columns ors(String name, String logic, Object... values) {
		Util.checkNullParas(this, name, values);
		for (int i = 0; i < values.length; i++) {
			Object value = values[i];
			if (value != null) {
				add(Column.create(name, value, logic));
				if (i != values.length - 1) {
					add(new Or());
				}
			}
		}
		return this;
	}

	public Columns orEqs(String name, Object... values) {
		return ors(name, Column.LOGIC_EQUALS, values);
	}

	/**
	 * 追加 新的 columns
	 *
	 * @param columns
	 * @return
	 */
	public Columns append(Columns columns) {
		if (columns != null && !columns.isEmpty()) {
			for (Column column : columns.getList()) {
				add(column);
			}
		}
		return this;
	}

	/**
	 * 追加 新的 columns
	 *
	 * @param columns
	 * @return
	 */
	public Columns appendIf(Columns columns, boolean condition) {
		if (condition) {
			append(columns);
		}
		return this;
	}

	public boolean isUseSafeMode() {
		return useSafeMode;
	}

	public Columns useSafeMode() {
		this.useSafeMode = true;
		return this;
	}

	public Columns unUseSafeMode() {
		this.useSafeMode = false;
		return this;
	}

	public boolean isEmpty() {
		return cols == null || cols.isEmpty();
	}

	public Object[] getValueArray() {
		return Util.getValueArray(cols);
	}

	public List<Column> getList() {
		return cols;
	}

	public String getCacheKey() {
		if (isEmpty()) {
			return null;
		}

		List<Column> columns = new ArrayList<>(cols);
		StringBuilder s = new StringBuilder();
		buildCacheKey(s, columns);

		return s.toString();
	}

	private static final char SQL_CACHE_SEPARATOR = '-';

	private void buildCacheKey(StringBuilder s, List<Column> columns) {
		for (int i = 0; i < columns.size(); i++) {

			Column column = columns.get(i);

			if (column instanceof Or) {
				Column before = i > 0 ? columns.get(i - 1) : null;
				if (before != null && !(before instanceof Or)) {
					s.append("or").append(SQL_CACHE_SEPARATOR);
				}
			} else if (column instanceof Group) {
				s.append('(');
				buildCacheKey(s, ((Group) column).getColumns().getList());
				s.append(')').append(SQL_CACHE_SEPARATOR);
			} else if (column instanceof SqlPart) {
				String sqlpart = ((SqlPart) column).getSql();
				Object value = column.getValue();
				if (value != null) {
					if (value.getClass().isArray()) {
						Object[] values = (Object[]) value;
						for (Object v : values) {
							sqlpart = Util.replaceSqlPara(sqlpart, v);
						}
					} else {
						sqlpart = Util.replaceSqlPara(sqlpart, value);
					}
				}
				s.append(Util.deleteWhitespace(sqlpart)).append(SQL_CACHE_SEPARATOR);
			} else {
				s.append(column.getName()).append(SQL_CACHE_SEPARATOR).append(getLogicStr(column.getLogic()))
						.append(SQL_CACHE_SEPARATOR);
				Object value = column.getValue();
				if (value != null) {
					if (value.getClass().isArray()) {
						s.append(Util.array2String((Object[]) value));
					} else {
						s.append(column.getValue());
					}
					s.append(SQL_CACHE_SEPARATOR);
				}
			}
		}
		s.deleteCharAt(s.length() - 1);
	}

	/**
	 * @param logic
	 * @return
	 */
	private String getLogicStr(String logic) {
		switch (logic) {
		case Column.LOGIC_LIKE:
			return "lk";
		case Column.LOGIC_GT:
			return "gt";
		case Column.LOGIC_GE:
			return "ge";
		case Column.LOGIC_LT:
			return "lt";
		case Column.LOGIC_LE:
			return "le";
		case Column.LOGIC_EQUALS:
			return "eq";
		case Column.LOGIC_NOT_EQUALS:
			return "neq";
		case Column.LOGIC_IS_NULL:
			return "isn";
		case Column.LOGIC_IS_NOT_NULL:
			return "nn";
		case Column.LOGIC_IN:
			return "in";
		case Column.LOGIC_NOT_IN:
			return "nin";
		case Column.LOGIC_BETWEEN:
			return "bt";
		case Column.LOGIC_NOT_BETWEEN:
			return "nbt";
		default:
			return "";
		}
	}

	/**
	 * 输出 where 后面的 sql 部分，风格是 mysql 的风格SQL
	 * @return
	 */
	public String toWherePartSql() {
		return toWherePartSql('`', false);
	}

	/**
	 * 输出 where 后面的 sql 部分，风格是 mysql 的风格SQL
	 * @param withWhereKeyword 是否带上 where 关键字
	 * @return
	 */
	public String toWherePartSql(boolean withWhereKeyword) {
		return toWherePartSql('`', withWhereKeyword);
	}

	/**
	 * 输出 where 部分的 sql
	 * @param separator 字段分隔符
	 * @param withWhereKeyword 是否带上 "where 关键字"
	 * @return
	 */
	public String toWherePartSql(char separator, boolean withWhereKeyword) {
		StringBuilder sb = new StringBuilder();
		SqlBuilder.buildWhereSql(sb, getList(), separator, withWhereKeyword);
		return sb.toString();
	}

	/**
	 * 这个只是用于调试
	 *
	 * @return
	 */
	public String toMysqlSql() {
		JbootMysqlDialect dialect = new JbootMysqlDialect();
		return dialect.forFindByColumns(null, null, "table", "*", getList(), null, null);
	}

	/**
	 * 这个只是用于调试
	 * @return
	 */
	public String toSqlServerSql() {
		JbootSqlServerDialect dialect = new JbootSqlServerDialect();
		return dialect.forFindByColumns(null, null, "table", "*", getList(), null, null);
	}

	@Override
	public String toString() {
		return getCacheKey();
	}

	public static void main(String[] args) {

		Columns columns = Columns.create().useSafeMode().or().or().or().eq("aa", "bb").or().or().or()
				.notIn("aaa", 123, 456, 789).like("titile", "a");
		columns.group(Columns.create().or().or().sqlPart("aa=bb"));
		columns.group(Columns.create("aa", "bb").eq("cc", "dd").group(Columns.create("aa", "bb").eq("cc", "dd"))
				.group(Columns.create("aa", "bb").eq("cc", "dd").group(Columns.create("aa", "bb").eq("cc", "dd"))));

		columns.ge("age", 10);
		columns.or();
		columns.or();
		columns.or();
		columns.or();
		columns.sqlPart("user.id != ? and xxx= ?", 1, "abc2");
		columns.sqlPart("user.id != ? and xxx= ?", 1, "abc2");

		columns.or();
		columns.or();
		columns.or();
		columns.group(
				Columns.create().likeAppendPercent("name", "null").or().or().or().eq("age", "18").eq("ddd", null));

		columns.or();
		columns.or();

		columns.group(Columns.create().or().or().sqlPart("name = ?", "zhangsan"));
		columns.or();
		columns.or();
		columns.or();

		columns.between("name", "123", "1233");
		columns.between("name", "123", "1233");
		columns.or();

		columns.sqlPartWithoutLink("group by xxx");
		columns.or();
		columns.or();
		columns.or();

		System.out.println(columns.getCacheKey());
		System.out.println(Arrays.toString(columns.getValueArray()));
		System.out.println(columns.toMysqlSql());
		System.out.println("-----------");
		System.out.println(columns.toWherePartSql('"', true));

	}

}
