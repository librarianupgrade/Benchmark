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
 * Protobuf type {@code Payload}
 */
public final class Payload extends com.google.protobuf.GeneratedMessageV3 implements
		// @@protoc_insertion_point(message_implements:Payload)
		PayloadOrBuilder {
	private static final long serialVersionUID = 0L;

	// Use Payload.newBuilder() to construct.
	private Payload(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
		super(builder);
	}

	private Payload() {
	}

	@Override
	@SuppressWarnings({ "unused" })
	protected Object newInstance(UnusedPrivateParameter unused) {
		return new Payload();
	}

	@Override
	public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
		return this.unknownFields;
	}

	private Payload(com.google.protobuf.CodedInputStream input,
			com.google.protobuf.ExtensionRegistryLite extensionRegistry)
			throws com.google.protobuf.InvalidProtocolBufferException {
		this();
		if (extensionRegistry == null) {
			throw new NullPointerException();
		}
		com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder();
		try {
			boolean done = false;
			while (!done) {
				int tag = input.readTag();
				switch (tag) {
				case 0:
					done = true;
					break;
				case 18: {
					Metadata.Builder subBuilder = null;
					if (metadata_ != null) {
						subBuilder = metadata_.toBuilder();
					}
					metadata_ = input.readMessage(Metadata.parser(), extensionRegistry);
					if (subBuilder != null) {
						subBuilder.mergeFrom(metadata_);
						metadata_ = subBuilder.buildPartial();
					}

					break;
				}
				case 26: {
					com.google.protobuf.Any.Builder subBuilder = null;
					if (body_ != null) {
						subBuilder = body_.toBuilder();
					}
					body_ = input.readMessage(com.google.protobuf.Any.parser(), extensionRegistry);
					if (subBuilder != null) {
						subBuilder.mergeFrom(body_);
						body_ = subBuilder.buildPartial();
					}

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
		return NacosGrpcService.internal_static_Payload_descriptor;
	}

	@Override
	protected FieldAccessorTable internalGetFieldAccessorTable() {
		return NacosGrpcService.internal_static_Payload_fieldAccessorTable
				.ensureFieldAccessorsInitialized(Payload.class, Builder.class);
	}

	public static final int METADATA_FIELD_NUMBER = 2;
	private Metadata metadata_;

	/**
	 * <code>.Metadata metadata = 2;</code>
	 */
	public boolean hasMetadata() {
		return metadata_ != null;
	}

	/**
	 * <code>.Metadata metadata = 2;</code>
	 */
	public Metadata getMetadata() {
		return metadata_ == null ? Metadata.getDefaultInstance() : metadata_;
	}

	/**
	 * <code>.Metadata metadata = 2;</code>
	 */
	public MetadataOrBuilder getMetadataOrBuilder() {
		return getMetadata();
	}

	public static final int BODY_FIELD_NUMBER = 3;
	private com.google.protobuf.Any body_;

	/**
	 * <code>.google.protobuf.Any body = 3;</code>
	 */
	public boolean hasBody() {
		return body_ != null;
	}

	/**
	 * <code>.google.protobuf.Any body = 3;</code>
	 */
	public com.google.protobuf.Any getBody() {
		return body_ == null ? com.google.protobuf.Any.getDefaultInstance() : body_;
	}

	/**
	 * <code>.google.protobuf.Any body = 3;</code>
	 */
	public com.google.protobuf.AnyOrBuilder getBodyOrBuilder() {
		return getBody();
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
		if (metadata_ != null) {
			output.writeMessage(2, getMetadata());
		}
		if (body_ != null) {
			output.writeMessage(3, getBody());
		}
		unknownFields.writeTo(output);
	}

	@Override
	public int getSerializedSize() {
		int size = memoizedSize;
		if (size != -1)
			return size;

		size = 0;
		if (metadata_ != null) {
			size += com.google.protobuf.CodedOutputStream.computeMessageSize(2, getMetadata());
		}
		if (body_ != null) {
			size += com.google.protobuf.CodedOutputStream.computeMessageSize(3, getBody());
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
		if (!(obj instanceof Payload)) {
			return super.equals(obj);
		}
		Payload other = (Payload) obj;

		if (hasMetadata() != other.hasMetadata())
			return false;
		if (hasMetadata()) {
			if (!getMetadata().equals(other.getMetadata()))
				return false;
		}
		if (hasBody() != other.hasBody())
			return false;
		if (hasBody()) {
			if (!getBody().equals(other.getBody()))
				return false;
		}
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
		if (hasMetadata()) {
			hash = (37 * hash) + METADATA_FIELD_NUMBER;
			hash = (53 * hash) + getMetadata().hashCode();
		}
		if (hasBody()) {
			hash = (37 * hash) + BODY_FIELD_NUMBER;
			hash = (53 * hash) + getBody().hashCode();
		}
		hash = (29 * hash) + unknownFields.hashCode();
		memoizedHashCode = hash;
		return hash;
	}

	public static Payload parseFrom(java.nio.ByteBuffer data)
			throws com.google.protobuf.InvalidProtocolBufferException {
		return PARSER.parseFrom(data);
	}

	public static Payload parseFrom(java.nio.ByteBuffer data,
			com.google.protobuf.ExtensionRegistryLite extensionRegistry)
			throws com.google.protobuf.InvalidProtocolBufferException {
		return PARSER.parseFrom(data, extensionRegistry);
	}

	public static Payload parseFrom(com.google.protobuf.ByteString data)
			throws com.google.protobuf.InvalidProtocolBufferException {
		return PARSER.parseFrom(data);
	}

	public static Payload parseFrom(com.google.protobuf.ByteString data,
			com.google.protobuf.ExtensionRegistryLite extensionRegistry)
			throws com.google.protobuf.InvalidProtocolBufferException {
		return PARSER.parseFrom(data, extensionRegistry);
	}

	public static Payload parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
		return PARSER.parseFrom(data);
	}

	public static Payload parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
			throws com.google.protobuf.InvalidProtocolBufferException {
		return PARSER.parseFrom(data, extensionRegistry);
	}

	public static Payload parseFrom(java.io.InputStream input) throws java.io.IOException {
		return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
	}

	public static Payload parseFrom(java.io.InputStream input,
			com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
		return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
	}

	public static Payload parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
		return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
	}

	public static Payload parseDelimitedFrom(java.io.InputStream input,
			com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
		return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
	}

	public static Payload parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
		return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
	}

	public static Payload parseFrom(com.google.protobuf.CodedInputStream input,
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

	public static Builder newBuilder(Payload prototype) {
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
	 * Protobuf type {@code Payload}
	 */
	public static final class Builder extends com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
			// @@protoc_insertion_point(builder_implements:Payload)
			PayloadOrBuilder {
		public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
			return NacosGrpcService.internal_static_Payload_descriptor;
		}

		@Override
		protected FieldAccessorTable internalGetFieldAccessorTable() {
			return NacosGrpcService.internal_static_Payload_fieldAccessorTable
					.ensureFieldAccessorsInitialized(Payload.class, Builder.class);
		}

		// Construct using com.alibaba.nacos.api.grpc.auto.Payload.newBuilder()
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
			if (metadataBuilder_ == null) {
				metadata_ = null;
			} else {
				metadata_ = null;
				metadataBuilder_ = null;
			}
			if (bodyBuilder_ == null) {
				body_ = null;
			} else {
				body_ = null;
				bodyBuilder_ = null;
			}
			return this;
		}

		@Override
		public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
			return NacosGrpcService.internal_static_Payload_descriptor;
		}

		@Override
		public Payload getDefaultInstanceForType() {
			return Payload.getDefaultInstance();
		}

		@Override
		public Payload build() {
			Payload result = buildPartial();
			if (!result.isInitialized()) {
				throw newUninitializedMessageException(result);
			}
			return result;
		}

		@Override
		public Payload buildPartial() {
			Payload result = new Payload(this);
			if (metadataBuilder_ == null) {
				result.metadata_ = metadata_;
			} else {
				result.metadata_ = metadataBuilder_.build();
			}
			if (bodyBuilder_ == null) {
				result.body_ = body_;
			} else {
				result.body_ = bodyBuilder_.build();
			}
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
			if (other instanceof Payload) {
				return mergeFrom((Payload) other);
			} else {
				super.mergeFrom(other);
				return this;
			}
		}

		public Builder mergeFrom(Payload other) {
			if (other == Payload.getDefaultInstance())
				return this;
			if (other.hasMetadata()) {
				mergeMetadata(other.getMetadata());
			}
			if (other.hasBody()) {
				mergeBody(other.getBody());
			}
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
			Payload parsedMessage = null;
			try {
				parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
			} catch (com.google.protobuf.InvalidProtocolBufferException e) {
				parsedMessage = (Payload) e.getUnfinishedMessage();
				throw e.unwrapIOException();
			} finally {
				if (parsedMessage != null) {
					mergeFrom(parsedMessage);
				}
			}
			return this;
		}

		private Metadata metadata_;
		private com.google.protobuf.SingleFieldBuilderV3<Metadata, Metadata.Builder, MetadataOrBuilder> metadataBuilder_;

		/**
		 * <code>.Metadata metadata = 2;</code>
		 */
		public boolean hasMetadata() {
			return metadataBuilder_ != null || metadata_ != null;
		}

		/**
		 * <code>.Metadata metadata = 2;</code>
		 */
		public Metadata getMetadata() {
			if (metadataBuilder_ == null) {
				return metadata_ == null ? Metadata.getDefaultInstance() : metadata_;
			} else {
				return metadataBuilder_.getMessage();
			}
		}

		/**
		 * <code>.Metadata metadata = 2;</code>
		 */
		public Builder setMetadata(Metadata value) {
			if (metadataBuilder_ == null) {
				if (value == null) {
					throw new NullPointerException();
				}
				metadata_ = value;
				onChanged();
			} else {
				metadataBuilder_.setMessage(value);
			}

			return this;
		}

		/**
		 * <code>.Metadata metadata = 2;</code>
		 */
		public Builder setMetadata(Metadata.Builder builderForValue) {
			if (metadataBuilder_ == null) {
				metadata_ = builderForValue.build();
				onChanged();
			} else {
				metadataBuilder_.setMessage(builderForValue.build());
			}

			return this;
		}

		/**
		 * <code>.Metadata metadata = 2;</code>
		 */
		public Builder mergeMetadata(Metadata value) {
			if (metadataBuilder_ == null) {
				if (metadata_ != null) {
					metadata_ = Metadata.newBuilder(metadata_).mergeFrom(value).buildPartial();
				} else {
					metadata_ = value;
				}
				onChanged();
			} else {
				metadataBuilder_.mergeFrom(value);
			}

			return this;
		}

		/**
		 * <code>.Metadata metadata = 2;</code>
		 */
		public Builder clearMetadata() {
			if (metadataBuilder_ == null) {
				metadata_ = null;
				onChanged();
			} else {
				metadata_ = null;
				metadataBuilder_ = null;
			}

			return this;
		}

		/**
		 * <code>.Metadata metadata = 2;</code>
		 */
		public Metadata.Builder getMetadataBuilder() {

			onChanged();
			return getMetadataFieldBuilder().getBuilder();
		}

		/**
		 * <code>.Metadata metadata = 2;</code>
		 */
		public MetadataOrBuilder getMetadataOrBuilder() {
			if (metadataBuilder_ != null) {
				return metadataBuilder_.getMessageOrBuilder();
			} else {
				return metadata_ == null ? Metadata.getDefaultInstance() : metadata_;
			}
		}

		/**
		 * <code>.Metadata metadata = 2;</code>
		 */
		private com.google.protobuf.SingleFieldBuilderV3<Metadata, Metadata.Builder, MetadataOrBuilder> getMetadataFieldBuilder() {
			if (metadataBuilder_ == null) {
				metadataBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<>(getMetadata(), getParentForChildren(),
						isClean());
				metadata_ = null;
			}
			return metadataBuilder_;
		}

		private com.google.protobuf.Any body_;
		private com.google.protobuf.SingleFieldBuilderV3<com.google.protobuf.Any, com.google.protobuf.Any.Builder, com.google.protobuf.AnyOrBuilder> bodyBuilder_;

		/**
		 * <code>.google.protobuf.Any body = 3;</code>
		 */
		public boolean hasBody() {
			return bodyBuilder_ != null || body_ != null;
		}

		/**
		 * <code>.google.protobuf.Any body = 3;</code>
		 */
		public com.google.protobuf.Any getBody() {
			if (bodyBuilder_ == null) {
				return body_ == null ? com.google.protobuf.Any.getDefaultInstance() : body_;
			} else {
				return bodyBuilder_.getMessage();
			}
		}

		/**
		 * <code>.google.protobuf.Any body = 3;</code>
		 */
		public Builder setBody(com.google.protobuf.Any value) {
			if (bodyBuilder_ == null) {
				if (value == null) {
					throw new NullPointerException();
				}
				body_ = value;
				onChanged();
			} else {
				bodyBuilder_.setMessage(value);
			}

			return this;
		}

		/**
		 * <code>.google.protobuf.Any body = 3;</code>
		 */
		public Builder setBody(com.google.protobuf.Any.Builder builderForValue) {
			if (bodyBuilder_ == null) {
				body_ = builderForValue.build();
				onChanged();
			} else {
				bodyBuilder_.setMessage(builderForValue.build());
			}

			return this;
		}

		/**
		 * <code>.google.protobuf.Any body = 3;</code>
		 */
		public Builder mergeBody(com.google.protobuf.Any value) {
			if (bodyBuilder_ == null) {
				if (body_ != null) {
					body_ = com.google.protobuf.Any.newBuilder(body_).mergeFrom(value).buildPartial();
				} else {
					body_ = value;
				}
				onChanged();
			} else {
				bodyBuilder_.mergeFrom(value);
			}

			return this;
		}

		/**
		 * <code>.google.protobuf.Any body = 3;</code>
		 */
		public Builder clearBody() {
			if (bodyBuilder_ == null) {
				body_ = null;
				onChanged();
			} else {
				body_ = null;
				bodyBuilder_ = null;
			}

			return this;
		}

		/**
		 * <code>.google.protobuf.Any body = 3;</code>
		 */
		public com.google.protobuf.Any.Builder getBodyBuilder() {

			onChanged();
			return getBodyFieldBuilder().getBuilder();
		}

		/**
		 * <code>.google.protobuf.Any body = 3;</code>
		 */
		public com.google.protobuf.AnyOrBuilder getBodyOrBuilder() {
			if (bodyBuilder_ != null) {
				return bodyBuilder_.getMessageOrBuilder();
			} else {
				return body_ == null ? com.google.protobuf.Any.getDefaultInstance() : body_;
			}
		}

		/**
		 * <code>.google.protobuf.Any body = 3;</code>
		 */
		private com.google.protobuf.SingleFieldBuilderV3<com.google.protobuf.Any, com.google.protobuf.Any.Builder, com.google.protobuf.AnyOrBuilder> getBodyFieldBuilder() {
			if (bodyBuilder_ == null) {
				bodyBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<>(getBody(), getParentForChildren(),
						isClean());
				body_ = null;
			}
			return bodyBuilder_;
		}

		@Override
		public final Builder setUnknownFields(final com.google.protobuf.UnknownFieldSet unknownFields) {
			return super.setUnknownFields(unknownFields);
		}

		@Override
		public final Builder mergeUnknownFields(final com.google.protobuf.UnknownFieldSet unknownFields) {
			return super.mergeUnknownFields(unknownFields);
		}

		// @@protoc_insertion_point(builder_scope:Payload)
	}

	// @@protoc_insertion_point(class_scope:Payload)
	private static final Payload DEFAULT_INSTANCE;
	static {
		DEFAULT_INSTANCE = new Payload();
	}

	public static Payload getDefaultInstance() {
		return DEFAULT_INSTANCE;
	}

	private static final com.google.protobuf.Parser<Payload> PARSER = new com.google.protobuf.AbstractParser<Payload>() {
		@Override
		public Payload parsePartialFrom(com.google.protobuf.CodedInputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException {
			return new Payload(input, extensionRegistry);
		}
	};

	public static com.google.protobuf.Parser<Payload> parser() {
		return PARSER;
	}

	@Override
	public com.google.protobuf.Parser<Payload> getParserForType() {
		return PARSER;
	}

	@Override
	public Payload getDefaultInstanceForType() {
		return DEFAULT_INSTANCE;
	}

}
