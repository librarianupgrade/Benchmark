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

import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.TypeUtils;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static com.alibaba.fastjson.util.TypeUtils.*;

public class JSONArray extends JSON implements List<Object>, Cloneable, RandomAccess, Serializable {

	private static final long serialVersionUID = 1L;
	private final List<Object> list;
	protected transient Object relatedArray;
	protected transient Type componentType;

	public JSONArray() {
		this.list = new ArrayList<Object>();
	}

	public JSONArray(List<Object> list) {
		this.list = list;
	}

	public JSONArray(int initialCapacity) {
		this.list = new ArrayList<Object>(initialCapacity);
	}

	/**
	 * @return
	 * @since 1.1.16
	 */
	public Object getRelatedArray() {
		return relatedArray;
	}

	public void setRelatedArray(Object relatedArray) {
		this.relatedArray = relatedArray;
	}

	public Type getComponentType() {
		return componentType;
	}

	public void setComponentType(Type componentType) {
		this.componentType = componentType;
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return list.contains(o);
	}

	@Override
	public Iterator<Object> iterator() {
		return list.iterator();
	}

	@Override
	public Object[] toArray() {
		return list.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return list.toArray(a);
	}

	@Override
	public boolean add(Object e) {
		return list.add(e);
	}

	public JSONArray fluentAdd(Object e) {
		list.add(e);
		return this;
	}

	@Override
	public boolean remove(Object o) {
		return list.remove(o);
	}

	public JSONArray fluentRemove(Object o) {
		list.remove(o);
		return this;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return list.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends Object> c) {
		return list.addAll(c);
	}

	public JSONArray fluentAddAll(Collection<? extends Object> c) {
		list.addAll(c);
		return this;
	}

	@Override
	public boolean addAll(int index, Collection<? extends Object> c) {
		return list.addAll(index, c);
	}

	public JSONArray fluentAddAll(int index, Collection<? extends Object> c) {
		list.addAll(index, c);
		return this;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return list.removeAll(c);
	}

	public JSONArray fluentRemoveAll(Collection<?> c) {
		list.removeAll(c);
		return this;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return list.retainAll(c);
	}

	public JSONArray fluentRetainAll(Collection<?> c) {
		list.retainAll(c);
		return this;
	}

	@Override
	public void clear() {
		list.clear();
	}

	public JSONArray fluentClear() {
		list.clear();
		return this;
	}

	@Override
	public Object set(int index, Object element) {
		if (index == -1) {
			list.add(element);
			return null;
		}

		if (list.size() <= index) {
			for (int i = list.size(); i < index; ++i) {
				list.add(null);
			}
			list.add(element);
			return null;
		}

		return list.set(index, element);
	}

	public JSONArray fluentSet(int index, Object element) {
		set(index, element);
		return this;
	}

	@Override
	public void add(int index, Object element) {
		list.add(index, element);
	}

	public JSONArray fluentAdd(int index, Object element) {
		list.add(index, element);
		return this;
	}

	@Override
	public Object remove(int index) {
		return list.remove(index);
	}

	public JSONArray fluentRemove(int index) {
		list.remove(index);
		return this;
	}

	@Override
	public int indexOf(Object o) {
		return list.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return list.lastIndexOf(o);
	}

	@Override
	public ListIterator<Object> listIterator() {
		return list.listIterator();
	}

	@Override
	public ListIterator<Object> listIterator(int index) {
		return list.listIterator(index);
	}

	@Override
	public List<Object> subList(int fromIndex, int toIndex) {
		return list.subList(fromIndex, toIndex);
	}

	@Override
	public Object get(int index) {
		return list.get(index);
	}

	public JSONObject getJSONObject(int index) {
		Object value = list.get(index);

		if (value instanceof JSONObject) {
			return (JSONObject) value;
		}

		if (value instanceof Map) {
			return new JSONObject((Map) value);
		}

		return (JSONObject) toJSON(value);
	}

