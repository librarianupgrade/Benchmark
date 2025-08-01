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
package com.opensymphony.xwork2.util;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A Stack that is implemented using a List.
 * 
 * @author plightbo
 * @version $Revision$
 */
public class CompoundRoot extends CopyOnWriteArrayList<Object> {

	private static final long serialVersionUID = 8563229069192473995L;

	public CompoundRoot() {
	}

	public CompoundRoot(List<?> list) {
		super(list);
	}

	public CompoundRoot cutStack(int index) {
		return new CompoundRoot(subList(index, size()));
	}

	public Object peek() {
		return get(0);
	}

	public Object pop() {
		return remove(0);
	}

	public void push(Object o) {
		add(0, o);
	}
}
