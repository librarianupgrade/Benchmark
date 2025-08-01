/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 Anders Wisch
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package cron;

import com.google.common.collect.ForwardingSet;

import java.util.HashSet;
import java.util.Set;

public class Integers extends ForwardingSet<Integer> {
	private final Set<Integer> delegate;

	public Integers(int... integers) {
		delegate = new HashSet<>();
		with(integers);
	}

	@Override
	protected Set<Integer> delegate() {
		return delegate;
	}

	public Integers with(int... integers) {
		for (int integer : integers)
			add(integer);
		return this;
	}

	public Integers withRange(int start, int end) {
		for (int i = start; i <= end; i++)
			add(i);
		return this;
	}

	public Integers withRange(int start, int end, int mod) {
		for (int i = start; i <= end; i++)
			if ((i - start) % mod == 0)
				add(i);
		return this;
	}
}
