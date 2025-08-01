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

import static com.google.common.base.Strings.commonPrefix;
import static com.google.common.base.Strings.commonSuffix;
import static com.google.common.truth.Fact.fact;
import static com.google.common.truth.SubjectUtils.concat;
import static java.lang.Character.isHighSurrogate;
import static java.lang.Character.isLowSurrogate;
import static java.lang.Math.max;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Contains part of the code responsible for creating a JUnit {@code ComparisonFailure} (if
 * available) or a plain {@code AssertionError} (if not).
 *
 * <p>This particular class is responsible for the fallback when a platform offers {@code
 * ComparisonFailure} but it is not available in a particular test environment. In practice, that
 * should mean open-source JRE users who choose to exclude our JUnit 4 dependency.
 *
 * <p>(This class also includes logic to format expected and actual values for easier reading.)
 *
 * <p>Another part of the fallback logic is {@code Platform.ComparisonFailureWithFacts}, which has a
 * different implementation under GWT/j2cl, where {@code ComparisonFailure} is also unavailable but
 * we can't just recover from that at runtime.
 */
final class ComparisonFailures {
	static ImmutableList<Fact> makeComparisonFailureFacts(ImmutableList<Fact> headFacts, ImmutableList<Fact> tailFacts,
			String expected, String actual) {
		return concat(headFacts, formatExpectedAndActual(expected, actual), tailFacts);
	}

	/**
	 * Returns one or more facts describing the difference between the given expected and actual
	 * values.
	 *
	 * <p>Currently, that means either 2 facts (one each for expected and actual) or 1 fact with a
	 * diff-like (but much simpler) view.
	 *
	 * <p>In the case of 2 facts, the facts contain either the full expected and actual values or, if
	 * the values have a long prefix or suffix in common, abbreviated values with "…" at the beginning
	 * or end.
	 */
	@VisibleForTesting
	static ImmutableList<Fact> formatExpectedAndActual(String expected, String actual) {
		ImmutableList<Fact> result;

		// TODO(cpovirk): Call attention to differences in trailing whitespace.
		// TODO(cpovirk): And changes in the *kind* of whitespace characters in the middle of the line.

		result = Platform.makeDiff(expected, actual);
		if (result != null) {
			return result;
		}

		result = removeCommonPrefixAndSuffix(expected, actual);
		if (result != null) {
			return result;
		}

		return ImmutableList.of(fact("expected", expected), fact("but was", actual));
	}

	private static @Nullable ImmutableList<Fact> removeCommonPrefixAndSuffix(String expected, String actual) {
		int originalExpectedLength = expected.length();

		// TODO(cpovirk): Use something like BreakIterator where available.
		/*
		 * TODO(cpovirk): If the abbreviated values contain newlines, maybe expand them to contain a
		 * newline on each end so that we don't start mid-line? That way, horizontally aligned text will
		 * remain horizontally aligned. But of course, for many multi-line strings, we won't enter this
		 * method at all because we'll generate diff-style output instead. So we might not need to worry
		 * too much about newlines here.
		 */
		// TODO(cpovirk): Avoid splitting in the middle of "\r\n."
		int prefix = commonPrefix(expected, actual).length();
		prefix = max(0, prefix - CONTEXT);
		while (prefix > 0 && validSurrogatePairAt(expected, prefix - 1)) {
			prefix--;
		}
		// No need to hide the prefix unless it's long.
		if (prefix > 3) {
			expected = "…" + expected.substring(prefix);
			actual = "…" + actual.substring(prefix);
		}

		int suffix = commonSuffix(expected, actual).length();
		suffix = max(0, suffix - CONTEXT);
		while (suffix > 0 && validSurrogatePairAt(expected, expected.length() - suffix - 1)) {
			suffix--;
		}
		// No need to hide the suffix unless it's long.
		if (suffix > 3) {
			expected = expected.substring(0, expected.length() - suffix) + "…";
			actual = actual.substring(0, actual.length() - suffix) + "…";
		}

		if (originalExpectedLength - expected.length() < WORTH_HIDING) {
			return null;
		}

		return ImmutableList.of(fact("expected", expected), fact("but was", actual));
	}

	private static final int CONTEXT = 20;
	private static final int WORTH_HIDING = 60;

	// From c.g.c.base.Strings.
	private static boolean validSurrogatePairAt(CharSequence string, int index) {
		return index >= 0 && index <= (string.length() - 2) && isHighSurrogate(string.charAt(index))
				&& isLowSurrogate(string.charAt(index + 1));
	}

	private ComparisonFailures() {
	}
}
