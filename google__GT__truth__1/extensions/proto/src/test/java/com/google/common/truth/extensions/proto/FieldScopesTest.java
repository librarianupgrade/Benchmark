/*
 * Copyright (c) 2016 Google, Inc.
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
package com.google.common.truth.extensions.proto;

import static com.google.common.truth.extensions.proto.ProtoTruth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.UnknownFieldSet;
import com.google.protobuf.UnknownFieldSet.Field;
import java.util.Collection;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/** Unit tests for {@link FieldScope}, and their interaction with {@link ProtoSubject}. */
@RunWith(Parameterized.class)
public class FieldScopesTest extends ProtoSubjectTestBase {

	@Parameters(name = "{0}")
	public static Collection<Object[]> parameters() {
		return ProtoSubjectTestBase.parameters();
	}

	// Set up for the ignoringTopLevelField tests.
	// ignoringFieldMessage and ignoringFieldDiffMessage are simple messages with two fields set. They
	// are the same for the "good" field, and different for the "bad" field. The *FieldNumber and
	// *FieldDescriptor members point to these fields.

	private final Message ignoringFieldMessage;
	private final Message ignoringFieldDiffMessage;
	private final int goodFieldNumber;
	private final int badFieldNumber;
	private final FieldDescriptor goodFieldDescriptor;
	private final FieldDescriptor badFieldDescriptor;

	public FieldScopesTest(TestType testType) {
		super(testType);

		ignoringFieldMessage = parse("o_int: 3 r_string: \"foo\"");
		ignoringFieldDiffMessage = parse("o_int: 3 r_string: \"bar\"");
		goodFieldNumber = getFieldNumber("o_int");
		badFieldNumber = getFieldNumber("r_string");
		goodFieldDescriptor = getFieldDescriptor("o_int");
		badFieldDescriptor = getFieldDescriptor("r_string");
	}

	@Test
	public void testUnequalMessages() {
		Message message = parse("o_int: 3 r_string: \"foo\"");
		Message diffMessage = parse("o_int: 5 r_string: \"bar\"");

		expectThat(diffMessage).isNotEqualTo(message);
	}

	@Test
	public void testFieldScopes_all() {
		Message message = parse("o_int: 3 r_string: \"foo\"");
		Message diffMessage = parse("o_int: 5 r_string: \"bar\"");

		expectThat(diffMessage).withPartialScope(FieldScopes.all()).isNotEqualTo(message);
		expectThat(diffMessage).ignoringFieldScope(FieldScopes.all()).isEqualTo(message);

		expectFailureWhenTesting().that(diffMessage).ignoringFieldScope(FieldScopes.all()).isNotEqualTo(message);
		expectIsNotEqualToFailed();
		expectThatFailure().hasMessageThat().contains("ignored: o_int");
		expectThatFailure().hasMessageThat().contains("ignored: r_string");
	}

	@Test
	public void testFieldScopes_none() {
		Message message = parse("o_int: 3 r_string: \"foo\"");
		Message diffMessage = parse("o_int: 5 r_string: \"bar\"");

		expectThat(diffMessage).ignoringFieldScope(FieldScopes.none()).isNotEqualTo(message);
		expectThat(diffMessage).withPartialScope(FieldScopes.none()).isEqualTo(message);

		expectFailureWhenTesting().that(diffMessage).withPartialScope(FieldScopes.none()).isNotEqualTo(message);
		expectIsNotEqualToFailed();
		expectThatFailure().hasMessageThat().contains("ignored: o_int");
		expectThatFailure().hasMessageThat().contains("ignored: r_string");
	}

	@Test
	public void testFieldScopes_none_withAnyField() {
		String typeUrl = isProto3() ? "type.googleapis.com/com.google.common.truth.extensions.proto.SubTestMessage3"
				: "type.googleapis.com/com.google.common.truth.extensions.proto.SubTestMessage2";
		Message message = parse("o_int: 3 o_any_message { [" + typeUrl + "]: { r_string: \"foo\" } }");
		Message diffMessage = parse("o_int: 5 o_any_message { [" + typeUrl + "]: { r_string: \"bar\" } }");

		expectThat(diffMessage).ignoringFieldScope(FieldScopes.none()).isNotEqualTo(message);
		expectThat(diffMessage).withPartialScope(FieldScopes.none()).isEqualTo(message);

		expectFailureWhenTesting().that(diffMessage).withPartialScope(FieldScopes.none()).isNotEqualTo(message);
		expectIsNotEqualToFailed();
		expectThatFailure().hasMessageThat().contains("ignored: o_int");
		expectThatFailure().hasMessageThat().contains("ignored: o_any_message");
	}

	@Test
	public void testIgnoringTopLevelField_ignoringField() {
		expectThat(ignoringFieldDiffMessage).ignoringFields(goodFieldNumber).isNotEqualTo(ignoringFieldMessage);
		expectThat(ignoringFieldDiffMessage).ignoringFields(badFieldNumber).isEqualTo(ignoringFieldMessage);

		expectFailureWhenTesting().that(ignoringFieldDiffMessage).ignoringFields(goodFieldNumber)
				.isEqualTo(ignoringFieldMessage);
		expectIsEqualToFailed();
		expectThatFailure().hasMessageThat().contains("modified: r_string[0]: \"foo\" -> \"bar\"");

		expectFailureWhenTesting().that(ignoringFieldDiffMessage).ignoringFields(badFieldNumber)
				.isNotEqualTo(ignoringFieldMessage);
		expectIsNotEqualToFailed();
		expectThatFailure().hasMessageThat().contains("ignored: r_string");
	}

	@Test
	public void testIgnoringTopLevelAnyField_ignoringField() {
		String typeUrl = isProto3() ? "type.googleapis.com/com.google.common.truth.extensions.proto.SubTestMessage3"
				: "type.googleapis.com/com.google.common.truth.extensions.proto.SubTestMessage2";
		Message message = parse("o_int: 1 o_any_message { [" + typeUrl + "]: { r_string: \"foo\" } }");
		Message diffMessage = parse("o_int: 1");
		int goodFieldNumber = getFieldNumber("o_int");
		int badFieldNumber = getFieldNumber("o_any_message");

		expectThat(diffMessage).ignoringFields(goodFieldNumber).isNotEqualTo(message);
		expectThat(diffMessage).ignoringFields(badFieldNumber).isEqualTo(diffMessage);

		expectFailureWhenTesting().that(diffMessage).ignoringFields(goodFieldNumber).isEqualTo(message);
		expectIsEqualToFailed();
		expectThatFailure().hasMessageThat().contains("deleted: o_any_message");

		expectFailureWhenTesting().that(diffMessage).ignoringFields(badFieldNumber).isNotEqualTo(message);
		expectIsNotEqualToFailed();
		expectThatFailure().hasMessageThat().contains("ignored: o_any_message");
	}

