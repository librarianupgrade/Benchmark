/*
 * Copyright (c) 2011, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.sun.javafx.cursor;

/**
 * Represents a frame of an animated cursor (ImageCursor created from an
 * animated image), non animated cursors and standard cursors have only a single
 * CursorFrame.
 */
public abstract class CursorFrame {
	public abstract CursorType getCursorType();

	/*
	private Class<?> firstPlatformCursorClass;
	private Object firstPlatformCursor;
	
	private Map<Class<?>, Object> otherPlatformCursors;
	
	public <T> T getPlatformCursor(final Class<T> platformCursorClass) {
	    if (firstPlatformCursorClass == platformCursorClass) {
	        return (T) firstPlatformCursor;
	    }
	
	    if (otherPlatformCursors != null) {
	        return (T) otherPlatformCursors.get(platformCursorClass);
	    }
	
	    return null;
	}
	
	public <T> void setPlatforCursor(final Class<T> platformCursorClass,
	                                 final T platformCursor) {
	
	    if ((firstPlatformCursorClass == null)
	            || (firstPlatformCursorClass == platformCursorClass)) {
	        // most common case
	        firstPlatformCursorClass = platformCursorClass;
	        firstPlatformCursor = platformCursor;
	        return;
	    }
	
	    if (otherPlatformCursors == null) {
	        otherPlatformCursors = new HashMap<Class<?>, Object>();
	    }
	
	    otherPlatformCursors.put(platformCursorClass, platformCursor);
	}
	*/
}
