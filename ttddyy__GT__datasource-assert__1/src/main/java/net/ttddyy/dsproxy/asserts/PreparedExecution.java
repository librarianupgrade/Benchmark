package net.ttddyy.dsproxy.asserts;

import net.ttddyy.dsproxy.proxy.ParameterKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import static net.ttddyy.dsproxy.asserts.ParameterKeyValueUtils.filterBy;
import static net.ttddyy.dsproxy.asserts.ParameterKeyValueUtils.filterByKeyType;
import static net.ttddyy.dsproxy.asserts.ParameterKeyValueUtils.toKeyIndexMap;

/**
 * Represent a single execution of {@link java.sql.PreparedStatement}.
 *
 * @author Tadaya Tsuyukubo
 * @since 1.0
 */
public class PreparedExecution extends BaseQueryExecution implements QueryHolder, ParameterByIndexHolder {

	private String query;
	private SortedSet<ParameterKeyValue> parameters = new TreeSet<>();

	@Override
	public boolean isBatch() {
		return false;
	}

	@Override
	public String getQuery() {
		return this.query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	@Override
	public SortedSet<ParameterKeyValue> getAllParameters() {
		return this.parameters;
	}

	@Override
	public SortedSet<ParameterKeyValue> getSetParams() {
		return filterBy(this.parameters, ParameterKeyValue.OperationType.SET_PARAM);
	}

	@Override
	public SortedSet<ParameterKeyValue> getSetNullParams() {
		return filterBy(this.parameters, ParameterKeyValue.OperationType.SET_NULL);
	}

	@Override
	public Map<Integer, Object> getSetParamsByIndex() {
		return toKeyIndexMap(filterByKeyType(getSetParams(), ParameterKey.ParameterKeyType.BY_INDEX));
	}

	@Override
	public Map<Integer, Integer> getSetNullParamsByIndex() {
		return toKeyIndexMap(filterByKeyType(getSetNullParams(), ParameterKey.ParameterKeyType.BY_INDEX));
	}

	@Override
	public List<Integer> getParamIndexes() {
		List<Integer> indexes = new ArrayList<>();
		indexes.addAll(getSetParamsByIndex().keySet());
		indexes.addAll(getSetNullParamsByIndex().keySet());
		return indexes;
	}

}
