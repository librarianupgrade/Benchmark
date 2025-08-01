/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.struts2;

import com.opensymphony.xwork2.util.location.Locatable;
import com.opensymphony.xwork2.util.location.Location;
import com.opensymphony.xwork2.util.location.LocationUtils;

/**
 * A generic runtime exception that optionally contains Location information
 */
public class StrutsException extends RuntimeException implements Locatable {

	private Location location;

	/**
	 * Constructs a <code>StrutsException</code> with no detail message.
	 */
	public StrutsException() {
	}

	/**
	 * Constructs a <code>StrutsException</code> with the specified
	 * detail message.
	 *
	 * @param s the detail message.
	 */
	public StrutsException(String s) {
		this(s, null, null);
	}

	/**
	 * Constructs a <code>StrutsException</code> with the specified
	 * detail message and target.
	 *
	 * @param s the detail message.
	 * @param target the target of the exception.
	 */
	public StrutsException(String s, Object target) {
		this(s, (Throwable) null, target);
	}

	/**
	 * Constructs a <code>StrutsException</code> with the root cause
	 *
	 * @param cause The wrapped exception
	 */
	public StrutsException(Throwable cause) {
		this(null, cause, null);
	}

	/**
	 * Constructs a <code>StrutsException</code> with the root cause and target
	 *
	 * @param cause The wrapped exception
	 * @param target The target of the exception
	 */
	public StrutsException(Throwable cause, Object target) {
		this(null, cause, target);
	}

	/**
	 * Constructs a <code>StrutsException</code> with the specified
	 * detail message and exception cause.
	 *
	 * @param s the detail message.
	 * @param cause the wrapped exception
	 */
	public StrutsException(String s, Throwable cause) {
		this(s, cause, null);
	}

	/**
	* Constructs a <code>StrutsException</code> with the specified
	* detail message, cause, and target
	*
	* @param s the detail message.
	* @param cause The wrapped exception
	* @param target The target of the exception
	*/
	public StrutsException(String s, Throwable cause, Object target) {
		super(s, cause);

		this.location = LocationUtils.getLocation(target);
		if (this.location == Location.UNKNOWN) {
			this.location = LocationUtils.getLocation(cause);
		}
	}

	/**
	 * Gets the location of the error, if available
	 *
	 * @return the location, <tt>null</tt> if not available
	 */
	public Location getLocation() {
		return this.location;
	}

	/**
	 * Returns a short description of this throwable object, including the
	 * location. If no detailed message is available, it will use the message
	 * of the underlying exception if available.
	 *
	 * @return a string representation of this <code>Throwable</code>.
	 */
	@Override
	public String toString() {
		String msg = getMessage();
		if (msg == null && getCause() != null) {
			msg = getCause().getMessage();
		}

		if (location != null) {
			if (msg != null) {
				return msg + " - " + location.toString();
			} else {
				return location.toString();
			}
		} else {
			return msg;
		}
	}
}