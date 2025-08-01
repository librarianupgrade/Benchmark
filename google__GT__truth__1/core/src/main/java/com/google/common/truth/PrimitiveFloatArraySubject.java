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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.truth.Correspondence.tolerance;

import com.google.common.primitives.Floats;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Arrays;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A Subject for {@code float[]}.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
public final class PrimitiveFloatArraySubject extends AbstractArraySubject {
	private final float @Nullable [] actual;

	PrimitiveFloatArraySubject(FailureMetadata metadata, float @Nullable [] o, @Nullable String typeDescription) {
		super(metadata, o, typeDescription);
		this.actual = o;
	}

	/**
	 * A check that the actual array and {@code expected} are arrays of the same length and type,
	 * containing elements such that each element in {@code expected} is equal to each element in the
	 * actual array, and in the same position, with element equality defined the same way that {@link
	 * Arrays#equals(float[], float[])} and {@link Float#equals(Object)} define it (which is different
	 * to the way that the {@code ==} operator on primitive {@code float} defines it). This method is
	 * <i>not</i> recommended when the code under test is doing any kind of arithmetic: use {@link
	 * #usingTolerance} with a suitable tolerance in that case, e.g. {@code
	 * assertThat(actualArray).usingTolerance(1.0e-5).containsExactly(expectedArray).inOrder()}.
	 * (Remember that the exact result of floating point arithmetic is sensitive to apparently trivial
	 * changes such as replacing {@code (a + b) + c} with {@code a + (b + c)}, and that unless {@code
	 * strictfp} is in force even the result of {@code (a + b) + c} is sensitive to the JVM's choice
	 * of precision for the intermediate result.) This method is recommended when the code under test
	 * is specified as either copying values without modification from its input or returning
	 * well-defined literal or constant values.
	 *
	 * <ul>
	 *   <li>It considers {@link Float#POSITIVE_INFINITY}, {@link Float#NEGATIVE_INFINITY}, and {@link
	 *       Float#NaN} to be equal to themselves (contrast with {@code usingTolerance(0.0)} which
	 *       does not).
	 *   <li>It does <i>not</i> consider {@code -0.0f} to be equal to {@code 0.0f} (contrast with
	 *       {@code usingTolerance(0.0)} which does).
	 * </ul>
	 */
	@Override
	public void isEqualTo(@Nullable Object expected) {
		super.isEqualTo(expected);
	}

	/**
	 * A check that the actual array and {@code expected} are not arrays of the same length and type,
	 * containing elements such that each element in {@code expected} is equal to each element in the
	 * actual array, and in the same position, with element equality defined the same way that {@link
	 * Arrays#equals(float[], float[])} and {@link Float#equals(Object)} define it (which is different
	 * to the way that the {@code ==} operator on primitive {@code float} defines it). See {@link
	 * #isEqualTo(Object)} for advice on when exact equality is recommended.
	 *
	 * <ul>
	 *   <li>It considers {@link Float#POSITIVE_INFINITY}, {@link Float#NEGATIVE_INFINITY}, and {@link
	 *       Float#NaN} to be equal to themselves.
	 *   <li>It does <i>not</i> consider {@code -0.0} to be equal to {@code 0.0}.
	 * </ul>
	 */
	@Override
	public void isNotEqualTo(@Nullable Object expected) {
		super.isNotEqualTo(expected);
	}

