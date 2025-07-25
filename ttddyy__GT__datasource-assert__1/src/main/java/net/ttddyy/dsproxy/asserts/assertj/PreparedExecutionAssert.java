package net.ttddyy.dsproxy.asserts.assertj;

import net.ttddyy.dsproxy.asserts.ParameterKeyValue;
import net.ttddyy.dsproxy.asserts.PreparedExecution;
import net.ttddyy.dsproxy.asserts.assertj.data.ExecutionParameter;
import net.ttddyy.dsproxy.asserts.assertj.data.ExecutionParameters;
import net.ttddyy.dsproxy.asserts.assertj.helper.ExecutionParameterAsserts;
import net.ttddyy.dsproxy.proxy.ParameterKey;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import static net.ttddyy.dsproxy.asserts.ParameterKeyValueUtils.toParamKeyMap;

/**
 * @author Tadaya Tsuyukubo
 */
public class PreparedExecutionAssert extends AbstractExecutionAssert<PreparedExecutionAssert, PreparedExecution> {

	private ExecutionParameterAsserts parameterAsserts = new ExecutionParameterAsserts(this.info);

	public PreparedExecutionAssert(PreparedExecution actual) {
		super(actual, PreparedExecutionAssert.class);
	}

	public PreparedExecutionAssert isSuccess() {
		isExecutionSuccess();
		return this;
	}

	public PreparedExecutionAssert isFailure() {
		isExecutionFailure();
		return this;
	}

	public PreparedExecutionAssert containsParam(int paramIndex, Object value) {
		containsParams(ExecutionParameter.param(paramIndex, value));
		return this;
	}

	public PreparedExecutionAssert containsNullParam(int index, int sqlType) {
		containsParams(ExecutionParameter.nullParam(index, sqlType));
		return this;
	}

	public PreparedExecutionAssert containsNullParam(int index) {
		containsParams(ExecutionParameter.nullParam(index));
		return this;
	}

	public PreparedExecutionAssert containsParams(ExecutionParameter... params) {
		ExecutionParameters executionParameters = ExecutionParameters.containsParams(params);
		this.parameterAsserts.assertParameterKeys(this.actual, executionParameters, false);
		this.parameterAsserts.assertExecutionParameters(this.actual, executionParameters);
		return this;
	}

	public PreparedExecutionAssert containsParamIndex(int paramIndex) {
		containsParamIndexes(paramIndex);
		return this;
	}

	public PreparedExecutionAssert containsParamIndexes(int... paramIndexes) {
		ExecutionParameters executionParameters = ExecutionParameters.containsParamIndexes(paramIndexes);
		this.parameterAsserts.assertParameterKeys(this.actual, executionParameters, false);
		return this;
	}

	/**
	 * @param paramValues Expecting parameter values. setNull is represented as {@code null}.
	 */
	public PreparedExecutionAssert containsParamValuesExactly(Object... paramValues) {

		SortedSet<ParameterKeyValue> actualKeyValues = this.actual.getAllParameters();
		if (!isContainsParamValuesExactly(actualKeyValues, paramValues)) {
			String failureMessage = getFailureMessageForValuesExactly(actualKeyValues, paramValues);
			failWithMessage(failureMessage);
		}

		return this;
	}

	private boolean isContainsParamValuesExactly(SortedSet<ParameterKeyValue> actualAsSet, Object[] expected) {

		ParameterKeyValue[] actual = actualAsSet.toArray(new ParameterKeyValue[actualAsSet.size()]);

		// size check
		if (actual.length != expected.length) {
			return false;
		}

		// value check
		for (int i = 0; i < actual.length; i++) {
			ParameterKeyValue actualKeyValue = actual[i];
			Object actualValue = actualKeyValue.isSetNull() ? null : actualKeyValue.getValue();
			Object expectedValue = expected[i];

			if ((actualValue == expectedValue) || (actualValue != null && actualValue.equals(expectedValue))) {
				continue;
			}
			return false;
		}

		return true;

	}

	private String getFailureMessageForValuesExactly(SortedSet<ParameterKeyValue> actualKeyValues, Object[] expected) {

		// construct map for expected
		Map<ParameterKey, Object> expectedParamAndValue = new LinkedHashMap<>();
		for (int i = 0; i < expected.length; i++) {
			expectedParamAndValue.put(new ParameterKey(i + 1), expected[i]);
		}

		String actualMessage = getActualKeyValueToDisplay(actualKeyValues);
		String expectedMessage = getExpectedKeyValueToDisplay(expectedParamAndValue);

		String missingMessage = getMissingToDisplay(actualKeyValues, expectedParamAndValue);
		String extraMessage = getExtraToDisplay(actualKeyValues, expectedParamAndValue);

		return String.format(
				"%nExpecting: prepared parameter values%n<%s>%nto be exactly:%n<%s>%nbut missing:%n<%s>%nextra:%n<%s>",
				actualMessage, expectedMessage, missingMessage, extraMessage);

	}

	private String getActualKeyValueToDisplay(SortedSet<ParameterKeyValue> keyValues) {
		Map<Integer, Object> actualValueMapToDisplay = new LinkedHashMap<>();

		for (ParameterKeyValue keyValue : keyValues) {
			int index = keyValue.getKey().getIndex();
			actualValueMapToDisplay.put(index, keyValue.getDisplayValue());
		}
		return actualValueMapToDisplay.toString();
	}

	private String getExpectedKeyValueToDisplay(Map<ParameterKey, Object> expectedParamValues) {
		Map<Integer, Object> toDisplay = new LinkedHashMap<>();
		for (Map.Entry<ParameterKey, Object> entry : expectedParamValues.entrySet()) {
			toDisplay.put(entry.getKey().getIndex(), entry.getValue() == null ? "NULL" : entry.getValue());
		}
		return toDisplay.toString();
	}

	private String getMissingToDisplay(SortedSet<ParameterKeyValue> actual, Map<ParameterKey, Object> expected) {
		Map<Integer, Object> missing = new LinkedHashMap<>();

		Map<ParameterKey, ParameterKeyValue> actualParamKeyMap = toParamKeyMap(new TreeSet<>(actual));

		for (Map.Entry<ParameterKey, Object> entry : expected.entrySet()) {
			int index = entry.getKey().getIndex();
			Object expectedValue = entry.getValue();
			ParameterKeyValue actualKeyValue = actualParamKeyMap.get(entry.getKey());
			if (actualKeyValue == null) {
				// expected has it, but actual doesn't => it is missing
				if (expectedValue == null) {
					missing.put(index, "NULL");
				} else {
					missing.put(index, expectedValue);
				}
			} else {
				// value check
				if (expectedValue == null) {
					if (!actualKeyValue.isSetNull()) {
						// expecting null, but actual was not setNull operation
						missing.put(index, "NULL");
					}
				} else if (!expectedValue.equals(actualKeyValue.getValue())) {
					missing.put(index, expectedValue); // expected has value but doesn't match
				}
			}
		}

		return missing.toString();
	}

	private String getExtraToDisplay(SortedSet<ParameterKeyValue> actual, Map<ParameterKey, Object> expected) {
		Map<Integer, Object> extra = new LinkedHashMap<>();
		for (ParameterKeyValue actualKeyValue : actual) {
			ParameterKey actualKey = actualKeyValue.getKey();
			if (!expected.containsKey(actualKey)) { // map may contains value=null, thus cannot compare the result of get with null
				// expected doesn't have the index.
				extra.put(actualKey.getIndex(), actualKeyValue.getDisplayValue());
			}
		}
		return extra.toString();
	}

}
