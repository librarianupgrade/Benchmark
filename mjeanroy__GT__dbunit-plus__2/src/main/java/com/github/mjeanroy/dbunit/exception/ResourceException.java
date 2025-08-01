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

package com.github.mjeanroy.dbunit.exception;

/**
 * Error thrown when an error occurred when dealing with resources.
 */
@SuppressWarnings("serial")
public class ResourceException extends AbstractDbUnitException {

	/**
	 * Resource path.
	 */
	private final String path;

	/**
	 * Create exception with error message.
	 *
	 * @param path The path of the resource that cannot be loaded.
	 * @param message Error message.
	 */
	ResourceException(String path, String message) {
		super(message);
		this.path = path;
	}

	/**
	 * Create exception with original cause.
	 *
	 * @param path The path of the resource that cannot be loaded.
	 * @param cause The original cause.
	 */
	public ResourceException(String path, Exception cause) {
		super(cause);
		this.path = path;
	}

	/**
	 * The resource path causing exception.
	 *
	 * @return Resource path.
	 */
	public String getPath() {
		return path;
	}
}