	@Test
	public void testIgnoringTopLevelField_fieldScopes_ignoringFields() {
		expectThat(ignoringFieldDiffMessage).withPartialScope(FieldScopes.ignoringFields(goodFieldNumber))
				.isNotEqualTo(ignoringFieldMessage);
		expectThat(ignoringFieldDiffMessage).ignoringFieldScope(FieldScopes.ignoringFields(goodFieldNumber))
				.isEqualTo(ignoringFieldMessage);
		expectThat(ignoringFieldDiffMessage).withPartialScope(FieldScopes.ignoringFields(badFieldNumber))
				.isEqualTo(ignoringFieldMessage);
		expectThat(ignoringFieldDiffMessage).ignoringFieldScope(FieldScopes.ignoringFields(badFieldNumber))
				.isNotEqualTo(ignoringFieldMessage);
	}

	@Test
	public void testIgnoringTopLevelField_fieldScopes_allowingFields() {
		expectThat(ignoringFieldDiffMessage).withPartialScope(FieldScopes.allowingFields(goodFieldNumber))
				.isEqualTo(ignoringFieldMessage);
		expectThat(ignoringFieldDiffMessage).ignoringFieldScope(FieldScopes.allowingFields(goodFieldNumber))
				.isNotEqualTo(ignoringFieldMessage);
		expectThat(ignoringFieldDiffMessage).withPartialScope(FieldScopes.allowingFields(badFieldNumber))
				.isNotEqualTo(ignoringFieldMessage);
		expectThat(ignoringFieldDiffMessage).ignoringFieldScope(FieldScopes.allowingFields(badFieldNumber))
				.isEqualTo(ignoringFieldMessage);
	}

	@Test
	public void testIgnoringTopLevelAnyField_fieldScopes_allowingFields() {
		String typeUrl = isProto3() ? "type.googleapis.com/com.google.common.truth.extensions.proto.SubTestMessage3"
				: "type.googleapis.com/com.google.common.truth.extensions.proto.SubTestMessage2";
		Message message = parse("o_int: 1 o_any_message { [" + typeUrl + "]: { o_int: 2 r_string: \"foo\" } }");
		Message diffMessage = parse("o_int: 1");
		int goodFieldNumber = getFieldNumber("o_int");

		expectThat(message).withPartialScope(FieldScopes.allowingFields(goodFieldNumber)).isEqualTo(diffMessage);
	}

	@Test
	public void testIgnoringTopLevelField_fieldScopes_allowingFieldDescriptors() {
		expectThat(ignoringFieldDiffMessage).withPartialScope(FieldScopes.allowingFieldDescriptors(goodFieldDescriptor))
				.isEqualTo(ignoringFieldMessage);
		expectThat(ignoringFieldDiffMessage)
				.ignoringFieldScope(FieldScopes.allowingFieldDescriptors(goodFieldDescriptor))
				.isNotEqualTo(ignoringFieldMessage);
		expectThat(ignoringFieldDiffMessage).withPartialScope(FieldScopes.allowingFieldDescriptors(badFieldDescriptor))
				.isNotEqualTo(ignoringFieldMessage);
		expectThat(ignoringFieldDiffMessage)
				.ignoringFieldScope(FieldScopes.allowingFieldDescriptors(badFieldDescriptor))
				.isEqualTo(ignoringFieldMessage);
	}

	@Test
	public void testIgnoringTopLevelField_fieldScopes_ignoringFieldDescriptors() {
		expectThat(ignoringFieldDiffMessage).withPartialScope(FieldScopes.ignoringFieldDescriptors(goodFieldDescriptor))
				.isNotEqualTo(ignoringFieldMessage);
		expectThat(ignoringFieldDiffMessage)
				.ignoringFieldScope(FieldScopes.ignoringFieldDescriptors(goodFieldDescriptor))
				.isEqualTo(ignoringFieldMessage);
		expectThat(ignoringFieldDiffMessage).withPartialScope(FieldScopes.ignoringFieldDescriptors(badFieldDescriptor))
				.isEqualTo(ignoringFieldMessage);
		expectThat(ignoringFieldDiffMessage)
				.ignoringFieldScope(FieldScopes.ignoringFieldDescriptors(badFieldDescriptor))
				.isNotEqualTo(ignoringFieldMessage);
	}

	@Test
	public void testEmptySubMessage() {
		Message message = parse("o_int: 1 o_sub_test_message: { }");
		Message eqMessage = parse("o_int: 2 o_sub_test_message: { }");
		Message diffMessage = parse("o_int: 3");

		// Different logic gets exercised when we add an 'ignore' clause.
		// Let's ensure o_sub_test_message is compared properly in all cases.
		int fieldNumber = getFieldNumber("o_int");

		expectThat(eqMessage).isNotEqualTo(message);
		expectThat(eqMessage).ignoringFieldAbsence().isNotEqualTo(message);
		expectThat(eqMessage).ignoringFields(fieldNumber).isEqualTo(message);
		expectThat(eqMessage).ignoringFields(fieldNumber).ignoringFieldAbsence().isEqualTo(message);

		expectThat(diffMessage).isNotEqualTo(message);
		expectThat(diffMessage).ignoringFieldAbsence().isNotEqualTo(message);
		expectThat(diffMessage).ignoringFields(fieldNumber).isNotEqualTo(message);
		expectThat(diffMessage).ignoringFields(fieldNumber).ignoringFieldAbsence().isEqualTo(message);
	}

	@Test
	public void testIgnoreSubMessageField() {
		Message message = parse("o_int: 1 o_sub_test_message: { o_int: 2 }");
		Message diffMessage = parse("o_int: 2 o_sub_test_message: { o_int: 2 }");
		Message eqMessage1 = parse("o_int: 1");
		Message eqMessage2 = parse("o_int: 1 o_sub_test_message: {}");
		Message eqMessage3 = parse("o_int: 1 o_sub_test_message: { o_int: 3 r_string: \"x\" }");
		int fieldNumber = getFieldNumber("o_sub_test_message");

		expectThat(diffMessage).ignoringFields(fieldNumber).isNotEqualTo(message);
		expectThat(eqMessage1).ignoringFields(fieldNumber).isEqualTo(message);
		expectThat(eqMessage2).ignoringFields(fieldNumber).isEqualTo(message);
		expectThat(eqMessage3).ignoringFields(fieldNumber).isEqualTo(message);

		expectFailureWhenTesting().that(diffMessage).ignoringFields(fieldNumber).isEqualTo(message);
		expectIsEqualToFailed();
		expectThatFailure().hasMessageThat().contains("modified: o_int: 1 -> 2");

		expectFailureWhenTesting().that(eqMessage3).ignoringFields(fieldNumber).isNotEqualTo(message);
		expectIsNotEqualToFailed();
		expectThatFailure().hasMessageThat().contains("ignored: o_sub_test_message");
	}

