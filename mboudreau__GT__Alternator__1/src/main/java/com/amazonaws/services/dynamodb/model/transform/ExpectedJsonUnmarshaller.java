package com.amazonaws.services.dynamodb.model.transform;

import com.amazonaws.services.dynamodb.model.AttributeValue;
import com.amazonaws.services.dynamodb.model.ExpectedAttributeValue;
import com.amazonaws.services.dynamodb.model.ProvisionedThroughput;
import com.amazonaws.transform.JsonUnmarshallerContext;
import com.amazonaws.transform.SimpleTypeJsonUnmarshallers;
import com.amazonaws.transform.Unmarshaller;
import org.codehaus.jackson.JsonToken;

import java.util.HashMap;
import java.util.Map;

public class ExpectedJsonUnmarshaller
		implements Unmarshaller<Map<String, ExpectedAttributeValue>, JsonUnmarshallerContext> {

	public Map<String, ExpectedAttributeValue> unmarshall(JsonUnmarshallerContext context) throws Exception {
		ProvisionedThroughput request = new ProvisionedThroughput();

		int originalDepth = context.getCurrentDepth();
		int targetDepth = originalDepth + 1;

		JsonToken token = context.currentToken;
		if (token == null)
			token = context.nextToken();

		while (true) {
			if (token == null)
				break;
			if (token == JsonToken.FIELD_NAME || token == JsonToken.START_OBJECT) {
				if (context.testExpression("ReadCapacityUnits", targetDepth)) {
					context.nextToken();
					request.setReadCapacityUnits(
							SimpleTypeJsonUnmarshallers.LongJsonUnmarshaller.getInstance().unmarshall(context));
				}
				if (context.testExpression("WriteCapacityUnits", targetDepth)) {
					context.nextToken();
					request.setWriteCapacityUnits(
							SimpleTypeJsonUnmarshallers.LongJsonUnmarshaller.getInstance().unmarshall(context));
				}
			} else if (token == JsonToken.END_ARRAY || token == JsonToken.END_OBJECT) {
				if (context.getCurrentDepth() <= originalDepth)
					break;
			}
			token = context.nextToken();
		}

		return new HashMap<String, ExpectedAttributeValue>();
	}

	private static ExpectedJsonUnmarshaller instance;

	public static ExpectedJsonUnmarshaller getInstance() {
		if (instance == null)
			instance = new ExpectedJsonUnmarshaller();
		return instance;
	}
}
