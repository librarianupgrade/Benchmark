/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.api.grpc.auto;

/**
 * Protobuf type {@code Metadata}
 */
public final class Metadata extends com.google.protobuf.GeneratedMessageV3 implements
		// @@protoc_insertion_point(message_implements:Metadata)
		MetadataOrBuilder {
	private static final long serialVersionUID = 0L;

	// Use Metadata.newBuilder() to construct.
	private Metadata(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
		super(builder);
	}

	private Metadata() {
		type_ = "";
		clientIp_ = "";
	}

	@Override
	@SuppressWarnings({ "unused" })
	protected Object newInstance(UnusedPrivateParameter unused) {
		return new Metadata();
	}

	@Override
	public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
		return this.unknownFields;
	}

	private Metadata(com.google.protobuf.CodedInputStream input,
			com.google.protobuf.ExtensionRegistryLite extensionRegistry)
			throws com.google.protobuf.InvalidProtocolBufferException {
		this();
		if (extensionRegistry == null) {
			throw new NullPointerException();
		}
		int mutable_bitField0_ = 0;
		com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder();
		try {
			boolean done = false;
			while (!done) {
				int tag = input.readTag();
				switch (tag) {
				case 0:
					done = true;
					break;
				case 26: {
					String s = input.readStringRequireUtf8();

					type_ = s;
					break;
				}
				case 58: {
					if (!((mutable_bitField0_ & 0x00000001) != 0)) {
						headers_ = com.google.protobuf.MapField.newMapField(HeadersDefaultEntryHolder.defaultEntry);
						mutable_bitField0_ |= 0x00000001;
					}
					com.google.protobuf.MapEntry<String, String> headers__ = input
							.readMessage(HeadersDefaultEntryHolder.defaultEntry.getParserForType(), extensionRegistry);
					headers_.getMutableMap().put(headers__.getKey(), headers__.getValue());
					break;
				}
				case 66: {
					String s = input.readStringRequireUtf8();

					clientIp_ = s;
					break;
				}
				default: {
					if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
						done = true;
					}
					break;
				}
				}
			}
		} catch (com.google.protobuf.InvalidProtocolBufferException e) {
			throw e.setUnfinishedMessage(this);
		} catch (java.io.IOException e) {
			throw new com.google.protobuf.InvalidProtocolBufferException(e).setUnfinishedMessage(this);
		} finally {
			this.unknownFields = unknownFields.build();
			makeExtensionsImmutable();
		}
	}

	public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
		return NacosGrpcService.internal_static_Metadata_descriptor;
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	protected com.google.protobuf.MapField internalGetMapField(int number) {
		switch (number) {
		case 7:
			return internalGetHeaders();
		default:
			throw new RuntimeException("Invalid map field number: " + number);
		}
	}

	@Override
	protected FieldAccessorTable internalGetFieldAccessorTable() {
		return NacosGrpcService.internal_static_Metadata_fieldAccessorTable
				.ensureFieldAccessorsInitialized(Metadata.class, Builder.class);
	}

	public static final int TYPE_FIELD_NUMBER = 3;
	private volatile Object type_;

	/**
	 * <code>string type = 3;</code>
	 */
	public String getType() {
		Object ref = type_;
		if (ref instanceof String) {
			return (String) ref;
		} else {
			com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
			String s = bs.toStringUtf8();
			type_ = s;
			return s;
		}
	}

	/**
	 * <code>string type = 3;</code>
	 */
	public com.google.protobuf.ByteString getTypeBytes() {
		Object ref = type_;
		if (ref instanceof String) {
			com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((String) ref);
			type_ = b;
			return b;
		} else {
			return (com.google.protobuf.ByteString) ref;
		}
	}

	public static final int CLIENTIP_FIELD_NUMBER = 8;
	private volatile Object clientIp_;

	/**
	 * <code>string clientIp = 8;</code>
	 */
	public String getClientIp() {
		Object ref = clientIp_;
		if (ref instanceof String) {
			return (String) ref;
		} else {
			com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
			String s = bs.toStringUtf8();
			clientIp_ = s;
			return s;
		}
	}

	/**
	 * <code>string clientIp = 8;</code>
	 */
	public com.google.protobuf.ByteString getClientIpBytes() {
		Object ref = clientIp_;
		if (ref instanceof String) {
			com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((String) ref);
			clientIp_ = b;
			return b;
		} else {
			return (com.google.protobuf.ByteString) ref;
		}
	}

	public static final int HEADERS_FIELD_NUMBER = 7;

	private static final class HeadersDefaultEntryHolder {
		static final com.google.protobuf.MapEntry<String, String> defaultEntry = com.google.protobuf.MapEntry
				.<String, String>newDefaultInstance(NacosGrpcService.internal_static_Metadata_HeadersEntry_descriptor,
						com.google.protobuf.WireFormat.FieldType.STRING, "",
						com.google.protobuf.WireFormat.FieldType.STRING, "");
	}

	private com.google.protobuf.MapField<String, String> headers_;

	private com.google.protobuf.MapField<String, String> internalGetHeaders() {
		if (headers_ == null) {
			return com.google.protobuf.MapField.emptyMapField(HeadersDefaultEntryHolder.defaultEntry);
		}
		return headers_;
	}

	public int getHeadersCount() {
		return internalGetHeaders().getMap().size();
	}

	/**
	 * <code>map&lt;string, string&gt; headers = 7;</code>
	 */

	public boolean containsHeaders(String key) {
		if (key == null) {
			throw new NullPointerException();
		}
		return internalGetHeaders().getMap().containsKey(key);
	}

	/**
	 * Use {@link #getHeadersMap()} instead.
	 */
	@Deprecated
	public java.util.Map<String, String> getHeaders() {
		return getHeadersMap();
	}

	/**
	 * <code>map&lt;string, string&gt; headers = 7;</code>
	 */

	public java.util.Map<String, String> getHeadersMap() {
		return internalGetHeaders().getMap();
	}

	/**
	 * <code>map&lt;string, string&gt; headers = 7;</code>
	 */

	public String getHeadersOrDefault(String key, String defaultValue) {
		if (key == null) {
			throw new NullPointerException();
		}
		java.util.Map<String, String> map = internalGetHeaders().getMap();
		return map.containsKey(key) ? map.get(key) : defaultValue;
	}

	/**
	 * <code>map&lt;string, string&gt; headers = 7;</code>
	 */

	public String getHeadersOrThrow(String key) {
		if (key == null) {
			throw new NullPointerException();
		}
		java.util.Map<String, String> map = internalGetHeaders().getMap();
		if (!map.containsKey(key)) {
			throw new IllegalArgumentException();
		}
		return map.get(key);
	}

	private byte memoizedIsInitialized = -1;

	@Override
	public final boolean isInitialized() {
		byte isInitialized = memoizedIsInitialized;
		if (isInitialized == 1)
			return true;
		if (isInitialized == 0)
			return false;

		memoizedIsInitialized = 1;
		return true;
	}

	@Override
	public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
		if (!getTypeBytes().isEmpty()) {
			com.google.protobuf.GeneratedMessageV3.writeString(output, 3, type_);
		}
		com.google.protobuf.GeneratedMessageV3.serializeStringMapTo(output, internalGetHeaders(),
				HeadersDefaultEntryHolder.defaultEntry, 7);
		if (!getClientIpBytes().isEmpty()) {
			com.google.protobuf.GeneratedMessageV3.writeString(output, 8, clientIp_);
		}
		unknownFields.writeTo(output);
	}

	@Override
	public int getSerializedSize() {
		int size = memoizedSize;
		if (size != -1)
			return size;

		size = 0;
		if (!getTypeBytes().isEmpty()) {
			size += com.google.protobuf.GeneratedMessageV3.computeStringSize(3, type_);
		}
		for (java.util.Map.Entry<String, String> entry : internalGetHeaders().getMap().entrySet()) {
			com.google.protobuf.MapEntry<String, String> headers__ = HeadersDefaultEntryHolder.defaultEntry
					.newBuilderForType().setKey(entry.getKey()).setValue(entry.getValue()).build();
			size += com.google.protobuf.CodedOutputStream.computeMessageSize(7, headers__);
		}
		if (!getClientIpBytes().isEmpty()) {
			size += com.google.protobuf.GeneratedMessageV3.computeStringSize(8, clientIp_);
		}
		size += unknownFields.getSerializedSize();
		memoizedSize = size;
		return size;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof Metadata)) {
			return super.equals(obj);
		}
		Metadata other = (Metadata) obj;

		if (!getType().equals(other.getType()))
			return false;
		if (!getClientIp().equals(other.getClientIp()))
			return false;
		if (!internalGetHeaders().equals(other.internalGetHeaders()))
			return false;
		if (!unknownFields.equals(other.unknownFields))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		if (memoizedHashCode != 0) {
			return memoizedHashCode;
		}
		int hash = 41;
		hash = (19 * hash) + getDescriptor().hashCode();
		hash = (37 * hash) + TYPE_FIELD_NUMBER;
		hash = (53 * hash) + getType().hashCode();
		hash = (37 * hash) + CLIENTIP_FIELD_NUMBER;
		hash = (53 * hash) + getClientIp().hashCode();
		if (!internalGetHeaders().getMap().isEmpty()) {
			hash = (37 * hash) + HEADERS_FIELD_NUMBER;
			hash = (53 * hash) + internalGetHeaders().hashCode();
		}
		hash = (29 * hash) + unknownFields.hashCode();
		memoizedHashCode = hash;
		return hash;
	}

	public static Metadata parseFrom(java.nio.ByteBuffer data)
			throws com.google.protobuf.InvalidProtocolBufferException {
		return PARSER.parseFrom(data);
	}

	public static Metadata parseFrom(java.nio.ByteBuffer data,
			com.google.protobuf.ExtensionRegistryLite extensionRegistry)
			throws com.google.protobuf.InvalidProtocolBufferException {
		return PARSER.parseFrom(data, extensionRegistry);
	}

	public static Metadata parseFrom(com.google.protobuf.ByteString data)
			throws com.google.protobuf.InvalidProtocolBufferException {
		return PARSER.parseFrom(data);
	}

	public static Metadata parseFrom(com.google.protobuf.ByteString data,
			com.google.protobuf.ExtensionRegistryLite extensionRegistry)
			throws com.google.protobuf.InvalidProtocolBufferException {
		return PARSER.parseFrom(data, extensionRegistry);
	}

	public static Metadata parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
		return PARSER.parseFrom(data);
	}

	public static Metadata parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
			throws com.google.protobuf.InvalidProtocolBufferException {
		return PARSER.parseFrom(data, extensionRegistry);
	}

	public static Metadata parseFrom(java.io.InputStream input) throws java.io.IOException {
		return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
	}

	public static Metadata parseFrom(java.io.InputStream input,
			com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
		return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
	}

	public static Metadata parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
		return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
	}

	public static Metadata parseDelimitedFrom(java.io.InputStream input,
			com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
		return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
	}

	public static Metadata parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
		return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
	}

	public static Metadata parseFrom(com.google.protobuf.CodedInputStream input,
			com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
		return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
	}

	@Override
	public Builder newBuilderForType() {
		return newBuilder();
	}

	public static Builder newBuilder() {
		return DEFAULT_INSTANCE.toBuilder();
	}

	public static Builder newBuilder(Metadata prototype) {
		return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
	}

	@Override
	public Builder toBuilder() {
		return this == DEFAULT_INSTANCE ? new Builder() : new Builder().mergeFrom(this);
	}

	@Override
	protected Builder newBuilderForType(BuilderParent parent) {
		Builder builder = new Builder(parent);
		return builder;
	}

	/**
	 * Protobuf type {@code Metadata}
	 */
	public static final class Builder extends com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
			// @@protoc_insertion_point(builder_implements:Metadata)
			MetadataOrBuilder {
		public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
			return NacosGrpcService.internal_static_Metadata_descriptor;
		}

		@SuppressWarnings({ "rawtypes" })
		protected com.google.protobuf.MapField internalGetMapField(int number) {
			switch (number) {
			case 7:
				return internalGetHeaders();
			default:
				throw new RuntimeException("Invalid map field number: " + number);
			}
		}

		@SuppressWarnings({ "rawtypes" })
		protected com.google.protobuf.MapField internalGetMutableMapField(int number) {
			switch (number) {
			case 7:
				return internalGetMutableHeaders();
			default:
				throw new RuntimeException("Invalid map field number: " + number);
			}
		}

		@Override
		protected FieldAccessorTable internalGetFieldAccessorTable() {
			return NacosGrpcService.internal_static_Metadata_fieldAccessorTable
					.ensureFieldAccessorsInitialized(Metadata.class, Builder.class);
		}

		// Construct using com.alibaba.nacos.api.grpc.auto.Metadata.newBuilder()
		private Builder() {
			maybeForceBuilderInitialization();
		}

		private Builder(BuilderParent parent) {
			super(parent);
			maybeForceBuilderInitialization();
		}

		private void maybeForceBuilderInitialization() {
			if (com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders) {
			}
		}

		@Override
		public Builder clear() {
			super.clear();
			type_ = "";

			clientIp_ = "";

			internalGetMutableHeaders().clear();
			return this;
		}

		@Override
		public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
			return NacosGrpcService.internal_static_Metadata_descriptor;
		}

		@Override
		public Metadata getDefaultInstanceForType() {
			return Metadata.getDefaultInstance();
		}

		@Override
		public Metadata build() {
			Metadata result = buildPartial();
			if (!result.isInitialized()) {
				throw newUninitializedMessageException(result);
			}
			return result;
		}

		@Override
		public Metadata buildPartial() {
			Metadata result = new Metadata(this);
			int from_bitField0_ = bitField0_;
			result.type_ = type_;
			result.clientIp_ = clientIp_;
			result.headers_ = internalGetHeaders();
			result.headers_.makeImmutable();
			onBuilt();
			return result;
		}

		@Override
		public Builder clone() {
			return super.clone();
		}

		@Override
		public Builder setField(com.google.protobuf.Descriptors.FieldDescriptor field, Object value) {
			return super.setField(field, value);
		}

		@Override
		public Builder clearField(com.google.protobuf.Descriptors.FieldDescriptor field) {
			return super.clearField(field);
		}

		@Override
		public Builder clearOneof(com.google.protobuf.Descriptors.OneofDescriptor oneof) {
			return super.clearOneof(oneof);
		}

		@Override
		public Builder setRepeatedField(com.google.protobuf.Descriptors.FieldDescriptor field, int index,
				Object value) {
			return super.setRepeatedField(field, index, value);
		}

		@Override
		public Builder addRepeatedField(com.google.protobuf.Descriptors.FieldDescriptor field, Object value) {
			return super.addRepeatedField(field, value);
		}

		@Override
		public Builder mergeFrom(com.google.protobuf.Message other) {
			if (other instanceof Metadata) {
				return mergeFrom((Metadata) other);
			} else {
				super.mergeFrom(other);
				return this;
			}
		}

		public Builder mergeFrom(Metadata other) {
			if (other == Metadata.getDefaultInstance())
				return this;
			if (!other.getType().isEmpty()) {
				type_ = other.type_;
				onChanged();
			}
			if (!other.getClientIp().isEmpty()) {
				clientIp_ = other.clientIp_;
				onChanged();
			}
			internalGetMutableHeaders().mergeFrom(other.internalGetHeaders());
			this.mergeUnknownFields(other.unknownFields);
			onChanged();
			return this;
		}

		@Override
		public final boolean isInitialized() {
			return true;
		}

		@Override
		public Builder mergeFrom(com.google.protobuf.CodedInputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
			Metadata parsedMessage = null;
			try {
				parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
			} catch (com.google.protobuf.InvalidProtocolBufferException e) {
				parsedMessage = (Metadata) e.getUnfinishedMessage();
				throw e.unwrapIOException();
			} finally {
				if (parsedMessage != null) {
					mergeFrom(parsedMessage);
				}
			}
			return this;
		}

		private int bitField0_;

		private Object type_ = "";

		/**
		 * <code>string type = 3;</code>
		 */
		public String getType() {
			Object ref = type_;
			if (!(ref instanceof String)) {
				com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
				String s = bs.toStringUtf8();
				type_ = s;
				return s;
			} else {
				return (String) ref;
			}
		}

		/**
		 * <code>string type = 3;</code>
		 */
		public com.google.protobuf.ByteString getTypeBytes() {
			Object ref = type_;
			if (ref instanceof String) {
				com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((String) ref);
				type_ = b;
				return b;
			} else {
				return (com.google.protobuf.ByteString) ref;
			}
		}

		/**
		 * <code>string type = 3;</code>
		 */
		public Builder setType(String value) {
			if (value == null) {
				throw new NullPointerException();
			}

			type_ = value;
			onChanged();
			return this;
		}

		/**
		 * <code>string type = 3;</code>
		 */
		public Builder clearType() {

			type_ = getDefaultInstance().getType();
			onChanged();
			return this;
		}

		/**
		 * <code>string type = 3;</code>
		 */
		public Builder setTypeBytes(com.google.protobuf.ByteString value) {
			if (value == null) {
				throw new NullPointerException();
			}
			checkByteStringIsUtf8(value);

			type_ = value;
			onChanged();
			return this;
		}

		private Object clientIp_ = "";

		/**
		 * <code>string clientIp = 8;</code>
		 */
		public String getClientIp() {
			Object ref = clientIp_;
			if (!(ref instanceof String)) {
				com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
				String s = bs.toStringUtf8();
				clientIp_ = s;
				return s;
			} else {
				return (String) ref;
			}
		}

		/**
		 * <code>string clientIp = 8;</code>
		 */
		public com.google.protobuf.ByteString getClientIpBytes() {
			Object ref = clientIp_;
			if (ref instanceof String) {
				com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((String) ref);
				clientIp_ = b;
				return b;
			} else {
				return (com.google.protobuf.ByteString) ref;
			}
		}

		/**
		 * <code>string clientIp = 8;</code>
		 */
		public Builder setClientIp(String value) {
			if (value == null) {
				throw new NullPointerException();
			}

			clientIp_ = value;
			onChanged();
			return this;
		}

		/**
		 * <code>string clientIp = 8;</code>
		 */
		public Builder clearClientIp() {

			clientIp_ = getDefaultInstance().getClientIp();
			onChanged();
			return this;
		}

		/**
		 * <code>string clientIp = 8;</code>
		 */
		public Builder setClientIpBytes(com.google.protobuf.ByteString value) {
			if (value == null) {
				throw new NullPointerException();
			}
			checkByteStringIsUtf8(value);

			clientIp_ = value;
			onChanged();
			return this;
		}

		private com.google.protobuf.MapField<String, String> headers_;

		private com.google.protobuf.MapField<String, String> internalGetHeaders() {
			if (headers_ == null) {
				return com.google.protobuf.MapField.emptyMapField(HeadersDefaultEntryHolder.defaultEntry);
			}
			return headers_;
		}

		private com.google.protobuf.MapField<String, String> internalGetMutableHeaders() {
			onChanged();
			;
			if (headers_ == null) {
				headers_ = com.google.protobuf.MapField.newMapField(HeadersDefaultEntryHolder.defaultEntry);
			}
			if (!headers_.isMutable()) {
				headers_ = headers_.copy();
			}
			return headers_;
		}

		public int getHeadersCount() {
			return internalGetHeaders().getMap().size();
		}

		/**
		 * <code>map&lt;string, string&gt; headers = 7;</code>
		 */

		public boolean containsHeaders(String key) {
			if (key == null) {
				throw new NullPointerException();
			}
			return internalGetHeaders().getMap().containsKey(key);
		}

		/**
		 * Use {@link #getHeadersMap()} instead.
		 */
		@Deprecated
		public java.util.Map<String, String> getHeaders() {
			return getHeadersMap();
		}

		/**
		 * <code>map&lt;string, string&gt; headers = 7;</code>
		 */

		public java.util.Map<String, String> getHeadersMap() {
			return internalGetHeaders().getMap();
		}

		/**
		 * <code>map&lt;string, string&gt; headers = 7;</code>
		 */

		public String getHeadersOrDefault(String key, String defaultValue) {
			if (key == null) {
				throw new NullPointerException();
			}
			java.util.Map<String, String> map = internalGetHeaders().getMap();
			return map.containsKey(key) ? map.get(key) : defaultValue;
		}

		/**
		 * <code>map&lt;string, string&gt; headers = 7;</code>
		 */

		public String getHeadersOrThrow(String key) {
			if (key == null) {
				throw new NullPointerException();
			}
			java.util.Map<String, String> map = internalGetHeaders().getMap();
			if (!map.containsKey(key)) {
				throw new IllegalArgumentException();
			}
			return map.get(key);
		}

		public Builder clearHeaders() {
			internalGetMutableHeaders().getMutableMap().clear();
			return this;
		}

		/**
		 * <code>map&lt;string, string&gt; headers = 7;</code>
		 */

		public Builder removeHeaders(String key) {
			if (key == null) {
				throw new NullPointerException();
			}
			internalGetMutableHeaders().getMutableMap().remove(key);
			return this;
		}

		/**
		 * Use alternate mutation accessors instead.
		 */
		@Deprecated
		public java.util.Map<String, String> getMutableHeaders() {
			return internalGetMutableHeaders().getMutableMap();
		}

		/**
		 * <code>map&lt;string, string&gt; headers = 7;</code>
		 */
		public Builder putHeaders(String key, String value) {
			if (key == null) {
				throw new NullPointerException();
			}
			if (value == null) {
				throw new NullPointerException();
			}
			internalGetMutableHeaders().getMutableMap().put(key, value);
			return this;
		}

		/**
		 * <code>map&lt;string, string&gt; headers = 7;</code>
		 */

		public Builder putAllHeaders(java.util.Map<String, String> values) {
			internalGetMutableHeaders().getMutableMap().putAll(values);
			return this;
		}

		@Override
		public final Builder setUnknownFields(final com.google.protobuf.UnknownFieldSet unknownFields) {
			return super.setUnknownFields(unknownFields);
		}

		@Override
		public final Builder mergeUnknownFields(final com.google.protobuf.UnknownFieldSet unknownFields) {
			return super.mergeUnknownFields(unknownFields);
		}

		// @@protoc_insertion_point(builder_scope:Metadata)
	}

	// @@protoc_insertion_point(class_scope:Metadata)
	private static final Metadata DEFAULT_INSTANCE;
	static {
		DEFAULT_INSTANCE = new Metadata();
	}

	public static Metadata getDefaultInstance() {
		return DEFAULT_INSTANCE;
	}

	private static final com.google.protobuf.Parser<Metadata> PARSER = new com.google.protobuf.AbstractParser<Metadata>() {
		@Override
		public Metadata parsePartialFrom(com.google.protobuf.CodedInputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return new Metadata(input, extensionRegistry);
		}
	};

	public static com.google.protobuf.Parser<Metadata> parser() {
		return PARSER;
	}

	@Override
	public com.google.protobuf.Parser<Metadata> getParserForType() {
		return PARSER;
	}

	@Override
	public Metadata getDefaultInstanceForType() {
		return DEFAULT_INSTANCE;
	}

}