	@Test
	public void testIgnoreFieldOfSubMessage() {
		// Ignore o_int of sub message fields.
		Message message = parse("o_int: 1 o_sub_test_message: { o_int: 2 r_string: \"foo\" }");
		Message diffMessage1 = parse("o_int: 2 o_sub_test_message: { o_int: 2 r_string: \"foo\" }");
		Message diffMessage2 = parse("o_int: 1 o_sub_test_message: { o_int: 2 r_string: \"bar\" }");
		Message eqMessage = parse("o_int: 1 o_sub_test_message: { o_int: 3 r_string: \"foo\" }");

		FieldDescriptor fieldDescriptor = getFieldDescriptor("o_sub_test_message").getMessageType()
				.findFieldByName("o_int");
		FieldScope partialScope = FieldScopes.ignoringFieldDescriptors(fieldDescriptor);

		expectThat(diffMessage1).withPartialScope(partialScope).isNotEqualTo(message);
		expectThat(diffMessage2).withPartialScope(partialScope).isNotEqualTo(message);
		expectThat(eqMessage).withPartialScope(partialScope).isEqualTo(message);

		expectFailureWhenTesting().that(diffMessage1).withPartialScope(partialScope).isEqualTo(message);
		expectIsEqualToFailed();
		expectThatFailure().hasMessageThat().contains("modified: o_int: 1 -> 2");

		expectFailureWhenTesting().that(diffMessage2).withPartialScope(partialScope).isEqualTo(message);
		expectIsEqualToFailed();
		expectThatFailure().hasMessageThat().contains("modified: o_sub_test_message.r_string[0]: \"foo\" -> \"bar\"");
	}

	@Test
	public void testIgnoringFieldOfAnyMessage() throws Exception {
		String typeUrl = isProto3() ? "type.googleapis.com/com.google.common.truth.extensions.proto.SubTestMessage3"
				: "type.googleapis.com/com.google.common.truth.extensions.proto.SubTestMessage2";

		Message message = parse("o_int: 1 o_any_message { [" + typeUrl + "]: { o_int: 2 r_string: \"foo\" } }");
		Message diffMessage1 = parse("o_int: 2 o_any_message { [" + typeUrl + "]: { o_int: 2 r_string: \"foo\" } }");
		Message diffMessage2 = parse("o_int: 1 o_any_message { [" + typeUrl + "]: { o_int: 2 r_string: \"bar\" } }");
		Message eqMessage = parse("o_int: 1 o_any_message { [" + typeUrl + "]: { o_int: 3 r_string: \"foo\" } }");

		FieldDescriptor fieldDescriptor = getTypeRegistry().getDescriptorForTypeUrl(typeUrl).findFieldByName("o_int");
		FieldScope partialScope = FieldScopes.ignoringFieldDescriptors(fieldDescriptor);
		expectThat(diffMessage1).unpackingAnyUsing(getTypeRegistry(), getExtensionRegistry())
				.withPartialScope(partialScope).isNotEqualTo(message);
		expectThat(diffMessage2).unpackingAnyUsing(getTypeRegistry(), getExtensionRegistry())
				.withPartialScope(partialScope).isNotEqualTo(message);
		expectThat(eqMessage).unpackingAnyUsing(getTypeRegistry(), getExtensionRegistry())
				.withPartialScope(partialScope).isEqualTo(message);

		expectFailureWhenTesting().that(diffMessage1).unpackingAnyUsing(getTypeRegistry(), getExtensionRegistry())
				.withPartialScope(partialScope).isEqualTo(message);
		expectIsEqualToFailed();
		expectThatFailure().hasMessageThat().contains("modified: o_int: 1 -> 2");

		expectFailureWhenTesting().that(diffMessage2).unpackingAnyUsing(getTypeRegistry(), getExtensionRegistry())
				.withPartialScope(partialScope).isEqualTo(message);
		expectIsEqualToFailed();
		expectThatFailure().hasMessageThat().contains("modified: o_any_message.value.r_string[0]: \"foo\" -> \"bar\"");
	}

	@Test
	public void testAnyMessageComparingExpectedFieldsOnly() throws Exception {

		String typeUrl = isProto3() ? "type.googleapis.com/com.google.common.truth.extensions.proto.SubTestMessage3"
				: "type.googleapis.com/com.google.common.truth.extensions.proto.SubTestMessage2";

		Message message = parse("o_any_message { [" + typeUrl + "]: { o_int: 2 } }");
		Message eqMessage = parse("o_any_message { [" + typeUrl + "]: { o_int: 2 r_string: \"foo\" } }");
		Message diffMessage = parse("o_any_message { [" + typeUrl + "]: { o_int: 3 r_string: \"bar\" } }");

		expectThat(eqMessage).unpackingAnyUsing(getTypeRegistry(), getExtensionRegistry()).comparingExpectedFieldsOnly()
				.isEqualTo(message);
		expectThat(diffMessage).unpackingAnyUsing(getTypeRegistry(), getExtensionRegistry())
				.comparingExpectedFieldsOnly().isNotEqualTo(message);
	}

	@Test
	public void testInvalidAnyMessageComparingExpectedFieldsOnly() throws Exception {

		Message message = parse("o_any_message { type_url: 'invalid-type' value: 'abc123' }");
		Message eqMessage = parse("o_any_message { type_url: 'invalid-type' value: 'abc123' }");
		Message diffMessage = parse("o_any_message { type_url: 'invalid-type' value: 'def456' }");

		expectThat(eqMessage).unpackingAnyUsing(getTypeRegistry(), getExtensionRegistry()).comparingExpectedFieldsOnly()
				.isEqualTo(message);
		expectThat(diffMessage).unpackingAnyUsing(getTypeRegistry(), getExtensionRegistry())
				.comparingExpectedFieldsOnly().isNotEqualTo(message);
	}

