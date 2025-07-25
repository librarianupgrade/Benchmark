package net.ttddyy.dsproxy.asserts;

/**
 * Base implementation class for {@link QueryExecution}.
 *
 * @author Tadaya Tsuyukubo
 */
public abstract class BaseQueryExecution implements QueryExecution {

	private boolean success;

	public void setSuccess(boolean success) {
		this.success = success;
	}

	@Override
	public boolean isSuccess() {
		return this.success;
	}

}
