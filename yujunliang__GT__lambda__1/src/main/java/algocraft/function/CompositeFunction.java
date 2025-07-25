package algocraft.function;

import com.google.common.base.Function;

import static algocraft.function.Functions.compose;

public class CompositeFunction<T1, T2> extends AbstractFunction<T1, T2> {

	protected <A> CompositeFunction(Function<T1, A> f1, Function<A, T2> f2) {
		super(compose(f1, f2));
	}

	protected <A, B> CompositeFunction(Function<T1, A> f1, Function<A, B> f2, Function<B, T2> f3) {
		super(compose(f1, f2, f3));
	}

	protected <A, B, C> CompositeFunction(Function<T1, A> f1, Function<A, B> f2, Function<B, C> f3,
			Function<C, T2> f4) {
		super(compose(f1, f2, f3, f4));
	}

	protected <A, B, C, D> CompositeFunction(Function<T1, A> f1, Function<A, B> f2, Function<B, C> f3,
			Function<C, D> f4, Function<D, T2> f5) {
		super(compose(f1, f2, f3, f4, f5));
	}
}