	@Test
	public void testDifferentAnyMessagesComparingExpectedFieldsOnly() throws Exception {

		// 'o_int' and 'o_float' have the same field numbers in both messages. However, to compare
		// accurately, we incorporate the unpacked Descriptor type into the FieldNumberTree as well to
		// disambiguate.
		String typeUrl1 = isProto3() ? "type.googleapis.com/com.google.common.truth.extensions.proto.SubTestMessage3"
				: "type.googleapis.com/com.google.common.truth.extensions.proto.SubTestMessage2";
		String typeUrl2 = isProto3() ? "type.googleapis.com/com.google.common.truth.extensions.proto.SubSubTestMessage3"
				: "type.googleapis.com/com.google.common.truth.extensions.proto.SubSubTestMessage2";

		Message message = parse("r_any_message { [" + typeUrl1 + "]: { o_int: 2 } } r_any_message { [" + typeUrl2
				+ "]: { o_float: 3.1 } }");
		Message eqMessage = parse("r_any_message { [" + typeUrl1 + "]: { o_int: 2 o_float: 1.9 } } r_any_message { ["
				+ typeUrl2 + "]: { o_int: 5 o_float: 3.1 } }");
		Message diffMessage = parse("r_any_message { [" + typeUrl1 + "]: { o_int: 5 o_float: 3.1 } } r_any_message { ["
				+ typeUrl2 + "]: { o_int: 2 o_float: 1.9 } }");

		expectThat(eqMessage).unpackingAnyUsing(getTypeRegistry(), getExtensionRegistry()).ignoringRepeatedFieldOrder()
				.comparingExpectedFieldsOnly().isEqualTo(message);
		expectThat(diffMessage).unpackingAnyUsing(getTypeRegistry(), getExtensionRegistry())
				.ignoringRepeatedFieldOrder().comparingExpectedFieldsOnly().isNotEqualTo(message);
	}

	@Test
	public void testIgnoringAllButOneFieldOfSubMessage() {
		// Consider all of TestMessage, but none of o_sub_test_message, except
		// o_sub_test_message.o_int.
		Message message = parse("o_int: 3 o_sub_test_message: { o_int: 4 r_string: \"foo\" } "
				+ "r_sub_test_message: { o_int: 5 r_string: \"bar\" }");

		// All of these differ in a critical field.
		Message diffMessage1 = parse("o_int: 999999 o_sub_test_message: { o_int: 4 r_string: \"foo\" } "
				+ "r_sub_test_message: { o_int: 5 r_string: \"bar\" }");
		Message diffMessage2 = parse("o_int: 3 o_sub_test_message: { o_int: 999999 r_string: \"foo\" } "
				+ "r_sub_test_message: { o_int: 5 r_string: \"bar\" }");
		Message diffMessage3 = parse("o_int: 3 o_sub_test_message: { o_int: 4 r_string: \"foo\" } "
				+ "r_sub_test_message: { o_int: 999999 r_string: \"bar\" }");
		Message diffMessage4 = parse("o_int: 3 o_sub_test_message: { o_int: 4 r_string: \"foo\" } "
				+ "r_sub_test_message: { o_int: 5 r_string: \"999999\" }");

		// This one only differs in o_sub_test_message.r_string, which is ignored.
		Message eqMessage = parse("o_int: 3 o_sub_test_message: { o_int: 4 r_string: \"999999\" } "
				+ "r_sub_test_message: { o_int: 5 r_string: \"bar\" }");

		FieldScope fieldScope = FieldScopes.ignoringFields(getFieldNumber("o_sub_test_message"))
				.allowingFieldDescriptors(
						getFieldDescriptor("o_sub_test_message").getMessageType().findFieldByName("o_int"));

		expectThat(diffMessage1).withPartialScope(fieldScope).isNotEqualTo(message);
		expectThat(diffMessage2).withPartialScope(fieldScope).isNotEqualTo(message);
		expectThat(diffMessage3).withPartialScope(fieldScope).isNotEqualTo(message);
		expectThat(diffMessage4).withPartialScope(fieldScope).isNotEqualTo(message);
		expectThat(eqMessage).withPartialScope(fieldScope).isEqualTo(message);

		expectFailureWhenTesting().that(diffMessage4).withPartialScope(fieldScope).isEqualTo(message);
		expectIsEqualToFailed();
		expectThatFailure().hasMessageThat()
				.contains("modified: r_sub_test_message[0].r_string[0]: \"bar\" -> \"999999\"");

		expectFailureWhenTesting().that(eqMessage).withPartialScope(fieldScope).isNotEqualTo(message);
		expectIsNotEqualToFailed();
		expectThatFailure().hasMessageThat().contains("ignored: o_sub_test_message.r_string");
	}

	@Test
	public void testFromSetFields() {
		Message scopeMessage = parse("o_int: 1 r_string: \"x\" o_test_message: { o_int: 1 } "
				+ "r_test_message: { r_string: \"x\" } r_test_message: { o_int: 1 } "
				+ "o_sub_test_message: { o_test_message: { o_int: 1 } }");

		// 1 = compared, [2, 3] = ignored, 4 = compared and fails
		Message message = parse("o_int: 1 r_string: \"1\" o_test_message: {o_int: 1 r_string: \"2\" } "
				+ "r_test_message: { o_int: 1 r_string: \"1\" } " + "r_test_message: { o_int: 1 r_string: \"1\" } "
				+ "o_sub_test_message: { o_int: 2 o_test_message: { o_int: 1 r_string: \"2\" } }");
		Message diffMessage = parse("o_int: 4 r_string: \"4\" o_test_message: {o_int: 4 r_string: \"3\" } "
				+ "r_test_message: { o_int: 4 r_string: \"4\" } " + "r_test_message: { o_int: 4 r_string: \"4\" }"
				+ "o_sub_test_message: { r_string: \"3\" o_int: 3 " + "o_test_message: { o_int: 4 r_string: \"3\" } }");
		Message eqMessage = parse("o_int: 1 r_string: \"1\" o_test_message: {o_int: 1 r_string: \"3\" } "
				+ "r_test_message: { o_int: 1 r_string: \"1\" } " + "r_test_message: { o_int: 1 r_string: \"1\" }"
				+ "o_sub_test_message: { o_int: 3 o_test_message: { o_int: 1 r_string: \"3\" } }");

		expectThat(diffMessage).isNotEqualTo(message);
		expectThat(eqMessage).isNotEqualTo(message);

		expectThat(diffMessage).withPartialScope(FieldScopes.fromSetFields(scopeMessage)).isNotEqualTo(message);
		expectThat(eqMessage).withPartialScope(FieldScopes.fromSetFields(scopeMessage)).isEqualTo(message);

		expectFailureWhenTesting().that(diffMessage).isEqualTo(message);
		expectIsEqualToFailed();
		expectThatFailure().hasMessageThat().contains("1 -> 4");
		expectThatFailure().hasMessageThat().contains("\"1\" -> \"4\"");
		expectThatFailure().hasMessageThat().contains("2 -> 3");
		expectThatFailure().hasMessageThat().contains("\"2\" -> \"3\"");

		expectFailureWhenTesting().that(diffMessage).withPartialScope(FieldScopes.fromSetFields(scopeMessage))
				.isEqualTo(message);
		expectIsEqualToFailed();
		expectThatFailure().hasMessageThat().contains("1 -> 4");
		expectThatFailure().hasMessageThat().contains("\"1\" -> \"4\"");
		expectThatFailure().hasMessageThat().doesNotContain("2 -> 3");
		expectThatFailure().hasMessageThat().doesNotContain("\"2\" -> \"3\"");

		expectFailureWhenTesting().that(eqMessage).withPartialScope(FieldScopes.fromSetFields(scopeMessage))
				.isNotEqualTo(message);
		expectIsNotEqualToFailed();
		expectThatFailure().hasMessageThat().contains("ignored: o_test_message.r_string");
		expectThatFailure().hasMessageThat().contains("ignored: o_sub_test_message.o_int");
		expectThatFailure().hasMessageThat().contains("ignored: o_sub_test_message.o_test_message.r_string");
	}

