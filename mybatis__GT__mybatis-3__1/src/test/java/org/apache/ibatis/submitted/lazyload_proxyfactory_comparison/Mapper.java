/*
 *    Copyright 2009-2012 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.submitted.lazyload_proxyfactory_comparison;

import org.apache.ibatis.annotations.Param;

public interface Mapper {
	UserWithGetObjectWithInterface getUserWithGetObjectWithInterface(@Param("id") Integer id);

	UserWithGetObjectWithoutInterface getUserWithGetObjectWithoutInterface(@Param("id") Integer id);

	UserWithGetXxxWithInterface getUserWithGetXxxWithInterface(@Param("id") Integer id);

	UserWithGetXxxWithoutInterface getUserWithGetXxxWithoutInterface(@Param("id") Integer id);

	UserWithNothingWithInterface getUserWithNothingWithInterface(@Param("id") Integer id);

	UserWithNothingWithoutInterface getUserWithNothingWithoutInterface(@Param("id") Integer id);
}
