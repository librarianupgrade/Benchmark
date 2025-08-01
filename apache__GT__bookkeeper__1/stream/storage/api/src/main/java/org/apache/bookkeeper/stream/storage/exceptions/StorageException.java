/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.bookkeeper.stream.storage.exceptions;

/**
 * Storage related exception.
 */
public class StorageException extends Exception {

	private static final long serialVersionUID = -1490659086557818714L;

	public StorageException(String msg) {
		super(msg);
	}

	public StorageException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
