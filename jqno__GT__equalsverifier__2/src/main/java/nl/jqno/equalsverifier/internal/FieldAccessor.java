/*
 * Copyright 2010-2012, 2014-2016 Jan Ouwens
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.jqno.equalsverifier.internal;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import nl.jqno.equalsverifier.internal.exceptions.ReflectionException;
import nl.jqno.equalsverifier.internal.prefabvalues.PrefabValues;
import nl.jqno.equalsverifier.internal.prefabvalues.TypeTag;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Provides reflective access to one field of an object.
 *
 * @author Jan Ouwens
 */
public class FieldAccessor {
	private final Object object;
	private final Field field;

	/**
	 * Constructor.
	 *
	 * @param object The object we want to access.
	 * @param field A field of object.
	 */
	public FieldAccessor(Object object, Field field) {
		this.object = object;
		this.field = field;
	}

	/**
	 * Getter.
	 */
	public Object getObject() {
		return object;
	}

	/**
	 * Getter.
	 */
	public Field getField() {
		return field;
	}

	/**
	 * Getter for the field's type.
	 */
	public Class<?> getFieldType() {
		return field.getType();
	}

	/**
	 * Getter for the field's name.
	 */
	public String getFieldName() {
		return field.getName();
	}

	/**
	 * Returns whether the field is of a primitive type.
	 */
	public boolean fieldIsPrimitive() {
		return getFieldType().isPrimitive();
	}

	/**
	 * Returns whether the field is marked with the final modifier.
	 */
	public boolean fieldIsFinal() {
		return Modifier.isFinal(field.getModifiers());
	}

	/**
	 * Returns whether the field is marked with the static modifier.
	 */
	public boolean fieldIsStatic() {
		return Modifier.isStatic(field.getModifiers());
	}

	/**
	 * Returns whether the field is marked with the transient modifier.
	 */
	public boolean fieldIsTransient() {
		return Modifier.isTransient(field.getModifiers());
	}

	/**
	 * Returns whether the field is an enum with a single value.
	 */
	public boolean fieldIsSingleValueEnum() {
		Class<?> type = field.getType();
		return type.isEnum() && type.getEnumConstants().length == 1;
	}

	/**
	 * Tries to get the field's value.
	 *
	 * @return The field's value.
	 * @throws ReflectionException If the operation fails.
	 */
	@SuppressFBWarnings(value = "DP_DO_INSIDE_DO_PRIVILEGED", justification = "Only called in test code, not production.")
	public Object get() {
		field.setAccessible(true);
		try {
			return field.get(object);
		} catch (IllegalAccessException e) {
			throw new ReflectionException(e);
		}
	}

	/**
	 * Tries to set the field to the specified value.
	 *
	 * Includes static fields but ignores fields that can't be modified
	 * reflectively.
	 *
	 * @param value The value that the field should get.
	 * @throws ReflectionException If the operation fails.
	 */
	public void set(Object value) {
		modify(new FieldSetter(value), true);
	}

	/**
	 * Tries to make the field null. Ignores static fields and fields that
	 * can't be modified reflectively.
	 *
	 * @throws ReflectionException If the operation fails.
	 */
	public void defaultField() {
		modify(new FieldDefaulter(), false);
	}

	/**
	 * Tries to make the field null. Includes static fields but ignores fields
	 * that can't be modified reflectively.
	 *
	 * @throws ReflectionException If the operation fails.
	 */
	public void defaultStaticField() {
		modify(new FieldDefaulter(), true);
	}

	/**
	 * Copies field's value to the corresponding field in the specified object.
	 *
	 * Ignores static fields and fields that can't be modified reflectively.
	 *
	 * @param to The object into which to copy the field.
	 * @throws ReflectionException If the operation fails.
	 */
	public void copyTo(Object to) {
		modify(new FieldCopier(to), false);
	}

	/**
	 * Changes the field's value to something else. The new value will never be
	 * null. Other than that, the precise value is undefined.
	 *
	 * Ignores static fields and fields that can't be modified reflectively.
	 *
	 * @param prefabValues If the field is of a type contained within
	 *          prefabValues, the new value will be taken from it.
	 * @throws ReflectionException If the operation fails.
	 */
	public void changeField(PrefabValues prefabValues, TypeTag enclosingType) {
		modify(new FieldChanger(prefabValues, enclosingType), false);
	}

	private void modify(FieldModifier modifier, boolean includeStatic) {
		if (!canBeModifiedReflectively()) {
			return;
		}
		if (!includeStatic && fieldIsStatic()) {
			return;
		}

		field.setAccessible(true);
		try {
			modifier.modify();
		} catch (IllegalAccessException e) {
			throw new ReflectionException(e);
		}
	}

	/**
	 * Determines whether the field can be modified using reflection.
	 *
	 * @return Whether or not the field can be modified reflectively.
	 */
	public boolean canBeModifiedReflectively() {
		if (field.isSynthetic()) {
			return false;
		}
		int modifiers = field.getModifiers();
		if (Modifier.isFinal(modifiers) && Modifier.isStatic(modifiers)) {
			return false;
		}
		return true;
	}

	private interface FieldModifier {
		void modify() throws IllegalAccessException;
	}

	private class FieldSetter implements FieldModifier {
		private final Object newValue;

		public FieldSetter(Object newValue) {
			this.newValue = newValue;
		}

		@Override
		public void modify() throws IllegalAccessException {
			field.set(object, newValue);
		}
	}

	private class FieldDefaulter implements FieldModifier {
		@Override
		public void modify() throws IllegalAccessException {
			Class<?> type = field.getType();
			if (type == boolean.class) {
				field.setBoolean(object, false);
			} else if (type == byte.class) {
				field.setByte(object, (byte) 0);
			} else if (type == char.class) {
				field.setChar(object, '\u0000');
			} else if (type == double.class) {
				field.setDouble(object, 0.0);
			} else if (type == float.class) {
				field.setFloat(object, 0.0f);
			} else if (type == int.class) {
				field.setInt(object, 0);
			} else if (type == long.class) {
				field.setLong(object, 0);
			} else if (type == short.class) {
				field.setShort(object, (short) 0);
			} else {
				field.set(object, null);
			}
		}
	}

	private class FieldCopier implements FieldModifier {
		private final Object to;

		public FieldCopier(Object to) {
			this.to = to;
		}

		@Override
		public void modify() throws IllegalAccessException {
			field.set(to, field.get(object));
		}
	}

	private class FieldChanger implements FieldModifier {
		private final PrefabValues prefabValues;
		private final TypeTag enclosingType;

		public FieldChanger(PrefabValues prefabValues, TypeTag enclosingType) {
			this.prefabValues = prefabValues;
			this.enclosingType = enclosingType;
		}

		@Override
		public void modify() throws IllegalAccessException {
			Object newValue = prefabValues.giveOther(TypeTag.of(field, enclosingType), field.get(object));
			field.set(object, newValue);
		}
	}
}
