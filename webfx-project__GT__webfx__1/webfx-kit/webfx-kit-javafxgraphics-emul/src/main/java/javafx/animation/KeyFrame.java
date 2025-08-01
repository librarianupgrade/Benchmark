/*
 * Copyright (c) 2010, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package javafx.animation;

import javafx.beans.NamedArg;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
//import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Defines target values at a specified point in time for a set of variables
 * that are interpolated along a {@link Timeline}.
 * <p>
 * The developer controls the interpolation of a set of variables for the
 * interval between successive key frames by providing a target value and an
 * {@link Interpolator} associated with each variable. The variables are
 * interpolated such that they will reach their target value at the specified
 * time. An {@link #onFinished} function is invoked on each {@code KeyFrame} if one
 * is provided. A {@code KeyFrame} can optionally have a {@link #name}, which
 * will result in a cuepoint that is automatically added to the {@code Timeline}.
 *
 * @see Timeline
 * @see KeyValue
 * @see Interpolator
 *
 * @since JavaFX 2.0
 */
public final class KeyFrame {

	private static final EventHandler<ActionEvent> DEFAULT_ON_FINISHED = null;
	private static final String DEFAULT_NAME = null;

	/**
	 * Returns the time offset of this {@code KeyFrame}.
	 *
	 * The returned {@link javafx.util.Duration} defines the time offset within
	 * a single cycle of a {@link Timeline} at which the {@link KeyValue
	 * KeyValues} will be set and at which the {@link #onFinished} function
	 * variable will be called.
	 * <p>
	 * The {@code time} of a {@code KeyFrame} has to be greater than or equal to
	 * {@link javafx.util.Duration#ZERO} and it cannot be
	 * {@link javafx.util.Duration#UNKNOWN}.
	 *
	 * Note: While the unit of {@code time} is a millisecond, the granularity
	 * depends on the underlying operating system and will in general be larger.
	 * For example animations on desktop systems usually run with a maximum of
	 * 60fps which gives a granularity of ~17 ms.
	 */
	public Duration getTime() {
		return time;
	}

	private final Duration time;

	/**
	 * Returns an immutable {@code Set} of {@link KeyValue} instances.
	 *
	 * A {@code KeyValue} defines a target and the desired value that should be
	 * interpolated at the specified time of this {@code KeyFrame}.
	 */
	public Set<KeyValue> getValues() {
		return values;
	}

	private final Set<KeyValue> values;

	/**
	 * Returns the {@code onFinished} event handler of this {@code KeyFrame}.
	 *
	 * The {@code onFinished} event handler is a function that is called when
	 * the elapsed time on a cycle passes the specified time of this
	 * {@code KeyFrame}. The {@code onFinished} function variable will be called
	 * if the elapsed time passes the indicated value, even if it never equaled
	 * the time value exactly.
	 */
	public EventHandler<ActionEvent> getOnFinished() {
		return onFinished;
	}

	private final EventHandler<ActionEvent> onFinished;

	/**
	 * Returns the {@code name} of this {@code KeyFrame}.
	 *
	 * If a named {@code KeyFrame} is added to a {@link Timeline}, a cuepoint
	 * with the {@code name} and the {@link #time} of the {@code KeyFrame} will
	 * be added automatically. If the {@code KeyFrame} is removed, the cuepoint
	 * will also be removed.
	 */
	public String getName() {
		return name;
	}

	private final String name;

	/**
	 * Constructor of {@code KeyFrame}
	 * <p>
	 * If a passed in {@code KeyValue} is {@code null} or a duplicate, it will
	 * be ignored.
	 *
	 * @param time
	 *            the {@link #time}
	 * @param name
	 *            the {@link #name}
	 * @param onFinished
	 *            the {@link #onFinished onFinished-handler}
	 * @param values
	 *            a {@link javafx.collections.ObservableList} of
	 *            {@link KeyValue} instances
	 * @throws NullPointerException
	 *             if {@code time} is null
	 * @throws IllegalArgumentException
	 *             if {@code time} is invalid (see {@link #time})
	 */
	public KeyFrame(@NamedArg("time") Duration time, @NamedArg("name") String name,
			@NamedArg("onFinished") EventHandler<ActionEvent> onFinished,
			@NamedArg("values") Collection<KeyValue> values) {
		if (time == null) {
			throw new NullPointerException("The time has to be specified");
		}
		if (time.lessThan(Duration.ZERO) || time.equals(Duration.UNKNOWN)) {
			throw new IllegalArgumentException("The time is invalid.");
		}
		this.time = time;
		this.name = name;
		if (values != null) {
			final Set<KeyValue> set = new java.util.HashSet<>(values); //new CopyOnWriteArraySet<KeyValue>(values);
			set.remove(null);
			this.values = (set.size() == 0) ? Collections.<KeyValue>emptySet()
					: (set.size() == 1) ? Collections.<KeyValue>singleton(set.iterator().next())
							: Collections.unmodifiableSet(set);
		} else {
			this.values = Collections.<KeyValue>emptySet();
		}
		this.onFinished = onFinished;
	}

