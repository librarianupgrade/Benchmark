/*
 * Copyright (c) 2014 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.common.truth;

import static com.google.common.truth.ExpectFailure.assertThat;
import static com.google.common.truth.Platform.doubleToString;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.truth.ExpectFailure.SimpleSubjectBuilderCallback;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for Double Subjects.
 *
 * @author Kurt Alfred Kluever
 */
@RunWith(JUnit4.class)
public class DoubleSubjectTest extends BaseSubjectTestCase {

	private static final double NEARLY_MAX = 1.7976931348623155E308;
	private static final double NEGATIVE_NEARLY_MAX = -1.7976931348623155E308;
	private static final double OVER_MIN = 9.9E-324;
	private static final double UNDER_NEGATIVE_MIN = -9.9E-324;
	private static final double GOLDEN = 1.23;
	private static final double OVER_GOLDEN = 1.2300000000000002;

	private static final Subject.Factory<DoubleSubject, Double> DOUBLE_SUBJECT_FACTORY = new Subject.Factory<DoubleSubject, Double>() {
		@Override
		public DoubleSubject createSubject(FailureMetadata metadata, Double that) {
			return new DoubleSubject(metadata, that);
		}
	};

	@CanIgnoreReturnValue
	private static AssertionError expectFailure(SimpleSubjectBuilderCallback<DoubleSubject, Double> callback) {
		return ExpectFailure.expectFailureAbout(DOUBLE_SUBJECT_FACTORY, callback);
	}

	@Test
	@GwtIncompatible("Math.nextAfter")
	public void testDoubleConstants_matchNextAfter() {
		assertThat(Math.nextAfter(Double.MIN_VALUE, 1.0)).isEqualTo(OVER_MIN);
		assertThat(Math.nextAfter(1.23, Double.POSITIVE_INFINITY)).isEqualTo(OVER_GOLDEN);
		assertThat(Math.nextAfter(Double.MAX_VALUE, 0.0)).isEqualTo(NEARLY_MAX);
		assertThat(Math.nextAfter(-1.0 * Double.MAX_VALUE, 0.0)).isEqualTo(NEGATIVE_NEARLY_MAX);
		assertThat(Math.nextAfter(-1.0 * Double.MIN_VALUE, -1.0)).isEqualTo(UNDER_NEGATIVE_MIN);
	}

	@Test
	public void testJ2clCornerCaseZero() {
		// GWT considers -0.0 to be equal to 0.0. But we've added a special workaround inside Truth.
		assertThatIsEqualToFails(-0.0, 0.0);
	}

	@Test
	@GwtIncompatible("GWT behavior difference")
	public void testJ2clCornerCaseDoubleVsFloat() {
		// Under GWT, 1.23f.toString() is different than 1.23d.toString(), so the message omits types.
		// TODO(b/35377736): Consider making Truth add the types anyway.
		expectFailureWhenTestingThat(1.23).isEqualTo(1.23f);
		assertFailureKeys("expected", "an instance of", "but was", "an instance of");
	}

	@Test
	public void isWithinOf() {
		assertThat(2.0).isWithin(0.0).of(2.0);
		assertThat(2.0).isWithin(0.00001).of(2.0);
		assertThat(2.0).isWithin(1000.0).of(2.0);
		assertThat(2.0).isWithin(1.00001).of(3.0);
		assertThatIsWithinFails(2.0, 0.99999, 3.0);
		assertThatIsWithinFails(2.0, 1000.0, 1003.0);
		assertThatIsWithinFails(2.0, 1000.0, Double.POSITIVE_INFINITY);
		assertThatIsWithinFails(2.0, 1000.0, Double.NaN);
		assertThatIsWithinFails(Double.NEGATIVE_INFINITY, 1000.0, 2.0);
		assertThatIsWithinFails(Double.NaN, 1000.0, 2.0);
	}

