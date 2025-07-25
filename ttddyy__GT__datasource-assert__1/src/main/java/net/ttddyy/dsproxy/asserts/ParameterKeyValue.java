package net.ttddyy.dsproxy.asserts;

import net.ttddyy.dsproxy.proxy.ParameterKey;

/**
 * @author Tadaya Tsuyukubo
 */
public class ParameterKeyValue implements Comparable<ParameterKeyValue> {

	public enum OperationType {
		SET_PARAM, SET_NULL, REGISTER_OUT
	}

	private ParameterKey key;
	private Object value;
	private String displayValue;
	private OperationType type;

	public ParameterKeyValue(int indexKey, Object value, String displayValue, OperationType type) {
		this(new ParameterKey(indexKey), value, displayValue, type);
	}

	public ParameterKeyValue(String nameKey, Object value, String displayValue, OperationType type) {
		this(new ParameterKey(nameKey), value, displayValue, type);
	}

	public ParameterKeyValue(ParameterKey key, Object value, String displayValue, OperationType type) {
		this.key = key;
		this.value = value;
		this.displayValue = displayValue;
		this.type = type;
	}

	public boolean isSetParam() {
		return this.type == OperationType.SET_PARAM;
	}

	public boolean isSetNull() {
		return this.type == OperationType.SET_NULL;
	}

	public boolean isRegisterOut() {
		return this.type == OperationType.REGISTER_OUT;
	}

	public ParameterKey getKey() {
		return this.key;
	}

	public void setKey(ParameterKey key) {
		this.key = key;
	}

	public Object getValue() {
		return this.value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public String getDisplayValue() {
		return this.displayValue;
	}

	public void setDisplayValue(String displayValue) {
		this.displayValue = displayValue;
	}

	public OperationType getType() {
		return this.type;
	}

	public void setType(OperationType type) {
		this.type = type;
	}

	@Override
	public int compareTo(ParameterKeyValue o) {
		int byKey = this.key.compareTo(o.key); //use key for ordering
		if (byKey != 0) {
			return byKey;
		}
		return this.value == o.value ? 0 : 1;
	}

}
