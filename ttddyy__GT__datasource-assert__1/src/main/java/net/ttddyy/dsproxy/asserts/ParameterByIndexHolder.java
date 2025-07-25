package net.ttddyy.dsproxy.asserts;

import java.util.List;
import java.util.Map;

/**
 * Represent an execution that hold parameter by index.
 *
 * @author Tadaya Tsuyukubo
 * @since 1.0
 */
public interface ParameterByIndexHolder extends ParameterHolder {

	Map<Integer, Object> getSetParamsByIndex();

	/**
	 * setNull operations.
	 *
	 * @return key is index, value is {@link java.sql.Types} (int).
	 */
	Map<Integer, Integer> getSetNullParamsByIndex();

	/**
	 * Keys of parameters.
	 *
	 * @return Integer keys.
	 */
	List<Integer> getParamIndexes();

}
