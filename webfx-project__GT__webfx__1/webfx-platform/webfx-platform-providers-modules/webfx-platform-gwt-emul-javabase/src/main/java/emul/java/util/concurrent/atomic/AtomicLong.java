/*
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

/*
 *
 *
 *
 *
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package emul.java.util.concurrent.atomic;

/* Used by rx.internal.operators.MergeOperator */

/**
 * An object reference that may be updated atomically. See the {@link
 * java.util.concurrent.atomic} package specification for description
 * of the properties of atomic variables.
 * @since 1.5
 * @author Doug Lea
 */
public class AtomicLong implements java.io.Serializable {
	//private static final long serialVersionUID = -1848883965231344442L;

	//private static final Unsafe unsafe = Unsafe.getUnsafe();
	//private static final long valueOffset;

	/*static {
	    try {
	        valueOffset = unsafe.objectFieldOffset
	                (AtomicReference.class.getDeclaredField("value"));
	    } catch (Exception ex) { throw new Error(ex); }
	}*/

	private volatile long value;

	/**
	 * Creates a new AtomicReference with the given initial value.
	 *
	 * @param initialValue the initial value
	 */
	public AtomicLong(long initialValue) {
		value = initialValue;
	}

	/**
	 * Creates a new AtomicReference with null initial value.
	 */
	public AtomicLong() {
	}

	/**
	 * Gets the current value.
	 *
	 * @return the current value
	 */
	public final long get() {
		return value;
	}

	/**
	 * Sets to the given value.
	 *
	 * @param newValue the new value
	 */
	public final void set(long newValue) {
		value = newValue;
	}

	/**
	 * Eventually sets to the given value.
	 *
	 * @param newValue the new value
	 * @since 1.6
	 */
	public final void lazySet(long newValue) {
		//unsafe.putOrderedObject(this, valueOffset, newValue);
		set(newValue);
	}

	/**
	 * Atomically sets to the given value and returns the old value.
	 *
	 * @param newValue the new value
	 * @return the previous value
	 */
	public final synchronized long getAndSet(long newValue) {
		long oldValue = value;
		value = newValue;
		return oldValue;
	}

	/**
	 * Atomically sets the value to the given updated value
	 * if the current value {@code ==} the expected value.
	 * @param expect the expected value
	 * @param update the new value
	 * @return {@code true} if successful. False return indicates that
	 * the actual value was not equal to the expected value.
	 */
	public final synchronized boolean compareAndSet(long expect, long update) {
		//return unsafe.compareAndSwapObject(this, valueOffset, expect, update);
		if (value != expect)
			return false;
		value = update;
		return true;
	}

	/**
	 * Atomically sets the value to the given updated value
	 * if the current value {@code ==} the expected value.
	 *
	 * <p><a href="package-summary.html#weakCompareAndSet">May fail
	 * spuriously and does not provide ordering guarantees</a>, so is
	 * only rarely an appropriate alternative to {@code compareAndSet}.
	 *
	 * @param expect the expected value
	 * @param update the new value
	 * @return {@code true} if successful
	 */
	public final boolean weakCompareAndSet(long expect, long update) {
		//return unsafe.compareAndSwapObject(this, valueOffset, expect, update);
		return compareAndSet(expect, update);
	}

	/**
	 * Atomically increments by one the current value.
	 *
	 * @return the previous value
	 */
	public final synchronized long getAndIncrement() {
		return value++;
	}

	/**
	 * Atomically decrements by one the current value.
	 *
	 * @return the previous value
	 */
	public final synchronized long getAndDecrement() {
		return value--;
	}

	/**
	 * Atomically adds the given value to the current value.
	 *
	 * @param delta the value to add
	 * @return the previous value
	 */
	public final synchronized long getAndAdd(long delta) {
		long oldValue = value;
		value += delta;
		return oldValue;
	}

	/**
	 * Atomically increments by one the current value.
	 *
	 * @return the updated value
	 */
	public final synchronized long incrementAndGet() {
		return ++value;
	}