	@Test
	public void testFromSetFields_comparingExpectedFieldsOnly() throws InvalidProtocolBufferException {

		Message message1 = parse("o_int: 1 o_double: 333 oneof_message1: { o_int: 3 o_double: 333 }");
		Message message2 = parse("o_int: 333 o_double: 1.2 oneof_message2: { o_int: 333 o_double: 3.14 }");
		Message diffMessage1 = parse("o_int: 1 oneof_message1: { o_int: 4 }");
		Message diffMessage2 = parse("o_double: 1.2 oneof_message2: { o_double: 4.14 }");
		Message eqMessage1 = parse("o_int: 1 oneof_message1: { o_int: 3 }");
		Message eqMessage2 = parse("o_double: 1.2 oneof_message2: { o_double: 3.14 }");

		expectThat(message1).comparingExpectedFieldsOnly().isEqualTo(eqMessage1);
		expectThat(message2).comparingExpectedFieldsOnly().isEqualTo(eqMessage2);
		expectFailureWhenTesting().that(message1).comparingExpectedFieldsOnly().isEqualTo(diffMessage1);
		expectFailureWhenTesting().that(message2).comparingExpectedFieldsOnly().isEqualTo(diffMessage2);

		expectThat(listOf(message1, message2)).comparingExpectedFieldsOnly().containsExactly(eqMessage1, eqMessage2);
		expectFailureWhenTesting().that(listOf(message1, message2)).comparingExpectedFieldsOnly()
				.containsExactly(diffMessage1, eqMessage2);
		expectFailureWhenTesting().that(listOf(message1, message2)).comparingExpectedFieldsOnly()
				.containsExactly(eqMessage1, diffMessage2);
	}

	@Test
	public void testFromSetFields_unknownFields() throws InvalidProtocolBufferException {
		// Make sure that merging of repeated fields, separation by tag number, and separation by
		// unknown field type all work.
		Message scopeMessage = fromUnknownFields(
				UnknownFieldSet.newBuilder().addField(333, Field.newBuilder().addFixed32(1).addFixed64(1).build())
						.addField(444,
								Field.newBuilder().addVarint(1).addLengthDelimited(ByteString.copyFrom("1", UTF_8))
										.addGroup(UnknownFieldSet.newBuilder()
												.addField(1, Field.newBuilder().addFixed32(1).build()).build())
										.addGroup(UnknownFieldSet.newBuilder()
												.addField(2, Field.newBuilder().addFixed64(1).build()).build())
										.build())
						.build());

		// 1 = compared, [2, 3] = ignored, 4 = compared and fails
		Message message = fromUnknownFields(
				UnknownFieldSet.newBuilder().addField(222, Field.newBuilder().addFixed32(2).addFixed64(2).build())
						.addField(333,
								Field.newBuilder().addFixed32(1).addFixed64(1).addVarint(2)
										.addLengthDelimited(ByteString.copyFrom("2", UTF_8))
										.addGroup(UnknownFieldSet.newBuilder()
												.addField(1, Field.newBuilder().addFixed32(2).build()).build())
										.build())
						.addField(444,
								Field.newBuilder().addFixed32(2).addFixed64(2).addVarint(1)
										.addLengthDelimited(ByteString.copyFrom("1", UTF_8))
										.addGroup(UnknownFieldSet.newBuilder()
												.addField(1, Field.newBuilder().addFixed32(1).addFixed64(2).build())
												.addField(2, Field.newBuilder().addFixed32(2).addFixed64(1).build())
												.addField(3, Field.newBuilder().addFixed32(2).build()).build())
										.build())
						.build());
		Message diffMessage = fromUnknownFields(
				UnknownFieldSet.newBuilder().addField(222, Field.newBuilder().addFixed32(3).addFixed64(3).build())
						.addField(333,
								Field.newBuilder().addFixed32(4).addFixed64(4).addVarint(3)
										.addLengthDelimited(ByteString.copyFrom("3", UTF_8))
										.addGroup(UnknownFieldSet.newBuilder()
												.addField(1, Field.newBuilder().addFixed32(3).build()).build())
										.build())
						.addField(444,
								Field.newBuilder().addFixed32(3).addFixed64(3).addVarint(4)
										.addLengthDelimited(ByteString.copyFrom("4", UTF_8))
										.addGroup(UnknownFieldSet.newBuilder()
												.addField(1, Field.newBuilder().addFixed32(4).addFixed64(3).build())
												.addField(2, Field.newBuilder().addFixed32(3).addFixed64(4).build())
												.addField(3, Field.newBuilder().addFixed32(3).build()).build())
										.build())
						.build());
		Message eqMessage = fromUnknownFields(
				UnknownFieldSet.newBuilder().addField(222, Field.newBuilder().addFixed32(3).addFixed64(3).build())
						.addField(333,
								Field.newBuilder().addFixed32(1).addFixed64(1).addVarint(3)
										.addLengthDelimited(ByteString.copyFrom("3", UTF_8))
										.addGroup(UnknownFieldSet.newBuilder()
												.addField(1, Field.newBuilder().addFixed32(3).build()).build())
										.build())
						.addField(444,
								Field.newBuilder().addFixed32(3).addFixed64(3).addVarint(1)
										.addLengthDelimited(ByteString.copyFrom("1", UTF_8))
										.addGroup(UnknownFieldSet.newBuilder()
												.addField(1, Field.newBuilder().addFixed32(1).addFixed64(3).build())
												.addField(2, Field.newBuilder().addFixed32(3).addFixed64(1).build())
												.addField(3, Field.newBuilder().addFixed32(3).build()).build())
										.build())
						.build());

		expectThat(diffMessage).isNotEqualTo(message);
		expectThat(eqMessage).isNotEqualTo(message);

		expectThat(diffMessage).withPartialScope(FieldScopes.fromSetFields(scopeMessage)).isNotEqualTo(message);
		expectThat(eqMessage).withPartialScope(FieldScopes.fromSetFields(scopeMessage)).isEqualTo(message);

		expectFailureWhenTesting().that(diffMessage).isEqualTo(message);
		expectIsEqualToFailed();
		expectThatFailure().hasMessageThat().contains("1 -> 4");
		expectThatFailure().hasMessageThat().contains("\"1\" -> \"4\"");
		expectThatFailure().hasMessageThat().contains("2 -> 3");
		expectThatFailure().hasMessageThat().contains("\"2\" -> \"3\"");

		expectFailureWhenTesting().that(diffMessage).withPartialScope(FieldScopes.fromSetFields(scopeMessage))
				.isEqualTo(message);
		expectIsEqualToFailed();
		expectThatFailure().hasMessageThat().contains("1 -> 4");
		expectThatFailure().hasMessageThat().contains("\"1\" -> \"4\"");
		expectThatFailure().hasMessageThat().doesNotContain("2 -> 3");
		expectThatFailure().hasMessageThat().doesNotContain("\"2\" -> \"3\"");

		expectFailureWhenTesting().that(eqMessage).withPartialScope(FieldScopes.fromSetFields(scopeMessage))
				.isNotEqualTo(message);
		expectIsNotEqualToFailed();
		expectThatFailure().hasMessageThat().doesNotContain("2 -> 3");
		expectThatFailure().hasMessageThat().doesNotContain("\"2\" -> \"3\"");
	}

