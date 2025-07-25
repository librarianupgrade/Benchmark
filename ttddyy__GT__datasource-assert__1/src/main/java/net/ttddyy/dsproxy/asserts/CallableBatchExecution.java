package net.ttddyy.dsproxy.asserts;

import java.util.ArrayList;
import java.util.List;

/**
 * Represent a single batch execution of {@link java.sql.CallableStatement}.
 *
 * @author Tadaya Tsuyukubo
 * @since 1.0
 */
public class CallableBatchExecution extends BaseQueryExecution
		implements BatchParameterHolder, QueryHolder, BatchExecution {

	private String query;

	private List<BatchExecutionEntry> batchExecutionEntries = new ArrayList<>();

	@Override
	public boolean isBatch() {
		return true;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	@Override
	public String getQuery() {
		return this.query;
	}

	@Override
	public List<BatchExecutionEntry> getBatchExecutionEntries() {
		return this.batchExecutionEntries;
	}

	public boolean addBatchExecutionEntry(CallableBatchExecutionEntry entry) {
		return this.batchExecutionEntries.add(entry);
	}

}
