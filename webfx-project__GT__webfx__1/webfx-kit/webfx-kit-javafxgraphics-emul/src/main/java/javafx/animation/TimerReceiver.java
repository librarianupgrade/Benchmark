package javafx.animation;

/**
 * A TimerReceiver receives per-frame pulses from the MasterTimer.
 */
public interface TimerReceiver {
	/**
	 * Callback triggered to send a message to the TimerReceiver
	 *
	 * @param now
	 *            The timestamp of the current frame given in nanoseconds. This
	 *            value will be the same for all {@code AnimationTimers} called
	 *            during one frame.
	 */
	public void handle(long now);
}
