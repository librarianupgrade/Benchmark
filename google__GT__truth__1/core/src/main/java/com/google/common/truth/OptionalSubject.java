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

import java.util.Optional;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Propositions for Java 8 {@link Optional} subjects.
 *
 * @author Christian Gruber
 * @since 1.3.0 (previously part of {@code truth-java8-extension})
 */
@SuppressWarnings("Java7ApiChecker") // used only from APIs with Java 8 in their signatures
@IgnoreJRERequirement
public final class OptionalSubject extends Subject {
	@SuppressWarnings("NullableOptional") // Truth always accepts nulls, no matter the type
	private final @Nullable Optional<?> actual;

	OptionalSubject(FailureMetadata failureMetadata, @SuppressWarnings("NullableOptional") // Truth always accepts nulls, no matter the type
	@Nullable Optional<?> subject, @Nullable String typeDescription) {
		super(failureMetadata, subject, typeDescription);
		this.actual = subject;
	}

	// TODO(cpovirk): Consider making OptionalIntSubject and OptionalLongSubject delegate to this.

	/** Fails if the {@link Optional}{@code <T>} is empty or the subject is null. */
	public void isPresent() {
		if (actual == null) {
			failWithActual(simpleFact("expected present optional"));
		} else if (!actual.isPresent()) {
			failWithoutActual(simpleFact("expected to be present"));
		}
	}

	/** Fails if the {@link Optional}{@code <T>} is present or the subject is null. */
	public void isEmpty() {
		if (actual == null) {
			failWithActual(simpleFact("expected empty optional"));
		} else if (actual.isPresent()) {
			failWithoutActual(simpleFact("expected to be empty"), fact("but was present with value", actual.get()));
		}
	}

	/**
	 * Fails if the {@link Optional}{@code <T>} does not have the given value or the subject is null.
	 *
	 * <p>To make more complex assertions on the optional's value split your assertion in two:
	 *
	 * <pre>{@code
	 * assertThat(myOptional).isPresent();
	 * assertThat(myOptional.get()).contains("foo");
	 * }</pre>
	 */
	public void hasValue(@Nullable Object expected) {
		if (expected == null) {
			throw new NullPointerException("Optional cannot have a null value.");
		}
		if (actual == null) {
			failWithActual("expected an optional with value", expected);
		} else if (!actual.isPresent()) {
			failWithoutActual(fact("expected to have value", expected), simpleFact("but was empty"));
		} else {
			checkNoNeedToDisplayBothValues("get()").that(actual.get()).isEqualTo(expected);
		}
	}

	/**
	 * Obsolete factory instance. This factory was previously necessary for assertions like {@code
	 * assertWithMessage(...).about(paths()).that(path)....}. Now, you can perform assertions like
	 * that without the {@code about(...)} call.
	 */
	public static Subject.Factory<OptionalSubject, Optional<?>> optionals() {
		return (metadata, subject) -> new OptionalSubject(metadata, subject, "optional");
	}
}
