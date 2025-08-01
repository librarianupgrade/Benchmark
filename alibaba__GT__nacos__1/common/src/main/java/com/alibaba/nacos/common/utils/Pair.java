/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.common.utils;

/**
 * Pair.
 *
 * @author nacos
 */
public class Pair<A, B> {

	private final A first;

	private final B second;

	Pair(A first, B second) {
		this.first = first;
		this.second = second;
	}

	public static <A, B> Pair<A, B> with(A first, B second) {
		return new Pair<>(first, second);
	}

	public A getFirst() {
		return first;
	}

	public B getSecond() {
		return second;
	}
}
