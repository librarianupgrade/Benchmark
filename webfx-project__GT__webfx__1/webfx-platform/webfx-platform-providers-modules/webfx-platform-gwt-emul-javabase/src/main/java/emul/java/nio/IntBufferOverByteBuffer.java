/*
 *  Copyright 2014 Alexey Andreev.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package emul.java.nio;

/**
 *
 * @author Alexey Andreev
 */
abstract class IntBufferOverByteBuffer extends IntBufferImpl {
	ByteBufferImpl byteByffer;
	boolean readOnly;
	int start;

	public IntBufferOverByteBuffer(int start, int capacity, ByteBufferImpl byteBuffer, int position, int limit,
			boolean readOnly) {
		super(capacity, position, limit);
		this.start = start;
		this.byteByffer = byteBuffer;
		this.readOnly = readOnly;
	}

	@Override
	boolean isArrayPresent() {
		return false;
	}

	@Override
	int[] getArray() {
		throw new UnsupportedOperationException();
	}

	@Override
	int getArrayOffset() {
		throw new UnsupportedOperationException();
	}

	@Override
	boolean readOnly() {
		return readOnly;
	}
}
