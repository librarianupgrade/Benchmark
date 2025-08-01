/**
 * Copyright 2010 Molindo GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.molindo.esi4j.module.hibernate;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;

public class SimpleQueryProvider implements HibernateQueryProvider {

	private final String _hql;
	private final DetachedCriteria _criteria;

	public SimpleQueryProvider(String hql) {
		if (hql == null) {
			throw new NullPointerException("hql");
		}
		_hql = hql;
		_criteria = null;
	}

	public SimpleQueryProvider(DetachedCriteria criteria) {
		if (criteria == null) {
			throw new NullPointerException("criteria");
		}
		_criteria = criteria;
		_hql = null;
	}

	@Override
	public final Criteria createCriteria(Class<?> type, Session session) {
		return _criteria == null ? null : _criteria.getExecutableCriteria(session);
	}

	@Override
	public final Query createQuery(Class<?> type, Session session) {
		return _hql == null ? null : session.createQuery(_hql);
	}

}
