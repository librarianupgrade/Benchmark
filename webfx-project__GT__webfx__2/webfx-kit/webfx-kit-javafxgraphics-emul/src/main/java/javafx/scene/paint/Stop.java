package javafx.scene.paint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Defines one element of the ramp of colors to use on a gradient.
 * For more information see {@code LinearGradient} and
 * {@code RadialGradient}.
 *
 * <p>Example:</p>
 * <pre><code>
 * // object bounding box relative (proportional:true, default)
 * Stop[] stops = { new Stop(0, Color.WHITE), new Stop(1, Color.BLACK)};
 * LinearGradient lg = new LinearGradient(0, 0, 1, 0, true, CycleMethod.No_CYCLE, stops);
 * Rectangle r = new Rectangle();
 * r.setFill(lg);
 *</code></pre>
 * @since JavaFX 2.0
 */
public final class Stop {

	/**
	 * The {@code offset} variable is a number ranging from {@code 0} to {@code 1}
	 * that indicates where this gradient stop is placed. For linear gradients,
	 * the {@code offset} variable represents a location along the gradient vector.
	 * For radial gradients, it represents a percentage distance from
	 * the focus point to the edge of the outermost/largest circle.
	 *
	 * @profile common
	 * @defaultValue 0.0
	 */
	private double offset;

	/**
	 * Gets a number ranging from {@code 0} to {@code 1}
	 * that indicates where this gradient stop is placed. For linear gradients,
	 * the {@code offset} variable represents a location along the gradient vector.
	 * For radial gradients, it represents a percentage distance from
	 * the focus point to the edge of the outermost/largest circle.
	 *
	 * @return position of the Stop within the gradient
	 *         (ranging from {@code 0} to {@code 1})
	 */
	public final double getOffset() {
		return offset;
	}

	/**
	 * The color of the gradient at this offset.
	 *
	 * @profile common
	 * @defaultValue Color.BLACK
	 */
	private Color color;

	/**
	 * Gets the color of the gradient at this offset.
	 * @return the color of the gradient at this offset
	 */
	public final Color getColor() {
		return color;
	}

	private static List<Stop> NO_STOPS = Collections
			.unmodifiableList(Arrays.asList(new Stop(0.0, Color.TRANSPARENT), (Stop) new Stop(1.0, Color.TRANSPARENT)));

	public static List<Stop> normalize(Stop stops[]) {
		return normalize((stops == null ? null : Arrays.asList(stops)));
	}

	public static List<Stop> normalize(List<Stop> stops) {
		if (stops == null)
			return NO_STOPS;
		Stop zerostop = null;
		Stop onestop = null;
		List<Stop> newlist = new ArrayList<Stop>(stops.size());
		for (Stop s : stops) {
			if (s == null || s.getColor() == null)
				continue;
			double off = s.getOffset();
			if (off <= 0.0) {
				if (zerostop == null || off >= zerostop.getOffset())
					zerostop = s;
			} else if (off >= 1.0) {
				if (onestop == null || off < onestop.getOffset())
					onestop = s;
			} else if (off == off) { // non-NaN
				for (int i = newlist.size() - 1; i >= 0; i--) {
					Stop s2 = newlist.get(i);
					if (s2.getOffset() <= off) {
						if (s2.getOffset() == off) {
							if (i > 0 && newlist.get(i - 1).getOffset() == off)
								newlist.set(i, s);
							else
								newlist.add(i + 1, s);
						} else
							newlist.add(i + 1, s);
						s = null;
						break;
					}
				}
				if (s != null)
					newlist.add(0, s);
			}
		}

		if (zerostop == null) {
			Color zerocolor;
			if (newlist.isEmpty()) {
				if (onestop == null)
					return NO_STOPS;
				zerocolor = onestop.getColor();
			} else {
				zerocolor = newlist.get(0).getColor();
				if (onestop == null && newlist.size() == 1) {
					// Special case for a single color with a non-0,1 offset.
					// If we leave the color in there we end up with a 3-color
					// gradient with all the colors being identical and we
					// will not catch the optimization to a solid color.
					newlist.clear();
				}
			}
			zerostop = new Stop(0.0, zerocolor);
		} else if (zerostop.getOffset() < 0.0)
			zerostop = new Stop(0.0, zerostop.getColor());
		newlist.add(0, zerostop);

		if (onestop == null)
			onestop = new Stop(1.0, newlist.get(newlist.size() - 1).getColor());
		else if (onestop.getOffset() > 1.0)
			onestop = new Stop(1.0, onestop.getColor());
		newlist.add(onestop);

		return Collections.unmodifiableList(newlist);
	}

	/**
	 * The cached hash code, used to improve performance in situations where
	 * we cache gradients, such as in the CSS routines.
	 */
	private int hash = 0;

	/**
	 * Creates a new instance of Stop.
	 * @param offset Stop's position (ranging from {@code 0} to {@code 1}
	 * @param color Stop's color
	 */
	public Stop(double offset, Color color) {
		this.offset = offset;
		this.color = color;
	}

	/**
	 * Indicates whether some other object is "equal to" this one.
	 * @param obj the reference object with which to compare.
	 * @return {@code true} if this object is equal to the {@code obj} argument; {@code false} otherwise.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (obj instanceof Stop) {
			Stop other = (Stop) obj;
			return offset == other.offset && (color == null ? other.color == null : color.equals(other.color));
		} else
			return false;
	}

	/**
	 * Returns a hash code for this {@code Stop} object.
	 * @return a hash code for this {@code Stop} object.
	 */
	@Override
	public int hashCode() {
		if (hash == 0) {
			long bits = 17L;
			bits = 37L * bits + Double.doubleToLongBits(offset);
			bits = 37L * bits + color.hashCode();
			hash = (int) (bits ^ (bits >> 32));
		}
		return hash;
	}

	/**
	 * Returns a string representation of this {@code Stop} object.
	 * @return a string representation of this {@code Stop} object.
	 */
	@Override
	public String toString() {
		return color + " " + offset * 100 + "%";
	}
}
