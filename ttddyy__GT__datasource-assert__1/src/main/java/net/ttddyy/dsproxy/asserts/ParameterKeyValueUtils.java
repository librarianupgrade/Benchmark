package net.ttddyy.dsproxy.asserts;

import net.ttddyy.dsproxy.listener.logging.RegisterOutParameterValueConverter;
import net.ttddyy.dsproxy.listener.logging.SetNullParameterValueConverter;
import net.ttddyy.dsproxy.proxy.ParameterKey;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author Tadaya Tsuyukubo
 */
public class ParameterKeyValueUtils {

	private static SetNullParameterValueConverter setNullValueConverter = new SetNullParameterValueConverter();
	private static RegisterOutParameterValueConverter registerOutParameterValueConverter = new RegisterOutParameterValueConverter();

	public static ParameterKeyValue createSetParam(int index, Object value) {
		String displayValue = value == null ? null : value.toString();
		return new ParameterKeyValue(index, value, displayValue, ParameterKeyValue.OperationType.SET_PARAM);
	}

	public static ParameterKeyValue createSetParam(String name, Object value) {
		String displayValue = value == null ? null : value.toString();
		return new ParameterKeyValue(name, value, displayValue, ParameterKeyValue.OperationType.SET_PARAM);
	}

	public static ParameterKeyValue createSetParam(ParameterKey key, Object value) {
		String displayValue = value == null ? null : value.toString();
		return new ParameterKeyValue(key, value, displayValue, ParameterKeyValue.OperationType.SET_PARAM);
	}

	public static ParameterKeyValue createSetNull(int index, int sqlType) {
		String displayValue = setNullValueConverter.getDisplayValue(sqlType);
		return new ParameterKeyValue(index, sqlType, displayValue, ParameterKeyValue.OperationType.SET_NULL);
	}

	public static ParameterKeyValue createSetNull(String name, int sqlType) {
		String displayValue = setNullValueConverter.getDisplayValue(sqlType);
		return new ParameterKeyValue(name, sqlType, displayValue, ParameterKeyValue.OperationType.SET_NULL);
	}

	public static ParameterKeyValue createSetNull(ParameterKey key, int sqlType) {
		String displayValue = setNullValueConverter.getDisplayValue(sqlType);
		return new ParameterKeyValue(key, sqlType, displayValue, ParameterKeyValue.OperationType.SET_NULL);
	}

	public static ParameterKeyValue createRegisterOut(int index, Object value) {
		String displayValue = registerOutParameterValueConverter.getDisplayValue(value);
		return new ParameterKeyValue(index, value, displayValue, ParameterKeyValue.OperationType.REGISTER_OUT);
	}

	public static ParameterKeyValue createRegisterOut(String name, Object value) {
		String displayValue = registerOutParameterValueConverter.getDisplayValue(value);
		return new ParameterKeyValue(name, value, displayValue, ParameterKeyValue.OperationType.REGISTER_OUT);
	}

	public static ParameterKeyValue createRegisterOut(ParameterKey key, Object value) {
		String displayValue = registerOutParameterValueConverter.getDisplayValue(value);
		return new ParameterKeyValue(key, value, displayValue, ParameterKeyValue.OperationType.REGISTER_OUT);
	}

	public static SortedSet<ParameterKeyValue> filterBy(SortedSet<ParameterKeyValue> parameters,
			ParameterKeyValue.OperationType... operationTypes) {
		SortedSet<ParameterKeyValue> result = new TreeSet<>();
		for (ParameterKeyValue keyValue : parameters) {
			if (Arrays.asList(operationTypes).contains(keyValue.getType())) {
				result.add(keyValue);
			}
		}
		return result;
	}

	public static SortedSet<ParameterKeyValue> filterByKeyType(SortedSet<ParameterKeyValue> parameters,
			ParameterKey.ParameterKeyType keyType) {
		SortedSet<ParameterKeyValue> result = new TreeSet<>();
		for (ParameterKeyValue keyValue : parameters) {
			if (keyValue.getKey().getType() == keyType) {
				result.add(keyValue);
			}
		}
		return result;
	}

	public static SortedSet<ParameterKey> toParamKeys(SortedSet<ParameterKeyValue> parameters) {
		SortedSet<ParameterKey> result = new TreeSet<>();
		for (ParameterKeyValue keyValue : parameters) {
			result.add(keyValue.getKey());
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public static <T> Map<ParameterKey, T> toKeyValueMap(SortedSet<ParameterKeyValue> keyValues) {
		Map<ParameterKey, T> result = new LinkedHashMap<>();
		for (ParameterKeyValue keyValue : keyValues) {
			result.put(keyValue.getKey(), (T) keyValue.getValue());
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public static <T> Map<Integer, T> toKeyIndexMap(SortedSet<ParameterKeyValue> keyValues) {
		Map<Integer, T> result = new LinkedHashMap<>();
		for (ParameterKeyValue keyValue : keyValues) {
			result.put(keyValue.getKey().getIndex(), (T) keyValue.getValue());
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public static <T> Map<String, T> toKeyNameMap(SortedSet<ParameterKeyValue> keyValues) {
		Map<String, T> result = new LinkedHashMap<>();
		for (ParameterKeyValue keyValue : keyValues) {
			result.put(keyValue.getKey().getName(), (T) keyValue.getValue());
		}
		return result;
	}

	public static Map<ParameterKey, ParameterKeyValue> toParamKeyMap(SortedSet<ParameterKeyValue> keyValues) {
		Map<ParameterKey, ParameterKeyValue> result = new LinkedHashMap<>();
		for (ParameterKeyValue keyValue : keyValues) {
			result.put(keyValue.getKey(), keyValue);
		}
		return result;
	}

}
