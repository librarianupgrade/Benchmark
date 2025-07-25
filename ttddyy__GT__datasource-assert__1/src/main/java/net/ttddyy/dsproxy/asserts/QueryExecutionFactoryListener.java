package net.ttddyy.dsproxy.asserts;

import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.StatementType;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;
import net.ttddyy.dsproxy.proxy.ParameterKey;
import net.ttddyy.dsproxy.proxy.ParameterSetOperation;

import java.util.ArrayList;
import java.util.List;

import static net.ttddyy.dsproxy.asserts.ParameterKeyValueUtils.createRegisterOut;
import static net.ttddyy.dsproxy.asserts.ParameterKeyValueUtils.createSetNull;
import static net.ttddyy.dsproxy.asserts.ParameterKeyValueUtils.createSetParam;

/**
 * @author Tadaya Tsuyukubo
 * @since 1.0
 */
public class QueryExecutionFactoryListener implements QueryExecutionListener {

	private List<QueryExecution> queryExecutions = new ArrayList<>();

	public QueryExecutionFactoryListener() {
	}

	@Override
	public void beforeQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
		// no-op
	}

	@Override
	public void afterQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
		StatementType statementType = execInfo.getStatementType();
		boolean isBatch = execInfo.isBatch();

		BaseQueryExecution queryExecution = null;
		switch (statementType) {
		case STATEMENT:
			if (isBatch) {
				queryExecution = createStatementBatchExecution(queryInfoList);
			} else {
				queryExecution = createStatementExecution(queryInfoList);
			}
			break;
		case PREPARED:
			if (isBatch) {
				queryExecution = createPreparedBatchExecution(queryInfoList);
			} else {
				queryExecution = createPreparedExecution(queryInfoList);
			}
			break;
		case CALLABLE:
			if (isBatch) {
				queryExecution = createCallableBatchExecution(queryInfoList);
			} else {
				queryExecution = createCallableExecution(queryInfoList);
			}
			break;
		}
		queryExecution.setSuccess(execInfo.isSuccess());

		this.queryExecutions.add(queryExecution);
	}

	private StatementExecution createStatementExecution(List<QueryInfo> queryInfoList) {
		String query = queryInfoList.get(0).getQuery();

		StatementExecution se = new StatementExecution();
		se.setQuery(query);
		return se;
	}

	private StatementBatchExecution createStatementBatchExecution(List<QueryInfo> queryInfoList) {
		StatementBatchExecution sbe = new StatementBatchExecution();
		for (QueryInfo queryInfo : queryInfoList) {
			sbe.getQueries().add(queryInfo.getQuery());
		}
		return sbe;
	}

	private PreparedExecution createPreparedExecution(List<QueryInfo> queryInfoList) {
		String query = queryInfoList.get(0).getQuery();

		PreparedExecution pe = new PreparedExecution();
		pe.setQuery(query);

		if (queryInfoList.size() != 1) {
			throw new DataSourceAssertException(
					"queryInfoList size for PreparedStatement must be 1 but was " + queryInfoList.size());
		}

		QueryInfo queryInfo = queryInfoList.get(0);

		if (queryInfo.getParametersList().size() != 1) {
			throw new DataSourceAssertException("parametersList size for PreparedStatement must be 1 but was "
					+ queryInfo.getParametersList().size());
		}

		List<ParameterSetOperation> params = queryInfo.getParametersList().get(0);

		populateParameterSetOperations(pe, params);

		return pe;
	}

	private PreparedBatchExecution createPreparedBatchExecution(List<QueryInfo> queryInfoList) {
		String query = queryInfoList.get(0).getQuery();

		PreparedBatchExecution pbe = new PreparedBatchExecution();
		pbe.setQuery(query);

		if (queryInfoList.size() != 1) {
			throw new DataSourceAssertException(
					"queryInfoList size for batch PreparedStatement must be 1 but was " + queryInfoList.size());
		}

		QueryInfo queryInfo = queryInfoList.get(0);

		for (List<ParameterSetOperation> params : queryInfo.getParametersList()) {
			PreparedBatchExecutionEntry batchEntry = new PreparedBatchExecutionEntry();
			populateParameterSetOperations(batchEntry, params);
			pbe.addBatchExecutionEntry(batchEntry);
		}

		return pbe;
	}

	private CallableExecution createCallableExecution(List<QueryInfo> queryInfoList) {
		String query = queryInfoList.get(0).getQuery();

		CallableExecution ce = new CallableExecution();
		ce.setQuery(query);

		if (queryInfoList.size() != 1) {
			throw new DataSourceAssertException(
					"queryInfoList size for CallableStatement must be 1 but was " + queryInfoList.size());
		}

		QueryInfo queryInfo = queryInfoList.get(0);

		if (queryInfo.getParametersList().size() != 1) {
			throw new DataSourceAssertException("parametersList size for CallableStatement must be 1 but was "
					+ queryInfo.getParametersList().size());
		}

		List<ParameterSetOperation> params = queryInfo.getParametersList().get(0);

		populateParameterSetOperations(ce, params);

		return ce;
	}

	private CallableBatchExecution createCallableBatchExecution(List<QueryInfo> queryInfoList) {
		String query = queryInfoList.get(0).getQuery();

		CallableBatchExecution cbe = new CallableBatchExecution();
		cbe.setQuery(query);

		if (queryInfoList.size() != 1) {
			throw new DataSourceAssertException(
					"queryInfoList size for batch CallableStatement must be 1 but was " + queryInfoList.size());
		}

		QueryInfo queryInfo = queryInfoList.get(0);

		for (List<ParameterSetOperation> params : queryInfo.getParametersList()) {
			CallableBatchExecutionEntry batchEntry = new CallableBatchExecutionEntry();
			populateParameterSetOperations(batchEntry, params);
			cbe.addBatchExecutionEntry(batchEntry);
		}

		return cbe;
	}

	private void populateParameterSetOperations(ParameterHolder holder, List<ParameterSetOperation> params) {
		for (ParameterSetOperation param : params) {
			populateParameterSetOperation(holder, param);
		}
	}

	private void populateParameterSetOperation(ParameterHolder holder, ParameterSetOperation setOperation) {
		Object[] args = setOperation.getArgs();
		Object key = args[0];
		Object value = args[1]; // use second arg as value for the parameter-set-operation

		ParameterKeyValue keyValue;
		if (ParameterSetOperation.isSetNullParameterOperation(setOperation)) {
			ParameterKey parameterKey = getParameterKey(key);
			keyValue = createSetNull(parameterKey, (Integer) value);
		} else if (ParameterSetOperation.isRegisterOutParameterOperation(setOperation)) {
			ParameterKey parameterKey = getParameterKey(key);
			keyValue = createRegisterOut(parameterKey, value);
		} else {
			ParameterKey parameterKey = getParameterKey(key);
			keyValue = createSetParam(parameterKey, value);
		}

		holder.getAllParameters().add(keyValue);
	}

	private ParameterKey getParameterKey(Object key) {
		if (key instanceof Integer) {
			return new ParameterKey((Integer) key);
		} else {
			return new ParameterKey((String) key);
		}
	}

	public List<QueryExecution> getQueryExecutions() {
		return this.queryExecutions;
	}

	public void reset() {
		this.queryExecutions.clear();
	}

}
