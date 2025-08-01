/*
 * Copyright (C) 2006 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.opensymphony.xwork2.inject;

import java.io.Serializable;

/**
 * Creates objects which will be injected.
 *
 * @author crazybob@google.com (Bob Lee)
 */
interface InternalFactory<T> extends Serializable {

	/**
	 * Creates an object to be injected.
	 *
	 * @param context of this injection
	 * @return instance to be injected
	 */
	T create(InternalContext context);

	/**
	 * Returns a class of <T>
	 *
	 * @return class of the object
	 */
	Class<? extends T> type();

}
