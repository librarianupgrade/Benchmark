/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.modeller.transformers;

import io.cloudslang.lang.entities.PromptType;
import io.cloudslang.lang.entities.bindings.InOutParam;
import io.cloudslang.lang.entities.bindings.prompt.Prompt;

import java.io.Serializable;
import java.util.Map;

import static io.cloudslang.lang.compiler.SlangTextualKeys.PROMPT_DELIMITER_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.PROMPT_MESSAGE_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.PROMPT_OPTIONS_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.PROMPT_TYPE_KEY;
import static java.util.Optional.ofNullable;

/**
 * @author Bonczidai Levente
 * @since 1/25/2016
 */
public abstract class InOutTransformer extends AbstractInOutForTransformer {

	private static final String DEFAULT_PROMPT_MESSAGE = "Enter a value for '%s'";

	public abstract Class<? extends InOutParam> getTransformedObjectsClass();

	protected Prompt extractPrompt(String inputName, Map<String, String> promptSettings) {
		final PromptType type = ofNullable(promptSettings.get(PROMPT_TYPE_KEY)).map(PromptType::fromString)
				.orElse(PromptType.TEXT);

		final String message = ofNullable(promptSettings.get(PROMPT_MESSAGE_KEY))
				.orElseGet(() -> String.format(DEFAULT_PROMPT_MESSAGE, inputName));

		return new Prompt.PromptBuilder().setPromptType(type).setPromptMessage(message)
				.setPromptOptions(promptSettings.get(PROMPT_OPTIONS_KEY))
				.setPromptDelimiter(promptSettings.get(PROMPT_DELIMITER_KEY)).build();
	}

	protected Accumulator getDependencyAccumulator(Serializable value, Prompt prompt) {
		String messageValue = null;
		String options = null;
		String delimiter = null;
		if (prompt != null) {
			messageValue = prompt.getPromptMessage();
			options = prompt.getPromptOptions();
			delimiter = prompt.getPromptDelimiter();
		}

		return extractFunctionData(value, messageValue, options, delimiter);
	}

}
