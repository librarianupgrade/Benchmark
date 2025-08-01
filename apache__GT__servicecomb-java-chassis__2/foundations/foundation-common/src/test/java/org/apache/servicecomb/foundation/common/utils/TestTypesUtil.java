/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicecomb.foundation.common.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestTypesUtil {
	@Test
	public void testTypesUtil() {
		Assertions.assertEquals(double.class, TypesUtil.wrapperTypeToPrimitive(Double.class));
		Assertions.assertEquals(Float.class, TypesUtil.primitiveTypeToWrapper(float.class));
		Assertions.assertEquals(TypesUtil.PRIMITIVE_CHAR,
				TypesUtil.wrapperJavaTypeToPrimitive(TypesUtil.PRIMITIVE_WRAPPER_CHAR));
		Assertions.assertEquals(TypesUtil.PRIMITIVE_WRAPPER_BYTE,
				TypesUtil.primitiveJavaTypeToWrapper(TypesUtil.PRIMITIVE_BYTE));
	}
}
