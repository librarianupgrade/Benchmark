package com.amazonaws.services.dynamodb.model.transform;

import com.amazonaws.services.dynamodb.model.*;
import com.amazonaws.transform.*;
import org.codehaus.jackson.JsonToken;

import java.util.HashMap;
import java.util.Map;

public class UpdateItemRequestJsonUnmarshaller implements Unmarshaller<UpdateItemRequest, JsonUnmarshallerContext> {

	public UpdateItemRequest unmarshall(JsonUnmarshallerContext context) throws Exception {
		UpdateItemRequest request = new UpdateItemRequest();

		int originalDepth = context.getCurrentDepth();
		int targetDepth = originalDepth + 1;

		JsonToken token = context.currentToken;
		if (token == null)
			token = context.nextToken();
		while (true) {
			if (token == null)
				break;

			if (token == JsonToken.FIELD_NAME || token == JsonToken.START_OBJECT) {
				if (context.testExpression("TableName", targetDepth)) {
					context.nextToken();
					request.setTableName(
							SimpleTypeJsonUnmarshallers.StringJsonUnmarshaller.getInstance().unmarshall(context));
				}
				if (context.testExpression("Key", targetDepth)) {
					request.setKey(KeyJsonUnmarshaller.getInstance().unmarshall(context));
				}
				if (context.testExpression("Expected", targetDepth)) {
					Map<String, AttributeValue> map = new MapUnmarshaller<String, AttributeValue>(
							SimpleTypeJsonUnmarshallers.StringJsonUnmarshaller.getInstance(),
							AttributeValueJsonUnmarshaller.getInstance()).unmarshall(context);
					Map<String, ExpectedAttributeValue> expected = new HashMap<String, ExpectedAttributeValue>();
					for (String key : map.keySet()) {
						ExpectedAttributeValue value = new ExpectedAttributeValue();
						value.setValue(map.get(key));
						value.setExists(true);
						expected.put(key, value);
					}
					request.setExpected(expected);
				}
				if (context.testExpression("AttributeUpdates", targetDepth)) {
					request.setAttributeUpdates(new MapUnmarshaller<String, AttributeValueUpdate>(
							SimpleTypeJsonUnmarshallers.StringJsonUnmarshaller.getInstance(),
							AttributeValueUpdateJsonUnmarshaller.getInstance()).unmarshall(context));
				}
			} else if (token == JsonToken.END_ARRAY || token == JsonToken.END_OBJECT) {
				if (context.getCurrentDepth() <= originalDepth)
					break;
			}
			token = context.nextToken();
		}
		return request;
	}

	private static UpdateItemRequestJsonUnmarshaller instance;

	public static UpdateItemRequestJsonUnmarshaller getInstance() {
		if (instance == null)
			instance = new UpdateItemRequestJsonUnmarshaller();
		return instance;
	}
}
