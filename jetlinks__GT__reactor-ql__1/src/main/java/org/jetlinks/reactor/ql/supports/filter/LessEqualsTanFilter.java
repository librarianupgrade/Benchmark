package org.jetlinks.reactor.ql.supports.filter;

import java.util.Date;

public class LessEqualsTanFilter extends BinaryFilterFeature {

	public LessEqualsTanFilter(String type) {
		super(type);
	}

	@Override
	protected boolean doTest(Number left, Number right) {
		return left.doubleValue() <= right.doubleValue();
	}

	@Override
	protected boolean doTest(Date left, Date right) {
		return left.getTime() <= right.getTime();
	}

	@Override
	protected boolean doTest(String left, String right) {
		return left.compareTo(right) <= 0;
	}

	@Override
	@SuppressWarnings("all")
	protected boolean doTest(Object left, Object right) {
		if (left instanceof Comparable) {
			return ((Comparable) left).compareTo(right) <= 0;
		}
		return false;
	}
}
