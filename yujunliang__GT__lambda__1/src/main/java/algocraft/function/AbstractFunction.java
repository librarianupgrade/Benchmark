package algocraft.function;

import com.google.common.base.Function;

public class AbstractFunction<T1, T2> implements Function<T1, T2> {

	private final Function<T1, T2> function;

	protected AbstractFunction(Function<T1, T2> function) {
		this.function = function;
	}

	@Override
	public final T2 apply(T1 from) {
		return function.apply(from);
	}
}
