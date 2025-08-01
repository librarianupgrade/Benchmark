/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2021 Mickael Jeanroy
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

class ExceptionsTest {

	@Test
	void launderThrowable_should_return_runtime_exception() {
		RuntimeException ex = new RuntimeException();
		try {
			Exceptions.launderThrowable(ex);
		} catch (Throwable thrown) {
			assertThat(thrown).isSameAs(ex);
		}
	}

	@Test
	void launderThrowable_should_return_errors() {
		Error ex = new Error();
		try {
			Exceptions.launderThrowable(ex);
		} catch (Throwable thrown) {
			assertThat(thrown).isSameAs(ex);
		}
	}

	@Test
	void launderThrowable_should_wrap_unchecked_exceptions() {
		Exception ex = new Exception();
		try {
			Exceptions.launderThrowable(ex);
		} catch (Throwable thrown) {
			assertThat(thrown).isNotSameAs(ex).hasMessage("Not unchecked");
			assertThat(thrown.getCause()).isSameAs(ex);
		}
	}
}
