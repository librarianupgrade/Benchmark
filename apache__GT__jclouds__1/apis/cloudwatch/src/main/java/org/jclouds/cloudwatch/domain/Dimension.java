/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jclouds.cloudwatch.domain;

import com.google.common.base.Objects;

/**
 * @see <a href="http://docs.amazonwebservices.com/AmazonCloudWatch/latest/APIReference/API_Dimension.html" />
 */
public class Dimension {

	private final String name;
	private final String value;

	public Dimension(String name, String value) {
		this.name = name;
		this.value = value;
	}

	/**
	* return the dimension name.
	*/
	public String getName() {
		return name;
	}

	/**
	* return the dimension value.
	*/
	public String getValue() {
		return value;
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public int hashCode() {
		return Objects.hashCode(name, value);
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Dimension other = (Dimension) obj;
		return Objects.equal(this.name, other.name) && Objects.equal(this.value, other.value);
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("name", name).add("value", value).toString();
	}

}