	@Test
	public void testFieldNumbersAreRecursive() {
		// o_int is compared, r_string is not.
		Message message = parse("o_int: 1 r_string: \"foo\" r_test_message: { o_int: 2 r_string: \"bar\" }");
		Message diffMessage = parse("o_int: 2 r_string: \"bar\" r_test_message: { o_int: 1 r_string: \"foo\" }");
		Message eqMessage = parse("o_int: 1 r_string: \"bar\" r_test_message: { o_int: 2 r_string: \"foo\" }");
		int fieldNumber = getFieldNumber("o_int");
		FieldDescriptor fieldDescriptor = getFieldDescriptor("o_int");

		expectThat(diffMessage).withPartialScope(FieldScopes.allowingFields(fieldNumber)).isNotEqualTo(message);
		expectThat(eqMessage).withPartialScope(FieldScopes.allowingFields(fieldNumber)).isEqualTo(message);
		expectThat(diffMessage).withPartialScope(FieldScopes.allowingFieldDescriptors(fieldDescriptor))
				.isNotEqualTo(message);
		expectThat(eqMessage).withPartialScope(FieldScopes.allowingFieldDescriptors(fieldDescriptor))
				.isEqualTo(message);

		expectFailureWhenTesting().that(diffMessage).withPartialScope(FieldScopes.allowingFields(fieldNumber))
				.isEqualTo(message);
		expectIsEqualToFailed();
		expectThatFailure().hasMessageThat().contains("modified: o_int: 1 -> 2");
		expectThatFailure().hasMessageThat().contains("modified: r_test_message[0].o_int: 2 -> 1");

		expectFailureWhenTesting().that(eqMessage).withPartialScope(FieldScopes.allowingFields(fieldNumber))
				.isNotEqualTo(message);
		expectIsNotEqualToFailed();
		expectThatFailure().hasMessageThat().contains("ignored: r_test_message[0].r_string");
	}

	@Test
	public void testMultipleFieldNumbers() {
		Message message = parse("o_int: 1 r_string: \"x\" o_enum: TWO");
		Message diffMessage = parse("o_int: 2 r_string: \"y\" o_enum: TWO");
		Message eqMessage = parse("o_int: 1 r_string: \"x\" o_enum: ONE o_sub_test_message: { r_string: \"bar\" }");

		FieldScope fieldScope = FieldScopes.allowingFields(getFieldNumber("o_int"), getFieldNumber("r_string"));

		expectThat(diffMessage).withPartialScope(fieldScope).isNotEqualTo(message);
		expectThat(eqMessage).withPartialScope(fieldScope).isEqualTo(message);

		expectFailureWhenTesting().that(diffMessage).withPartialScope(fieldScope).isEqualTo(message);
		expectIsEqualToFailed();
		expectThatFailure().hasMessageThat().contains("modified: o_int: 1 -> 2");
		expectThatFailure().hasMessageThat().contains("modified: r_string[0]: \"x\" -> \"y\"");

		expectFailureWhenTesting().that(eqMessage).withPartialScope(fieldScope).isNotEqualTo(message);
		expectIsNotEqualToFailed();
		expectThatFailure().hasMessageThat().contains("ignored: o_enum");
		expectThatFailure().hasMessageThat().contains("ignored: o_sub_test_message");
	}

	@Test
	public void testInvalidFieldNumber() {
		Message message1 = parse("o_int: 44");
		Message message2 = parse("o_int: 33");

		try {
			assertThat(message1).ignoringFields(999).isEqualTo(message2);
			fail("Expected failure.");
		} catch (Exception expected) {
			// TODO(user): Use hasTransitiveCauseThat() if/when it becomes available.

			Throwable cause = expected;
			while (cause != null) {
				if (cause.getMessage()
						.contains("Message type " + fullMessageName() + " has no field with number 999.")) {
					break;
				} else {
					cause = cause.getCause();
				}
			}
			if (cause == null) {
				fail("No cause with field number error message.");
			}
		}
	}

