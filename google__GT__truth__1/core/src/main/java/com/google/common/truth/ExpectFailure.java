/*
 * Copyright (c) 2017 Google, Inc.
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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Strings.lenientFormat;
import static com.google.common.truth.Truth.assertAbout;
import static com.google.common.truth.TruthFailureSubject.truthFailures;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.truth.Truth.SimpleAssertionError;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * A utility for testing that assertions against a custom {@link Subject} fail when they should,
 * plus a utility to assert about parts of the resulting failure messages.
 *
 * <p>Usage:
 *
 * <pre>{@code
 *   AssertionError failure =
 *       expectFailure(whenTesting -> whenTesting.that(cancelButton).isVisible());
 *   assertThat(failure).factKeys().containsExactly("expected to be visible");
 *
 * ...
 *
 * private static AssertionError expectFailure(
 *     ExpectFailure.SimpleSubjectBuilderCallback<UiElementSubject, UiElement> assertionCallback) {
 *   return ExpectFailure.expectFailureAbout(uiElements(), assertionCallback);
 * }
 * }</pre>
 *
 * Or, if you can't use lambdas:
 *
 * <pre>
 * {@code @Rule public final ExpectFailure expectFailure = new ExpectFailure();}
 *
 * {@code ...
 *
 *     expectFailure.whenTesting().about(uiElements()).that(cancelButton).isVisible();
 *     assertThat(failure).factKeys().containsExactly("expected to be visible");
 * }</pre>
 *
 * <p>{@code ExpectFailure} is similar to JUnit's {@code assertThrows} (<a
 * href="https://junit.org/junit4/javadoc/latest/org/junit/Assert.html#assertThrows%28java.lang.Class,%20org.junit.function.ThrowingRunnable%29">JUnit
 * 4</a>, <a
 * href="https://junit.org/junit5/docs/current/api/org/junit/jupiter/api/Assertions.html#assertThrows%28java.lang.Class,org.junit.jupiter.api.function.Executable%29">JUnit
 * 5</a>). We recommend it over {@code assertThrows} when you're testing a Truth subject because it
 * also checks that the assertion you're testing uses the supplied {@link FailureStrategy} and calls
 * {@link FailureStrategy#fail} only once.
 */
public final class ExpectFailure implements Platform.JUnitTestRule {
	private boolean inRuleContext = false;
	private boolean failureExpected = false;
	private @Nullable AssertionError failure = null;

	/**
	 * Creates a new instance for use as a {@code @Rule}. See the class documentation for details, and
	 * consider using {@linkplain #expectFailure the lambda version} instead.
	 */
	public ExpectFailure() {
	}

	/**
	 * Returns a test verb that expects the chained assertion to fail, and makes the failure available
	 * via {@link #getFailure}.
	 *
	 * <p>An instance of {@code ExpectFailure} supports only one {@code whenTesting} call per test
	 * method. The static {@link #expectFailure} method, by contrast, does not have this limitation.
	 */
	public StandardSubjectBuilder whenTesting() {
		checkState(inRuleContext, "ExpectFailure must be used as a JUnit @Rule");
		if (failure != null) {
			throw SimpleAssertionError.create("ExpectFailure already captured a failure", failure);
		}
		if (failureExpected) {
			throw new AssertionError("ExpectFailure.whenTesting() called previously, but did not capture a failure.");
		}
		failureExpected = true;
		return StandardSubjectBuilder.forCustomFailureStrategy(this::captureFailure);
	}

	/**
	 * Enters rule context to be ready to capture failures.
	 *
	 * <p>This should be rarely used directly, except if this class is as a long living object but not
	 * as a JUnit rule, like truth subject tests where for GWT compatible reasons.
	 */
	void enterRuleContext() {
		this.inRuleContext = true;
	}

	/** Leaves rule context and verify if a failure has been caught if it's expected. */
	void leaveRuleContext() {
		this.inRuleContext = false;
	}

	/**
	 * Ensures a failure is caught if it's expected (i.e., {@link #whenTesting} is called) and throws
	 * error if not.
	 */
	void ensureFailureCaught() {
		if (failureExpected && failure == null) {
			throw new AssertionError("ExpectFailure.whenTesting() invoked, but no failure was caught."
					+ Platform.EXPECT_FAILURE_WARNING_IF_GWT);
		}
	}

