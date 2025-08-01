/*
 * {{{ header & license
 * XRRuntimeException.java
 * Copyright (c) 2004, 2005 Patrick Wright
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package com.openhtmltopdf.util;

/**
 * General runtime exception used in XHTMLRenderer. Auto-logs messages to
 * plumbing.exception hierarchy.
 *
 * @author   Patrick Wright
 */
public class XRRuntimeException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new Exception with a "reason" message.
	 *
	 * @param msg  Reason the exception is being thrown.
	 */
	public XRRuntimeException(String msg) {
		super(msg);
		log(msg);
	}

	/**
	 * Instantiates a new Exception with a "reason" message.
	 *
	 * @param msg    Reason the exception is being thrown.
	 * @param cause  Throwable that caused this exception to be thrown (e.g.
	 *      IOException.
	 */
	public XRRuntimeException(String msg, Throwable cause) {
		super(msg, cause);
		log(msg, cause);
	}

	/**
	 * Logs the exception message.
	 *
	 * @param msg  Message for the log.
	 */
	private void log(String msg) {
		XRLog.exception("Unhandled exception. " + msg);
	}

	/**
	 * Logs the exception's message, plus the Throwable that caused the
	 * exception to be thrown.
	 *
	 * @param msg    Message for the log.
	 * @param cause  Throwable that caused this exception to be thrown (e.g.
	 *      IOException.
	 */
	private void log(String msg, Throwable cause) {
		XRLog.exception("Unhandled exception. " + msg, cause);
	}
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.5  2007/05/20 23:25:31  peterbrant
 * Various code cleanups (e.g. remove unused imports)
 *
 * Patch from Sean Bright
 *
 * Revision 1.4  2005/01/29 20:18:38  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.3  2004/10/23 14:06:57  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 * Revision 1.2  2004/10/14 12:54:54  pdoubleya
 * Use XRLog for logging.
 *
 * Revision 1.1  2004/10/13 23:00:33  pdoubleya
 * Added to CVS.
 *
 */
