package com.github.davidmoten.rx.jdbc;

import static com.github.davidmoten.rx.jdbc.Queries.bufferedParameters;
import static com.github.davidmoten.rx.jdbc.Queries.schedule;

import java.util.List;

import rx.Observable;
import rx.Observable.OnSubscribeFunc;
import rx.Observer;
import rx.Subscription;
import rx.util.functions.Func1;

/**
 * Always emits an Observable<Integer> of size 1 containing the number of
 * affected records.
 */
final public class QueryUpdate implements Query {

	private final String sql;
	private final Observable<Parameter> parameters;
	private final QueryContext context;
	private final Observable<?> depends;

	/**
	 * Private constructor.
	 * 
	 * @param sql
	 * @param parameters
	 * @param depends
	 * @param context
	 */
	private QueryUpdate(String sql, Observable<Parameter> parameters, Observable<?> depends, QueryContext context) {
		this.sql = sql;
		this.parameters = parameters;
		this.depends = depends;
		this.context = context;
	}

	@Override
	public String sql() {
		return sql;
	}

	@Override
	public Observable<Parameter> parameters() {
		return parameters;
	}

	@Override
	public QueryContext context() {
		return context;
	}

	@Override
	public String toString() {
		return "QueryUpdate [sql=" + sql + "]";
	}

	@Override
	public Observable<?> depends() {
		return depends;
	}

	/**
	 * Returns the results of an update query. Should be an {@link Observable}
	 * of size 1 containing the number of records affected by the update (or
	 * insert) statement.
	 * 
	 * @param query
	 * @return
	 */
	public Observable<Integer> getCount() {
		return context.handlers().updateHandler().call(bufferedParameters(this).flatMap(executeOnce()));
	}

	/**
	 * Returns a {@link Func1} that itself returns the results of pushing
	 * parameters through an update query.
	 * 
	 * @param query
	 * @return
	 */
	private Func1<List<Parameter>, Observable<Integer>> executeOnce() {
		return new Func1<List<Parameter>, Observable<Integer>>() {
			@Override
			public Observable<Integer> call(final List<Parameter> params) {
				return executeOnce(params);
			}
		};
	}

	/**
	 * Returns the results of an update query. Should return an
	 * {@link Observable} of size one containing the rows affected count.
	 * 
	 * @param query
	 * @param parameters
	 * @return
	 */
	private Observable<Integer> executeOnce(final List<Parameter> parameters) {
		return Observable.create(new OnSubscribeFunc<Integer>() {
			@Override
			public Subscription onSubscribe(Observer<? super Integer> o) {
				final QueryUpdateAction action = new QueryUpdateAction(QueryUpdate.this, parameters, o);
				return schedule(QueryUpdate.this, action);
			}
		});
	}

	/**
	 * Builds a {@link QueryUpdate}.
	 */
	final public static class Builder {

		/**
		 * Standard query builder.
		 */
		private final QueryBuilder builder;

		/**
		 * Constructor.
		 * 
		 * @param sql
		 * @param db
		 */
		public Builder(String sql, Database db) {
			this.builder = new QueryBuilder(sql, db);
		}

		/**
		 * Appends the given parameters to the parameter list for the query. If
		 * there are more parameters than required for one execution of the
		 * query then more than one execution of the query will occur.
		 * 
		 * @param parameters
		 * @return this
		 */
		public <T> Builder parameters(Observable<T> parameters) {
			builder.parameters(parameters);
			return this;
		}

		/**
		 * Appends the given parameter values to the parameter list for the
		 * query. If there are more parameters than required for one execution
		 * of the query then more than one execution of the query will occur.
		 * 
		 * @param objects
		 * @return this
		 */
		public Builder parameters(Object... objects) {
			builder.parameters(objects);
			return this;
		}

		/**
		 * Appends a parameter to the parameter list for the query. If there are
		 * more parameters than required for one execution of the query then
		 * more than one execution of the query will occur.
		 * 
		 * @param value
		 * @return this
		 */
		public Builder parameter(Object value) {
			builder.parameter(value);
			return this;
		}

		/**
		 * Appends a dependency to the dependencies that have to complete their
		 * emitting before the query is executed.
		 * 
		 * @param dependency
		 * @return this
		 */
		public Builder dependsOn(Observable<?> dependency) {
			builder.dependsOn(dependency);
			return this;
		}

		/**
		 * Appends a dependency on the result of the last transaction (
		 * <code>true</code> for commit or <code>false</code> for rollback) to
		 * the dependencies that have to complete their emitting before the
		 * query is executed.
		 * 
		 * @return this
		 */
		public Builder dependsOnLastTransaction() {
			builder.dependsOnLastTransaction();
			return this;
		}

		/**
		 * Returns an {@link Observable} with the count of rows affected by the
		 * update statement.
		 * 
		 * @return
		 */
		public Observable<Integer> getCount() {
			return new QueryUpdate(builder.sql(), builder.parameters(), builder.depends(), builder.context())
					.getCount();
		}
	}
}