	/**
	 * Constructor of {@code KeyFrame}
	 * <p>
	 * If a passed in {@code KeyValue} is {@code null} or a duplicate, it will
	 * be ignored.
	 *
	 * @param time
	 *            the {@link #time}
	 * @param name
	 *            the {@link #name}
	 * @param onFinished
	 *            the {@link #onFinished onFinished-handler}
	 * @param values
	 *            the {@link KeyValue} instances
	 * @throws NullPointerException
	 *             if {@code time} is null
	 * @throws IllegalArgumentException
	 *             if {@code time} is invalid (see {@link #time})
	 */
	public KeyFrame(@NamedArg("time") Duration time, @NamedArg("name") String name,
			@NamedArg("onFinished") EventHandler<ActionEvent> onFinished, @NamedArg("values") KeyValue... values) {
		if (time == null) {
			throw new NullPointerException("The time has to be specified");
		}
		if (time.lessThan(Duration.ZERO) || time.equals(Duration.UNKNOWN)) {
			throw new IllegalArgumentException("The time is invalid.");
		}
		this.time = time;
		this.name = name;
		if (values != null) {
			final Set<KeyValue> set = new java.util.HashSet<>(); //new CopyOnWriteArraySet<KeyValue>();
			for (final KeyValue keyValue : values) {
				if (keyValue != null) {
					set.add(keyValue);
				}
			}
			this.values = (set.size() == 0) ? Collections.<KeyValue>emptySet()
					: (set.size() == 1) ? Collections.<KeyValue>singleton(set.iterator().next())
							: Collections.unmodifiableSet(set);
		} else {
			this.values = Collections.emptySet();
		}
		this.onFinished = onFinished;
	}

	/**
	 * Constructor of {@code KeyFrame}
	 *
	 * @param time
	 *            the {@link #time}
	 * @param onFinished
	 *            the {@link #onFinished onFinished-handler}
	 * @param values
	 *            the {@link KeyValue} instances
	 * @throws NullPointerException
	 *             if {@code time} is null
	 * @throws IllegalArgumentException
	 *             if {@code time} is invalid (see {@link #time})
	 */
	public KeyFrame(@NamedArg("time") Duration time, @NamedArg("onFinished") EventHandler<ActionEvent> onFinished,
			@NamedArg("values") KeyValue... values) {
		this(time, DEFAULT_NAME, onFinished, values);
	}

	/**
	 * Constructor of {@code KeyFrame}
	 *
	 * @param time
	 *            the {@link #time}
	 * @param name
	 *            the {@link #name}
	 * @param values
	 *            the {@link KeyValue} instances
	 * @throws NullPointerException
	 *             if {@code time} is null
	 * @throws IllegalArgumentException
	 *             if {@code time} is invalid (see {@link #time})
	 */
	public KeyFrame(@NamedArg("time") Duration time, @NamedArg("name") String name,
			@NamedArg("values") KeyValue... values) {
		this(time, name, DEFAULT_ON_FINISHED, values);
	}

	/**
	 * Constructor of {@code KeyFrame}
	 *
	 * @param time
	 *            the {@link #time}
	 * @param values
	 *            the {@link KeyValue} instances
	 * @throws NullPointerException
	 *             if {@code time} is null
	 * @throws IllegalArgumentException
	 *             if {@code time} is invalid (see {@link #time})
	 */
	public KeyFrame(@NamedArg("time") Duration time, @NamedArg("values") KeyValue... values) {
		this(time, DEFAULT_NAME, DEFAULT_ON_FINISHED, values);
	}

	/**
	 * Returns a string representation of this {@code KeyFrame} object.
	 * @return a string representation of this {@code KeyFrame} object.
	 */
	@Override
	public String toString() {
		return "KeyFrame [time=" + time + ", values=" + values + ", onFinished=" + onFinished + ", name=" + name + "]";
	}

	/**
	 * Returns a hash code for this {@code KeyFrame} object.
	 * @return a hash code for this {@code KeyFrame} object.
	 */
	@Override
	public int hashCode() {
		assert (time != null) && (values != null);
		final int prime = 31;
		int result = 1;
		result = prime * result + time.hashCode();
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((onFinished == null) ? 0 : onFinished.hashCode());
		result = prime * result + values.hashCode();
		return result;
	}

	/**
	 * Indicates whether some other object is "equal to" this one.
	 * Two {@code KeyFrames} are considered equal, if their {@link #getTime()
	 * time}, {@link #onFinished onFinished}, and {@link #getValues() values}
	 * are equal.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof KeyFrame) {
			final KeyFrame kf = (KeyFrame) obj;
			assert (time != null) && (values != null) && (kf.time != null) && (kf.values != null);
			return time.equals(kf.time) && ((name == null) ? kf.name == null : name.equals(kf.name))
					&& ((onFinished == null) ? kf.onFinished == null : onFinished.equals(kf.onFinished))
					&& values.equals(kf.values);
		}
		return false;
	}

}
