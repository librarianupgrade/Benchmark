package javafx.animation;

import javafx.util.Duration;

/**
 * An instance of ClipEnvelope handles the loop-part of a clip.
 *
 * The functionality to react on a pulse-signal from the timer is implemented in
 * two classes: ClipEnvelope and ClipCore. ClipEnvelope is responsible for the
 * "loop-part" (keeping track of the number of cycles, handling the direction of
 * the clip etc.). ClipCore takes care of the inner part (interpolating the
 * values, triggering the action-functions, ...)
 *
 * Both classes have an abstract public definition and can only be created using
 * the factory method create(). The intent is to provide a general
 * implementation plus eventually some fast-track implementations for common use
 * cases.
 */

public abstract class ClipEnvelope {

	protected static final long INDEFINITE = Long.MAX_VALUE;
	protected static final double EPSILON = 1e-12;

	protected Animation animation;
	protected double rate = 1;
	protected long cycleTicks = 0;

	// internal state-variables used by all implementations
	protected long deltaTicks = 0;
	protected long ticks = 0;
	protected double currentRate = rate;
	protected boolean inTimePulse = false;
	protected boolean aborted = false;

	protected ClipEnvelope(Animation animation) {
		this.animation = animation;
		if (animation != null) {
			final Duration cycleDuration = animation.getCycleDuration();
			cycleTicks = TickCalculation.fromDuration(cycleDuration);
			rate = animation.getRate();
		}
	}

	public static ClipEnvelope create(Animation animation) {
		if ((animation.getCycleCount() == 1) /* || (animation.getCycleDuration().isIndefinite()) */) {
			return new SingleLoopClipEnvelope(animation);
		} else if (animation.getCycleCount() == Animation.INDEFINITE) {
			return new InfiniteClipEnvelope(animation);
		} else {
			return new FiniteClipEnvelope(animation);
		}
	}

	public abstract ClipEnvelope setCycleDuration(Duration cycleDuration);

	public abstract void setRate(double rate);

	public abstract void setAutoReverse(boolean autoReverse);

	public abstract ClipEnvelope setCycleCount(int cycleCount);

	protected void updateCycleTicks(Duration cycleDuration) {
		cycleTicks = TickCalculation.fromDuration(cycleDuration);
	}

	public boolean wasSynched() {
		return cycleTicks != 0;
	}

	public void start() {
		setCurrentRate(calculateCurrentRate());
		deltaTicks = ticks;
	}

	public abstract void timePulse(long currentTick);

	public abstract void jumpTo(long ticks);

	public void abortCurrentPulse() {
		if (inTimePulse) {
			aborted = true;
			inTimePulse = false;
		}
	}

	protected abstract double calculateCurrentRate();

	protected void setCurrentRate(double currentRate) {
		this.currentRate = currentRate;
		AnimationAccessor.getDefault().setCurrentRate(animation, currentRate);
	}

	protected static long checkBounds(long value, long max) {
		return Math.max(0L, Math.min(value, max));
	}

	public double getCurrentRate() {
		return currentRate;
	}
}