	private static void assertThatIsWithinFails(double actual, double tolerance, double expected) {
		ExpectFailure.SimpleSubjectBuilderCallback<DoubleSubject, Double> callback = new ExpectFailure.SimpleSubjectBuilderCallback<DoubleSubject, Double>() {
			@Override
			public void invokeAssertion(SimpleSubjectBuilder<DoubleSubject, Double> expect) {
				expect.that(actual).isWithin(tolerance).of(expected);
			}
		};
		AssertionError failure = expectFailure(callback);
		assertThat(failure).factKeys().containsExactly("expected", "but was", "outside tolerance").inOrder();
		assertThat(failure).factValue("expected").isEqualTo(doubleToString(expected));
		assertThat(failure).factValue("but was").isEqualTo(doubleToString(actual));
		assertThat(failure).factValue("outside tolerance").isEqualTo(doubleToString(tolerance));
	}

	@Test
	public void isNotWithinOf() {
		assertThatIsNotWithinFails(2.0, 0.0, 2.0);
		assertThatIsNotWithinFails(2.0, 0.00001, 2.0);
		assertThatIsNotWithinFails(2.0, 1000.0, 2.0);
		assertThatIsNotWithinFails(2.0, 1.00001, 3.0);
		assertThat(2.0).isNotWithin(0.99999).of(3.0);
		assertThat(2.0).isNotWithin(1000.0).of(1003.0);
		assertThatIsNotWithinFails(2.0, 0.0, Double.POSITIVE_INFINITY);
		assertThatIsNotWithinFails(2.0, 0.0, Double.NaN);
		assertThatIsNotWithinFails(Double.NEGATIVE_INFINITY, 1000.0, 2.0);
		assertThatIsNotWithinFails(Double.NaN, 1000.0, 2.0);
	}

	private static void assertThatIsNotWithinFails(double actual, double tolerance, double expected) {
		ExpectFailure.SimpleSubjectBuilderCallback<DoubleSubject, Double> callback = new ExpectFailure.SimpleSubjectBuilderCallback<DoubleSubject, Double>() {
			@Override
			public void invokeAssertion(SimpleSubjectBuilder<DoubleSubject, Double> expect) {
				expect.that(actual).isNotWithin(tolerance).of(expected);
			}
		};
		AssertionError failure = expectFailure(callback);
		assertThat(failure).factValue("expected not to be").isEqualTo(doubleToString(expected));
		assertThat(failure).factValue("within tolerance").isEqualTo(doubleToString(tolerance));
	}

	@Test
	public void negativeTolerances() {
		isWithinNegativeToleranceThrowsIAE(5.0, -0.5, 4.9);
		isWithinNegativeToleranceThrowsIAE(5.0, -0.5, 4.0);

		isNotWithinNegativeToleranceThrowsIAE(5.0, -0.5, 4.9);
		isNotWithinNegativeToleranceThrowsIAE(5.0, -0.5, 4.0);

		isWithinNegativeToleranceThrowsIAE(+0.0, -0.00001, +0.0);
		isWithinNegativeToleranceThrowsIAE(+0.0, -0.00001, -0.0);
		isWithinNegativeToleranceThrowsIAE(-0.0, -0.00001, +0.0);
		isWithinNegativeToleranceThrowsIAE(-0.0, -0.00001, -0.0);

		isNotWithinNegativeToleranceThrowsIAE(+0.0, -0.00001, +1.0);
		isNotWithinNegativeToleranceThrowsIAE(+0.0, -0.00001, -1.0);
		isNotWithinNegativeToleranceThrowsIAE(-0.0, -0.00001, +1.0);
		isNotWithinNegativeToleranceThrowsIAE(-0.0, -0.00001, -1.0);

		isNotWithinNegativeToleranceThrowsIAE(+1.0, -0.00001, +0.0);
		isNotWithinNegativeToleranceThrowsIAE(+1.0, -0.00001, -0.0);
		isNotWithinNegativeToleranceThrowsIAE(-1.0, -0.00001, +0.0);
		isNotWithinNegativeToleranceThrowsIAE(-1.0, -0.00001, -0.0);

		// You know what's worse than zero? Negative zero.

		isWithinNegativeToleranceThrowsIAE(+0.0, -0.0, +0.0);
		isWithinNegativeToleranceThrowsIAE(+0.0, -0.0, -0.0);
		isWithinNegativeToleranceThrowsIAE(-0.0, -0.0, +0.0);
		isWithinNegativeToleranceThrowsIAE(-0.0, -0.0, -0.0);

		isNotWithinNegativeToleranceThrowsIAE(+1.0, -0.0, +0.0);
		isNotWithinNegativeToleranceThrowsIAE(+1.0, -0.0, -0.0);
		isNotWithinNegativeToleranceThrowsIAE(-1.0, -0.0, +0.0);
		isNotWithinNegativeToleranceThrowsIAE(-1.0, -0.0, -0.0);
	}

