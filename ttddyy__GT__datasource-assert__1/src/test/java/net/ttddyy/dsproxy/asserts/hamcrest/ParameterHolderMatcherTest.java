package net.ttddyy.dsproxy.asserts.hamcrest;

import net.ttddyy.dsproxy.asserts.ParameterByIndexHolder;
import net.ttddyy.dsproxy.asserts.ParameterByNameHolder;
import net.ttddyy.dsproxy.asserts.ParameterHolder;
import net.ttddyy.dsproxy.asserts.ParameterKeyValue;
import net.ttddyy.dsproxy.asserts.PreparedBatchExecutionEntry;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;

/**
 * @author Tadaya Tsuyukubo
 * @since 1.0
 */
public class ParameterHolderMatcherTest {

	// ParamHolder that only implements ParameterByIndexHolder
	private static class MockIndexOnlyParamHolder implements ParameterHolder, ParameterByIndexHolder {

		@Override
		public SortedSet<ParameterKeyValue> getAllParameters() {
			return null;
		}

		@Override
		public SortedSet<ParameterKeyValue> getSetParams() {
			return null;
		}

		@Override
		public SortedSet<ParameterKeyValue> getSetNullParams() {
			return null;
		}

		@Override
		public Map<Integer, Object> getSetParamsByIndex() {
			return null;
		}

		@Override
		public List<Integer> getParamIndexes() {
			return null;
		}

		@Override
		public Map<Integer, Integer> getSetNullParamsByIndex() {
			return null;
		}
	}

	// ParamHolder that only implements ParameterByNameHolder
	private static class MockNameOnlyParamHolder implements ParameterHolder, ParameterByNameHolder {

		@Override
		public SortedSet<ParameterKeyValue> getAllParameters() {
			return null;
		}

		@Override
		public SortedSet<ParameterKeyValue> getSetParams() {
			return null;
		}

		@Override
		public SortedSet<ParameterKeyValue> getSetNullParams() {
			return null;
		}

		@Override
		public Map<String, Object> getSetParamsByName() {
			return null;
		}

		@Override
		public List<String> getParamNames() {
			return null;
		}

		@Override
		public Map<String, Integer> getSetNullParamsByName() {
			return null;
		}

	}

	@Test
	public void testSuccess() {
		Matcher<Object> subMatcher = is(nullValue());
		ParameterHolderMatcher.ParameterByIndexMatcher<Object> byIndexMatcher = new ParameterHolderMatcher.ParameterByIndexMatcher<Object>(
				subMatcher) {
			@Override
			public String featureValueOf(ParameterByIndexHolder actual) {
				return null;
			}
		};
		ParameterHolderMatcher.ParameterByNameMatcher<Object> byNameMatcher = new ParameterHolderMatcher.ParameterByNameMatcher<Object>(
				subMatcher) {
			@Override
			public Object featureValueOf(ParameterByNameHolder actual) {
				return null;
			}
		};

		Assert.assertThat(new MockIndexOnlyParamHolder(), byIndexMatcher);
		Assert.assertThat(new MockNameOnlyParamHolder(), byNameMatcher);
	}

	@Test
	public void testNonParameterHolderInstance() {
		Matcher<String> subMatcher = is("foo");
		ParameterHolderMatcher.ParameterByIndexMatcher<String> byIndexMatcher = new ParameterHolderMatcher.ParameterByIndexMatcher<String>(
				subMatcher) {
			@Override
			public String featureValueOf(ParameterByIndexHolder actual) {
				return null;
			}
		};
		ParameterHolderMatcher.ParameterByNameMatcher<String> byNameMatcher = new ParameterHolderMatcher.ParameterByNameMatcher<String>(
				subMatcher) {
			@Override
			public String featureValueOf(ParameterByNameHolder actual) {
				return null;
			}
		};

		Object object = new Object();

		validateAssertMessage(object, byIndexMatcher, "\nExpected: implementation of ParameterHolder\n"
				+ "     but: Object didn't implement ParameterHolder");
		validateAssertMessage(object, byNameMatcher, "\nExpected: implementation of ParameterHolder\n"
				+ "     but: Object didn't implement ParameterHolder");
	}

