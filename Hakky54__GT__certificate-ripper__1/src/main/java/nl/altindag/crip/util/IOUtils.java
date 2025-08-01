/*
 * Copyright 2021 Thunderberry.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.altindag.crip.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public final class IOUtils {

	private IOUtils() {

	}

	public static Path getCurrentDirectory() {
		return Paths.get(System.getProperty("user.dir"));
	}

	public static void write(Path path, String value) {
		write(path, value.getBytes(StandardCharsets.UTF_8));
	}

	public static void write(Path path, byte[] bytes) {
		try {
			Files.write(path, bytes, StandardOpenOption.CREATE);
		} catch (IOException e) {
			System.err.println("Failed to export the certificates. Error message: " + e.getMessage());
		}
	}

}