	private static void isWithinNegativeToleranceThrowsIAE(double actual, double tolerance, double expected) {
		try {
			assertThat(actual).isWithin(tolerance).of(expected);
			fail("Expected IllegalArgumentException to be thrown but wasn't");
		} catch (IllegalArgumentException iae) {
			assertThat(iae).hasMessageThat().isEqualTo("tolerance (" + tolerance + ") cannot be negative");
		}
	}

	private static void isNotWithinNegativeToleranceThrowsIAE(double actual, double tolerance, double expected) {
		try {
			assertThat(actual).isNotWithin(tolerance).of(expected);
			fail("Expected IllegalArgumentException to be thrown but wasn't");
		} catch (IllegalArgumentException iae) {
			assertThat(iae).hasMessageThat().isEqualTo("tolerance (" + tolerance + ") cannot be negative");
		}
	}

	@Test
	public void nanTolerances() {
		try {
			assertThat(1.0).isWithin(Double.NaN).of(1.0);
			fail("Expected IllegalArgumentException to be thrown but wasn't");
		} catch (IllegalArgumentException iae) {
			assertThat(iae).hasMessageThat().isEqualTo("tolerance cannot be NaN");
		}
		try {
			assertThat(1.0).isNotWithin(Double.NaN).of(2.0);
			fail("Expected IllegalArgumentException to be thrown but wasn't");
		} catch (IllegalArgumentException iae) {
			assertThat(iae).hasMessageThat().isEqualTo("tolerance cannot be NaN");
		}
	}

	@Test
	public void infiniteTolerances() {
		try {
			assertThat(1.0).isWithin(Double.POSITIVE_INFINITY).of(1.0);
			fail("Expected IllegalArgumentException to be thrown but wasn't");
		} catch (IllegalArgumentException iae) {
			assertThat(iae).hasMessageThat().isEqualTo("tolerance cannot be POSITIVE_INFINITY");
		}
		try {
			assertThat(1.0).isNotWithin(Double.POSITIVE_INFINITY).of(2.0);
			fail("Expected IllegalArgumentException to be thrown but wasn't");
		} catch (IllegalArgumentException iae) {
			assertThat(iae).hasMessageThat().isEqualTo("tolerance cannot be POSITIVE_INFINITY");
		}
	}

	@Test
	public void isWithinOfZero() {
		assertThat(+0.0).isWithin(0.00001).of(+0.0);
		assertThat(+0.0).isWithin(0.00001).of(-0.0);
		assertThat(-0.0).isWithin(0.00001).of(+0.0);
		assertThat(-0.0).isWithin(0.00001).of(-0.0);

		assertThat(+0.0).isWithin(0.0).of(+0.0);
		assertThat(+0.0).isWithin(0.0).of(-0.0);
		assertThat(-0.0).isWithin(0.0).of(+0.0);
		assertThat(-0.0).isWithin(0.0).of(-0.0);
	}

	@Test
	public void isNotWithinOfZero() {
		assertThat(+0.0).isNotWithin(0.00001).of(+1.0);
		assertThat(+0.0).isNotWithin(0.00001).of(-1.0);
		assertThat(-0.0).isNotWithin(0.00001).of(+1.0);
		assertThat(-0.0).isNotWithin(0.00001).of(-1.0);

		assertThat(+1.0).isNotWithin(0.00001).of(+0.0);
		assertThat(+1.0).isNotWithin(0.00001).of(-0.0);
		assertThat(-1.0).isNotWithin(0.00001).of(+0.0);
		assertThat(-1.0).isNotWithin(0.00001).of(-0.0);

		assertThat(+1.0).isNotWithin(0.0).of(+0.0);
		assertThat(+1.0).isNotWithin(0.0).of(-0.0);
		assertThat(-1.0).isNotWithin(0.0).of(+0.0);
		assertThat(-1.0).isNotWithin(0.0).of(-0.0);

		assertThatIsNotWithinFails(-0.0, 0.0, 0.0);
	}

