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
package com.opensymphony.xwork2.conversion.impl;

import com.opensymphony.xwork2.conversion.ObjectTypeDeterminer;
import com.opensymphony.xwork2.conversion.TypeConverter;
import com.opensymphony.xwork2.inject.Inject;

import java.lang.reflect.Member;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class CollectionConverter extends DefaultTypeConverter {

	private ObjectTypeDeterminer objectTypeDeterminer;

	@Inject
	public void setObjectTypeDeterminer(ObjectTypeDeterminer determiner) {
		this.objectTypeDeterminer = determiner;
	}

	public Object convertValue(Map<String, Object> context, Object target, Member member, String propertyName,
			Object value, Class toType) {
		Collection result;
		Class memberType = String.class;

		if (target != null) {
			memberType = objectTypeDeterminer.getElementClass(target.getClass(), propertyName, null);

			if (memberType == null) {
				memberType = String.class;
			}
		}

		if (toType.isAssignableFrom(value.getClass())) {
			// no need to do anything
			result = (Collection) value;
		} else if (value.getClass().isArray()) {
			Object[] objArray = (Object[]) value;
			TypeConverter converter = getTypeConverter(context);
			result = createCollection(toType, memberType, objArray.length);

			for (Object anObjArray : objArray) {
				Object convertedValue = converter.convertValue(context, target, member, propertyName, anObjArray,
						memberType);
				if (!TypeConverter.NO_CONVERSION_POSSIBLE.equals(convertedValue)) {
					result.add(convertedValue);
				}
			}
		} else if (Collection.class.isAssignableFrom(value.getClass())) {
			Collection col = (Collection) value;
			TypeConverter converter = getTypeConverter(context);
			result = createCollection(toType, memberType, col.size());

			for (Object aCol : col) {
				Object convertedValue = converter.convertValue(context, target, member, propertyName, aCol, memberType);
				if (!TypeConverter.NO_CONVERSION_POSSIBLE.equals(convertedValue)) {
					result.add(convertedValue);
				}
			}
		} else {
			result = createCollection(toType, memberType, -1);
			TypeConverter converter = getTypeConverter(context);
			Object convertedValue = converter.convertValue(context, target, member, propertyName, value, memberType);
			if (!TypeConverter.NO_CONVERSION_POSSIBLE.equals(convertedValue)) {
				result.add(convertedValue);
			}
		}

		return result;
	}

	private Collection createCollection(Class toType, Class memberType, int size) {
		Collection result;

		if (toType == Set.class) {
			if (size > 0) {
				result = new HashSet(size);
			} else {
				result = new HashSet();
			}
		} else if (toType == SortedSet.class) {
			result = new TreeSet();
		} else {
			if (size > 0) {
				result = new XWorkList(memberType, size);
			} else {
				result = new XWorkList(memberType);
			}
		}

		return result;
	}

}
