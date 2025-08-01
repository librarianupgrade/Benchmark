/*
 *  Copyright 2019 the original author or authors.
 *
 *  Licensed under the LGPL, Version 3.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at  http://www.gnu.org/licenses/lgpl-3.0.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.alibaba.fastjson;

import com.alibaba.fastjson.util.ParameterizedTypeImpl;
import com.jn.langx.util.reflect.type.Types;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Represents a generic type {@code T}. Java doesn't yet provide a way to
 * represent generic types, so this class does. Forces clients to create a
 * subclass of this class which enables retrieval the type information even at
 * runtime.
 * <p>
 * <p>For example, to create a type literal for {@code List<String>}, you can
 * create an empty anonymous inner class:
 * <p>
 * <pre>
 * TypeReference&lt;List&lt;String&gt;&gt; list = new TypeReference&lt;List&lt;String&gt;&gt;() {};
 * </pre>
 * This syntax cannot be used to create type literals that have wildcard
 * parameters, such as {@code Class<?>} or {@code List<? extends CharSequence>}.
 */
public class TypeReference<T> {
	static ConcurrentMap<Type, Type> classTypeCache = new ConcurrentHashMap<Type, Type>(16, 0.75f, 1);

	protected final Type type;

	/**
	 * Constructs a new type literal. Derives represented class from type
	 * parameter.
	 * <p>
	 * <p>Clients create an empty anonymous subclass. Doing so embeds the type
	 * parameter in the anonymous class's type hierarchy so we can reconstitute it
	 * at runtime despite erasure.
	 */
	protected TypeReference() {
		Type superClass = getClass().getGenericSuperclass();

		Type type = ((ParameterizedType) superClass).getActualTypeArguments()[0];

		Type cachedType = classTypeCache.get(type);
		if (cachedType == null) {
			classTypeCache.putIfAbsent(type, type);
			cachedType = classTypeCache.get(type);
		}

		this.type = cachedType;
	}

	/**
	 * @param actualTypeArguments
	 * @since 1.2.9
	 */
	protected TypeReference(Type... actualTypeArguments) {
		Class<?> thisClass = this.getClass();
		Type superClass = thisClass.getGenericSuperclass();

		ParameterizedType argType = (ParameterizedType) ((ParameterizedType) superClass).getActualTypeArguments()[0];
		Type rawType = argType.getRawType();
		Type[] argTypes = argType.getActualTypeArguments();

		int actualIndex = 0;
		for (int i = 0; i < argTypes.length; ++i) {
			if (argTypes[i] instanceof TypeVariable && actualIndex < actualTypeArguments.length) {
				argTypes[i] = actualTypeArguments[actualIndex++];
			}
			// fix for openjdk and android env
			if (argTypes[i] instanceof GenericArrayType) {
				argTypes[i] = Types.checkPrimitiveArray((GenericArrayType) argTypes[i]);
			}

			// 如果有多层泛型且该泛型已经注明实现的情况下，判断该泛型下一层是否还有泛型
			if (argTypes[i] instanceof ParameterizedType) {
				argTypes[i] = handlerParameterizedType((ParameterizedType) argTypes[i], actualTypeArguments,
						actualIndex);
			}
		}

		Type key = new ParameterizedTypeImpl(argTypes, thisClass, rawType);
		Type cachedType = classTypeCache.get(key);
		if (cachedType == null) {
			classTypeCache.putIfAbsent(key, key);
			cachedType = classTypeCache.get(key);
		}

		type = cachedType;

	}

	private Type handlerParameterizedType(ParameterizedType type, Type[] actualTypeArguments, int actualIndex) {
		Class<?> thisClass = this.getClass();
		Type rawType = type.getRawType();
		Type[] argTypes = type.getActualTypeArguments();

		for (int i = 0; i < argTypes.length; ++i) {
			if (argTypes[i] instanceof TypeVariable && actualIndex < actualTypeArguments.length) {
				argTypes[i] = actualTypeArguments[actualIndex++];
			}

			// fix for openjdk and android env
			if (argTypes[i] instanceof GenericArrayType) {
				argTypes[i] = Types.checkPrimitiveArray((GenericArrayType) argTypes[i]);
			}

			// 如果有多层泛型且该泛型已经注明实现的情况下，判断该泛型下一层是否还有泛型
			if (argTypes[i] instanceof ParameterizedType) {
				return handlerParameterizedType((ParameterizedType) argTypes[i], actualTypeArguments, actualIndex);
			}
		}

		Type key = new ParameterizedTypeImpl(argTypes, thisClass, rawType);
		return key;
	}

	/**
	 * Gets underlying {@code Type} instance.
	 */
	public Type getType() {
		return type;
	}

	public final static Type LIST_STRING = new TypeReference<List<String>>() {
	}.getType();
}
