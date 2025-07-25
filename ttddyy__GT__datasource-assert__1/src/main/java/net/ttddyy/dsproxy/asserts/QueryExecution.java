package net.ttddyy.dsproxy.asserts;

/**
 * Represent single query execution.
 *
 * @author Tadaya Tsuyukubo
 * @since 1.0
 */
public interface QueryExecution {

	boolean isSuccess();

	boolean isBatch();

}
