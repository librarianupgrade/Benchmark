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
package com.mercateo.common.rest.schemagen.types;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.stream.Collectors;

import com.mercateo.common.rest.schemagen.JsonHyperSchema;

public class ListResponseBuilder<ElementIn, ElementOut> extends
		ResponseBuilderAbstract<ListResponseBuilder<ElementIn, ElementOut>, ElementIn, ElementOut, ListResponse<ElementOut>> {
	private List<ElementIn> list;

	/**
	 * @deprecated please use {@link ListResponse#builder()} instead
	 */
	@Deprecated
	public ListResponseBuilder() {
	}

	@Override
	public ListResponse<ElementOut> build() {
		requireNonNull(list);
		requireNonNull(elementMapper);
		requireNonNull(containerLinks);

		List<ObjectWithSchema<ElementOut>> mappedList = list.stream().map(elementMapper).collect(Collectors.toList());

		JsonHyperSchema schema = JsonHyperSchema.from(containerLinks);
		return ListResponse.create(mappedList, schema);
	}

	public ListResponseBuilder<ElementIn, ElementOut> withList(List<ElementIn> list) {
		this.list = list;
		return this;
	}
}
