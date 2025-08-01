/**
 * Copyright © 2015 Mercateo AG (http://www.mercateo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mercateo.common.rest.schemagen.plugin;

import java.lang.reflect.Field;
import java.util.function.BiPredicate;

import com.mercateo.common.rest.schemagen.parameter.CallContext;

/**
 * this class checks, if a field of a bean should be contained in the schema
 * 
 * @author joerg_adler
 *
 */
public interface FieldCheckerForSchema extends BiPredicate<Field, CallContext> {
	static FieldCheckerForSchema fromBiPredicate(BiPredicate<Field, CallContext> predicate) {
		return predicate::test;
	}
}
