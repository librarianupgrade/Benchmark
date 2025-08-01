package com.michelboudreau.alternator.validators;

import com.amazonaws.services.dynamodb.model.DeleteTableRequest;
import com.amazonaws.services.dynamodb.model.DescribeTableRequest;
import com.amazonaws.services.dynamodb.model.ListTablesRequest;
import com.michelboudreau.alternator.validation.Validator;
import com.michelboudreau.alternator.validation.ValidatorUtils;

import java.util.ArrayList;
import java.util.List;

public class DeleteTableRequestValidator extends Validator {

	public Boolean supports(Class clazz) {
		return DescribeTableRequest.class.isAssignableFrom(clazz);
	}

	public List<Error> validate(Object target) {
		DeleteTableRequest instance = (DeleteTableRequest) target;
		List<Error> errors = ValidatorUtils.rejectIfNullOrEmptyOrWhitespace(instance.getTableName());
		if (instance.getTableName() != null) {
			errors.addAll(ValidatorUtils.invokeValidator(new TableNameValidator(), instance.getTableName()));
		}
		return removeNulls(errors);
	}
}