	/**
	 * Starts a method chain for a check in which the actual values (i.e. the elements of the array
	 * under test) are compared to expected elements using a {@link Correspondence} which considers
	 * values to correspond if they are finite values within {@code tolerance} of each other. The
	 * check is actually executed by continuing the method chain. For example:
	 *
	 * <pre>{@code
	 * assertThat(actualFloatArray).usingTolerance(1.0e-5f).contains(3.14159f);
	 * }</pre>
	 *
	 * <ul>
	 *   <li>It does not consider values to correspond if either value is infinite or NaN.
	 *   <li>It considers {@code -0.0f} to be within any tolerance of {@code 0.0f}.
	 *   <li>The expected values provided later in the chain will be {@link Number} instances which
	 *       will be converted to floats, which may result in a loss of precision for some numeric
	 *       types.
	 *   <li>The subsequent methods in the chain may throw a {@link NullPointerException} if any
	 *       expected {@link Number} instance is null.
	 * </ul>
	 *
	 * @param tolerance an inclusive upper bound on the difference between the float values of the
	 *     actual and expected numbers, which must be a non-negative finite value, i.e. not {@link
	 *     Float#NaN}, {@link Float#POSITIVE_INFINITY}, or negative, including {@code -0.0f}
	 */
	public FloatArrayAsIterable usingTolerance(double tolerance) {
		return new FloatArrayAsIterable(tolerance(tolerance), iterableSubject());
	}

	private static final Correspondence<Float, Number> EXACT_EQUALITY_CORRESPONDENCE = Correspondence.from(
			// If we were allowed lambdas, this would be:
			// (a, e) -> Float.floatToIntBits(a) == Float.floatToIntBits(checkedToFloat(e)),
			new Correspondence.BinaryPredicate<Float, Number>() {

				@Override
				public boolean apply(Float actual, Number expected) {
					return Float.floatToIntBits(actual) == Float.floatToIntBits(checkedToFloat(expected));
				}
			}, "is exactly equal to");

	private static float checkedToFloat(Number expected) {
		checkNotNull(expected);
		checkArgument(!(expected instanceof Double),
				"Expected value in assertion using exact float equality was a double, which is not "
						+ "supported as a double may not have an exact float representation");
		checkArgument(expected instanceof Float || expected instanceof Integer || expected instanceof Long,
				"Expected value in assertion using exact float equality was of unsupported type %s "
						+ "(it may not have an exact float representation)",
				expected.getClass());
		if (expected instanceof Integer) {
			checkArgument(Math.abs((Integer) expected) <= 1 << 24,
					"Expected value %s in assertion using exact float equality was an int with an absolute "
							+ "value greater than 2^24 which has no exact float representation",
					expected);
		}
		if (expected instanceof Long) {
			checkArgument(Math.abs((Long) expected) <= 1L << 24,
					"Expected value %s in assertion using exact float equality was a long with an absolute "
							+ "value greater than 2^24 which has no exact float representation",
					expected);
		}
		return expected.floatValue();
	}

	/**
	 * Starts a method chain for a check in which the actual values (i.e. the elements of the array
	 * under test) are compared to expected elements using a {@link Correspondence} which considers
	 * values to correspond if they are exactly equal, with equality defined by {@link Float#equals}.
	 * This method is <i>not</i> recommended when the code under test is doing any kind of arithmetic:
	 * use {@link #usingTolerance} with a suitable tolerance in that case. (Remember that the exact
	 * result of floating point arithmetic is sensitive to apparently trivial changes such as
	 * replacing {@code (a + b) + c} with {@code a + (b + c)}, and that unless {@code strictfp} is in
	 * force even the result of {@code (a + b) + c} is sensitive to the JVM's choice of precision for
	 * the intermediate result.) This method is recommended when the code under test is specified as
	 * either copying a value without modification from its input or returning a well-defined literal
	 * or constant value. The check is actually executed by continuing the method chain. For example:
	 *
	 * <pre>{@code
	 * assertThat(actualFloatArray).usingExactEquality().contains(3.14159f);
	 * }</pre>
	 *
	 * <p>For convenience, some subsequent methods accept expected values as {@link Number} instances.
	 * These numbers must be either of type {@link Float}, {@link Integer}, or {@link Long}, and if
	 * they are {@link Integer} or {@link Long} then their absolute values must not exceed 2^24 which
	 * is 16,777,216. (This restriction ensures that the expected values have exact {@link Float}
	 * representations: using exact equality makes no sense if they do not.)
	 *
	 * <ul>
	 *   <li>It considers {@link Float#POSITIVE_INFINITY}, {@link Float#NEGATIVE_INFINITY}, and {@link
	 *       Float#NaN} to be equal to themselves (contrast with {@code usingTolerance(0.0)} which
	 *       does not).
	 *   <li>It does <i>not</i> consider {@code -0.0f} to be equal to {@code 0.0f} (contrast with
	 *       {@code usingTolerance(0.0)} which does).
	 *   <li>The subsequent methods in the chain may throw a {@link NullPointerException} if any
	 *       expected {@link Float} instance is null.
	 * </ul>
	 */
	public FloatArrayAsIterable usingExactEquality() {
		return new FloatArrayAsIterable(EXACT_EQUALITY_CORRESPONDENCE, iterableSubject());
	}