	@Test
	public void isWithinZeroTolerance() {
		double max = Double.MAX_VALUE;
		assertThat(max).isWithin(0.0).of(max);
		assertThat(NEARLY_MAX).isWithin(0.0).of(NEARLY_MAX);
		assertThatIsWithinFails(max, 0.0, NEARLY_MAX);
		assertThatIsWithinFails(NEARLY_MAX, 0.0, max);

		double negativeMax = -1.0 * Double.MAX_VALUE;
		assertThat(negativeMax).isWithin(0.0).of(negativeMax);
		assertThat(NEGATIVE_NEARLY_MAX).isWithin(0.0).of(NEGATIVE_NEARLY_MAX);
		assertThatIsWithinFails(negativeMax, 0.0, NEGATIVE_NEARLY_MAX);
		assertThatIsWithinFails(NEGATIVE_NEARLY_MAX, 0.0, negativeMax);

		double min = Double.MIN_VALUE;
		assertThat(min).isWithin(0.0).of(min);
		assertThat(OVER_MIN).isWithin(0.0).of(OVER_MIN);
		assertThatIsWithinFails(min, 0.0, OVER_MIN);
		assertThatIsWithinFails(OVER_MIN, 0.0, min);

		double negativeMin = -1.0 * Double.MIN_VALUE;
		assertThat(negativeMin).isWithin(0.0).of(negativeMin);
		assertThat(UNDER_NEGATIVE_MIN).isWithin(0.0).of(UNDER_NEGATIVE_MIN);
		assertThatIsWithinFails(negativeMin, 0.0, UNDER_NEGATIVE_MIN);
		assertThatIsWithinFails(UNDER_NEGATIVE_MIN, 0.0, negativeMin);
	}

	@Test
	public void isNotWithinZeroTolerance() {
		double max = Double.MAX_VALUE;
		assertThatIsNotWithinFails(max, 0.0, max);
		assertThatIsNotWithinFails(NEARLY_MAX, 0.0, NEARLY_MAX);
		assertThat(max).isNotWithin(0.0).of(NEARLY_MAX);
		assertThat(NEARLY_MAX).isNotWithin(0.0).of(max);

		double min = Double.MIN_VALUE;
		assertThatIsNotWithinFails(min, 0.0, min);
		assertThatIsNotWithinFails(OVER_MIN, 0.0, OVER_MIN);
		assertThat(min).isNotWithin(0.0).of(OVER_MIN);
		assertThat(OVER_MIN).isNotWithin(0.0).of(min);
	}

	@Test
	public void isWithinNonFinite() {
		assertThatIsWithinFails(Double.NaN, 0.00001, Double.NaN);
		assertThatIsWithinFails(Double.NaN, 0.00001, Double.POSITIVE_INFINITY);
		assertThatIsWithinFails(Double.NaN, 0.00001, Double.NEGATIVE_INFINITY);
		assertThatIsWithinFails(Double.NaN, 0.00001, +0.0);
		assertThatIsWithinFails(Double.NaN, 0.00001, -0.0);
		assertThatIsWithinFails(Double.NaN, 0.00001, +1.0);
		assertThatIsWithinFails(Double.NaN, 0.00001, -0.0);
		assertThatIsWithinFails(Double.POSITIVE_INFINITY, 0.00001, Double.POSITIVE_INFINITY);
		assertThatIsWithinFails(Double.POSITIVE_INFINITY, 0.00001, Double.NEGATIVE_INFINITY);
		assertThatIsWithinFails(Double.POSITIVE_INFINITY, 0.00001, +0.0);
		assertThatIsWithinFails(Double.POSITIVE_INFINITY, 0.00001, -0.0);
		assertThatIsWithinFails(Double.POSITIVE_INFINITY, 0.00001, +1.0);
		assertThatIsWithinFails(Double.POSITIVE_INFINITY, 0.00001, -0.0);
		assertThatIsWithinFails(Double.NEGATIVE_INFINITY, 0.00001, Double.NEGATIVE_INFINITY);
		assertThatIsWithinFails(Double.NEGATIVE_INFINITY, 0.00001, +0.0);
		assertThatIsWithinFails(Double.NEGATIVE_INFINITY, 0.00001, -0.0);
		assertThatIsWithinFails(Double.NEGATIVE_INFINITY, 0.00001, +1.0);
		assertThatIsWithinFails(Double.NEGATIVE_INFINITY, 0.00001, -0.0);
		assertThatIsWithinFails(+1.0, 0.00001, Double.NaN);
		assertThatIsWithinFails(+1.0, 0.00001, Double.POSITIVE_INFINITY);
		assertThatIsWithinFails(+1.0, 0.00001, Double.NEGATIVE_INFINITY);
	}

