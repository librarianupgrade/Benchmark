package bdv.util;

import java.util.function.Function;

import bdv.viewer.Interpolation;
import net.imglib2.RandomAccessible;
import net.imglib2.interpolation.InterpolatorFactory;
import net.imglib2.interpolation.randomaccess.ClampingNLinearInterpolatorFactory;
import net.imglib2.interpolation.randomaccess.NearestNeighborInterpolatorFactory;
import net.imglib2.type.numeric.NumericType;

public class DefaultInterpolators<T extends NumericType<T>>
		implements Function<Interpolation, InterpolatorFactory<T, RandomAccessible<T>>> {
	private final InterpolatorFactory<T, RandomAccessible<T>>[] factories;

	@SuppressWarnings("unchecked")
	public DefaultInterpolators() {
		factories = new InterpolatorFactory[Interpolation.values().length];
		factories[Interpolation.NEARESTNEIGHBOR.ordinal()] = new NearestNeighborInterpolatorFactory<>();
		factories[Interpolation.NLINEAR.ordinal()] = new ClampingNLinearInterpolatorFactory<>();
	}

	public InterpolatorFactory<T, RandomAccessible<T>> get(final Interpolation method) {
		return factories[method.ordinal()];
	}

	public int size() {
		return factories.length;
	}

	@Override
	public InterpolatorFactory<T, RandomAccessible<T>> apply(final Interpolation t) {
		return get(t);
	}
}