	/**
	 * A partially specified check for doing assertions on the array similar to the assertions
	 * supported for {@link Iterable} subjects, in which the elements of the array under test are
	 * compared to expected elements using either exact or tolerant float equality: see {@link
	 * #usingExactEquality} and {@link #usingTolerance}. Call methods on this object to actually
	 * execute the check.
	 *
	 * <p>In the exact equality case, the methods on this class which take {@link Number} arguments
	 * only accept certain instances: again, see {@link #usingExactEquality} for details.
	 */
	public static final class FloatArrayAsIterable extends IterableSubject.UsingCorrespondence<Float, Number> {

		FloatArrayAsIterable(Correspondence<? super Float, Number> correspondence, IterableSubject subject) {
			super(subject, correspondence);
		}

		/**
		 * As {@link #containsAtLeast(Object, Object, Object...)} but taking a primitive float array.
		 */
		@CanIgnoreReturnValue
		public Ordered containsAtLeast(float[] expected) {
			return containsAtLeastElementsIn(Floats.asList(expected));
		}

		/** As {@link #containsAnyOf(Object, Object, Object...)} but taking a primitive float array. */
		public void containsAnyOf(float[] expected) {
			containsAnyIn(Floats.asList(expected));
		}

		/** As {@link #containsExactly(Object...)} but taking a primitive float array. */
		@CanIgnoreReturnValue
		public Ordered containsExactly(float[] expected) {
			return containsExactlyElementsIn(Floats.asList(expected));
		}

		/** As {@link #containsNoneOf(Object, Object, Object...)} but taking a primitive float array. */
		public void containsNoneOf(float[] excluded) {
			containsNoneIn(Floats.asList(excluded));
		}
	}

	private IterableSubject iterableSubject() {
		return checkNoNeedToDisplayBothValues("asList()").about(iterablesWithCustomFloatToString())
				.that(Floats.asList(checkNotNull(actual)));
	}

	/*
	 * TODO(cpovirk): Should we make Floats.asList().toString() smarter rather than do all this?
	 *
	 * TODO(cpovirk): Or find a general solution for this and MultimapSubject.IterableEntries. But
	 * note that here we don't use _exactly_ PrimitiveFloatArraySubject.this.toString(), as that
	 * contains "float[]." Or maybe we should stop including that in
	 * PrimitiveFloatArraySubject.this.toString(), too, someday?
	 */
	private Factory<IterableSubject, Iterable<?>> iterablesWithCustomFloatToString() {
		return new Factory<IterableSubject, Iterable<?>>() {
			@Override
			public IterableSubject createSubject(FailureMetadata metadata, @Nullable Iterable<?> actual) {
				return new IterableSubjectWithInheritedToString(metadata, actual);
			}
		};
	}

	private final class IterableSubjectWithInheritedToString extends IterableSubject {

		IterableSubjectWithInheritedToString(FailureMetadata metadata, @Nullable Iterable<?> actual) {
			super(metadata, actual);
		}

		@Override
		protected String actualCustomStringRepresentation() {
			return PrimitiveFloatArraySubject.this.actualCustomStringRepresentationForPackageMembersToCall();
		}
	}
}