	@Test
	public void testIgnoreFieldsAtDifferentLevels() {
		// Ignore all 'o_int' fields, in different ways.
		Message message = parse("o_int: 1 r_string: \"foo\" o_sub_test_message: { o_int: 2 "
				+ "o_sub_sub_test_message: { o_int: 3 r_string: \"bar\" } }");

		// Even though o_int is ignored, message presence is not.  So these all fail.
		Message diffMessage1 = parse("r_string: \"baz\"");
		Message diffMessage2 = parse("r_string: \"foo\"");
		Message diffMessage3 = parse("r_string: \"foo\" o_sub_test_message: {}");
		Message diffMessage4 = parse("r_string: \"foo\" o_sub_test_message: { o_sub_sub_test_message: {} }");

		// All of these messages are equivalent, because all o_int are ignored.
		Message eqMessage1 = parse("o_int: 111 r_string: \"foo\" o_sub_test_message: { o_int: 222 "
				+ "o_sub_sub_test_message: { o_int: 333 r_string: \"bar\" } }");
		Message eqMessage2 = parse("o_int: 1 r_string: \"foo\" o_sub_test_message: { o_int: 2 "
				+ "o_sub_sub_test_message: { o_int: 3 r_string: \"bar\" } }");
		Message eqMessage3 = parse(
				"r_string: \"foo\" o_sub_test_message: { " + "o_sub_sub_test_message: { r_string: \"bar\" } }");
		Message eqMessage4 = parse("o_int: 333 r_string: \"foo\" o_sub_test_message: { o_int: 111 "
				+ "o_sub_sub_test_message: { o_int: 222 r_string: \"bar\" } }");

		FieldDescriptor top = getFieldDescriptor("o_int");
		FieldDescriptor middle = getFieldDescriptor("o_sub_test_message").getMessageType().findFieldByName("o_int");
		FieldDescriptor bottom = getFieldDescriptor("o_sub_test_message").getMessageType()
				.findFieldByName("o_sub_sub_test_message").getMessageType().findFieldByName("o_int");

		ImmutableMap<String, FieldScope> fieldScopes = ImmutableMap.of("BASIC",
				FieldScopes.ignoringFieldDescriptors(top, middle, bottom), "CHAINED",
				FieldScopes.ignoringFieldDescriptors(top).ignoringFieldDescriptors(middle)
						.ignoringFieldDescriptors(bottom),
				"REPEATED", FieldScopes.ignoringFieldDescriptors(top, middle).ignoringFieldDescriptors(middle, bottom));

		for (String scopeName : fieldScopes.keySet()) {
			String msg = "FieldScope(" + scopeName + ")";
			FieldScope scope = fieldScopes.get(scopeName);

			expectThatWithMessage(msg, diffMessage1).withPartialScope(scope).isNotEqualTo(message);
			expectThatWithMessage(msg, diffMessage2).withPartialScope(scope).isNotEqualTo(message);
			expectThatWithMessage(msg, diffMessage3).withPartialScope(scope).isNotEqualTo(message);
			expectThatWithMessage(msg, diffMessage4).withPartialScope(scope).isNotEqualTo(message);

			expectThatWithMessage(msg, eqMessage1).withPartialScope(scope).isEqualTo(message);
			expectThatWithMessage(msg, eqMessage2).withPartialScope(scope).isEqualTo(message);
			expectThatWithMessage(msg, eqMessage3).withPartialScope(scope).isEqualTo(message);
			expectThatWithMessage(msg, eqMessage4).withPartialScope(scope).isEqualTo(message);
		}
	}

	@Test
	public void testFromSetFields_skipNulls() {
		Message message1 = parse("o_int: 1 r_string: \"foo\" r_string: \"bar\"");
		Message eqMessage1 = parse("o_int: 1 r_string: \"foo\" r_string: \"bar\"");
		Message eqIgnoredMessage1 = parse("o_int: 2 r_string: \"foo\" r_string: \"bar\"");
		Message message2 = parse("o_int: 3 r_string: \"baz\" r_string: \"qux\"");
		Message eqMessage2 = parse("o_int: 3 r_string: \"baz\" r_string: \"qux\"");
		Message eqIgnoredMessage2 = parse("o_int: 4 r_string: \"baz\" r_string: \"qux\"");

		List<Message> messages = Lists.newArrayList();
		Message nullMessage = null;
		messages.add(parse("o_int: -1"));
		messages.add(nullMessage);
		messages.add(parse("r_string: \"NaN\""));

		expectThat(listOf(message1, message2)).withPartialScope(FieldScopes.fromSetFields(messages))
				.containsExactly(eqMessage1, eqMessage2);
		expectThat(listOf(message1, message2))
				.withPartialScope(
						FieldScopes.fromSetFields(parse("o_int: -1"), nullMessage, parse("r_string: \"NaN\"")))
				.containsExactly(eqMessage1, eqMessage2);

		expectFailureWhenTesting().that(listOf(message1, message2))
				.withPartialScope(FieldScopes.fromSetFields(messages))
				.containsExactly(eqIgnoredMessage1, eqIgnoredMessage2);
		expectThatFailure().factValue("testing whether")
				.contains("is equivalent according to " + "assertThat(proto)" + ".withPartialScope("
						+ "FieldScopes.fromSetFields([" + "{o_int: -1\n}, null, {r_string: \"NaN\"\n}]))"
						+ ".isEqualTo(target)");

		expectFailureWhenTesting().that(listOf(message1, message2))
				.withPartialScope(
						FieldScopes.fromSetFields(parse("o_int: -1"), nullMessage, parse("r_string: \"NaN\"")))
				.containsExactly(eqIgnoredMessage1, eqIgnoredMessage2);
		expectThatFailure().factValue("testing whether")
				.contains("is equivalent according to " + "assertThat(proto)" + ".withPartialScope("
						+ "FieldScopes.fromSetFields([" + "{o_int: -1\n}, null, {r_string: \"NaN\"\n}]))"
						+ ".isEqualTo(target)");
	}

	@Test
	public void testFromSetFields_iterables_vacuousIfEmptyOrAllNull() {
		Message message1 = parse("o_int: 1 r_string: \"foo\" r_string: \"bar\"");
		Message eqIgnoredMessage1 = parse("o_int: 2 r_string: \"foo\" r_string: \"bar\"");
		Message message2 = parse("o_int: 3 r_string: \"baz\" r_string: \"qux\"");
		Message eqIgnoredMessage2 = parse("o_int: 4 r_string: \"baz\" r_string: \"qux\"");

		List<Message> messages = Lists.newArrayList();
		messages.add(null);
		messages.add(null);

		expectThat(listOf(message1, message2)).withPartialScope(FieldScopes.fromSetFields(ImmutableList.<Message>of()))
				.containsExactly(eqIgnoredMessage1, eqIgnoredMessage2);
		expectThat(listOf(message1, message2)).withPartialScope(FieldScopes.fromSetFields(messages))
				.containsExactly(eqIgnoredMessage1, eqIgnoredMessage2);

		expectFailureWhenTesting().that(listOf(message1, message2))
				.withPartialScope(FieldScopes.fromSetFields(ImmutableList.<Message>of()))
				.containsNoneOf(eqIgnoredMessage1, eqIgnoredMessage2);

		expectFailureWhenTesting().that(listOf(message1, message2))
				.withPartialScope(FieldScopes.fromSetFields(messages))
				.containsNoneOf(eqIgnoredMessage1, eqIgnoredMessage2);
		expectThatFailure().isNotNull();
	}

	@Test
	public void testFromSetFields_iterables_errorForDifferentMessageTypes() {
		// Don't run this test twice.
		if (!testIsRunOnce()) {
			return;
		}

		try {
			FieldScopes.fromSetFields(TestMessage2.newBuilder().setOInt(2).build(),
					TestMessage3.newBuilder().setOInt(2).build());
			fail("Expected failure.");
		} catch (RuntimeException expected) {
			expect.that(expected).hasMessageThat()
					.contains("Cannot create scope from messages with different descriptors");
			expect.that(expected).hasMessageThat().contains(TestMessage2.getDescriptor().getFullName());
			expect.that(expected).hasMessageThat().contains(TestMessage3.getDescriptor().getFullName());
		}
	}

