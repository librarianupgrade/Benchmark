/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the LGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at  http://www.gnu.org/licenses/lgpl-3.0.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.util.FieldInfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * @since 1.2.9
 */
public final class BeanContext {

	private final Class<?> beanClass;
	private final FieldInfo fieldInfo;
	private final String format;

	public BeanContext(Class<?> beanClass, FieldInfo fieldInfo) {
		this.beanClass = beanClass;
		this.fieldInfo = fieldInfo;
		this.format = fieldInfo.getFormat();
	}

	public Class<?> getBeanClass() {
		return beanClass;
	}

	public Method getMethod() {
		return fieldInfo.method;
	}

	public Field getField() {
		return fieldInfo.field;
	}

	public String getName() {
		return fieldInfo.name;
	}

	public String getLabel() {
		return fieldInfo.label;
	}

	public Class<?> getFieldClass() {
		return fieldInfo.fieldClass;
	}

	public Type getFieldType() {
		return fieldInfo.fieldType;
	}

	public int getFeatures() {
		return fieldInfo.serialzeFeatures;
	}

	public boolean isJsonDirect() {
		return this.fieldInfo.jsonDirect;
	}

	public <T extends Annotation> T getAnnation(Class<T> annotationClass) {
		return fieldInfo.getAnnation(annotationClass);
	}

	public String getFormat() {
		return format;
	}
}
