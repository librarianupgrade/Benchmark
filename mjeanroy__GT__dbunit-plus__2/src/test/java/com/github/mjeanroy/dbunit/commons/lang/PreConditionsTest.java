/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2019 Mickael Jeanroy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.mjeanroy.dbunit.commons.lang;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings("SameParameterValue")
class PreConditionsTest {

	@Test
	void it_should_throw_null_pointer_exception() {
		final String message = "should not be null";
		assertThatThrownBy(() -> PreConditions.notNull(null, message)).isExactlyInstanceOf(NullPointerException.class)
				.hasMessage(message);
	}

	@Test
	void it_should_not_throw_null_pointer_exception_and_return_value() {
		final String message = "should not be null";
		final String value = "value";
		final String result = PreConditions.notNull(value, message);

		assertThat(result).isSameAs(value);
	}

	@Test
	void it_should_throw_null_pointer_exception_if_value_is_null() {
		final String message = "should not be blank";
		assertThatThrownBy(() -> PreConditions.notBlank(null, message)).isExactlyInstanceOf(NullPointerException.class)
				.hasMessage(message);
	}

	@Test
	void it_should_throw_illegal_argument_exception_if_value_is_empty() {
		final String message = "should not be blank";
		assertThatThrownBy(() -> PreConditions.notBlank("", message))
				.isExactlyInstanceOf(IllegalArgumentException.class).hasMessage(message);
	}

	@Test
	void it_should_throw_illegal_argument_exception_if_value_is_blank() {
		final String message = "should not be blank";
		assertThatThrownBy(() -> PreConditions.notBlank("   ", message))
				.isExactlyInstanceOf(IllegalArgumentException.class).hasMessage(message);
	}

	@Test
	void it_should_not_throw_exception_and_return_value_if_value_is_not_blank() {
		final String message = "should not be null";
		final String value = "value";
		final String result = PreConditions.notBlank(value, message);

		assertThat(result).isSameAs(value);
	}

	@Test
	void it_should_throw_illegal_argument_exception_if_character_is_blank() {
		final String message = "should not be blank";
		assertThatThrownBy(() -> PreConditions.notBlank(' ', message))
				.isExactlyInstanceOf(IllegalArgumentException.class).hasMessage(message);
	}

	@Test
	void it_should_not_throw_exception_and_return_value_if_character_is_not_blank() {
		final String message = "should not be null";
		final char value = ';';
		final char result = PreConditions.notBlank(value, message);

		assertThat(result).isSameAs(value);
	}

	@Test
	void startsWith_should_throw_null_pointer_exception_if_value_is_null() {
		final String message = "should not be blank";
		assertThatThrownBy(() -> PreConditions.startsWith(null, "prefix", message))
				.isExactlyInstanceOf(NullPointerException.class).hasMessage(message);
	}

	@Test
	void startsWith_should_throw_illegal_argument_exception_if_value_is_empty() {
		final String message = "should not be blank";
		assertThatThrownBy(() -> PreConditions.startsWith("", "prefix", message))
				.isExactlyInstanceOf(IllegalArgumentException.class).hasMessage(message);
	}

	@Test
	void startsWith_should_throw_illegal_argument_exception_if_value_is_blank() {
		final String message = "should not be blank";
		assertThatThrownBy(() -> PreConditions.startsWith("   ", "prefix", message))
				.isExactlyInstanceOf(IllegalArgumentException.class).hasMessage(message);
	}

	@Test
	void startsWith_should_throw_illegal_argument_exception_if_value_does_not_start_with_prefix() {
		final String message = "should not be blank";
		assertThatThrownBy(() -> PreConditions.startsWith("foo:bar", "prefix", message))
				.isExactlyInstanceOf(IllegalArgumentException.class).hasMessage(message);
	}

	@Test
	void startsWith_should_not_throw_exception_and_return_value_if_value_start_with_prefix() {
		final String message = "should not be null";
		final String value = "prefix:value";
		final String result = PreConditions.startsWith(value, "prefix", message);

		assertThat(result).isSameAs(value);
	}

	@Test
	void checkArgument_should_not_throw_exception_with_true() {
		final String message = "should not be null";
		PreConditions.checkArgument(true, message);
	}

	@Test
	void checkArgument_should_throw_exception_with_false() {
		final String message = "should be thrown";
		assertThatThrownBy(() -> PreConditions.checkArgument(false, message))
				.isExactlyInstanceOf(IllegalArgumentException.class).hasMessage(message);
	}
}
