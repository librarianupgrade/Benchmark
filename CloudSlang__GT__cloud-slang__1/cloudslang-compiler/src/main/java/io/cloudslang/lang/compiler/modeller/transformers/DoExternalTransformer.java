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

import io.cloudslang.lang.compiler.SlangTextualKeys;

public class DoExternalTransformer extends DoTransformer {

	@Override
	public String keyToTransform() {
		return SlangTextualKeys.DO_EXTERNAL_KEY;
	}

	@Override
	public Type getType() {
		return Type.EXTERNAL;
	}
}
