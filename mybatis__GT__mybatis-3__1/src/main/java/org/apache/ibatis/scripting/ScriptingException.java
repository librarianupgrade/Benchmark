/*
 *    Copyright 2009-2012 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.scripting;

import org.apache.ibatis.exceptions.PersistenceException;

/**
 * @author Frank D. Martinez [mnesarco]
 */
public class ScriptingException extends PersistenceException {

	private static final long serialVersionUID = 7642570221267566591L;

	public ScriptingException() {
		super();
	}

	public ScriptingException(String message) {
		super(message);
	}

	public ScriptingException(String message, Throwable cause) {
		super(message, cause);
	}

	public ScriptingException(Throwable cause) {
		super(cause);
	}

}
