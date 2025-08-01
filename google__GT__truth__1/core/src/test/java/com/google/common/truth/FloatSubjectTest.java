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
import static com.google.common.truth.Platform.floatToString;
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
 * Tests for Float Subjects.
 *
 * @author Kurt Alfred Kluever
 */
@RunWith(JUnit4.class)
public class FloatSubjectTest extends BaseSubjectTestCase {
	private static final float NEARLY_MAX = 3.4028233E38f;
	private static final float NEGATIVE_NEARLY_MAX = -3.4028233E38f;
	private static final float JUST_OVER_MIN = 2.8E-45f;
	private static final float JUST_UNDER_NEGATIVE_MIN = -2.8E-45f;
	private static final float GOLDEN = 1.23f;
	private static final float JUST_OVER_GOLDEN = 1.2300001f;

	private static final Subject.Factory<FloatSubject, Float> FLOAT_SUBJECT_FACTORY = new Subject.Factory<FloatSubject, Float>() {
		@Override
		public FloatSubject createSubject(FailureMetadata metadata, Float that) {
			return new FloatSubject(metadata, that);
		}
	};

	@CanIgnoreReturnValue
	private static AssertionError expectFailure(SimpleSubjectBuilderCallback<FloatSubject, Float> callback) {
		return ExpectFailure.expectFailureAbout(FLOAT_SUBJECT_FACTORY, callback);
	}

	@Test
	@GwtIncompatible("Math.nextAfter")
	public void testFloatConstants_matchNextAfter() {
		assertThat(Math.nextAfter(Float.MAX_VALUE, 0.0f)).isEqualTo(NEARLY_MAX);
		assertThat(Math.nextAfter(-1.0f * Float.MAX_VALUE, 0.0f)).isEqualTo(NEGATIVE_NEARLY_MAX);
		assertThat(Math.nextAfter(Float.MIN_VALUE, 1.0f)).isEqualTo(JUST_OVER_MIN);
		assertThat(Math.nextAfter(-1.0f * Float.MIN_VALUE, -1.0f)).isEqualTo(JUST_UNDER_NEGATIVE_MIN);
		assertThat(1.23f).isEqualTo(GOLDEN);
		assertThat(Math.nextAfter(1.23f, Float.POSITIVE_INFINITY)).isEqualTo(JUST_OVER_GOLDEN);
	}

	@Test
	public void testJ2clCornerCaseZero() {
		// GWT considers -0.0 to be equal to 0.0. But we've added a special workaround inside Truth.
		assertThatIsEqualToFails(-0.0f, 0.0f);
	}

	@Test
	@GwtIncompatible("GWT behavior difference")
	public void j2clCornerCaseDoubleVsFloat() {
		// Under GWT, 1.23f.toString() is different than 1.23d.toString(), so the message omits types.
		// TODO(b/35377736): Consider making Truth add the types manually.
		expectFailureWhenTestingThat(1.23f).isEqualTo(1.23);
		assertFailureKeys("expected", "an instance of", "but was", "an instance of");
	}

	@Test
	public void isWithinOf() {
		assertThat(2.0f).isWithin(0.0f).of(2.0f);
		assertThat(2.0f).isWithin(0.00001f).of(2.0f);
		assertThat(2.0f).isWithin(1000.0f).of(2.0f);
		assertThat(2.0f).isWithin(1.00001f).of(3.0f);
		assertThatIsWithinFails(2.0f, 0.99999f, 3.0f);
		assertThatIsWithinFails(2.0f, 1000.0f, 1003.0f);
		assertThatIsWithinFails(2.0f, 1000.0f, Float.POSITIVE_INFINITY);
		assertThatIsWithinFails(2.0f, 1000.0f, Float.NaN);
		assertThatIsWithinFails(Float.NEGATIVE_INFINITY, 1000.0f, 2.0f);
		assertThatIsWithinFails(Float.NaN, 1000.0f, 2.0f);
	}

	private static void assertThatIsWithinFails(float actual, float tolerance, float expected) {
		ExpectFailure.SimpleSubjectBuilderCallback<FloatSubject, Float> callback = new ExpectFailure.SimpleSubjectBuilderCallback<FloatSubject, Float>() {
			@Override
			public void invokeAssertion(SimpleSubjectBuilder<FloatSubject, Float> expect) {
				expect.that(actual).isWithin(tolerance).of(expected);
			}
		};
		AssertionError failure = expectFailure(callback);
		assertThat(failure).factKeys().containsExactly("expected", "but was", "outside tolerance").inOrder();
		assertThat(failure).factValue("expected").isEqualTo(floatToString(expected));
		assertThat(failure).factValue("but was").isEqualTo(floatToString(actual));
		assertThat(failure).factValue("outside tolerance").isEqualTo(floatToString(tolerance));
	}