	/**
	 * Atomically decrements by one the current value.
	 *
	 * @return the updated value
	 */
	public final synchronized long decrementAndGet() {
		return --value;
	}

	/**
	 * Atomically adds the given value to the current value.
	 *
	 * @param delta the value to add
	 * @return the updated value
	 */
	public final synchronized long addAndGet(long delta) {
		return value += delta;
	}

	/**
	 * Atomically updates the current value with the results of
	 * applying the given function, returning the previous value. The
	 * function should be side-effect-free, since it may be re-applied
	 * when attempted updates fail due to contention among threads.
	 *
	 * @param updateFunction a side-effect-free function
	 * @return the previous value
	 * @since 1.8
	 */
	/* public final long getAndUpdate(LongUnaryOperator updateFunction) {
	    long prev, next;
	    do {
	        prev = get();
	        next = updateFunction.applyAsLong(prev);
	    } while (!compareAndSet(prev, next));
	    return prev;
	}*/

	/**
	 * Atomically updates the current value with the results of
	 * applying the given function, returning the updated value. The
	 * function should be side-effect-free, since it may be re-applied
	 * when attempted updates fail due to contention among threads.
	 *
	 * @param updateFunction a side-effect-free function
	 * @return the updated value
	 * @since 1.8
	 */
	/* public final long updateAndGet(LongUnaryOperator updateFunction) {
	    long prev, next;
	    do {
	        prev = get();
	        next = updateFunction.applyAsLong(prev);
	    } while (!compareAndSet(prev, next));
	    return next;
	}*/

	/**
	 * Atomically updates the current value with the results of
	 * applying the given function to the current and given values,
	 * returning the previous value. The function should be
	 * side-effect-free, since it may be re-applied when attempted
	 * updates fail due to contention among threads.  The function
	 * is applied with the current value as its first argument,
	 * and the given update as the second argument.
	 *
	 * @param x the update value
	 * @param accumulatorFunction a side-effect-free function of two arguments
	 * @return the previous value
	 * @since 1.8
	 */
	/* public final long getAndAccumulate(long x,
	                                   LongBinaryOperator accumulatorFunction) {
	    long prev, next;
	    do {
	        prev = get();
	        next = accumulatorFunction.applyAsLong(prev, x);
	    } while (!compareAndSet(prev, next));
	    return prev;
	}*/

	/**
	 * Atomically updates the current value with the results of
	 * applying the given function to the current and given values,
	 * returning the updated value. The function should be
	 * side-effect-free, since it may be re-applied when attempted
	 * updates fail due to contention among threads.  The function
	 * is applied with the current value as its first argument,
	 * and the given update as the second argument.
	 *
	 * @param x the update value
	 * @param accumulatorFunction a side-effect-free function of two arguments
	 * @return the updated value
	 * @since 1.8
	 */
	/* public final long accumulateAndGet(long x,
	                                   LongBinaryOperator accumulatorFunction) {
	    long prev, next;
	    do {
	        prev = get();
	        next = accumulatorFunction.applyAsLong(prev, x);
	    } while (!compareAndSet(prev, next));
	    return next;
	}*/

	/**
	 * Returns the String representation of the current value.
	 * @return the String representation of the current value
	 */
	public String toString() {
		return Long.toString(get());
	}

	/**
	 * Returns the value of this {@code AtomicLong} as an {@code int}
	 * after a narrowing primitive conversion.
	 * @jls 5.1.3 Narrowing Primitive Conversions
	 */
	public int intValue() {
		return (int) get();
	}

	/**
	 * Returns the value of this {@code AtomicLong} as a {@code long}.
	 */
	public long longValue() {
		return get();
	}

	/**
	 * Returns the value of this {@code AtomicLong} as a {@code float}
	 * after a widening primitive conversion.
	 * @jls 5.1.2 Widening Primitive Conversions
	 */
	public float floatValue() {
		return (float) get();
	}

	/**
	 * Returns the value of this {@code AtomicLong} as a {@code double}
	 * after a widening primitive conversion.
	 * @jls 5.1.2 Widening Primitive Conversions
	 */
	public double doubleValue() {
		return (double) get();
	}

}
