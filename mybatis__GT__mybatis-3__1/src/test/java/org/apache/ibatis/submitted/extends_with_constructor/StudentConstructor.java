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
package org.apache.ibatis.submitted.extends_with_constructor;

import java.util.LinkedList;
import java.util.List;

public class StudentConstructor {
	public enum Constructor {
		ID, ID_NAME
	}

	private List<Constructor> constructors = new LinkedList<Constructor>();
	private final int id;
	private String name;
	private Teacher teacher;

	public StudentConstructor(Integer id) {
		constructors.add(Constructor.ID);
		this.id = id;
	}

	public StudentConstructor(Integer id, String name) {
		constructors.add(Constructor.ID_NAME);
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Teacher getTeacher() {
		return teacher;
	}

	public void setTeacher(Teacher teacher) {
		this.teacher = teacher;
	}

	public List<Constructor> getConstructors() {
		return constructors;
	}
}
