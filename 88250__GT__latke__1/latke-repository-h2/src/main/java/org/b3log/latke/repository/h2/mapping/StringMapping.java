/*
 * Copyright (c) 2009, 2010, 2011, B3log Team
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
package org.b3log.latke.repository.h2.mapping;

import org.b3log.latke.repository.jdbc.mapping.Mapping;
import org.b3log.latke.repository.jdbc.util.FieldDefinition;

/**
 * H2 string type mapping.
 * 
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Dec 27, 2012
 */
public final class StringMapping implements Mapping {

	@Override
	public String toDataBaseSting(final FieldDefinition definition) {
		final StringBuilder sql = new StringBuilder();

		sql.append(definition.getName());

		if (definition.getLength() == null) {
			definition.setLength(new Integer("0"));
		}

		if (definition.getLength() > new Integer("1024")) {
			sql.append(" text");
		} else {
			sql.append(" varchar(").append(definition.getLength() < 1 ? new Integer("100") : definition.getLength());
			sql.append(")");

			if (!definition.getNullable()) {
				sql.append(" not null");
			}
		}

		return sql.toString();
	}
}
