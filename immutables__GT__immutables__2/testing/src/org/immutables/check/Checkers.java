/*
   Copyright 2013-2018 Immutables Authors and Contributors

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.immutables.check;

import com.google.common.base.Optional;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Convenient wrappers for matcher checking, designed for better code assist(completion) and
 * discoverability for most commonly used matchers.
 */
public class Checkers {

	public static void check(boolean actualCheckResult) {
		check("failed", actualCheckResult);
	}

	public static ObjectChecker<Boolean> check(@Nullable Boolean actualValue) {
		return new ObjectChecker<>(actualValue, false);
	}

	@SafeVarargs
	public static <E> IterableChecker<Iterable<E>, E> checkAll(E... actualValues) {
		return new IterableChecker<Iterable<E>, E>(Arrays.asList(actualValues), false);
	}

	public static <I extends Iterable<E>, E> IterableChecker<I, E> check(@Nullable I actualValue) {
		return new IterableChecker<>(actualValue, false);
	}

	public static <E> IterableChecker<List<E>, E> check(@Nullable E[] actualValue) {
		return check(actualValue == null ? null : Arrays.asList(actualValue));
	}

	public static IterableChecker<List<Long>, Long> check(@Nullable long[] actualLongArray) {
		return check(actualLongArray == null ? null : Longs.asList(actualLongArray));
	}

	public static IterableChecker<List<Double>, Double> check(@Nullable double[] actualDoubleArray) {
		return check(actualDoubleArray == null ? null : Doubles.asList(actualDoubleArray));
	}

	public static IterableChecker<List<Integer>, Integer> check(@Nullable int[] actualIntArray) {
		return check(actualIntArray == null ? null : Ints.asList(actualIntArray));
	}

	public static IterableChecker<List<Byte>, Byte> check(@Nullable byte[] actualByteArray) {
		return check(actualByteArray == null ? null : Bytes.asList(actualByteArray));
	}

	public static StringChecker check(@Nullable char[] actualCharArray) {
		return check(actualCharArray == null ? null : String.valueOf(actualCharArray));
	}

	public static <T> OptionalChecker<T> check(Optional<T> actualValue) {
		return new OptionalChecker<>(actualValue);
	}

	public static StringChecker check(@Nullable String actualString) {
		return new StringChecker(actualString, false);
	}

	public static void check(String description, boolean actualCheckResult) {
		ObjectChecker.verifyCheck(description, actualCheckResult);
	}

	public static <T> ObjectChecker<T> check(@Nullable T actualValue) {
		return new ObjectChecker<>(actualValue, false);
	}
}
