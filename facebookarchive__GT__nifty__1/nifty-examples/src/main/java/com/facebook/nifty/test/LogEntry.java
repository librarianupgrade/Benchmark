/*
 * Copyright (C) 2012-2013 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Autogenerated by Thrift Compiler (0.9.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package com.facebook.nifty.test;

import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;

import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.EncodingUtils;
import org.apache.thrift.TException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class LogEntry implements org.apache.thrift.TBase<LogEntry, LogEntry._Fields>, java.io.Serializable, Cloneable {
	private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct(
			"LogEntry");

	private static final org.apache.thrift.protocol.TField CATEGORY_FIELD_DESC = new org.apache.thrift.protocol.TField(
			"category", org.apache.thrift.protocol.TType.STRING, (short) 1);
	private static final org.apache.thrift.protocol.TField MESSAGE_FIELD_DESC = new org.apache.thrift.protocol.TField(
			"message", org.apache.thrift.protocol.TType.STRING, (short) 2);

	private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
	static {
		schemes.put(StandardScheme.class, new LogEntryStandardSchemeFactory());
		schemes.put(TupleScheme.class, new LogEntryTupleSchemeFactory());
	}

	public String category; // required
	public String message; // required

	/** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
	public enum _Fields implements org.apache.thrift.TFieldIdEnum {
		CATEGORY((short) 1, "category"), MESSAGE((short) 2, "message");

		private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

		static {
			for (_Fields field : EnumSet.allOf(_Fields.class)) {
				byName.put(field.getFieldName(), field);
			}
		}

		/**
		 * Find the _Fields constant that matches fieldId, or null if its not found.
		 */
		public static _Fields findByThriftId(int fieldId) {
			switch (fieldId) {
			case 1: // CATEGORY
				return CATEGORY;
			case 2: // MESSAGE
				return MESSAGE;
			default:
				return null;
			}
		}

		/**
		 * Find the _Fields constant that matches fieldId, throwing an exception
		 * if it is not found.
		 */
		public static _Fields findByThriftIdOrThrow(int fieldId) {
			_Fields fields = findByThriftId(fieldId);
			if (fields == null)
				throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
			return fields;
		}

		/**
		 * Find the _Fields constant that matches name, or null if its not found.
		 */
		public static _Fields findByName(String name) {
			return byName.get(name);
		}

		private final short _thriftId;
		private final String _fieldName;

		_Fields(short thriftId, String fieldName) {
			_thriftId = thriftId;
			_fieldName = fieldName;
		}

		public short getThriftFieldId() {
			return _thriftId;
		}

		public String getFieldName() {
			return _fieldName;
		}
	}

	// isset id assignments
	public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
	static {
		Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(
				_Fields.class);
		tmpMap.put(_Fields.CATEGORY,
				new org.apache.thrift.meta_data.FieldMetaData("category",
						org.apache.thrift.TFieldRequirementType.DEFAULT,
						new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
		tmpMap.put(_Fields.MESSAGE,
				new org.apache.thrift.meta_data.FieldMetaData("message",
						org.apache.thrift.TFieldRequirementType.DEFAULT,
						new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
		metaDataMap = Collections.unmodifiableMap(tmpMap);
		org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(LogEntry.class, metaDataMap);
	}

	public LogEntry() {
	}

	public LogEntry(String category, String message) {
		this();
		this.category = category;
		this.message = message;
	}

	/**
	 * Performs a deep copy on <i>other</i>.
	 */
	public LogEntry(LogEntry other) {
		if (other.isSetCategory()) {
			this.category = other.category;
		}
		if (other.isSetMessage()) {
			this.message = other.message;
		}
	}

	public LogEntry deepCopy() {
		return new LogEntry(this);
	}

	@Override
	public void clear() {
		this.category = null;
		this.message = null;
	}

	public String getCategory() {
		return this.category;
	}

	public LogEntry setCategory(String category) {
		this.category = category;
		return this;
	}

	public void unsetCategory() {
		this.category = null;
	}

	/** Returns true if field category is set (has been assigned a value) and false otherwise */
	public boolean isSetCategory() {
		return this.category != null;
	}

	public void setCategoryIsSet(boolean value) {
		if (!value) {
			this.category = null;
		}
	}

	public String getMessage() {
		return this.message;
	}

	public LogEntry setMessage(String message) {
		this.message = message;
		return this;
	}

	public void unsetMessage() {
		this.message = null;
	}

	/** Returns true if field message is set (has been assigned a value) and false otherwise */
	public boolean isSetMessage() {
		return this.message != null;
	}

	public void setMessageIsSet(boolean value) {
		if (!value) {
			this.message = null;
		}
	}

	public void setFieldValue(_Fields field, Object value) {
		switch (field) {
		case CATEGORY:
			if (value == null) {
				unsetCategory();
			} else {
				setCategory((String) value);
			}
			break;

		case MESSAGE:
			if (value == null) {
				unsetMessage();
			} else {
				setMessage((String) value);
			}
			break;

		}
	}

	public Object getFieldValue(_Fields field) {
		switch (field) {
		case CATEGORY:
			return getCategory();

		case MESSAGE:
			return getMessage();

		}
		throw new IllegalStateException();
	}

	/** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
	public boolean isSet(_Fields field) {
		if (field == null) {
			throw new IllegalArgumentException();
		}

		switch (field) {
		case CATEGORY:
			return isSetCategory();
		case MESSAGE:
			return isSetMessage();
		}
		throw new IllegalStateException();
	}

	@Override
	public boolean equals(Object that) {
		if (that == null)
			return false;
		if (that instanceof LogEntry)
			return this.equals((LogEntry) that);
		return false;
	}

	public boolean equals(LogEntry that) {
		if (that == null)
			return false;

		boolean this_present_category = true && this.isSetCategory();
		boolean that_present_category = true && that.isSetCategory();
		if (this_present_category || that_present_category) {
			if (!(this_present_category && that_present_category))
				return false;
			if (!this.category.equals(that.category))
				return false;
		}

		boolean this_present_message = true && this.isSetMessage();
		boolean that_present_message = true && that.isSetMessage();
		if (this_present_message || that_present_message) {
			if (!(this_present_message && that_present_message))
				return false;
			if (!this.message.equals(that.message))
				return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return 0;
	}

	public int compareTo(LogEntry other) {
		if (!getClass().equals(other.getClass())) {
			return getClass().getName().compareTo(other.getClass().getName());
		}

		int lastComparison = 0;
		LogEntry typedOther = (LogEntry) other;

		lastComparison = Boolean.valueOf(isSetCategory()).compareTo(typedOther.isSetCategory());
		if (lastComparison != 0) {
			return lastComparison;
		}
		if (isSetCategory()) {
			lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.category, typedOther.category);
			if (lastComparison != 0) {
				return lastComparison;
			}
		}
		lastComparison = Boolean.valueOf(isSetMessage()).compareTo(typedOther.isSetMessage());
		if (lastComparison != 0) {
			return lastComparison;
		}
		if (isSetMessage()) {
			lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.message, typedOther.message);
			if (lastComparison != 0) {
				return lastComparison;
			}
		}
		return 0;
	}

	public _Fields fieldForId(int fieldId) {
		return _Fields.findByThriftId(fieldId);
	}

	public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
		schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
	}

	public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
		schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("LogEntry(");
		boolean first = true;

		sb.append("category:");
		if (this.category == null) {
			sb.append("null");
		} else {
			sb.append(this.category);
		}
		first = false;
		if (!first)
			sb.append(", ");
		sb.append("message:");
		if (this.message == null) {
			sb.append("null");
		} else {
			sb.append(this.message);
		}
		first = false;
		sb.append(")");
		return sb.toString();
	}

	public void validate() throws org.apache.thrift.TException {
		// check for required fields
		// check for sub-struct validity
	}

	private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
		try {
			write(new org.apache.thrift.protocol.TCompactProtocol(
					new org.apache.thrift.transport.TIOStreamTransport(out)));
		} catch (org.apache.thrift.TException te) {
			throw new java.io.IOException(te);
		}
	}

	private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
		try {
			read(new org.apache.thrift.protocol.TCompactProtocol(
					new org.apache.thrift.transport.TIOStreamTransport(in)));
		} catch (org.apache.thrift.TException te) {
			throw new java.io.IOException(te);
		}
	}

	private static class LogEntryStandardSchemeFactory implements SchemeFactory {
		public LogEntryStandardScheme getScheme() {
			return new LogEntryStandardScheme();
		}
	}

	private static class LogEntryStandardScheme extends StandardScheme<LogEntry> {

		public void read(org.apache.thrift.protocol.TProtocol iprot, LogEntry struct)
				throws org.apache.thrift.TException {
			org.apache.thrift.protocol.TField schemeField;
			iprot.readStructBegin();
			while (true) {
				schemeField = iprot.readFieldBegin();
				if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
					break;
				}
				switch (schemeField.id) {
				case 1: // CATEGORY
					if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
						struct.category = iprot.readString();
						struct.setCategoryIsSet(true);
					} else {
						org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
					}
					break;
				case 2: // MESSAGE
					if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
						struct.message = iprot.readString();
						struct.setMessageIsSet(true);
					} else {
						org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
					}
					break;
				default:
					org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
				}
				iprot.readFieldEnd();
			}
			iprot.readStructEnd();

			// check for required fields of primitive type, which can't be checked in the validate method
			struct.validate();
		}

		public void write(org.apache.thrift.protocol.TProtocol oprot, LogEntry struct)
				throws org.apache.thrift.TException {
			struct.validate();

			oprot.writeStructBegin(STRUCT_DESC);
			if (struct.category != null) {
				oprot.writeFieldBegin(CATEGORY_FIELD_DESC);
				oprot.writeString(struct.category);
				oprot.writeFieldEnd();
			}
			if (struct.message != null) {
				oprot.writeFieldBegin(MESSAGE_FIELD_DESC);
				oprot.writeString(struct.message);
				oprot.writeFieldEnd();
			}
			oprot.writeFieldStop();
			oprot.writeStructEnd();
		}

	}

	private static class LogEntryTupleSchemeFactory implements SchemeFactory {
		public LogEntryTupleScheme getScheme() {
			return new LogEntryTupleScheme();
		}
	}

	private static class LogEntryTupleScheme extends TupleScheme<LogEntry> {

		@Override
		public void write(org.apache.thrift.protocol.TProtocol prot, LogEntry struct)
				throws org.apache.thrift.TException {
			TTupleProtocol oprot = (TTupleProtocol) prot;
			BitSet optionals = new BitSet();
			if (struct.isSetCategory()) {
				optionals.set(0);
			}
			if (struct.isSetMessage()) {
				optionals.set(1);
			}
			oprot.writeBitSet(optionals, 2);
			if (struct.isSetCategory()) {
				oprot.writeString(struct.category);
			}
			if (struct.isSetMessage()) {
				oprot.writeString(struct.message);
			}
		}

		@Override
		public void read(org.apache.thrift.protocol.TProtocol prot, LogEntry struct)
				throws org.apache.thrift.TException {
			TTupleProtocol iprot = (TTupleProtocol) prot;
			BitSet incoming = iprot.readBitSet(2);
			if (incoming.get(0)) {
				struct.category = iprot.readString();
				struct.setCategoryIsSet(true);
			}
			if (incoming.get(1)) {
				struct.message = iprot.readString();
				struct.setMessageIsSet(true);
			}
		}
	}

}
