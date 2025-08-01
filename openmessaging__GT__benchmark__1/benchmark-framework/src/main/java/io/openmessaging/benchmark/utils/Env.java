/*
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
package io.openmessaging.benchmark.utils;

import java.util.Optional;
import java.util.function.Function;

public final class Env {
	private Env() {
	}

	public static long getLong(String key, long defaultValue) {
		return get(key, Long::parseLong, defaultValue);
	}

	public static double getDouble(String key, double defaultValue) {
		return get(key, Double::parseDouble, defaultValue);
	}

	public static <T> T get(String key, Function<String, T> function, T defaultValue) {
		return Optional.ofNullable(System.getenv(key)).map(function).orElse(defaultValue);
	}
}
