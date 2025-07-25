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
public abstract class IntBuffer extends Buffer implements Comparable<IntBuffer> {
	IntBuffer(int capacity, int position, int limit) {
		super(capacity);
		this.position = position;
		this.limit = limit;
	}

	/*public static IntBuffer allocate(int capacity) {
	    if (capacity < 0) {
	        throw new IllegalArgumentException("Capacity is negative: " + capacity);
	    }
	    return new IntBufferOverArray(capacity);
	}*/

	/* public static IntBuffer wrap(int[] array, int offset, int length) {
	    return new IntBufferOverArray(0, array.length, array, offset, offset + length, false);
	}*/

	/*public static IntBuffer wrap(int[] array) {
	    return wrap(array, 0, array.length);
	}*/

	public abstract IntBuffer slice();

	public abstract IntBuffer duplicate();

	public abstract IntBuffer asReadOnlyBuffer();

	public abstract int get();

	public abstract IntBuffer put(int b);

	public abstract int get(int index);

	public abstract IntBuffer put(int index, int b);

	abstract int getElement(int index);

	abstract void putElement(int index, int value);

	public IntBuffer get(int[] dst, int offset, int length) {
		if (offset < 0 || offset >= dst.length) {
			throw new IndexOutOfBoundsException("Offset " + offset + " is outside of range [0;" + dst.length + ")");
		}
		if (offset + length > dst.length) {
			throw new IndexOutOfBoundsException(
					"The last int in dst " + (offset + length) + " is outside " + "of array of size " + dst.length);
		}
		if (remaining() < length) {
			throw new emul.java.nio.BufferUnderflowException();
		}
		if (length < 0) {
			throw new IndexOutOfBoundsException("Length " + length + " must be non-negative");
		}
		int pos = position;
		for (int i = 0; i < length; ++i) {
			dst[offset++] = getElement(pos++);
		}
		position += length;
		return this;
	}

	public IntBuffer get(int[] dst) {
		return get(dst, 0, dst.length);
	}

	public IntBuffer put(IntBuffer src) {
		if (isReadOnly()) {
			throw new emul.java.nio.ReadOnlyBufferException();
		}
		if (remaining() < src.remaining()) {
			throw new emul.java.nio.BufferOverflowException();
		}
		int length = src.remaining();
		int pos = position;
		int offset = src.position;
		for (int i = 0; i < length; ++i) {
			putElement(pos++, src.getElement(offset++));
		}
		position += length;
		return this;
	}

	public IntBuffer put(int[] src, int offset, int length) {
		if (isReadOnly()) {
			throw new emul.java.nio.ReadOnlyBufferException();
		}
		if (remaining() < length) {
			throw new emul.java.nio.BufferOverflowException();
		}
		if (offset < 0 || offset >= src.length) {
			throw new IndexOutOfBoundsException("Offset " + offset + " is outside of range [0;" + src.length + ")");
		}
		if (offset + length > src.length) {
			throw new IndexOutOfBoundsException(
					"The last int in src " + (offset + length) + " is outside " + "of array of size " + src.length);
		}
		if (length < 0) {
			throw new IndexOutOfBoundsException("Length " + length + " must be non-negative");
		}
		int pos = position;
		for (int i = 0; i < length; ++i) {
			putElement(pos++, src[offset++]);
		}
		position += length;
		return this;
	}

	public final IntBuffer put(int[] src) {
		return put(src, 0, src.length);
	}

	@Override
	public final boolean hasArray() {
		return isArrayPresent();
	}

	@Override
	public final int[] array() {
		return getArray();
	}

	@Override
	public final int arrayOffset() {
		return getArrayOffset();
	}

	abstract boolean isArrayPresent();

	abstract int[] getArray();

	abstract int getArrayOffset();

	public abstract IntBuffer compact();

	@Override
	public abstract boolean isDirect();

	@Override
	public String toString() {
		return "[InBuffer position=" + position + ", limit=" + limit + ", capacity=" + capacity + ", mark "
				+ (mark >= 0 ? " at " + mark : " is not set") + "]";
	}

	@Override
	public int hashCode() {
		int hashCode = 0;
		int pos = position;
		for (int i = position; i < limit; ++i) {
			hashCode = 31 * hashCode + getElement(pos++);
		}
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof IntBuffer)) {
			return false;
		}
		IntBuffer other = (IntBuffer) obj;
		int sz = remaining();
		if (sz != other.remaining()) {
			return false;
		}
		int a = position;
		int b = other.position;
		for (int i = 0; i < sz; ++i) {
			if (getElement(a++) != other.getElement(b++)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int compareTo(IntBuffer other) {
		if (this == other) {
			return 0;
		}
		int sz = Math.min(remaining(), other.remaining());
		int a = position;
		int b = other.position;
		for (int i = 0; i < sz; ++i) {
			int x = getElement(a++);
			int y = other.getElement(b++);
			int r = (x < y) ? -1 : ((x == y) ? 0 : 1);
			if (r != 0) {
				return r;
			}
		}
		int x = remaining();
		int y = other.remaining();
		return (x < y) ? -1 : ((x == y) ? 0 : 1);
	}

	public abstract emul.java.nio.ByteOrder order();
}