	@Test
	public void isNotWithinNonFinite() {
		assertThatIsNotWithinFails(Double.NaN, 0.00001, Double.NaN);
		assertThatIsNotWithinFails(Double.NaN, 0.00001, Double.POSITIVE_INFINITY);
		assertThatIsNotWithinFails(Double.NaN, 0.00001, Double.NEGATIVE_INFINITY);
		assertThatIsNotWithinFails(Double.NaN, 0.00001, +0.0);
		assertThatIsNotWithinFails(Double.NaN, 0.00001, -0.0);
		assertThatIsNotWithinFails(Double.NaN, 0.00001, +1.0);
		assertThatIsNotWithinFails(Double.NaN, 0.00001, -0.0);
		assertThatIsNotWithinFails(Double.POSITIVE_INFINITY, 0.00001, Double.POSITIVE_INFINITY);
		assertThatIsNotWithinFails(Double.POSITIVE_INFINITY, 0.00001, Double.NEGATIVE_INFINITY);
		assertThatIsNotWithinFails(Double.POSITIVE_INFINITY, 0.00001, +0.0);
		assertThatIsNotWithinFails(Double.POSITIVE_INFINITY, 0.00001, -0.0);
		assertThatIsNotWithinFails(Double.POSITIVE_INFINITY, 0.00001, +1.0);
		assertThatIsNotWithinFails(Double.POSITIVE_INFINITY, 0.00001, -0.0);
		assertThatIsNotWithinFails(Double.NEGATIVE_INFINITY, 0.00001, Double.NEGATIVE_INFINITY);
		assertThatIsNotWithinFails(Double.NEGATIVE_INFINITY, 0.00001, +0.0);
		assertThatIsNotWithinFails(Double.NEGATIVE_INFINITY, 0.00001, -0.0);
		assertThatIsNotWithinFails(Double.NEGATIVE_INFINITY, 0.00001, +1.0);
		assertThatIsNotWithinFails(Double.NEGATIVE_INFINITY, 0.00001, -0.0);
		assertThatIsNotWithinFails(+1.0, 0.00001, Double.NaN);
		assertThatIsNotWithinFails(+1.0, 0.00001, Double.POSITIVE_INFINITY);
		assertThatIsNotWithinFails(+1.0, 0.00001, Double.NEGATIVE_INFINITY);
	}

	@SuppressWarnings("TruthSelfEquals")
	@Test
	public void isEqualTo() {
		assertThat(1.23).isEqualTo(1.23);
		assertThatIsEqualToFails(GOLDEN, OVER_GOLDEN);
		assertThat(Double.POSITIVE_INFINITY).isEqualTo(Double.POSITIVE_INFINITY);
		assertThat(Double.NaN).isEqualTo(Double.NaN);
		assertThat((Double) null).isEqualTo(null);
		assertThat(1.0).isEqualTo(1);
	}

	private static void assertThatIsEqualToFails(double actual, double expected) {
		ExpectFailure.SimpleSubjectBuilderCallback<DoubleSubject, Double> callback = new ExpectFailure.SimpleSubjectBuilderCallback<DoubleSubject, Double>() {
			@Override
			public void invokeAssertion(SimpleSubjectBuilder<DoubleSubject, Double> expect) {
				expect.that(actual).isEqualTo(expected);
			}
		};
		expectFailure(callback);
	}

