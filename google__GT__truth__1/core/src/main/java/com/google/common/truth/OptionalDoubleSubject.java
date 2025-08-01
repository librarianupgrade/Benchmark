/*
 * Copyright (c) 2016 Google, Inc.
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

import static com.google.common.truth.Fact.fact;
import static com.google.common.truth.Fact.simpleFact;

import java.util.OptionalDouble;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Propositions for Java 8 {@link OptionalDouble} subjects.
 *
 * @author Ben Douglass
 * @since 1.3.0 (previously part of {@code truth-java8-extension})
 */
@SuppressWarnings("Java7ApiChecker") // used only from APIs with Java 8 in their signatures
@IgnoreJRERequirement
public final class OptionalDoubleSubject extends Subject {

	private final OptionalDouble actual;

	OptionalDoubleSubject(FailureMetadata failureMetadata, @Nullable OptionalDouble subject,
			@Nullable String typeDescription) {
		super(failureMetadata, subject, typeDescription);
		this.actual = subject;
	}

	/** Fails if the {@link OptionalDouble} is empty or the subject is null. */
	public void isPresent() {
		if (actual == null) {
			failWithActual(simpleFact("expected present optional"));
		} else if (!actual.isPresent()) {
			failWithoutActual(simpleFact("expected to be present"));
		}
	}

	/** Fails if the {@link OptionalDouble} is present or the subject is null. */
	public void isEmpty() {
		if (actual == null) {
			failWithActual(simpleFact("expected empty optional"));
		} else if (actual.isPresent()) {
			failWithoutActual(simpleFact("expected to be empty"),
					fact("but was present with value", actual.getAsDouble()));
		}
	}

	/**
	 * Fails if the {@link OptionalDouble} does not have the given value or the subject is null. This
	 * method is <i>not</i> recommended when the code under test is doing any kind of arithmetic,
	 * since the exact result of floating point arithmetic is sensitive to apparently trivial changes.
	 * More sophisticated comparisons can be done using {@code assertThat(optional.getAsDouble())…}.
	 * This method is recommended when the code under test is specified as either copying a value
	 * without modification from its input or returning a well-defined literal or constant value.
	 */
	public void hasValue(double expected) {
		if (actual == null) {
			failWithActual("expected an optional with value", expected);
		} else if (!actual.isPresent()) {
			failWithoutActual(fact("expected to have value", expected), simpleFact("but was absent"));
		} else {
			checkNoNeedToDisplayBothValues("getAsDouble()").that(actual.getAsDouble()).isEqualTo(expected);
		}
	}

	/**
	 * Obsolete factory instance. This factory was previously necessary for assertions like {@code
	 * assertWithMessage(...).about(optionalDoubles()).that(optional)....}. Now, you can perform
	 * assertions like that without the {@code about(...)} call.
	 */
	public static Subject.Factory<OptionalDoubleSubject, OptionalDouble> optionalDoubles() {
		return (metadata, subject) -> new OptionalDoubleSubject(metadata, subject, "optionalDouble");
	}
}
