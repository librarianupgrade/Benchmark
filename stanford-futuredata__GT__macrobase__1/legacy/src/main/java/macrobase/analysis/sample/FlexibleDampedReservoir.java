package macrobase.analysis.sample;

import java.util.Random;

/**
 * Keeps an exponentially weighted sample with specified bias parameter
 * N.B. The current period is advanced explicitly.
 */
public class FlexibleDampedReservoir<T> extends AChao<T> {
	private final double bias;

	public FlexibleDampedReservoir(int capacity, double bias, Random random) {
		super(capacity, random);
		assert (bias >= 0 && bias < 1);
		this.bias = bias;
	}

	public FlexibleDampedReservoir(int capacity, double bias) {
		super(capacity);
		assert (bias >= 0 && bias < 1);
		this.bias = bias;
	}

	public void advancePeriod() {
		advancePeriod(1);
	}

	public void advancePeriod(int numPeriods) {
		decayWeights(Math.pow(1 - bias, numPeriods));
	}

	public void insert(T ele) {
		insert(ele, 1);
	}
}