	@Test
	public void isNotWithinOf() {
		assertThatIsNotWithinFails(2.0f, 0.0f, 2.0f);
		assertThatIsNotWithinFails(2.0f, 0.00001f, 2.0f);
		assertThatIsNotWithinFails(2.0f, 1000.0f, 2.0f);
		assertThatIsNotWithinFails(2.0f, 1.00001f, 3.0f);
		assertThat(2.0f).isNotWithin(0.99999f).of(3.0f);
		assertThat(2.0f).isNotWithin(1000.0f).of(1003.0f);
		assertThatIsNotWithinFails(2.0f, 0.0f, Float.POSITIVE_INFINITY);
		assertThatIsNotWithinFails(2.0f, 0.0f, Float.NaN);
		assertThatIsNotWithinFails(Float.NEGATIVE_INFINITY, 1000.0f, 2.0f);
		assertThatIsNotWithinFails(Float.NaN, 1000.0f, 2.0f);
	}

	private static void assertThatIsNotWithinFails(float actual, float tolerance, float expected) {
		ExpectFailure.SimpleSubjectBuilderCallback<FloatSubject, Float> callback = new ExpectFailure.SimpleSubjectBuilderCallback<FloatSubject, Float>() {
			@Override
			public void invokeAssertion(SimpleSubjectBuilder<FloatSubject, Float> expect) {
				expect.that(actual).isNotWithin(tolerance).of(expected);
			}
		};
		AssertionError failure = expectFailure(callback);
		assertThat(failure).factValue("expected not to be").isEqualTo(floatToString(expected));
		assertThat(failure).factValue("within tolerance").isEqualTo(floatToString(tolerance));
	}

	@Test
	public void negativeTolerances() {
		isWithinNegativeToleranceThrowsIAE(5.0f, -0.5f, 4.9f);
		isWithinNegativeToleranceThrowsIAE(5.0f, -0.5f, 4.0f);

		isNotWithinNegativeToleranceThrowsIAE(5.0f, -0.5f, 4.9f);
		isNotWithinNegativeToleranceThrowsIAE(5.0f, -0.5f, 4.0f);

		isWithinNegativeToleranceThrowsIAE(+0.0f, -0.00001f, +0.0f);
		isWithinNegativeToleranceThrowsIAE(+0.0f, -0.00001f, -0.0f);
		isWithinNegativeToleranceThrowsIAE(-0.0f, -0.00001f, +0.0f);
		isWithinNegativeToleranceThrowsIAE(-0.0f, -0.00001f, -0.0f);

		isNotWithinNegativeToleranceThrowsIAE(+0.0f, -0.00001f, +1.0f);
		isNotWithinNegativeToleranceThrowsIAE(+0.0f, -0.00001f, -1.0f);
		isNotWithinNegativeToleranceThrowsIAE(-0.0f, -0.00001f, +1.0f);
		isNotWithinNegativeToleranceThrowsIAE(-0.0f, -0.00001f, -1.0f);

		isNotWithinNegativeToleranceThrowsIAE(+1.0f, -0.00001f, +0.0f);
		isNotWithinNegativeToleranceThrowsIAE(+1.0f, -0.00001f, -0.0f);
		isNotWithinNegativeToleranceThrowsIAE(-1.0f, -0.00001f, +0.0f);
		isNotWithinNegativeToleranceThrowsIAE(-1.0f, -0.00001f, -0.0f);

		// You know what's worse than zero? Negative zero.

		isWithinNegativeToleranceThrowsIAE(+0.0f, -0.0f, +0.0f);
		isWithinNegativeToleranceThrowsIAE(+0.0f, -0.0f, -0.0f);
		isWithinNegativeToleranceThrowsIAE(-0.0f, -0.0f, +0.0f);
		isWithinNegativeToleranceThrowsIAE(-0.0f, -0.0f, -0.0f);

		isNotWithinNegativeToleranceThrowsIAE(+1.0f, -0.0f, +0.0f);
		isNotWithinNegativeToleranceThrowsIAE(+1.0f, -0.0f, -0.0f);
		isNotWithinNegativeToleranceThrowsIAE(-1.0f, -0.0f, +0.0f);
		isNotWithinNegativeToleranceThrowsIAE(-1.0f, -0.0f, -0.0f);
	}