	@Test
	public void isNotEqualTo() {
		assertThatIsNotEqualToFails(1.23);
		assertThat(GOLDEN).isNotEqualTo(OVER_GOLDEN);
		assertThatIsNotEqualToFails(Double.POSITIVE_INFINITY);
		assertThatIsNotEqualToFails(Double.NaN);
		assertThat(-0.0).isNotEqualTo(0.0);
		assertThatIsNotEqualToFails(null);
		assertThat(1.23).isNotEqualTo(1.23f);
		assertThat(1.0).isNotEqualTo(2);
	}

	private static void assertThatIsNotEqualToFails(@Nullable Double value) {
		ExpectFailure.SimpleSubjectBuilderCallback<DoubleSubject, Double> callback = new ExpectFailure.SimpleSubjectBuilderCallback<DoubleSubject, Double>() {
			@Override
			public void invokeAssertion(SimpleSubjectBuilder<DoubleSubject, Double> expect) {
				expect.that(value).isNotEqualTo(value);
			}
		};
		expectFailure(callback);
	}

	@Test
	public void isZero() {
		assertThat(0.0).isZero();
		assertThat(-0.0).isZero();
		assertThatIsZeroFails(Double.MIN_VALUE);
		assertThatIsZeroFails(-1.23);
		assertThatIsZeroFails(Double.POSITIVE_INFINITY);
		assertThatIsZeroFails(Double.NaN);
		assertThatIsZeroFails(null);
	}

	private static void assertThatIsZeroFails(@Nullable Double value) {
		ExpectFailure.SimpleSubjectBuilderCallback<DoubleSubject, Double> callback = new ExpectFailure.SimpleSubjectBuilderCallback<DoubleSubject, Double>() {
			@Override
			public void invokeAssertion(SimpleSubjectBuilder<DoubleSubject, Double> expect) {
				expect.that(value).isZero();
			}
		};
		AssertionError failure = expectFailure(callback);
		assertThat(failure).factKeys().containsExactly("expected zero", "but was").inOrder();
	}

	@Test
	public void isNonZero() {
		assertThatIsNonZeroFails(0.0, "expected not to be zero");
		assertThatIsNonZeroFails(-0.0, "expected not to be zero");
		assertThat(Double.MIN_VALUE).isNonZero();
		assertThat(-1.23).isNonZero();
		assertThat(Double.POSITIVE_INFINITY).isNonZero();
		assertThat(Double.NaN).isNonZero();
		assertThatIsNonZeroFails(null, "expected a double other than zero");
	}

	private static void assertThatIsNonZeroFails(@Nullable Double value, String factKey) {
		ExpectFailure.SimpleSubjectBuilderCallback<DoubleSubject, Double> callback = new ExpectFailure.SimpleSubjectBuilderCallback<DoubleSubject, Double>() {
			@Override
			public void invokeAssertion(SimpleSubjectBuilder<DoubleSubject, Double> expect) {
				expect.that(value).isNonZero();
			}
		};
		AssertionError failure = expectFailure(callback);
		assertThat(failure).factKeys().containsExactly(factKey, "but was").inOrder();
	}

	@Test
	public void isPositiveInfinity() {
		assertThat(Double.POSITIVE_INFINITY).isPositiveInfinity();
		assertThatIsPositiveInfinityFails(1.23);
		assertThatIsPositiveInfinityFails(Double.NEGATIVE_INFINITY);
		assertThatIsPositiveInfinityFails(Double.NaN);
		assertThatIsPositiveInfinityFails(null);
	}

	private static void assertThatIsPositiveInfinityFails(@Nullable Double value) {
		ExpectFailure.SimpleSubjectBuilderCallback<DoubleSubject, Double> callback = new ExpectFailure.SimpleSubjectBuilderCallback<DoubleSubject, Double>() {
			@Override
			public void invokeAssertion(SimpleSubjectBuilder<DoubleSubject, Double> expect) {
				expect.that(value).isPositiveInfinity();
			}
		};
		expectFailure(callback);
	}

	@Test
	public void isNegativeInfinity() {
		assertThat(Double.NEGATIVE_INFINITY).isNegativeInfinity();
		assertThatIsNegativeInfinityFails(1.23);
		assertThatIsNegativeInfinityFails(Double.POSITIVE_INFINITY);
		assertThatIsNegativeInfinityFails(Double.NaN);
		assertThatIsNegativeInfinityFails(null);
	}

