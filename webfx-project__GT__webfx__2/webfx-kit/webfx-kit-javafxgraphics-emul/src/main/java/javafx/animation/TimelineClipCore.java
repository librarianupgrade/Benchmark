package javafx.animation;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

/**
 * An instance of ClipCore handles the core part of a clip.
 *
 * The functionality to react on a pulse-signal from the timer is implemented in
 * two classes: ClipEnvelope and ClipCore. ClipEnvelope is responsible for the
 * "loop-part" (keeping track of the number of cycles, handling the direction of
 * the clip etc.). ClipCore takes care of the inner part (interpolating the
 * values, triggering the action-functions, ...)
 *
 * Both classes have an abstract public definition and can only be created using
 * the factory method create(). The intend is to provide a general
 * implementation plus eventually some fast-track implementations for common use
 * cases.
 */

// @@OPT
// - Use known information (kf) in visitKeyFrame to set values?

public class TimelineClipCore {

	private static final int UNDEFINED_KEYFRAME = -1;

	/**
	 * Note: this comparator imposes orderings that are inconsistent with
	 * equals.
	 */
	private static final Comparator<KeyFrame> KEY_FRAME_COMPARATOR = (kf1, kf2) -> kf1.getTime()
			.compareTo(kf2.getTime());

	// The owner of this ClipCore
	Timeline timeline;

	// The sorted list of keyframes
	private KeyFrame[] keyFrames = new KeyFrame[0];
	private long[] keyFrameTicks = new long[0];
	// If there are no KeyFrames with onFinished handler then we can skip frames
	// This works because KeyFrame.onFinished is final
	private boolean canSkipFrames = true;

	private ClipInterpolator clipInterpolator;

	public TimelineClipCore(Timeline timeline) {
		this.timeline = timeline;
		this.clipInterpolator = ClipInterpolator.create(keyFrames, keyFrameTicks);
	}

	/**
	 * Changes the keyframes.
	 */
	public Duration setKeyFrames(Collection<KeyFrame> keyFrames) {
		final int n = keyFrames.size();
		final KeyFrame[] sortedKeyFrames = new KeyFrame[n];
		keyFrames.toArray(sortedKeyFrames);
		Arrays.sort(sortedKeyFrames, KEY_FRAME_COMPARATOR);

		canSkipFrames = true;
		this.keyFrames = sortedKeyFrames;
		keyFrameTicks = new long[n];
		for (int i = 0; i < n; ++i) {
			keyFrameTicks[i] = TickCalculation.fromDuration(this.keyFrames[i].getTime());
			if (canSkipFrames && this.keyFrames[i].getOnFinished() != null) {
				canSkipFrames = false;
			}
		}
		clipInterpolator = clipInterpolator.setKeyFrames(sortedKeyFrames, keyFrameTicks);
		return (n == 0) ? Duration.ZERO : sortedKeyFrames[n - 1].getTime();
	}

	public void notifyCurrentRateChanged() {
		// special case: if clip is toggled while stopped, we want to revisit
		// all key frames
		if (timeline.getStatus() != Animation.Status.RUNNING) {
			clearLastKeyFrame();
		}
	}

	/**
	 * This method is called if while visiting a keyframe of a timeline the time
	 * or rate are changed, or if the timeline is stopped. In these cases
	 * visiting the keyframes must be aborted.
	 */
	public void abort() {
		aborted = true;
	}

	private boolean aborted = false;

	// The index of the keyframe that was visited last
	private int lastKF = UNDEFINED_KEYFRAME;

	// The position where clip currently stands
	private long curTicks = 0;

	private void clearLastKeyFrame() {
		lastKF = UNDEFINED_KEYFRAME;
	}

	public void jumpTo(long ticks, boolean forceJump) {
		lastKF = UNDEFINED_KEYFRAME;
		curTicks = ticks;
		if (timeline.getStatus() != Animation.Status.STOPPED || forceJump) {
			if (forceJump) {
				clipInterpolator.validate(false);
			}
			clipInterpolator.interpolate(ticks);
		}
	}

	public void start(boolean forceSync) {
		clearLastKeyFrame();
		clipInterpolator.validate(forceSync);
		if (curTicks > 0) {
			clipInterpolator.interpolate(curTicks);
		}
	}

	/**
	 * Called to visit all keyframes within a specified time-interval.
	 */
	public void playTo(long ticks) {
		if (canSkipFrames) {
			clearLastKeyFrame();
			setTime(ticks);
			clipInterpolator.interpolate(ticks);
			return;
		}
		aborted = false;
		final boolean forward = curTicks <= ticks;

		if (forward) {
			final int fromKF = (lastKF == UNDEFINED_KEYFRAME) ? 0
					: (keyFrameTicks[lastKF] <= curTicks) ? lastKF + 1 : lastKF;
			for (int fi = fromKF, toKF = keyFrames.length; fi < toKF; fi++) {
				final long kfTicks = keyFrameTicks[fi];
				if (kfTicks > ticks) {
					lastKF = fi - 1;
					break;
				}
				if (kfTicks >= curTicks) {
					visitKeyFrame(fi, kfTicks);
					if (aborted) {
						break;
					}
				}
			}
		} else {
			final int fromKF = (lastKF == UNDEFINED_KEYFRAME) ? keyFrames.length - 1
					: (keyFrameTicks[lastKF] >= curTicks) ? lastKF - 1 : lastKF;
			for (int fi = fromKF; fi >= 0; fi--) {
				final long kfTicks = keyFrameTicks[fi];
				if (kfTicks < ticks) {
					lastKF = fi + 1;
					break;
				}
				if (kfTicks <= curTicks) {
					visitKeyFrame(fi, kfTicks);
					if (aborted) {
						break;
					}
				}
			}
		}
		if (!aborted && ((lastKF == UNDEFINED_KEYFRAME) || keyFrameTicks[lastKF] != ticks
				|| (keyFrames[lastKF].getOnFinished() == null))) {
			setTime(ticks);
			clipInterpolator.interpolate(ticks);
		}
	}

	private void setTime(long ticks) {
		curTicks = ticks;
		AnimationAccessor.getDefault().setCurrentTicks(timeline, ticks);
	}

	/**
	 * Visit a single keyframe.
	 *
	 * @param kfIndex
	 *            the index of the keyframe in the keyframe-array
	 * @param kfTicks
	 *            the time of that keyframe
	 */
	private void visitKeyFrame(int kfIndex, long kfTicks) {
		if (kfIndex != lastKF) { // suppress double visiting on toggle for
			// autoReverse
			lastKF = kfIndex;

			final KeyFrame kf = keyFrames[kfIndex];
			final EventHandler<ActionEvent> onFinished = kf.getOnFinished();

			if (onFinished != null) {
				// visit the action of this keyframe
				setTime(kfTicks);
				clipInterpolator.interpolate(kfTicks);
				try {
					onFinished.handle(null /*new ActionEvent(kf, null)*/);
				} catch (Throwable ex) {
					//Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), ex);
					ex.printStackTrace();
				}
			}
		}
	}
}
