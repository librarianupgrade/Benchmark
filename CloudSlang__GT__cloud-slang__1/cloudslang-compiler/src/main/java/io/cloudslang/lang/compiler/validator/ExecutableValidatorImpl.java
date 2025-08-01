/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.validator;

import io.cloudslang.lang.compiler.parser.model.ParsedSlang;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.cloudslang.lang.compiler.utils.SlangSourceUtils.getNavigationStepName;

/**
 * @author Bonczidai Levente
 * @since 8/24/2016
 */
public class ExecutableValidatorImpl extends AbstractValidator implements ExecutableValidator {

	private SystemPropertyValidator systemPropertyValidator;

	public void setSystemPropertyValidator(SystemPropertyValidator systemPropertyValidator) {
		this.systemPropertyValidator = systemPropertyValidator;
	}

	public SystemPropertyValidator getSystemPropertyValidator() {
		return systemPropertyValidator;
	}

	@Override
	public void validateNamespace(ParsedSlang parsedSlang) {
		String namespace = parsedSlang.getNamespace();
		ParsedSlang.Type executableType = parsedSlang.getType();
		switch (executableType) {
		case SYSTEM_PROPERTY_FILE:
			getSystemPropertyValidator().validateNamespace(namespace);
			break;
		case FLOW:
		case OPERATION:
		case DECISION:
			// namespace cannot be empty
			if (StringUtils.isEmpty(namespace)) {
				throw new RuntimeException("For source[" + parsedSlang.getName() + "] namespace cannot be empty.");
			} else {
				validateNamespaceRules(namespace);
			}
			break;
		default:
			throw new RuntimeException("Not yet implemented");
		}
	}

	@Override
	public void validateImportsSection(ParsedSlang parsedSlang) {
		Map<String, String> imports = parsedSlang.getImports();
		ParsedSlang.Type executableType = parsedSlang.getType();
		switch (executableType) {
		case FLOW:
			break;
		case OPERATION:
		case DECISION:
		case SYSTEM_PROPERTY_FILE:
			if (MapUtils.isNotEmpty(imports)) {
				throw new RuntimeException("Type[" + executableType.name() + "] cannot have imports section");
			}
			break;
		default:
			throw new RuntimeException("Not yet implemented");
		}
		if (MapUtils.isNotEmpty(imports)) {
			Set<Map.Entry<String, String>> entrySet = imports.entrySet();
			for (Map.Entry<String, String> entry : entrySet) {
				String alias = entry.getKey();
				String namespace = entry.getValue();
				validateNamespaceRules(namespace);
				validateSimpleNameRules(alias);
			}
		}
	}

	@Override
	public void validateStepReferenceId(String referenceId) {
		if (StringUtils.isEmpty(referenceId)) {
			throw new RuntimeException("Reference ID cannot be empty");
		}
		validateNamespaceRules(referenceId);
	}

	@Override
	public void validateExecutableName(String executableName) {
		validateSimpleNameRules(executableName);
	}

	@Override
	public void validateStepName(String stepName) {
		validateSimpleNameRules(stepName);
	}

	@Override
	public void validateResultName(String resultName) {
		validateResultNameRules(resultName);
	}

	@Override
	public void validateNavigationStrings(List<Map<String, Serializable>> navigationStrings) {
		for (Map<String, Serializable> element : navigationStrings) {
			Map.Entry<String, Serializable> navigation = element.entrySet().iterator().next();
			String navigationKey = navigation.getKey();
			Serializable navigationValue = navigation.getValue();
			validateNavigationKey(navigationKey);
			validateNavigationValue(navigationValue);
		}

	}

	@Override
	public void validateBreakKeys(List<String> breakKeys) {
		for (String breakOn : breakKeys) {
			validateResultNameRules(breakOn);
		}
	}

	@Override
	public void validateInputName(String name) {
		validateInOutName(name);
	}

	@Override
	public void validateOutputName(String name) {
		validateInOutName(name);
	}

	private void validateInOutName(String name) {
		try {
			validateVariableNameRules(name);
		} catch (RuntimeException e) {
			throw new RuntimeException(PreCompileValidator.VALIDATION_ERROR + e.getMessage(), e);
		}
	}

	@Override
	public void validateLoopStatementVariable(String name) {
		validateVariableNameRules(name);
	}

	private void validateNavigationKey(String navigationKey) {
		validateResultNameRules(navigationKey);
	}

	private void validateNavigationValue(Serializable navigationValue) {
		try {
			validateStepName(getNavigationStepName(navigationValue));
		} catch (RuntimeException rex) {
			validateResultName(getNavigationStepName(navigationValue));
		}
	}

}
