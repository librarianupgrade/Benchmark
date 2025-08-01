/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.bookkeeper.util;

import com.sun.jna.LastErrorException;
import com.sun.jna.Native;
import java.io.FileDescriptor;
import java.lang.reflect.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Native I/O operations.
 */
public final class NativeIO {
	private static final Logger LOG = LoggerFactory.getLogger(NativeIO.class);

	private static final int POSIX_FADV_DONTNEED = 4; /* fadvise.h */

	private static boolean initialized = false;
	private static boolean fadvisePossible = true;

	static {
		try {
			Native.register("c");
			initialized = true;
		} catch (NoClassDefFoundError e) {
			LOG.info("JNA not found. Native methods will be disabled.");
		} catch (UnsatisfiedLinkError e) {
			LOG.info("Unable to link C library. Native methods will be disabled.");
		} catch (NoSuchMethodError e) {
			LOG.warn("Obsolete version of JNA present; unable to register C library");
		}
	}

	// fadvice
	public static native int posix_fadvise(int fd, long offset, long len, int flag) throws LastErrorException;

	private NativeIO() {
	}

	private static Field getFieldByReflection(Class cls, String fieldName) {
		Field field = null;

		try {
			field = cls.getDeclaredField(fieldName);
			field.setAccessible(true);
		} catch (Exception e) {
			// We don't really expect this so throw an assertion to
			// catch this during development
			LOG.warn("Unable to read {} field from {}", fieldName, cls.getName());
			assert false;
		}

		return field;
	}

	/**
	 * Get system file descriptor (int) from FileDescriptor object.
	 * @param descriptor - FileDescriptor object to get fd from
	 * @return file descriptor, -1 or error
	 */
	public static int getSysFileDescriptor(FileDescriptor descriptor) {
		Field field = getFieldByReflection(descriptor.getClass(), "fd");
		try {
			return field.getInt(descriptor);
		} catch (Exception e) {
			LOG.warn("Unable to read fd field from java.io.FileDescriptor");
		}

		return -1;
	}

	/**
	 * Remove pages from the file system page cache when they wont
	 * be accessed again.
	 *
	 * @param fd     The file descriptor of the source file.
	 * @param offset The offset within the file.
	 * @param len    The length to be flushed.
	 */
	public static void bestEffortRemoveFromPageCache(int fd, long offset, long len) {
		if (!initialized || !fadvisePossible || fd < 0) {
			return;
		}
		try {
			posix_fadvise(fd, offset, len, POSIX_FADV_DONTNEED);
		} catch (UnsupportedOperationException uoe) {
			LOG.warn("posix_fadvise is not supported : ", uoe);
			fadvisePossible = false;
		} catch (UnsatisfiedLinkError ule) {
			// if JNA is unavailable just skipping Direct I/O
			// instance of this class will act like normal RandomAccessFile
			LOG.warn("Unsatisfied Link error: posix_fadvise failed on file descriptor {}, offset {} : ", fd, offset,
					ule);
			fadvisePossible = false;
		} catch (Exception e) {
			// This is best effort anyway so lets just log that there was an
			// exception and forget
			LOG.warn("Unknown exception: posix_fadvise failed on file descriptor {}, offset {} : ", fd, offset, e);
		}
	}

}
