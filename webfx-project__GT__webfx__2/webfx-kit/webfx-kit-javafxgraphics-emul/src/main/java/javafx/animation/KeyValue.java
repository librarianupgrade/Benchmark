package javafx.animation;

import javafx.beans.value.*;

/**
 * Defines a key value to be interpolated for a particular interval along the
 * animation. A {@link KeyFrame}, which defines a specific point on a timeline,
 * can hold multiple {@code KeyValues}. {@code KeyValue} is an immutable class.
 * <p>
 * A {@code KeyValue} is defined by a target, which is an implementation of
 * {@link javafx.beans.value.WritableValue}, an end value and an
 * {@link Interpolator}.
 * <p>
 * Most interpolators define the interpolation between two {@code KeyFrames}.
 * (The only exception are tangent-interpolators.)
 * The {@code KeyValue} of the second {@code KeyFrame} (in forward
 * direction) specifies the interpolator to be used in the interval.
 * <p>
 * Tangent-interpolators define the interpolation to the left and to the right of
 * a {@code KeyFrame} (see {@link  Interpolator#TANGENT(javafx.util.Duration, double, javafx.util.Duration, double)
 * Interpolator.TANGENT}).
 * <p>
 * By default, {@link Interpolator#LINEAR} is used in the interval.
 *
 * @see Timeline
 * @see KeyFrame
 * @see Interpolator
 *
 * @since JavaFX 2.0
 */
public final class KeyValue {

	private static final Interpolator DEFAULT_INTERPOLATOR = Interpolator.LINEAR;

	/**
	 * @treatAsPrivate implementation detail
	 * @deprecated This is an internal API that is not intended for use and will be removed in the next version
	 * @since JavaFX 2.0
	 */
	@Deprecated
	public enum Type {
		BOOLEAN, DOUBLE, FLOAT, INTEGER, LONG, OBJECT
	}

	/**
	 * @treatAsPrivate implementation detail
	 * @deprecated This is an internal API that is not intended for use and will be removed in the next version
	 */
	@Deprecated
	public Type getType() {
		return type;
	}

	private final Type type;

	/**
	 * Returns the target of this {@code KeyValue}
	 *
	 * @return the target
	 */
	public WritableValue<?> getTarget() {
		return target;
	}

	private final WritableValue<?> target;

	/**
	 * Returns the end value of this {@code KeyValue}
	 *
	 * @return the end value
	 */
	public Object getEndValue() {
		return endValue;
	}

	private final Object endValue;

	/**
	 * {@link Interpolator} to be used for calculating the key value along the
	 * particular interval. By default, {@link Interpolator#LINEAR} is used.
	 */
	public Interpolator getInterpolator() {
		return interpolator;
	}

	private final Interpolator interpolator;

	/**
	 * Creates a {@code KeyValue}.
	 *
	 * @param target
	 *            the target
	 * @param endValue
	 *            the end value
	 * @param interpolator
	 *            the {@link Interpolator}
	 * @throws NullPointerException
	 *             if {@code target} or {@code interpolator} are {@code null}
	 */
	public <T> KeyValue(WritableValue<T> target, T endValue, Interpolator interpolator) {
		if (target == null)
			throw new NullPointerException("Target needs to be specified");
		if (interpolator == null)
			throw new NullPointerException("Interpolator needs to be specified");

		this.target = target;
		this.endValue = endValue;
		this.interpolator = interpolator;
		this.type = (target instanceof WritableNumberValue)
				? (target instanceof WritableDoubleValue) ? Type.DOUBLE
						: (target instanceof WritableIntegerValue) ? Type.INTEGER
								: (target instanceof WritableFloatValue) ? Type.FLOAT
										: (target instanceof WritableLongValue) ? Type.LONG : Type.OBJECT
				: (target instanceof WritableBooleanValue) ? Type.BOOLEAN : Type.OBJECT;
	}

	/**
	 * Creates a {@code KeyValue} that uses {@link Interpolator#LINEAR}.
	 *
	 * @param target
	 *            the target
	 * @param endValue
	 *            the end value
	 * @throws NullPointerException
	 *             if {@code target} or {@code interpolator} are {@code null}
	 */
	public <T> KeyValue(WritableValue<T> target, T endValue) {
		this(target, endValue, DEFAULT_INTERPOLATOR);
	}

	/**
	 * Returns a string representation of this {@code KeyValue} object.
	 * @return a string representation of this {@code KeyValue} object.
	 */
	@Override
	public String toString() {
		return "KeyValue [target=" + target + ", endValue=" + endValue + ", interpolator=" + interpolator + "]";
	}

	/**
	 * Returns a hash code for this {@code KeyValue} object.
	 * @return a hash code for this {@code KeyValue} object.
	 */
	@Override
	public int hashCode() {
		assert (target != null) && (interpolator != null);
		final int prime = 31;
		int result = 1;
		result = prime * result + target.hashCode();
		result = prime * result + ((endValue == null) ? 0 : endValue.hashCode());
		result = prime * result + interpolator.hashCode();
		return result;
	}

	/**
	 * Indicates whether some other object is "equal to" this one.
	 * Two {@code KeyValues} are considered equal, if their {@link #getTarget()
	 * target}, {@link #getEndValue() endValue}, and {@link #getInterpolator()
	 * interpolator} are equal.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof KeyValue) {
			final KeyValue keyValue = (KeyValue) obj;
			assert (target != null) && (interpolator != null) && (keyValue.target != null)
					&& (keyValue.interpolator != null);
			return target.equals(keyValue.target)
					&& ((endValue == null) ? (keyValue.endValue == null) : endValue.equals(keyValue.endValue))
					&& interpolator.equals(keyValue.interpolator);
		}
		return false;
	}

}