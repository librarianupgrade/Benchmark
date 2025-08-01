/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.runtime.bindings;

/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/

import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.Input;
import io.cloudslang.lang.entities.bindings.prompt.Prompt;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.lang.entities.utils.ExpressionUtils;
import org.apache.commons.lang.Validate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@Component
public class InputsBinding extends AbstractBinding {

	/**
	 * Binds the inputs to a new result map
	 *
	 * @param inputs  : the inputs to bind
	 * @param context : initial context
	 * @param prompts
	 * @return : a new map with all inputs resolved (does not include initial context)
	 */
	public Map<String, Value> bindInputs(List<Input> inputs, Map<String, ? extends Value> context,
			Map<String, Value> promptContext, Set<SystemProperty> systemProperties, List<Input> missingInputs,
			boolean useEmptyValuesForPrompts, Map<String, Prompt> prompts) {
		Map<String, Value> resultContext = new LinkedHashMap<>();

		// we do not want to change original context map
		Map<String, Value> srcContext = new LinkedHashMap<>(context);

		Map<String, Value> actualPromptContext = defaultIfNull(promptContext, emptyMap());
		for (Input input : inputs) {
			// prompts might be passed from arguments
			// this is the case for step inputs
			input = overridePromptSettingIfExists(prompts, input);

			bindInput(input, srcContext, actualPromptContext, resultContext, systemProperties, missingInputs,
					useEmptyValuesForPrompts);
		}

		return resultContext;
	}

	private void bindInput(Input input, Map<String, ? extends Value> context, Map<String, Value> promptContext,
			Map<String, Value> targetContext, Set<SystemProperty> systemProperties, List<Input> missingInputs,
			boolean useEmptyValuesForPrompts) {
		Value value;

		String inputName = input.getName();
		Validate.notEmpty(inputName);
		String errorMessagePrefix = "Error binding input: '" + inputName;

		try {
			final Value promptValue = promptContext.get(inputName);

			if (nonNull(promptValue)) {
				Value valueFromContext = context.get(inputName);
				boolean sensitive = input.getValue() != null && input.getValue().isSensitive()
						|| valueFromContext != null && valueFromContext.isSensitive();
				if (!input.isPrivateInput() && sensitive) {
					value = ValueFactory.create(promptValue, true);
					promptContext.put(inputName, value);
				} else {
					value = promptValue;
				}
			} else {
				value = resolveValue(input, context, targetContext, systemProperties);
			}

			if (input.hasPrompt()) {
				if (useEmptyValuesForPrompts) {
					if (isNull(value)) {
						value = createEmptyValue(input);
					}
				} else if (isNull(promptValue)) {
					resolvePromptExpressions(input, context, targetContext, systemProperties);

					missingInputs.add(createMissingInput(input, value));
					return;
				}
			} else if (input.isRequired() && isEmpty(value)) {
				missingInputs.add(input);
				return;
			}

		} catch (Exception exc) {
			throw new RuntimeException(errorMessagePrefix + "', \n\t" + exc.getMessage(), exc);
		}

		validateStringValue(errorMessagePrefix, value);
		targetContext.put(inputName, value);
	}

	private void resolvePromptExpressions(Input input, Map<String, ? extends Value> context,
			Map<String, Value> targetContext, Set<SystemProperty> systemProperties) {
		if (input.hasPrompt()) {
			EvaluationContextHolder evaluationContextHolder = new EvaluationContextHolder(context, targetContext,
					systemProperties, input.getValue(), input.getName(), input.getFunctionDependencies());

			resolvePromptExpressions(input.getPrompt(), evaluationContextHolder);
		}

	}

	private Value resolveValue(Input input, Map<String, ? extends Value> context,
			Map<String, ? extends Value> targetContext, Set<SystemProperty> systemProperties) {
		Value value = null;
		String inputName = input.getName();
		Value valueFromContext = context.get(inputName);
		boolean sensitive = input.getValue() != null && input.getValue().isSensitive()
				|| valueFromContext != null && valueFromContext.isSensitive();
		if (!input.isPrivateInput()) {
			value = ValueFactory.create(valueFromContext, sensitive);
		}

		if (isEmpty(value)) {
			Value rawValue = input.getValue();
			String expressionToEvaluate = ExpressionUtils.extractExpression(rawValue == null ? null : rawValue.get());
			if (expressionToEvaluate != null) {
				// we do not want to change original context map
				Map<String, Value> scriptContext = new HashMap<>(context);
				if (context.containsKey(inputName)) {
					scriptContext.put(inputName, valueFromContext);
				}
				// so you can resolve previous inputs already bound
				scriptContext.putAll(targetContext);
				value = scriptEvaluator.evalExpr(expressionToEvaluate, scriptContext, systemProperties,
						input.getFunctionDependencies());
				value = ValueFactory.create(value, sensitive);
			} else if ((value == null && rawValue != null)
					|| (containsEmptyStringOrNull(value) && doesNotContainNull(rawValue))) {
				value = rawValue;
			}
		}

		return value;
	}

	private boolean containsEmptyStringOrNull(Value value) {
		return value != null && (value.get() == null || value.get().equals(""));
	}

	private boolean doesNotContainNull(Value value) {
		return value != null && value.get() != null;
	}

	private boolean isEmpty(Value value) {
		return value == null || value.get() == null || value.get().equals("");
	}

	private Input createMissingInput(Input input, Value value) {
		return new Input.InputBuilder(input, value).build();
	}

	private Value createEmptyValue(Input input) {
		return ValueFactory.create(EMPTY, input.isSensitive());
	}

	private Input overridePromptSettingIfExists(Map<String, Prompt> prompts, Input input) {
		if (prompts.containsKey(input.getName())) {
			return new Input.InputBuilder(input, input.getValue()).withPrompt(prompts.get(input.getName())).build();
		} else {
			return input;
		}
	}

}
