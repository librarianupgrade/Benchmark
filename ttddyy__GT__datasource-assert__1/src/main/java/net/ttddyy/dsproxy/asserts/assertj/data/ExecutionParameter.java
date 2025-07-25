package net.ttddyy.dsproxy.asserts.assertj.data;

import net.ttddyy.dsproxy.proxy.ParameterKey;

import java.sql.SQLType;

/**
 * @author Tadaya Tsuyukubo
 */
public abstract class ExecutionParameter {

	/**
	 * Only for param keys. Do not care whether it is from setParams, setNullParams, or registerOutParams.
	 */
	public static class ParamKeyOnlyExecution extends ExecutionParameter {
		public ParamKeyOnlyExecution(ParameterKey parameterKey) {
			super(parameterKey);
		}
	}

	/**
	 * For setParam operations.
	 */
	public static class SetParamExecution extends ExecutionParameter {
		private Object value;

		public SetParamExecution(ParameterKey parameterKey, Object value) {
			super(parameterKey);
			this.value = value;
		}

		public Object getValue() {
			return value;
		}
	}

	/**
	 * For setNull operations
	 */
	public static class SetNullParamExecution extends ExecutionParameter {
		private Integer sqlType; // null if do not check sqlType

		public SetNullParamExecution(ParameterKey parameterKey, Integer sqlType) {
			super(parameterKey);
			this.sqlType = sqlType;
		}

		public Integer getSqlType() {
			return this.sqlType;
		}
	}

	public static class RegisterOutParamExecutionWithIntType extends ExecutionParameter {
		private int sqlType;

		public RegisterOutParamExecutionWithIntType(ParameterKey parameterKey, int sqlType) {
			super(parameterKey);
			this.sqlType = sqlType;
		}

		public int getSqlType() {
			return this.sqlType;
		}
	}

	public static class RegisterOutParamExecutionWithSQLType extends ExecutionParameter {
		private SQLType sqlType;

		public RegisterOutParamExecutionWithSQLType(ParameterKey parameterKey, SQLType sqlType) {
			super(parameterKey);
			this.sqlType = sqlType;
		}

		public SQLType getSqlType() {
			return this.sqlType;
		}
	}

	public static ExecutionParameter param(int paramIndex, Object value) {
		return new SetParamExecution(new ParameterKey(paramIndex), value);
	}

	public static ExecutionParameter param(String paramName, Object value) {
		return new SetParamExecution(new ParameterKey(paramName), value);
	}

	/**
	 * @param index
	 * @param sqlType the SQL type code defined in <code>java.sql.Types</code>
	 * @return
	 */
	public static ExecutionParameter nullParam(int index, int sqlType) {
		return new SetNullParamExecution(new ParameterKey(index), sqlType);
	}

	// do not care sqlType
	public static ExecutionParameter nullParam(int index) {
		return new SetNullParamExecution(new ParameterKey(index), null);
	}

	public static ExecutionParameter nullParam(String name, int sqlType) {
		return new SetNullParamExecution(new ParameterKey(name), sqlType);
	}

	public static ExecutionParameter nullParam(String name) {
		return new SetNullParamExecution(new ParameterKey(name), null);
	}

	public static ExecutionParameter outParam(int paramIndex, int sqlType) {
		return new RegisterOutParamExecutionWithIntType(new ParameterKey(paramIndex), sqlType);
	}

	public static ExecutionParameter outParam(int paramIndex, SQLType sqlType) {
		return new RegisterOutParamExecutionWithSQLType(new ParameterKey(paramIndex), sqlType);
	}

	public static ExecutionParameter outParam(String paramName, int sqlType) {
		return new RegisterOutParamExecutionWithIntType(new ParameterKey(paramName), sqlType);
	}

	public static ExecutionParameter outParam(String paramName, SQLType sqlType) {
		return new RegisterOutParamExecutionWithSQLType(new ParameterKey(paramName), sqlType);
	}

	protected ParameterKey key;

	public ExecutionParameter(ParameterKey key) {
		this.key = key;
	}

	public ParameterKey.ParameterKeyType getKeyType() {
		return this.key.getType();
	}

	public ParameterKey getKey() {
		return this.key;
	}

}