	@Test
	public void testFromSetFields_iterables_errorIfDescriptorMismatchesSubject() {
		// Don't run this test twice.
		if (!testIsRunOnce()) {
			return;
		}

		Message message = TestMessage2.newBuilder().setOInt(1).addRString("foo").addRString("bar").build();
		Message eqMessage = TestMessage2.newBuilder().setOInt(1).addRString("foo").addRString("bar").build();

		try {
			assertThat(message).withPartialScope(FieldScopes.fromSetFields(TestMessage3.newBuilder().setOInt(2).build(),
					TestMessage3.newBuilder().addRString("foo").build())).isEqualTo(eqMessage);
			fail("Expected failure.");
		} catch (RuntimeException expected) {
			expect.that(expected).hasMessageThat().contains("Message given to FieldScopes.fromSetFields() "
					+ "does not have the same descriptor as the message being tested");
			expect.that(expected).hasMessageThat().contains(TestMessage2.getDescriptor().getFullName());
			expect.that(expected).hasMessageThat().contains(TestMessage3.getDescriptor().getFullName());
		}
	}

	@Test
	public void testFromSetFields_iterables_unionsElements() {
		Message message = parse("o_int: 1 r_string: \"foo\" r_string: \"bar\"");
		Message diffMessage1 = parse("o_int: 2 r_string: \"foo\" r_string: \"bar\"");
		Message diffMessage2 = parse("o_int: 4 r_string: \"baz\" r_string: \"qux\"");

		expectThat(listOf(message))
				.ignoringFieldScope(FieldScopes.fromSetFields(parse("o_int: 1"), parse("o_enum: TWO")))
				.containsExactly(diffMessage1);

		expectFailureWhenTesting().that(listOf(message))
				.ignoringFieldScope(FieldScopes.fromSetFields(parse("o_int: 1"), parse("o_enum: TWO")))
				.containsExactly(diffMessage2);
		expectThatFailure().isNotNull();
	}

	@Test
	public void testIterableFieldScopeMethodVariants_protoSubject() {
		Message message = parse("o_int: 1 r_string: \"foo\"");
		Message eqExceptInt = parse("o_int: 2 r_string: \"foo\"");

		expectThat(message).ignoringFields(listOf(getFieldNumber("o_int"))).isEqualTo(eqExceptInt);
		expectThat(message).reportingMismatchesOnly().ignoringFields(listOf(getFieldNumber("o_int")))
				.isEqualTo(eqExceptInt);
		expectThat(message).ignoringFieldScope(FieldScopes.allowingFields(listOf(getFieldNumber("o_int"))))
				.isEqualTo(eqExceptInt);
		expectThat(message).withPartialScope(FieldScopes.ignoringFields(listOf(getFieldNumber("o_int"))))
				.isEqualTo(eqExceptInt);
		expectThat(message).ignoringFieldDescriptors(listOf(getFieldDescriptor("o_int"))).isEqualTo(eqExceptInt);
		expectThat(message).reportingMismatchesOnly().ignoringFieldDescriptors(listOf(getFieldDescriptor("o_int")))
				.isEqualTo(eqExceptInt);
		expectThat(message)
				.ignoringFieldScope(FieldScopes.allowingFieldDescriptors(listOf(getFieldDescriptor("o_int"))))
				.isEqualTo(eqExceptInt);
		expectThat(message).withPartialScope(FieldScopes.ignoringFieldDescriptors(listOf(getFieldDescriptor("o_int"))))
				.isEqualTo(eqExceptInt);
	}

	@Test
	public void testIterableFieldScopeMethodVariants_iterableOfProtosSubject() {
		ImmutableList<Message> messages = listOf(parse("o_int: 1 r_string: \"foo\""));
		ImmutableList<Message> eqExceptInt = listOf(parse("o_int: 2 r_string: \"foo\""));

		expectThat(messages).ignoringFields(listOf(getFieldNumber("o_int"))).containsExactlyElementsIn(eqExceptInt);
		expectThat(messages).reportingMismatchesOnly().ignoringFields(listOf(getFieldNumber("o_int")))
				.containsExactlyElementsIn(eqExceptInt);
		expectThat(messages).ignoringFieldDescriptors(listOf(getFieldDescriptor("o_int")))
				.containsExactlyElementsIn(eqExceptInt);
		expectThat(messages).reportingMismatchesOnly().ignoringFieldDescriptors(listOf(getFieldDescriptor("o_int")))
				.containsExactlyElementsIn(eqExceptInt);
	}

	@Test
	public void testIterableFieldScopeMethodVariants_mapWithProtoValuesSubject() {
		ImmutableMap<String, Message> messages = ImmutableMap.of("foo", parse("o_int: 1 r_string: \"foo\""));
		ImmutableMap<String, Message> eqExceptInt = ImmutableMap.of("foo", parse("o_int: 2 r_string: \"foo\""));

		expectThat(messages).ignoringFieldsForValues(listOf(getFieldNumber("o_int")))
				.containsExactlyEntriesIn(eqExceptInt);
		expectThat(messages).reportingMismatchesOnlyForValues().ignoringFieldsForValues(listOf(getFieldNumber("o_int")))
				.containsExactlyEntriesIn(eqExceptInt);
		expectThat(messages).ignoringFieldDescriptorsForValues(listOf(getFieldDescriptor("o_int")))
				.containsExactlyEntriesIn(eqExceptInt);
		expectThat(messages).reportingMismatchesOnlyForValues()
				.ignoringFieldDescriptorsForValues(listOf(getFieldDescriptor("o_int")))
				.containsExactlyEntriesIn(eqExceptInt);
	}

	@Test
	public void testIterableFieldScopeMethodVariants_multimapWithProtoValuesSubject() {
		ImmutableMultimap<String, Message> messages = ImmutableMultimap.of("foo", parse("o_int: 1 r_string: \"foo\""));
		ImmutableMultimap<String, Message> eqExceptInt = ImmutableMultimap.of("foo",
				parse("o_int: 2 r_string: \"foo\""));

		expectThat(messages).ignoringFieldsForValues(listOf(getFieldNumber("o_int")))
				.containsExactlyEntriesIn(eqExceptInt);
		expectThat(messages).reportingMismatchesOnlyForValues().ignoringFieldsForValues(listOf(getFieldNumber("o_int")))
				.containsExactlyEntriesIn(eqExceptInt);
		expectThat(messages).ignoringFieldDescriptorsForValues(listOf(getFieldDescriptor("o_int")))
				.containsExactlyEntriesIn(eqExceptInt);
		expectThat(messages).reportingMismatchesOnlyForValues()
				.ignoringFieldDescriptorsForValues(listOf(getFieldDescriptor("o_int")))
				.containsExactlyEntriesIn(eqExceptInt);
	}
}
