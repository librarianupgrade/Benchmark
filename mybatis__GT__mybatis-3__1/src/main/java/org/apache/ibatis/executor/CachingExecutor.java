/*
 *    Copyright 2009-2014 the original author or authors.
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
package org.apache.ibatis.executor;

import java.sql.SQLException;
import java.util.List;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.cache.TransactionalCacheManager;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;

/**
 * @author Clinton Begin
 * @author Eduardo Macarron
 */
public class CachingExecutor implements Executor {

	private Executor delegate;
	private TransactionalCacheManager tcm = new TransactionalCacheManager();

	public CachingExecutor(Executor delegate) {
		this.delegate = delegate;
		delegate.setExecutorWrapper(this);
	}

	public Transaction getTransaction() {
		return delegate.getTransaction();
	}

	public void close(boolean forceRollback) {
		try {
			//issues #499, #524 and #573
			if (forceRollback) {
				tcm.rollback();
			} else {
				tcm.commit();
			}
		} finally {
			delegate.close(forceRollback);
		}
	}

	public boolean isClosed() {
		return delegate.isClosed();
	}

	public int update(MappedStatement ms, Object parameterObject) throws SQLException {
		flushCacheIfRequired(ms);
		return delegate.update(ms, parameterObject);
	}

	public <E> List<E> query(MappedStatement ms, Object parameterObject, RowBounds rowBounds,
			ResultHandler resultHandler) throws SQLException {
		BoundSql boundSql = ms.getBoundSql(parameterObject);
		CacheKey key = createCacheKey(ms, parameterObject, rowBounds, boundSql);
		return query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
	}

	public <E> List<E> query(MappedStatement ms, Object parameterObject, RowBounds rowBounds,
			ResultHandler resultHandler, CacheKey key, BoundSql boundSql) throws SQLException {
		Cache cache = ms.getCache();
		if (cache != null) {
			flushCacheIfRequired(ms);
			if (ms.isUseCache() && resultHandler == null) {
				ensureNoOutParams(ms, parameterObject, boundSql);
				@SuppressWarnings("unchecked")
				List<E> list = (List<E>) tcm.getObject(cache, key);
				if (list == null) {
					try {
						list = delegate.<E>query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
					} finally {
						tcm.putObject(cache, key, list); // issue #578 and #116
					}
				}
				return list;
			}
		}
		return delegate.<E>query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
	}

	public List<BatchResult> flushStatements() throws SQLException {
		return delegate.flushStatements();
	}

	public void commit(boolean required) throws SQLException {
		delegate.commit(required);
		tcm.commit();
	}

	public void rollback(boolean required) throws SQLException {
		try {
			delegate.rollback(required);
		} finally {
			if (required) {
				tcm.rollback();
			}
		}
	}

	private void ensureNoOutParams(MappedStatement ms, Object parameter, BoundSql boundSql) {
		if (ms.getStatementType() == StatementType.CALLABLE) {
			for (ParameterMapping parameterMapping : boundSql.getParameterMappings()) {
				if (parameterMapping.getMode() != ParameterMode.IN) {
					throw new ExecutorException(
							"Caching stored procedures with OUT params is not supported.  Please configure useCache=false in "
									+ ms.getId() + " statement.");
				}
			}
		}
	}

	public CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql) {
		return delegate.createCacheKey(ms, parameterObject, rowBounds, boundSql);
	}

	public boolean isCached(MappedStatement ms, CacheKey key) {
		return delegate.isCached(ms, key);
	}

	public void deferLoad(MappedStatement ms, MetaObject resultObject, String property, CacheKey key,
			Class<?> targetType) {
		delegate.deferLoad(ms, resultObject, property, key, targetType);
	}

	public void clearLocalCache() {
		delegate.clearLocalCache();
	}

	private void flushCacheIfRequired(MappedStatement ms) {
		Cache cache = ms.getCache();
		if (cache != null && ms.isFlushCacheRequired()) {
			tcm.clear(cache);
		}
	}

	@Override
	public void setExecutorWrapper(Executor executor) {
		throw new UnsupportedOperationException("This method should not be called");
	}

}
