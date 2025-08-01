package javafx.animation;

/**
 * @author Bruno Salmon
 */

import com.sun.javafx.collections.TrackableObservableList;
import javafx.util.Duration;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 * A {@code Timeline} can be used to define a free form animation of any
 * {@link javafx.beans.value.WritableValue}, e.g. all
 * {@link javafx.beans.property.Property JavaFX Properties}.
 * <p>
 * A {@code Timeline}, defined by one or more {@link KeyFrame}s, processes
 * individual {@code KeyFrame} sequentially, in the order specified by
 * {@code KeyFrame.time}. The animated properties, defined as key values in
 * {@code KeyFrame.values}, are interpolated
 * to/from the targeted key values at the specified time of the {@code KeyFrame}
 * to {@code Timeline}'s initial position, depends on {@code Timeline}'s
 * direction.
 * <p>
 * {@code Timeline} processes individual {@code KeyFrame} at or after specified
 * time interval elapsed, it does not guarantee the timing when {@code KeyFrame}
 * is processed.
 * <p>
 * The {@link #cycleDurationProperty()} will be set to the largest time value
 * of Timeline's keyFrames.
 * <p>
 * If a {@code KeyFrame} is not provided for the {@code time==0s} instant, one
 * will be synthesized using the target values that are current at the time
 * {@link #play()} or {@link #playFromStart()} is called.
 * <p>
 * It is not possible to change the {@code keyFrames} of a running {@code Timeline}.
 * If the value of {@code keyFrames} is changed for a running {@code Timeline}, it
 * has to be stopped and started again to pick up the new value.
 * <p>
 * A simple Timeline can be created like this:
 * <pre>{@code
 * final Timeline timeline = new Timeline();
 * timeline.setCycleCount(2);
 * timeline.setAutoReverse(true);
 * timeline.getKeyFrames().add(new KeyFrame(Duration.millis(5000),
 *   new KeyValue (node.translateXProperty(), 25)));
 * timeline.play();
 * }</pre>
 * <p>
 * This Timeline will run for 10s, animating the node by x axis to value 25 and then back to 0 on the second cycle.
 * <p>
 * <b>Warning :</b> A running Timeline is being referenced from the FX runtime. Infinite Timeline
 * might result in a memory leak if not stopped properly. All the objects with animated properties would not be garbage collected.
 *
 * @see Animation
 * @see KeyFrame
 * @see KeyValue
 *
 * @since JavaFX 2.0
 */
public final class Timeline extends Animation {
	/* Package-private for testing purposes */
	final TimelineClipCore clipCore;

	/**
	 * Returns the {@link KeyFrame KeyFrames} of this {@code Timeline}.
	 */
	public final ObservableList<KeyFrame> getKeyFrames() {
		return keyFrames;
	}

	private final ObservableList<KeyFrame> keyFrames = new TrackableObservableList<KeyFrame>() {
		@Override
		protected void onChanged(ListChangeListener.Change<KeyFrame> c) {
			while (c.next()) {
				if (!c.wasPermutated()) {
					for (final KeyFrame keyFrame : c.getRemoved()) {
						final String cuePoint = keyFrame.getName();
						if (cuePoint != null) {
							getCuePoints().remove(cuePoint);
						}
					}
					for (final KeyFrame keyFrame : c.getAddedSubList()) {
						final String cuePoint = keyFrame.getName();
						if (cuePoint != null) {
							getCuePoints().put(cuePoint, keyFrame.getTime());
						}
					}
					Duration duration = clipCore.setKeyFrames(getKeyFrames());
					setCycleDuration(duration);
				}
			}
		}
	};

	/**
	 * The constructor of {@code Timeline}.
	 *
	 * This constructor allows to define a {@link Animation#targetFramerate}.
	 *
	 * @param targetFramerate
	 *            The custom target frame rate for this {@code Timeline}
	 * @param keyFrames
	 *            The keyframes of this {@code Timeline}
	 */
	public Timeline(double targetFramerate, KeyFrame... keyFrames) {
		super(targetFramerate);
		clipCore = new TimelineClipCore(this);
		getKeyFrames().setAll(keyFrames);
	}

	/**
	 * The constructor of {@code Timeline}.
	 *
	 * @param keyFrames
	 *            The keyframes of this {@code Timeline}
	 */
	public Timeline(KeyFrame... keyFrames) {
		super();
		clipCore = new TimelineClipCore(this);
		getKeyFrames().setAll(keyFrames);
	}

	/**
	 * The constructor of {@code Timeline}.
	 *
	 * This constructor allows to define a {@link Animation#targetFramerate}.
	 *
	 * @param targetFramerate
	 *            The custom target frame rate for this {@code Timeline}
	 */
	public Timeline(double targetFramerate) {
		super(targetFramerate);
		clipCore = new TimelineClipCore(this);
	}

	/**
	 * The constructor of {@code Timeline}.
	 */
	public Timeline() {
		super();
		clipCore = new TimelineClipCore(this);
	}

	// This constructor is only for testing purposes
	Timeline(final AbstractMasterTimer timer) {
		super(timer);
		clipCore = new TimelineClipCore(this);
	}

	@Override
	void impl_playTo(long currentTicks, long cycleTicks) {
		clipCore.playTo(currentTicks);
	}

	@Override
	void impl_jumpTo(long currentTicks, long cycleTicks, boolean forceJump) {
		impl_sync(false);
		impl_setCurrentTicks(currentTicks);
		clipCore.jumpTo(currentTicks, forceJump);
	}

	@Override
	void impl_setCurrentRate(double currentRate) {
		super.impl_setCurrentRate(currentRate);
		clipCore.notifyCurrentRateChanged();
	}

	@Override
	void impl_start(boolean forceSync) {
		super.impl_start(forceSync);
		clipCore.start(forceSync);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void stop() {
		if (parent != null) {
			throw new IllegalStateException("Cannot stop when embedded in another animation");
		}
		if (getStatus() == Status.RUNNING) {
			clipCore.abort();
		}
		super.stop();
	}
}
