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

/*
 * Created by orius123 on 05/11/14.
 */

import io.cloudslang.lang.compiler.CompilerConstants;
import io.cloudslang.lang.compiler.modeller.result.TransformModellingResult;
import io.cloudslang.lang.entities.SensitivityLevel;
import io.cloudslang.lang.entities.bindings.Argument;
import io.cloudslang.lang.entities.bindings.Output;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PublishTransformer extends AbstractOutputsTransformer implements Transformer<List<Object>, List<Output>> {

	private Type type = Type.INTERNAL;

	@Override
	public TransformModellingResult<List<Output>> transform(List<Object> rawData) {
		return transform(rawData, CompilerConstants.DEFAULT_SENSITIVITY_LEVEL);
	}

	@Override
	public TransformModellingResult<List<Output>> transform(List<Object> rawData, SensitivityLevel sensitivityLevel) {
		return super.transform(rawData, sensitivityLevel);
	}

	@Override
	public List<Scope> getScopes() {
		return Collections.singletonList(Scope.AFTER_STEP);
	}

	@Override
	public String keyToTransform() {
		return null;
	}

	@Override
	void handleOutputProperties(List<Output> transformedData, Map.Entry<String, ?> entry, List<RuntimeException> errors,
			SensitivityLevel sensitivityLevel, List<Argument> arguments) {
		addOutput(transformedData,
				createPropOutput((Map.Entry<String, Map<String, Serializable>>) entry, sensitivityLevel, arguments),
				errors);
	}

	@Override
	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}
}