	private static void assertThatIsNegativeInfinityFails(@Nullable Double value) {
		ExpectFailure.SimpleSubjectBuilderCallback<DoubleSubject, Double> callback = new ExpectFailure.SimpleSubjectBuilderCallback<DoubleSubject, Double>() {
			@Override
			public void invokeAssertion(SimpleSubjectBuilder<DoubleSubject, Double> expect) {
				expect.that(value).isNegativeInfinity();
			}
		};
		expectFailure(callback);
	}

	@Test
	public void isNaN() {
		assertThat(Double.NaN).isNaN();
		assertThatIsNaNFails(1.23);
		assertThatIsNaNFails(Double.POSITIVE_INFINITY);
		assertThatIsNaNFails(Double.NEGATIVE_INFINITY);
		assertThatIsNaNFails(null);
	}

	private static void assertThatIsNaNFails(@Nullable Double value) {
		ExpectFailure.SimpleSubjectBuilderCallback<DoubleSubject, Double> callback = new ExpectFailure.SimpleSubjectBuilderCallback<DoubleSubject, Double>() {
			@Override
			public void invokeAssertion(SimpleSubjectBuilder<DoubleSubject, Double> expect) {
				expect.that(value).isNaN();
			}
		};
		expectFailure(callback);
	}

	@Test
	public void isFinite() {
		assertThat(1.23).isFinite();
		assertThat(Double.MAX_VALUE).isFinite();
		assertThat(-1.0 * Double.MIN_VALUE).isFinite();
		assertThatIsFiniteFails(Double.POSITIVE_INFINITY);
		assertThatIsFiniteFails(Double.NEGATIVE_INFINITY);
		assertThatIsFiniteFails(Double.NaN);
		assertThatIsFiniteFails(null);
	}

	private static void assertThatIsFiniteFails(@Nullable Double value) {
		ExpectFailure.SimpleSubjectBuilderCallback<DoubleSubject, Double> callback = new ExpectFailure.SimpleSubjectBuilderCallback<DoubleSubject, Double>() {
			@Override
			public void invokeAssertion(SimpleSubjectBuilder<DoubleSubject, Double> expect) {
				expect.that(value).isFinite();
			}
		};
		AssertionError failure = expectFailure(callback);
		assertThat(failure).factKeys().containsExactly("expected to be finite", "but was").inOrder();
	}

	@Test
	public void isNotNaN() {
		assertThat(1.23).isNotNaN();
		assertThat(Double.MAX_VALUE).isNotNaN();
		assertThat(-1.0 * Double.MIN_VALUE).isNotNaN();
		assertThat(Double.POSITIVE_INFINITY).isNotNaN();
		assertThat(Double.NEGATIVE_INFINITY).isNotNaN();
	}

	@Test
	public void isNotNaNIsNaN() {
		expectFailureWhenTestingThat(Double.NaN).isNotNaN();
	}

	@Test
	public void isNotNaNIsNull() {
		expectFailureWhenTestingThat(null).isNotNaN();
		assertFailureKeys("expected a double other than NaN", "but was");
	}

	@Test
	public void isGreaterThan_int_strictly() {
		expectFailureWhenTestingThat(2.0).isGreaterThan(3);
	}

	@Test
	public void isGreaterThan_int() {
		expectFailureWhenTestingThat(2.0).isGreaterThan(2);
		assertThat(2.0).isGreaterThan(1);
	}

	@Test
	public void isLessThan_int_strictly() {
		expectFailureWhenTestingThat(2.0).isLessThan(1);
	}

	@Test
	public void isLessThan_int() {
		expectFailureWhenTestingThat(2.0).isLessThan(2);
		assertThat(2.0).isLessThan(3);
	}

	@Test
	public void isAtLeast_int() {
		expectFailureWhenTestingThat(2.0).isAtLeast(3);
		assertThat(2.0).isAtLeast(2);
		assertThat(2.0).isAtLeast(1);
	}

	@Test
	public void isAtMost_int() {
		expectFailureWhenTestingThat(2.0).isAtMost(1);
		assertThat(2.0).isAtMost(2);
		assertThat(2.0).isAtMost(3);
	}

	private DoubleSubject expectFailureWhenTestingThat(Double actual) {
		return expectFailure.whenTesting().that(actual);
	}
}
