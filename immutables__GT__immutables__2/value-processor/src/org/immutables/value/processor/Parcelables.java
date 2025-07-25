/*
   Copyright 2017 Immutables Authors and Contributors

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

import org.immutables.generator.Generator;
import org.immutables.generator.Templates;
import org.immutables.value.processor.encode.Generator_Renderers;
import org.immutables.value.processor.encode.Renderers;
import org.immutables.value.processor.meta.ValueAttribute;
import org.immutables.value.processor.meta.ValueType;

/**
 * The Class Parcelables.
 */
@Generator.Template
public abstract class Parcelables extends AbstractValuesTemplate {
	@Generator.Typedef
	ValueAttribute Attribute;
	@Generator.Typedef
	ValueType Type;

	// renderers for encoding elements
	final Renderers renderers = new Generator_Renderers();

	public abstract Templates.Invokable generate();
}