	/** Returns the captured failure, if one occurred. */
	public AssertionError getFailure() {
		if (failure == null) {
			throw new AssertionError("ExpectFailure did not capture a failure.");
		}
		return failure;
	}

	/**
	 * Captures the provided failure, or throws an {@link AssertionError} if a failure had previously
	 * been captured.
	 */
	private void captureFailure(AssertionError captured) {
		if (failure != null) {
			// TODO(diamondm) is it worthwhile to add the failures as suppressed exceptions?
			throw new AssertionError(
					lenientFormat("ExpectFailure.whenTesting() caught multiple failures:\n\n%s\n\n%s\n",
							Platform.getStackTraceAsString(failure), Platform.getStackTraceAsString(captured)));
		}
		failure = captured;
	}

	/**
	 * Static alternative that directly returns the triggered failure. This is intended to be used in
	 * Java 8+ tests similar to {@code expectThrows()}:
	 *
	 * <p>{@code AssertionError failure = expectFailure(whenTesting ->
	 * whenTesting.that(4).isNotEqualTo(4));}
	 */
	@CanIgnoreReturnValue
	public static AssertionError expectFailure(StandardSubjectBuilderCallback assertionCallback) {
		ExpectFailure expectFailure = new ExpectFailure();
		expectFailure.enterRuleContext(); // safe since this instance doesn't leave this method
		assertionCallback.invokeAssertion(expectFailure.whenTesting());
		return expectFailure.getFailure();
	}

	/**
	 * Static alternative that directly returns the triggered failure. This is intended to be used in
	 * Java 8+ tests similar to {@code expectThrows()}:
	 *
	 * <p>{@code AssertionError failure = expectFailureAbout(myTypes(), whenTesting ->
	 * whenTesting.that(myType).hasProperty());}
	 */
	@CanIgnoreReturnValue
	public static <S extends Subject, A> AssertionError expectFailureAbout(Subject.Factory<S, A> factory,
			SimpleSubjectBuilderCallback<S, A> assertionCallback) {
		return expectFailure(whenTesting -> assertionCallback.invokeAssertion(whenTesting.about(factory)));
	}

	/**
	 * Creates a subject for asserting about the given {@link AssertionError}, usually one produced by
	 * Truth.
	 */
	public static TruthFailureSubject assertThat(@Nullable AssertionError actual) {
		return assertAbout(truthFailures()).that(actual);
	}

	@Override
	@GwtIncompatible("org.junit.rules.TestRule")
	@J2ktIncompatible
	public Statement apply(Statement base, Description description) {
		checkNotNull(base);
		checkNotNull(description);
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				enterRuleContext();
				try {
					base.evaluate();
				} finally {
					leaveRuleContext();
				}
				ensureFailureCaught();
			}
		};
	}

	/**
	 * A "functional interface" for {@link #expectFailure expectFailure()} to invoke and capture
	 * failures.
	 *
	 * <p>Java 8+ users should pass a lambda to {@code .expectFailure()} rather than directly
	 * implement this interface. Java 7+ users can define an {@code @Rule ExpectFailure} instance
	 * instead, however if you prefer the {@code .expectFailure()} pattern you can use this interface
	 * to pass in an anonymous class.
	 */
	public interface StandardSubjectBuilderCallback {
		void invokeAssertion(StandardSubjectBuilder whenTesting);
	}

	/**
	 * A "functional interface" for {@link #expectFailureAbout expectFailureAbout()} to invoke and
	 * capture failures.
	 *
	 * <p>Java 8+ users should pass a lambda to {@code .expectFailureAbout()} rather than directly
	 * implement this interface. Java 7+ users can define an {@code @Rule ExpectFailure} instance
	 * instead, however if you prefer the {@code .expectFailureAbout()} pattern you can use this
	 * interface to pass in an anonymous class.
	 */
	public interface SimpleSubjectBuilderCallback<S extends Subject, A> {
		void invokeAssertion(SimpleSubjectBuilder<S, A> whenTesting);
	}
}
