/*
   Copyright 2015 Immutables Authors and Contributors

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
package org.immutables.func;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Generates projection Function and predicates for attributes. Useful for pre-java 8
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.CLASS)
public @interface Functional {
	/**
	 * Place on non-accessor methods of abstract value type to
	 * generate function to which parameters can be bound.
	 * 
	 * <pre>
	 * &#064;Value.Immutable
	 * &#064;Functional
	 * public abstract class Xyz {
	 *   &#064;Value.Parameter
	 *   public abstract String getX();
	 * 
	 *   &#064;Functional.BindParameters
	 *   public String computeZ(String y) {
	 *     return getX() + y;
	 *   }
	 * }
	 * ...
	 * // Generated function
	 * public static Function<Xyz, String> computeZ(String y) {
	 *   return new Function<Xyz, String>() {
	 *     &#064;Override
	 *     public String apply(Xyz input) {
	 *       return input.computeZ(y);
	 *     }
	 *     &#064;Override
	 *     public String toString() {
	 *       return "XyzFunctions.computeZ(y)";
	 *     }
	 *   }
	 * }
	 * ...
	 * // Use as
	 * Function<Xyz, String> fn = XyzFunctions.computeZ("Y");
	 * </pre>
	 */
	@Target({ ElementType.METHOD })
	@Retention(RetentionPolicy.CLASS)
	public @interface BindParameters {
	}
}