	private static void isWithinNegativeToleranceThrowsIAE(float actual, float tolerance, float expected) {
		try {
			assertThat(actual).isWithin(tolerance).of(expected);
			fail("Expected IllegalArgumentException to be thrown but wasn't");
		} catch (IllegalArgumentException iae) {
			assertThat(iae).hasMessageThat().isEqualTo("tolerance (" + tolerance + ") cannot be negative");
		}
	}

	private static void isNotWithinNegativeToleranceThrowsIAE(float actual, float tolerance, float expected) {
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
			assertThat(1.0f).isWithin(Float.NaN).of(1.0f);
			fail("Expected IllegalArgumentException to be thrown but wasn't");
		} catch (IllegalArgumentException iae) {
			assertThat(iae).hasMessageThat().isEqualTo("tolerance cannot be NaN");
		}
		try {
			assertThat(1.0f).isNotWithin(Float.NaN).of(2.0f);
			fail("Expected IllegalArgumentException to be thrown but wasn't");
		} catch (IllegalArgumentException iae) {
			assertThat(iae).hasMessageThat().isEqualTo("tolerance cannot be NaN");
		}
	}

	@Test
	public void infiniteTolerances() {
		try {
			assertThat(1.0f).isWithin(Float.POSITIVE_INFINITY).of(1.0f);
			fail("Expected IllegalArgumentException to be thrown but wasn't");
		} catch (IllegalArgumentException iae) {
			assertThat(iae).hasMessageThat().isEqualTo("tolerance cannot be POSITIVE_INFINITY");
		}
		try {
			assertThat(1.0f).isNotWithin(Float.POSITIVE_INFINITY).of(2.0f);
			fail("Expected IllegalArgumentException to be thrown but wasn't");
		} catch (IllegalArgumentException iae) {
			assertThat(iae).hasMessageThat().isEqualTo("tolerance cannot be POSITIVE_INFINITY");
		}
	}

	@Test
	public void isWithinOfZero() {
		assertThat(+0.0f).isWithin(0.00001f).of(+0.0f);
		assertThat(+0.0f).isWithin(0.00001f).of(-0.0f);
		assertThat(-0.0f).isWithin(0.00001f).of(+0.0f);
		assertThat(-0.0f).isWithin(0.00001f).of(-0.0f);

		assertThat(+0.0f).isWithin(0.0f).of(+0.0f);
		assertThat(+0.0f).isWithin(0.0f).of(-0.0f);
		assertThat(-0.0f).isWithin(0.0f).of(+0.0f);
		assertThat(-0.0f).isWithin(0.0f).of(-0.0f);
	}

	@Test
	public void isNotWithinOfZero() {
		assertThat(+0.0f).isNotWithin(0.00001f).of(+1.0f);
		assertThat(+0.0f).isNotWithin(0.00001f).of(-1.0f);
		assertThat(-0.0f).isNotWithin(0.00001f).of(+1.0f);
		assertThat(-0.0f).isNotWithin(0.00001f).of(-1.0f);

		assertThat(+1.0f).isNotWithin(0.00001f).of(+0.0f);
		assertThat(+1.0f).isNotWithin(0.00001f).of(-0.0f);
		assertThat(-1.0f).isNotWithin(0.00001f).of(+0.0f);
		assertThat(-1.0f).isNotWithin(0.00001f).of(-0.0f);

		assertThat(+1.0f).isNotWithin(0.0f).of(+0.0f);
		assertThat(+1.0f).isNotWithin(0.0f).of(-0.0f);
		assertThat(-1.0f).isNotWithin(0.0f).of(+0.0f);
		assertThat(-1.0f).isNotWithin(0.0f).of(-0.0f);

		assertThatIsNotWithinFails(-0.0f, 0.0f, 0.0f);
	}

	@Test
	public void isWithinZeroTolerance() {
		float max = Float.MAX_VALUE;
		assertThat(max).isWithin(0.0f).of(max);
		assertThat(NEARLY_MAX).isWithin(0.0f).of(NEARLY_MAX);
		assertThatIsWithinFails(max, 0.0f, NEARLY_MAX);
		assertThatIsWithinFails(NEARLY_MAX, 0.0f, max);

		float negativeMax = -1.0f * Float.MAX_VALUE;
		assertThat(negativeMax).isWithin(0.0f).of(negativeMax);
		assertThat(NEGATIVE_NEARLY_MAX).isWithin(0.0f).of(NEGATIVE_NEARLY_MAX);
		assertThatIsWithinFails(negativeMax, 0.0f, NEGATIVE_NEARLY_MAX);
		assertThatIsWithinFails(NEGATIVE_NEARLY_MAX, 0.0f, negativeMax);

		float min = Float.MIN_VALUE;
		assertThat(min).isWithin(0.0f).of(min);
		assertThat(JUST_OVER_MIN).isWithin(0.0f).of(JUST_OVER_MIN);
		assertThatIsWithinFails(min, 0.0f, JUST_OVER_MIN);
		assertThatIsWithinFails(JUST_OVER_MIN, 0.0f, min);

		float negativeMin = -1.0f * Float.MIN_VALUE;
		assertThat(negativeMin).isWithin(0.0f).of(negativeMin);
		assertThat(JUST_UNDER_NEGATIVE_MIN).isWithin(0.0f).of(JUST_UNDER_NEGATIVE_MIN);
		assertThatIsWithinFails(negativeMin, 0.0f, JUST_UNDER_NEGATIVE_MIN);
		assertThatIsWithinFails(JUST_UNDER_NEGATIVE_MIN, 0.0f, negativeMin);
	}

	@Test
	public void isNotWithinZeroTolerance() {
		float max = Float.MAX_VALUE;
		assertThatIsNotWithinFails(max, 0.0f, max);
		assertThatIsNotWithinFails(NEARLY_MAX, 0.0f, NEARLY_MAX);
		assertThat(max).isNotWithin(0.0f).of(NEARLY_MAX);
		assertThat(NEARLY_MAX).isNotWithin(0.0f).of(max);

		float min = Float.MIN_VALUE;
		assertThatIsNotWithinFails(min, 0.0f, min);
		assertThatIsNotWithinFails(JUST_OVER_MIN, 0.0f, JUST_OVER_MIN);
		assertThat(min).isNotWithin(0.0f).of(JUST_OVER_MIN);
		assertThat(JUST_OVER_MIN).isNotWithin(0.0f).of(min);
	}

	@Test
	public void isWithinNonFinite() {
		assertThatIsWithinFails(Float.NaN, 0.00001f, Float.NaN);
		assertThatIsWithinFails(Float.NaN, 0.00001f, Float.POSITIVE_INFINITY);
		assertThatIsWithinFails(Float.NaN, 0.00001f, Float.NEGATIVE_INFINITY);
		assertThatIsWithinFails(Float.NaN, 0.00001f, +0.0f);
		assertThatIsWithinFails(Float.NaN, 0.00001f, -0.0f);
		assertThatIsWithinFails(Float.NaN, 0.00001f, +1.0f);
		assertThatIsWithinFails(Float.NaN, 0.00001f, -0.0f);
		assertThatIsWithinFails(Float.POSITIVE_INFINITY, 0.00001f, Float.POSITIVE_INFINITY);
		assertThatIsWithinFails(Float.POSITIVE_INFINITY, 0.00001f, Float.NEGATIVE_INFINITY);
		assertThatIsWithinFails(Float.POSITIVE_INFINITY, 0.00001f, +0.0f);
		assertThatIsWithinFails(Float.POSITIVE_INFINITY, 0.00001f, -0.0f);
		assertThatIsWithinFails(Float.POSITIVE_INFINITY, 0.00001f, +1.0f);
		assertThatIsWithinFails(Float.POSITIVE_INFINITY, 0.00001f, -0.0f);
		assertThatIsWithinFails(Float.NEGATIVE_INFINITY, 0.00001f, Float.NEGATIVE_INFINITY);
		assertThatIsWithinFails(Float.NEGATIVE_INFINITY, 0.00001f, +0.0f);
		assertThatIsWithinFails(Float.NEGATIVE_INFINITY, 0.00001f, -0.0f);
		assertThatIsWithinFails(Float.NEGATIVE_INFINITY, 0.00001f, +1.0f);
		assertThatIsWithinFails(Float.NEGATIVE_INFINITY, 0.00001f, -0.0f);
		assertThatIsWithinFails(+1.0f, 0.00001f, Float.NaN);
		assertThatIsWithinFails(+1.0f, 0.00001f, Float.POSITIVE_INFINITY);
		assertThatIsWithinFails(+1.0f, 0.00001f, Float.NEGATIVE_INFINITY);
	}

	@Test
	public void isNotWithinNonFinite() {
		assertThatIsNotWithinFails(Float.NaN, 0.00001f, Float.NaN);
		assertThatIsNotWithinFails(Float.NaN, 0.00001f, Float.POSITIVE_INFINITY);
		assertThatIsNotWithinFails(Float.NaN, 0.00001f, Float.NEGATIVE_INFINITY);
		assertThatIsNotWithinFails(Float.NaN, 0.00001f, +0.0f);
		assertThatIsNotWithinFails(Float.NaN, 0.00001f, -0.0f);
		assertThatIsNotWithinFails(Float.NaN, 0.00001f, +1.0f);
		assertThatIsNotWithinFails(Float.NaN, 0.00001f, -0.0f);
		assertThatIsNotWithinFails(Float.POSITIVE_INFINITY, 0.00001f, Float.POSITIVE_INFINITY);
		assertThatIsNotWithinFails(Float.POSITIVE_INFINITY, 0.00001f, Float.NEGATIVE_INFINITY);
		assertThatIsNotWithinFails(Float.POSITIVE_INFINITY, 0.00001f, +0.0f);
		assertThatIsNotWithinFails(Float.POSITIVE_INFINITY, 0.00001f, -0.0f);
		assertThatIsNotWithinFails(Float.POSITIVE_INFINITY, 0.00001f, +1.0f);
		assertThatIsNotWithinFails(Float.POSITIVE_INFINITY, 0.00001f, -0.0f);
		assertThatIsNotWithinFails(Float.NEGATIVE_INFINITY, 0.00001f, Float.NEGATIVE_INFINITY);
		assertThatIsNotWithinFails(Float.NEGATIVE_INFINITY, 0.00001f, +0.0f);
		assertThatIsNotWithinFails(Float.NEGATIVE_INFINITY, 0.00001f, -0.0f);
		assertThatIsNotWithinFails(Float.NEGATIVE_INFINITY, 0.00001f, +1.0f);
		assertThatIsNotWithinFails(Float.NEGATIVE_INFINITY, 0.00001f, -0.0f);
		assertThatIsNotWithinFails(+1.0f, 0.00001f, Float.NaN);
		assertThatIsNotWithinFails(+1.0f, 0.00001f, Float.POSITIVE_INFINITY);
		assertThatIsNotWithinFails(+1.0f, 0.00001f, Float.NEGATIVE_INFINITY);
	}

	@SuppressWarnings("TruthSelfEquals")
	@Test
	public void isEqualTo() {
		assertThat(GOLDEN).isEqualTo(GOLDEN);
		assertThatIsEqualToFails(GOLDEN, JUST_OVER_GOLDEN);
		assertThat(Float.POSITIVE_INFINITY).isEqualTo(Float.POSITIVE_INFINITY);
		assertThat(Float.NaN).isEqualTo(Float.NaN);
		assertThat((Float) null).isEqualTo(null);
		assertThat(1.0f).isEqualTo(1);
	}

	private static void assertThatIsEqualToFails(float actual, float expected) {
		ExpectFailure.SimpleSubjectBuilderCallback<FloatSubject, Float> callback = new ExpectFailure.SimpleSubjectBuilderCallback<FloatSubject, Float>() {
			@Override
			public void invokeAssertion(SimpleSubjectBuilder<FloatSubject, Float> expect) {
				expect.that(actual).isEqualTo(expected);
			}
		};
		expectFailure(callback);
	}

	@Test
	public void isNotEqualTo() {
		assertThatIsNotEqualToFails(GOLDEN);
		assertThat(GOLDEN).isNotEqualTo(JUST_OVER_GOLDEN);
		assertThatIsNotEqualToFails(Float.POSITIVE_INFINITY);
		assertThatIsNotEqualToFails(Float.NaN);
		assertThat(-0.0f).isNotEqualTo(0.0f);
		assertThatIsNotEqualToFails(null);
		assertThat(1.23f).isNotEqualTo(1.23);
		assertThat(1.0f).isNotEqualTo(2);
	}

	private static void assertThatIsNotEqualToFails(@Nullable Float value) {
		ExpectFailure.SimpleSubjectBuilderCallback<FloatSubject, Float> callback = new ExpectFailure.SimpleSubjectBuilderCallback<FloatSubject, Float>() {
			@Override
			public void invokeAssertion(SimpleSubjectBuilder<FloatSubject, Float> expect) {
				expect.that(value).isNotEqualTo(value);
			}
		};
		expectFailure(callback);
	}

	@Test
	public void isZero() {
		assertThat(0.0f).isZero();
		assertThat(-0.0f).isZero();
		assertThatIsZeroFails(Float.MIN_VALUE);
		assertThatIsZeroFails(-1.23f);
		assertThatIsZeroFails(Float.POSITIVE_INFINITY);
		assertThatIsZeroFails(Float.NaN);
		assertThatIsZeroFails(null);
	}

	private static void assertThatIsZeroFails(@Nullable Float value) {
		ExpectFailure.SimpleSubjectBuilderCallback<FloatSubject, Float> callback = new ExpectFailure.SimpleSubjectBuilderCallback<FloatSubject, Float>() {
			@Override
			public void invokeAssertion(SimpleSubjectBuilder<FloatSubject, Float> expect) {
				expect.that(value).isZero();
			}
		};
		AssertionError failure = expectFailure(callback);
		assertThat(failure).factKeys().containsExactly("expected zero", "but was").inOrder();
	}

	@Test
	public void isNonZero() {
		assertThatIsNonZeroFails(0.0f, "expected not to be zero");
		assertThatIsNonZeroFails(-0.0f, "expected not to be zero");
		assertThat(Float.MIN_VALUE).isNonZero();
		assertThat(-1.23f).isNonZero();
		assertThat(Float.POSITIVE_INFINITY).isNonZero();
		assertThat(Float.NaN).isNonZero();
		assertThatIsNonZeroFails(null, "expected a float other than zero");
	}

	private static void assertThatIsNonZeroFails(@Nullable Float value, String factKey) {
		ExpectFailure.SimpleSubjectBuilderCallback<FloatSubject, Float> callback = new ExpectFailure.SimpleSubjectBuilderCallback<FloatSubject, Float>() {
			@Override
			public void invokeAssertion(SimpleSubjectBuilder<FloatSubject, Float> expect) {
				expect.that(value).isNonZero();
			}
		};
		AssertionError failure = expectFailure(callback);
		assertThat(failure).factKeys().containsExactly(factKey, "but was").inOrder();
	}

	@Test
	public void isPositiveInfinity() {
		assertThat(Float.POSITIVE_INFINITY).isPositiveInfinity();
		assertThatIsPositiveInfinityFails(1.23f);
		assertThatIsPositiveInfinityFails(Float.NEGATIVE_INFINITY);
		assertThatIsPositiveInfinityFails(Float.NaN);
		assertThatIsPositiveInfinityFails(null);
	}

	private static void assertThatIsPositiveInfinityFails(@Nullable Float value) {
		ExpectFailure.SimpleSubjectBuilderCallback<FloatSubject, Float> callback = new ExpectFailure.SimpleSubjectBuilderCallback<FloatSubject, Float>() {
			@Override
			public void invokeAssertion(SimpleSubjectBuilder<FloatSubject, Float> expect) {
				expect.that(value).isPositiveInfinity();
			}
		};
		expectFailure(callback);
	}

	@Test
	public void isNegativeInfinity() {
		assertThat(Float.NEGATIVE_INFINITY).isNegativeInfinity();
		assertThatIsNegativeInfinityFails(1.23f);
		assertThatIsNegativeInfinityFails(Float.POSITIVE_INFINITY);
		assertThatIsNegativeInfinityFails(Float.NaN);
		assertThatIsNegativeInfinityFails(null);
	}

	private static void assertThatIsNegativeInfinityFails(@Nullable Float value) {
		ExpectFailure.SimpleSubjectBuilderCallback<FloatSubject, Float> callback = new ExpectFailure.SimpleSubjectBuilderCallback<FloatSubject, Float>() {
			@Override
			public void invokeAssertion(SimpleSubjectBuilder<FloatSubject, Float> expect) {
				expect.that(value).isNegativeInfinity();
			}
		};
		expectFailure(callback);
	}

	@Test
	public void isNaN() {
		assertThat(Float.NaN).isNaN();
		assertThatIsNaNFails(1.23f);
		assertThatIsNaNFails(Float.POSITIVE_INFINITY);
		assertThatIsNaNFails(Float.NEGATIVE_INFINITY);
		assertThatIsNaNFails(null);
	}

	private static void assertThatIsNaNFails(@Nullable Float value) {
		ExpectFailure.SimpleSubjectBuilderCallback<FloatSubject, Float> callback = new ExpectFailure.SimpleSubjectBuilderCallback<FloatSubject, Float>() {
			@Override
			public void invokeAssertion(SimpleSubjectBuilder<FloatSubject, Float> expect) {
				expect.that(value).isNaN();
			}
		};
		expectFailure(callback);
	}

	@Test
	public void isFinite() {
		assertThat(1.23f).isFinite();
		assertThat(Float.MAX_VALUE).isFinite();
		assertThat(-1.0 * Float.MIN_VALUE).isFinite();
		assertThatIsFiniteFails(Float.POSITIVE_INFINITY);
		assertThatIsFiniteFails(Float.NEGATIVE_INFINITY);
		assertThatIsFiniteFails(Float.NaN);
		assertThatIsFiniteFails(null);
	}

	private static void assertThatIsFiniteFails(@Nullable Float value) {
		ExpectFailure.SimpleSubjectBuilderCallback<FloatSubject, Float> callback = new ExpectFailure.SimpleSubjectBuilderCallback<FloatSubject, Float>() {
			@Override
			public void invokeAssertion(SimpleSubjectBuilder<FloatSubject, Float> expect) {
				expect.that(value).isFinite();
			}
		};
		AssertionError failure = expectFailure(callback);
		assertThat(failure).factKeys().containsExactly("expected to be finite", "but was").inOrder();
	}

	@Test
	public void isNotNaN() {
		assertThat(1.23f).isNotNaN();
		assertThat(Float.MAX_VALUE).isNotNaN();
		assertThat(-1.0 * Float.MIN_VALUE).isNotNaN();
		assertThat(Float.POSITIVE_INFINITY).isNotNaN();
		assertThat(Float.NEGATIVE_INFINITY).isNotNaN();
	}

	@Test
	public void isNotNaNIsNaN() {
		expectFailureWhenTestingThat(Float.NaN).isNotNaN();
	}

	@Test
	public void isNotNaNIsNull() {
		expectFailureWhenTestingThat(null).isNotNaN();
		assertFailureKeys("expected a float other than NaN", "but was");
	}

	@Test
	public void isGreaterThan_int_strictly() {
		expectFailureWhenTestingThat(2.0f).isGreaterThan(3);
	}

	@Test
	public void isGreaterThan_int() {
		expectFailureWhenTestingThat(2.0f).isGreaterThan(2);
		assertThat(2.0f).isGreaterThan(1);
		assertThat(0x1.0p30f).isGreaterThan((1 << 30) - 1);
	}

	@Test
	public void isLessThan_int_strictly() {
		expectFailureWhenTestingThat(2.0f).isLessThan(1);
	}

	@Test
	public void isLessThan_int() {
		expectFailureWhenTestingThat(2.0f).isLessThan(2);
		assertThat(2.0f).isLessThan(3);
		assertThat(0x1.0p30f).isLessThan((1 << 30) + 1);
	}

	@Test
	public void isAtLeast_int() {
		expectFailureWhenTestingThat(2.0f).isAtLeast(3);
		assertThat(2.0f).isAtLeast(2);
		assertThat(2.0f).isAtLeast(1);
	}

	@Test
	public void isAtLeast_int_withNoExactFloatRepresentation() {
		expectFailureWhenTestingThat(0x1.0p30f).isAtLeast((1 << 30) + 1);
	}

	@Test
	public void isAtMost_int() {
		expectFailureWhenTestingThat(2.0f).isAtMost(1);
		assertThat(2.0f).isAtMost(2);
		assertThat(2.0f).isAtMost(3);
	}

	@Test
	public void isAtMost_int_withNoExactFloatRepresentation() {
		expectFailureWhenTestingThat(0x1.0p30f).isAtMost((1 << 30) - 1);
	}

	private FloatSubject expectFailureWhenTestingThat(Float actual) {
		return expectFailure.whenTesting().that(actual);
	}
}
