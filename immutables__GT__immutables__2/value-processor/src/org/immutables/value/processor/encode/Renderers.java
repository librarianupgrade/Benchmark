/*
   Copyright 2016 Immutables Authors and Contributors

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
package org.immutables.value.processor.encode;

import org.immutables.generator.Generator;
import org.immutables.generator.Templates;
import org.immutables.value.processor.AbstractValuesTemplate;

@Generator.Template
public abstract class Renderers extends AbstractValuesTemplate {
	@Generator.Typedef
	Instantiation Inst;

	@Generator.Typedef
	Templates.Invokable Invokable;

	@Generator.Typedef
	Code.Interpolator Interpolator;

	@Generator.Typedef
	EncodedElement Elem;

	@Generator.Typedef
	EncodingInfo Enc;

	@Generator.Typedef
	Code.Term Term;

	public abstract Templates.Invokable declareFields();

	public abstract Templates.Invokable defaultValue();

	public abstract Templates.Invokable staticFields();

	public abstract Templates.Invokable staticMethods();

	public abstract Templates.Invokable builderFields();

	public abstract Templates.Invokable builderInit();

	public abstract Templates.Invokable virtualImpl();

	public abstract Templates.Invokable constructorAcceptType();

	public abstract Templates.Invokable builderStaticFields();

	public abstract Templates.Invokable builderHelperMethods();

	public abstract Templates.Invokable valueHelperMethods();

	public abstract Templates.Invokable copyMethods();

	public abstract Templates.Invokable accessor();

	public abstract Templates.Invokable string();

	public abstract Templates.Invokable hash();

	public abstract Templates.Invokable equals();

	public abstract Templates.Invokable from();

	public abstract Templates.Invokable implType();

	public abstract Templates.Invokable fromBuild();

	public abstract Templates.Invokable wasInit();

	public abstract Templates.Invokable builderCopyFrom();

	public abstract Templates.Invokable unitializedFieldValue();

	public abstract Templates.Invokable shimFields();

	public abstract Templates.Invokable shimAssign();

	public abstract Templates.Invokable shimAccessor();

	public abstract Templates.Invokable shimAssignExtract();

	public abstract Templates.Invokable derivedAssign();
}
