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
package org.jclouds.ec2.xml;

import org.jclouds.http.functions.ParseSax;

/**
 * 
 * @see <a href="http://docs.amazonwebservices.com/AWSEC2/latest/APIReference/index.html?ApiReference-query-DescribeInstanceAttribute.html"
 *      />
 */
public class BooleanValueHandler extends ParseSax.HandlerWithResult<Boolean> {

	private StringBuilder currentText = new StringBuilder();
	private boolean value;

	public Boolean getResult() {
		return value;
	}

	public void endElement(String uri, String name, String qName) {
		if (qName.equalsIgnoreCase("value")) {
			this.value = Boolean.parseBoolean(currentText.toString().trim());
		}
		currentText.setLength(0);
	}

	public void characters(char ch[], int start, int length) {
		currentText.append(ch, start, length);
	}
}