	public JSONArray getJSONArray(int index) {
		Object value = list.get(index);

		if (value instanceof JSONArray) {
			return (JSONArray) value;
		}

		if (value instanceof List) {
			return new JSONArray((List) value);
		}

		return (JSONArray) toJSON(value);
	}

	public <T> T getObject(int index, Class<T> clazz) {
		Object obj = list.get(index);
		return TypeUtils.castToJavaBean(obj, clazz);
	}

	public <T> T getObject(int index, Type type) {
		Object obj = list.get(index);
		if (type instanceof Class) {
			return (T) TypeUtils.castToJavaBean(obj, (Class) type);
		} else {
			String json = JSON.toJSONString(obj);
			return (T) JSON.parseObject(json, type);
		}
	}

	public Boolean getBoolean(int index) {
		Object value = get(index);

		if (value == null) {
			return null;
		}

		return castToBoolean(value);
	}

	public boolean getBooleanValue(int index) {
		Object value = get(index);

		if (value == null) {
			return false;
		}

		return castToBoolean(value).booleanValue();
	}

	public Byte getByte(int index) {
		Object value = get(index);

		return castToByte(value);
	}

	public byte getByteValue(int index) {
		Object value = get(index);

		Byte byteVal = castToByte(value);
		if (byteVal == null) {
			return 0;
		}

		return byteVal.byteValue();
	}

	public Short getShort(int index) {
		Object value = get(index);

		return castToShort(value);
	}

	public short getShortValue(int index) {
		Object value = get(index);

		Short shortVal = castToShort(value);
		if (shortVal == null) {
			return 0;
		}

		return shortVal.shortValue();
	}

	public Integer getInteger(int index) {
		Object value = get(index);

		return castToInt(value);
	}

	public int getIntValue(int index) {
		Object value = get(index);

		Integer intVal = castToInt(value);
		if (intVal == null) {
			return 0;
		}

		return intVal.intValue();
	}

	public Long getLong(int index) {
		Object value = get(index);

		return castToLong(value);
	}

	public long getLongValue(int index) {
		Object value = get(index);

		Long longVal = castToLong(value);
		if (longVal == null) {
			return 0L;
		}

		return longVal.longValue();
	}

	public Float getFloat(int index) {
		Object value = get(index);

		return castToFloat(value);
	}

	public float getFloatValue(int index) {
		Object value = get(index);

		Float floatValue = castToFloat(value);
		if (floatValue == null) {
			return 0F;
		}

		return floatValue.floatValue();
	}

	public Double getDouble(int index) {
		Object value = get(index);

		return castToDouble(value);
	}

	public double getDoubleValue(int index) {
		Object value = get(index);

		Double doubleValue = castToDouble(value);
		if (doubleValue == null) {
			return 0D;
		}

		return doubleValue.doubleValue();
	}

	public BigDecimal getBigDecimal(int index) {
		Object value = get(index);

		return castToBigDecimal(value);
	}

	public BigInteger getBigInteger(int index) {
		Object value = get(index);

		return castToBigInteger(value);
	}

	public String getString(int index) {
		Object value = get(index);

		return castToString(value);
	}

	public java.util.Date getDate(int index) {
		Object value = get(index);

		return castToDate(value);
	}

	public java.sql.Date getSqlDate(int index) {
		Object value = get(index);

		return castToSqlDate(value);
	}

	public java.sql.Timestamp getTimestamp(int index) {
		Object value = get(index);

		return castToTimestamp(value);
	}

	/**
	 * @since 1.2.23
	 */
	public <T> List<T> toJavaList(Class<T> clazz) {
		List<T> list = new ArrayList<T>(this.size());

		ParserConfig config = ParserConfig.getGlobalInstance();

		for (Object item : this) {
			T classItem = (T) TypeUtils.cast(item, clazz, config);
			list.add(classItem);
		}

		return list;
	}

	@Override
	public Object clone() {
		return new JSONArray(new ArrayList<Object>(list));
	}

	@Override
	public boolean equals(Object obj) {
		return this.list.equals(obj);
	}

	@Override
	public int hashCode() {
		return this.list.hashCode();
	}

}
