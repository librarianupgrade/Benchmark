/*
   Copyright 2014-2018 Immutables Authors and Contributors

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.immutables.value.processor;

import com.google.common.collect.Multimap;
import org.immutables.generator.Templates;
import org.immutables.value.processor.meta.Proto.DeclaringPackage;
import org.immutables.value.processor.meta.ValueType;

abstract class ValuesTemplate extends AbstractValuesTemplate {
	public abstract Templates.Invokable generate();

	Multimap<DeclaringPackage, ValueType> values;

	ValuesTemplate usingValues(Multimap<DeclaringPackage, ValueType> values) {
		this.values = values;
		return this;
	}
}