	@Test
	public void testPassIndexOnlyHolderToNameMatcher() {

		MockIndexOnlyParamHolder entry = new MockIndexOnlyParamHolder();

		Matcher<String> subMatcher = is("foo");
		ParameterHolderMatcher.ParameterByNameMatcher<String> matcher = new ParameterHolderMatcher.ParameterByNameMatcher<String>(
				subMatcher) {
			@Override
			public String featureValueOf(ParameterByNameHolder actual) {
				return "bar";
			}
		};

		validateAssertMessage(entry, matcher, "\nExpected: implementation of ParameterByNameHolder\n"
				+ "     but: MockIndexOnlyParamHolder didn't implement ParameterByNameHolder");

	}

	@Test
	public void testPassNameOnlyHolderToIndexMatcher() {

		MockNameOnlyParamHolder entry = new MockNameOnlyParamHolder();

		Matcher<String> subMatcher = is("foo");
		ParameterHolderMatcher.ParameterByIndexMatcher<String> matcher = new ParameterHolderMatcher.ParameterByIndexMatcher<String>(
				subMatcher) {
			@Override
			public String featureValueOf(ParameterByIndexHolder actual) {
				return "bar";
			}
		};

		validateAssertMessage(entry, matcher, "\nExpected: implementation of ParameterByIndexHolder\n"
				+ "     but: MockNameOnlyParamHolder didn't implement ParameterByIndexHolder");

	}

	@Test
	public void testFailureMessageOfValidateParametersByIndex() {

		MockIndexOnlyParamHolder entry = new MockIndexOnlyParamHolder();

		Matcher<String> subMatcher = is("foo");
		ParameterHolderMatcher.ParameterByIndexMatcher<String> matcher = new ParameterHolderMatcher.ParameterByIndexMatcher<String>(
				subMatcher) {
			@Override
			public String featureValueOf(ParameterByIndexHolder actual) {
				return "bar";
			}

			@Override
			public boolean validateParameterByIndex(ParameterByIndexHolder actual, Description descForExpected,
					Description descForFailure) {
				descForExpected.appendText("EXPECTED_DESC");
				descForFailure.appendText("FAILURE_DESC");
				return false; // make validation fail
			}
		};

		validateAssertMessage(entry, matcher, "\nExpected: EXPECTED_DESC\n     but: FAILURE_DESC");

	}

	@Test
	public void testFailureMessageOfValidateParametersByName() {

		MockNameOnlyParamHolder entry = new MockNameOnlyParamHolder();

		Matcher<String> subMatcher = is("foo");
		ParameterHolderMatcher.ParameterByNameMatcher<String> matcher = new ParameterHolderMatcher.ParameterByNameMatcher<String>(
				subMatcher) {
			@Override
			public String featureValueOf(ParameterByNameHolder actual) {
				return "bar";
			}

			@Override
			public boolean validateParameterByName(ParameterByNameHolder actual, Description descForExpected,
					Description descForFailure) {
				descForExpected.appendText("EXPECTED_DESC");
				descForFailure.appendText("FAILURE_DESC");
				return false; // make validation fail
			}
		};

		validateAssertMessage(entry, matcher, "\nExpected: EXPECTED_DESC\n     but: FAILURE_DESC");

	}

	@Test
	public void testFailureMessageOfSubMatcherFailure() {

		PreparedBatchExecutionEntry entry = new PreparedBatchExecutionEntry();

		Matcher<String> subMatcher = is("foo");
		ParameterHolderMatcher.ParameterByIndexMatcher<String> matcher = new ParameterHolderMatcher.ParameterByIndexMatcher<String>(
				subMatcher) {
			@Override
			public String featureValueOf(ParameterByIndexHolder actual) {
				return "bar";
			}
		};

		validateAssertMessage(entry, matcher, "\nExpected: is \"foo\"\n     but: was \"bar\"");
	}

	@SuppressWarnings("unchecked")
	private void validateAssertMessage(Object actual, Matcher matcher, String expected) {
		String message = "";
		try {
			Assert.assertThat(actual, matcher);
			fail("expect asserts to be failed");
		} catch (AssertionError e) {
			message = e.getMessage();
		}

		assertThat(message).isEqualTo(expected);
	}

}
