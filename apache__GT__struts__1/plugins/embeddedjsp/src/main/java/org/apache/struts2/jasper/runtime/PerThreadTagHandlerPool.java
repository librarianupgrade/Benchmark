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
package org.apache.struts2.jasper.runtime;

import java.util.Enumeration;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;

import org.apache.struts2.jasper.Constants;

/**
 * Thread-local based pool of tag handlers that can be reused.
 *
 * @author Jan Luehe
 * @author Costin Manolache
 */
public class PerThreadTagHandlerPool extends TagHandlerPool {

	private int maxSize;

	// For cleanup
	private Vector perThreadDataVector;

	private ThreadLocal perThread;

	private static class PerThreadData {
		Tag handlers[];
		int current;
	}

	/**
	 * Constructs a tag handler pool with the default capacity.
	 */
	public PerThreadTagHandlerPool() {
		super();
		perThreadDataVector = new Vector();
	}

	protected void init(ServletConfig config) {
		maxSize = Constants.MAX_POOL_SIZE;
		String maxSizeS = getOption(config, OPTION_MAXSIZE, null);
		if (maxSizeS != null) {
			maxSize = Integer.parseInt(maxSizeS);
			if (maxSize < 0) {
				maxSize = Constants.MAX_POOL_SIZE;
			}
		}

		perThread = new ThreadLocal() {
			protected Object initialValue() {
				PerThreadData ptd = new PerThreadData();
				ptd.handlers = new Tag[maxSize];
				ptd.current = -1;
				perThreadDataVector.addElement(ptd);
				return ptd;
			}
		};
	}

	/**
	 * Gets the next available tag handler from this tag handler pool,
	 * instantiating one if this tag handler pool is empty.
	 *
	 * @param handlerClass Tag handler class
	 *
	 * @return Reused or newly instantiated tag handler
	 *
	 * @throws JspException if a tag handler cannot be instantiated
	 */
	public Tag get(Class handlerClass) throws JspException {
		PerThreadData ptd = (PerThreadData) perThread.get();
		if (ptd.current >= 0) {
			return ptd.handlers[ptd.current--];
		} else {
			try {
				return (Tag) handlerClass.newInstance();
			} catch (Exception e) {
				throw new JspException(e.getMessage(), e);
			}
		}
	}

	/**
	 * Adds the given tag handler to this tag handler pool, unless this tag
	 * handler pool has already reached its capacity, in which case the tag
	 * handler's release() method is called.
	 *
	 * @param handler Tag handler to add to this tag handler pool
	 */
	public void reuse(Tag handler) {
		PerThreadData ptd = (PerThreadData) perThread.get();
		if (ptd.current < (ptd.handlers.length - 1)) {
			ptd.handlers[++ptd.current] = handler;
		} else {
			handler.release();
		}
	}

	/**
	 * Calls the release() method of all tag handlers in this tag handler pool.
	 */
	public void release() {
		Enumeration enumeration = perThreadDataVector.elements();
		while (enumeration.hasMoreElements()) {
			PerThreadData ptd = (PerThreadData) enumeration.nextElement();
			if (ptd.handlers != null) {
				for (int i = ptd.current; i >= 0; i--) {
					if (ptd.handlers[i] != null) {
						ptd.handlers[i].release();
					}
				}
			}
		}
	}
}
