package javafx.scene.layout;

/**
 * Defines the radii of each of the four corners of a BorderStroke. The
 * CornerRadii class is immutable and therefore can be reused on multiple
 * BorderStrokes. This class defines 8 different values, corresponding
 * to the horizontal and vertical components of 4 quarter ellipses, which
 * in turn define the curvature of the corners of the BorderStroke.
 *
 * @since JavaFX 8.0
 */
public class CornerRadii {
	/**
	 * A CornerRadii which is entirely empty, indicating squared corners.
	 * This is the default value for a BorderStroke's radii.
	 */
	public static final CornerRadii EMPTY = new CornerRadii(0, 0, 0, 0, 0, 0, 0, 0, false, false, false, false, false,
			false, false, false);

	/**
	 * The length of the horizontal radii of the top-left corner.
	 */
	public final double getTopLeftHorizontalRadius() {
		return topLeftHorizontalRadius;
	}

	private double topLeftHorizontalRadius;

	/**
	 * The length of the vertical radii of the top-left corner.
	 */
	public final double getTopLeftVerticalRadius() {
		return topLeftVerticalRadius;
	}

	private double topLeftVerticalRadius;

	/**
	 * The length of the vertical radii of the top-right corner.
	 */
	public final double getTopRightVerticalRadius() {
		return topRightVerticalRadius;
	}

	private double topRightVerticalRadius;

	/**
	 * The length of the horizontal radii of the top-right corner.
	 */
	public final double getTopRightHorizontalRadius() {
		return topRightHorizontalRadius;
	}

	private double topRightHorizontalRadius;

	/**
	 * The length of the horizontal radii of the bottom-right corner.
	 */
	public final double getBottomRightHorizontalRadius() {
		return bottomRightHorizontalRadius;
	}

	private double bottomRightHorizontalRadius;

	/**
	 * The length of the vertical radii of the bottom-right corner.
	 */
	public final double getBottomRightVerticalRadius() {
		return bottomRightVerticalRadius;
	}

	private double bottomRightVerticalRadius;

	/**
	 * The length of the vertical radii of the bottom-left corner.
	 */
	public final double getBottomLeftVerticalRadius() {
		return bottomLeftVerticalRadius;
	}

	private double bottomLeftVerticalRadius;

	/**
	 * The length of the horizontal radii of the bottom-left corner.
	 */
	public final double getBottomLeftHorizontalRadius() {
		return bottomLeftHorizontalRadius;
	}

	private double bottomLeftHorizontalRadius;

	/**
	 * indicates whether {@code topLeftHorizontalRadius} is interpreted as a value or a percentage.
	 */
	public final boolean isTopLeftHorizontalRadiusAsPercentage() {
		return topLeftHorizontalRadiusAsPercentage;
	}

	private final boolean topLeftHorizontalRadiusAsPercentage;

	/**
	 * indicates whether {@code topLeftVerticalRadius} is interpreted as a value or a percentage.
	 */
	public final boolean isTopLeftVerticalRadiusAsPercentage() {
		return topLeftVerticalRadiusAsPercentage;
	}

	private final boolean topLeftVerticalRadiusAsPercentage;

	/**
	 * indicates whether {@code topRightVerticalRadius} is interpreted as a value or a percentage.
	 */
	public final boolean isTopRightVerticalRadiusAsPercentage() {
		return topRightVerticalRadiusAsPercentage;
	}

	private final boolean topRightVerticalRadiusAsPercentage;

	/**
	 * indicates whether {@code topRightHorizontalRadius} is interpreted as a value or a percentage.
	 */
	public final boolean isTopRightHorizontalRadiusAsPercentage() {
		return topRightHorizontalRadiusAsPercentage;
	}

	private final boolean topRightHorizontalRadiusAsPercentage;

	/**
	 * indicates whether {@code bottomRightHorizontalRadius} is interpreted as a value or a percentage.
	 */
	public final boolean isBottomRightHorizontalRadiusAsPercentage() {
		return bottomRightHorizontalRadiusAsPercentage;
	}

	private final boolean bottomRightHorizontalRadiusAsPercentage;

	/**
	 * indicates whether {@code bottomRightVerticalRadius} is interpreted as a value or a percentage.
	 */
	public final boolean isBottomRightVerticalRadiusAsPercentage() {
		return bottomRightVerticalRadiusAsPercentage;
	}

	private final boolean bottomRightVerticalRadiusAsPercentage;

	/**
	 * indicates whether {@code bottomLeftVerticalRadius} is interpreted as a value or a percentage.
	 */
	public final boolean isBottomLeftVerticalRadiusAsPercentage() {
		return bottomLeftVerticalRadiusAsPercentage;
	}

	private final boolean bottomLeftVerticalRadiusAsPercentage;

	/**
	 * indicates whether {@code bottomLeftHorizontalRadius} is interpreted as a value or a percentage.
	 */
	public final boolean isBottomLeftHorizontalRadiusAsPercentage() {
		return bottomLeftHorizontalRadiusAsPercentage;
	}

	private final boolean bottomLeftHorizontalRadiusAsPercentage;

	final boolean hasPercentBasedRadii;

	/**
	 * Indicates whether each corner radius is exactly the same, and each are either uniformly percentage-based
	 * or not.
	 */
	public final boolean isUniform() {
		return uniform;
	}

	final boolean uniform;

	/**
	 * The cached hash code.
	 */
	private final int hash;

	/**
	 * Create a new CornerRadii with a single uniform radii value for all components of all
	 * corners. This constructor will create the CornerRadii such that none of the values are
	 * percentages.
	 *
	 * @param radius    The radii for each corner. Negative values are not allowed.
	 */
	public CornerRadii(double radius) {
		// As per the CSS Spec 5.1
		if (radius < 0) {
			throw new IllegalArgumentException("The radii value may not be < 0");
		}
		this.topLeftHorizontalRadius = this.topLeftVerticalRadius = this.topRightVerticalRadius = this.topRightHorizontalRadius = this.bottomRightHorizontalRadius = this.bottomRightVerticalRadius = this.bottomLeftVerticalRadius = this.bottomLeftHorizontalRadius = radius;

		this.topLeftHorizontalRadiusAsPercentage = this.topLeftVerticalRadiusAsPercentage = this.topRightVerticalRadiusAsPercentage = this.topRightHorizontalRadiusAsPercentage = this.bottomRightHorizontalRadiusAsPercentage = this.bottomRightVerticalRadiusAsPercentage = this.bottomLeftVerticalRadiusAsPercentage = this.bottomLeftHorizontalRadiusAsPercentage = false;

		hasPercentBasedRadii = false;
		uniform = true;
		this.hash = preComputeHash();
	}

	/**
	 * Create a new CornerRadii with the given radii for each corner. The value is
	 * interpreted either as being a percentage or not based on the {@code asPercent}
	 * argument.
	 *
	 * @param radius       The radii for each corner. Negative values are not allowed.
	 * @param asPercent    Whether the radii should be interpreted as a percentage.
	 */
	public CornerRadii(double radius, boolean asPercent) {
		if (radius < 0) {
			throw new IllegalArgumentException("The radii value may not be < 0");
		}
		this.topLeftHorizontalRadius = this.topLeftVerticalRadius = this.topRightVerticalRadius = this.topRightHorizontalRadius = this.bottomRightHorizontalRadius = this.bottomRightVerticalRadius = this.bottomLeftVerticalRadius = this.bottomLeftHorizontalRadius = radius;

		this.topLeftHorizontalRadiusAsPercentage = this.topLeftVerticalRadiusAsPercentage = this.topRightVerticalRadiusAsPercentage = this.topRightHorizontalRadiusAsPercentage = this.bottomRightHorizontalRadiusAsPercentage = this.bottomRightVerticalRadiusAsPercentage = this.bottomLeftVerticalRadiusAsPercentage = this.bottomLeftHorizontalRadiusAsPercentage = asPercent;

		uniform = true;
		hasPercentBasedRadii = asPercent;
		this.hash = preComputeHash();
	}

	/**
	 * Create a new CornerRadii with uniform yet independent radii for each corner. That is, each corner
	 * can be specified independently, but the horizontal and vertical components of each corner is uniform.
	 *
	 * @param topLeft        The radii of the top-left corner. Negative numbers are not allowed.
	 * @param topRight       The radii of the top-right corner. Negative numbers are not allowed.
	 * @param bottomRight    The radii of the bottom-right corner. Negative numbers are not allowed.
	 * @param bottomLeft     The radii of the bottom-left corner. Negative numbers are not allowed.
	 * @param asPercent      Whether all four radii should be considered as values or percentages
	 */
	public CornerRadii(double topLeft, double topRight, double bottomRight, double bottomLeft, boolean asPercent) {
		if (topLeft < 0 || topRight < 0 || bottomRight < 0 || bottomLeft < 0) {
			throw new IllegalArgumentException("No radii value may be < 0");
		}

		this.topLeftHorizontalRadius = this.topLeftVerticalRadius = topLeft;
		this.topRightVerticalRadius = this.topRightHorizontalRadius = topRight;
		this.bottomRightHorizontalRadius = this.bottomRightVerticalRadius = bottomRight;
		this.bottomLeftVerticalRadius = this.bottomLeftHorizontalRadius = bottomLeft;
		this.topLeftHorizontalRadiusAsPercentage = this.topLeftVerticalRadiusAsPercentage = this.topRightVerticalRadiusAsPercentage = this.topRightHorizontalRadiusAsPercentage = this.bottomRightHorizontalRadiusAsPercentage = this.bottomRightVerticalRadiusAsPercentage = this.bottomLeftVerticalRadiusAsPercentage = this.bottomLeftHorizontalRadiusAsPercentage = asPercent;

		uniform = topLeft == topRight && topLeft == bottomLeft && topLeft == bottomRight;
		hasPercentBasedRadii = asPercent;
		this.hash = preComputeHash();
	}

	/**
	 * Creates a new CornerRadii, allowing for specification of each component of each corner
	 * radii and whether each component should be treated as a value or percentage.
	 *
	 * @param topLeftHorizontalRadius
	 * @param topLeftVerticalRadius
	 * @param topRightVerticalRadius
	 * @param topRightHorizontalRadius
	 * @param bottomRightHorizontalRadius
	 * @param bottomRightVerticalRadius
	 * @param bottomLeftVerticalRadius
	 * @param bottomLeftHorizontalRadius
	 * @param topLeftHorizontalRadiusAsPercent
	 * @param topLeftVerticalRadiusAsPercent
	 * @param topRightVerticalRadiusAsPercent
	 * @param topRightHorizontalRadiusAsPercent
	 * @param bottomRightHorizontalRadiusAsPercent
	 * @param bottomRightVerticalRadiusAsPercent
	 * @param bottomLeftVerticalRadiusAsPercent
	 * @param bottomLeftHorizontalRadiusAsPercent
	 */
	public CornerRadii(double topLeftHorizontalRadius, double topLeftVerticalRadius, double topRightVerticalRadius,
			double topRightHorizontalRadius, double bottomRightHorizontalRadius, double bottomRightVerticalRadius,
			double bottomLeftVerticalRadius, double bottomLeftHorizontalRadius,
			boolean topLeftHorizontalRadiusAsPercent, boolean topLeftVerticalRadiusAsPercent,
			boolean topRightVerticalRadiusAsPercent, boolean topRightHorizontalRadiusAsPercent,
			boolean bottomRightHorizontalRadiusAsPercent, boolean bottomRightVerticalRadiusAsPercent,
			boolean bottomLeftVerticalRadiusAsPercent, boolean bottomLeftHorizontalRadiusAsPercent) {
		if (topLeftHorizontalRadius < 0 || topLeftVerticalRadius < 0 || topRightVerticalRadius < 0
				|| topRightHorizontalRadius < 0 || bottomRightHorizontalRadius < 0 || bottomRightVerticalRadius < 0
				|| bottomLeftVerticalRadius < 0 || bottomLeftHorizontalRadius < 0) {
			throw new IllegalArgumentException("No radii value may be < 0");
		}
		this.topLeftHorizontalRadius = topLeftHorizontalRadius;
		this.topLeftVerticalRadius = topLeftVerticalRadius;
		this.topRightVerticalRadius = topRightVerticalRadius;
		this.topRightHorizontalRadius = topRightHorizontalRadius;
		this.bottomRightHorizontalRadius = bottomRightHorizontalRadius;
		this.bottomRightVerticalRadius = bottomRightVerticalRadius;
		this.bottomLeftVerticalRadius = bottomLeftVerticalRadius;
		this.bottomLeftHorizontalRadius = bottomLeftHorizontalRadius;
		this.topLeftHorizontalRadiusAsPercentage = topLeftHorizontalRadiusAsPercent;
		this.topLeftVerticalRadiusAsPercentage = topLeftVerticalRadiusAsPercent;
		this.topRightVerticalRadiusAsPercentage = topRightVerticalRadiusAsPercent;
		this.topRightHorizontalRadiusAsPercentage = topRightHorizontalRadiusAsPercent;
		this.bottomRightHorizontalRadiusAsPercentage = bottomRightHorizontalRadiusAsPercent;
		this.bottomRightVerticalRadiusAsPercentage = bottomRightVerticalRadiusAsPercent;
		this.bottomLeftVerticalRadiusAsPercentage = bottomLeftVerticalRadiusAsPercent;
		this.bottomLeftHorizontalRadiusAsPercentage = bottomLeftHorizontalRadiusAsPercent;
		this.hash = preComputeHash();
		hasPercentBasedRadii = topLeftHorizontalRadiusAsPercent || topLeftVerticalRadiusAsPercent
				|| topRightVerticalRadiusAsPercent || topRightHorizontalRadiusAsPercent
				|| bottomRightHorizontalRadiusAsPercent || bottomRightVerticalRadiusAsPercent
				|| bottomLeftVerticalRadiusAsPercent || bottomLeftHorizontalRadiusAsPercent;
		uniform = topLeftHorizontalRadius == topRightHorizontalRadius && topLeftVerticalRadius == topRightVerticalRadius
				&& topLeftHorizontalRadius == bottomRightHorizontalRadius
				&& topLeftVerticalRadius == bottomRightVerticalRadius
				&& topLeftHorizontalRadius == bottomLeftHorizontalRadius
				&& topLeftVerticalRadius == bottomLeftVerticalRadius
				&& topLeftHorizontalRadiusAsPercent == topRightHorizontalRadiusAsPercent
				&& topLeftVerticalRadiusAsPercent == topRightVerticalRadiusAsPercent
				&& topLeftHorizontalRadiusAsPercent == bottomRightHorizontalRadiusAsPercent
				&& topLeftVerticalRadiusAsPercent == bottomRightVerticalRadiusAsPercent
				&& topLeftHorizontalRadiusAsPercent == bottomLeftHorizontalRadiusAsPercent
				&& topLeftVerticalRadiusAsPercent == bottomLeftVerticalRadiusAsPercent;
	}

	private int preComputeHash() {
		int result;
		long temp;
		temp = topLeftHorizontalRadius != +0.0d ? Double.doubleToLongBits(topLeftHorizontalRadius) : 0L;
		result = (int) (temp ^ (temp >>> 32));
		temp = topLeftVerticalRadius != +0.0d ? Double.doubleToLongBits(topLeftVerticalRadius) : 0L;
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		temp = topRightVerticalRadius != +0.0d ? Double.doubleToLongBits(topRightVerticalRadius) : 0L;
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		temp = topRightHorizontalRadius != +0.0d ? Double.doubleToLongBits(topRightHorizontalRadius) : 0L;
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		temp = bottomRightHorizontalRadius != +0.0d ? Double.doubleToLongBits(bottomRightHorizontalRadius) : 0L;
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		temp = bottomRightVerticalRadius != +0.0d ? Double.doubleToLongBits(bottomRightVerticalRadius) : 0L;
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		temp = bottomLeftVerticalRadius != +0.0d ? Double.doubleToLongBits(bottomLeftVerticalRadius) : 0L;
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		temp = bottomLeftHorizontalRadius != +0.0d ? Double.doubleToLongBits(bottomLeftHorizontalRadius) : 0L;
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		result = 31 * result + (topLeftHorizontalRadiusAsPercentage ? 1 : 0);
		result = 31 * result + (topLeftVerticalRadiusAsPercentage ? 1 : 0);
		result = 31 * result + (topRightVerticalRadiusAsPercentage ? 1 : 0);
		result = 31 * result + (topRightHorizontalRadiusAsPercentage ? 1 : 0);
		result = 31 * result + (bottomRightHorizontalRadiusAsPercentage ? 1 : 0);
		result = 31 * result + (bottomRightVerticalRadiusAsPercentage ? 1 : 0);
		result = 31 * result + (bottomLeftVerticalRadiusAsPercentage ? 1 : 0);
		result = 31 * result + (bottomLeftHorizontalRadiusAsPercentage ? 1 : 0);
		result = 31 * result + result;
		return result;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		CornerRadii that = (CornerRadii) o;
		if (this.hash != that.hash)
			return false;

		if (Double.compare(that.bottomLeftHorizontalRadius, bottomLeftHorizontalRadius) != 0)
			return false;
		if (bottomLeftHorizontalRadiusAsPercentage != that.bottomLeftHorizontalRadiusAsPercentage)
			return false;
		if (Double.compare(that.bottomLeftVerticalRadius, bottomLeftVerticalRadius) != 0)
			return false;
		if (bottomLeftVerticalRadiusAsPercentage != that.bottomLeftVerticalRadiusAsPercentage)
			return false;
		if (Double.compare(that.bottomRightVerticalRadius, bottomRightVerticalRadius) != 0)
			return false;
		if (bottomRightVerticalRadiusAsPercentage != that.bottomRightVerticalRadiusAsPercentage)
			return false;
		if (Double.compare(that.bottomRightHorizontalRadius, bottomRightHorizontalRadius) != 0)
			return false;
		if (bottomRightHorizontalRadiusAsPercentage != that.bottomRightHorizontalRadiusAsPercentage)
			return false;
		if (Double.compare(that.topLeftVerticalRadius, topLeftVerticalRadius) != 0)
			return false;
		if (topLeftVerticalRadiusAsPercentage != that.topLeftVerticalRadiusAsPercentage)
			return false;
		if (Double.compare(that.topLeftHorizontalRadius, topLeftHorizontalRadius) != 0)
			return false;
		if (topLeftHorizontalRadiusAsPercentage != that.topLeftHorizontalRadiusAsPercentage)
			return false;
		if (Double.compare(that.topRightHorizontalRadius, topRightHorizontalRadius) != 0)
			return false;
		if (topRightHorizontalRadiusAsPercentage != that.topRightHorizontalRadiusAsPercentage)
			return false;
		if (Double.compare(that.topRightVerticalRadius, topRightVerticalRadius) != 0)
			return false;
		if (topRightVerticalRadiusAsPercentage != that.topRightVerticalRadiusAsPercentage)
			return false;

		return true;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public int hashCode() {
		return hash;
	}

	@Override
	public String toString() {
		if (isUniform()) {
			return "CornerRadii [uniform radius = " + topLeftHorizontalRadius + "]";
		}

		return "CornerRadii ["
				+ (topLeftHorizontalRadius == topLeftVerticalRadius ? "topLeft=" + topLeftHorizontalRadius
						: "topLeftHorizontalRadius=" + topLeftHorizontalRadius + ", topLeftVerticalRadius="
								+ topLeftVerticalRadius)
				+ (topRightHorizontalRadius == topRightVerticalRadius ? ", topRight=" + topRightHorizontalRadius
						: ", topRightVerticalRadius=" + topRightVerticalRadius + ", topRightHorizontalRadius="
								+ topRightHorizontalRadius)
				+ (bottomRightHorizontalRadius == bottomRightVerticalRadius
						? ", bottomRight=" + bottomRightHorizontalRadius
						: ", bottomRightHorizontalRadius=" + bottomRightHorizontalRadius
								+ ", bottomRightVerticalRadius=" + bottomRightVerticalRadius)
				+ (bottomLeftHorizontalRadius == bottomLeftVerticalRadius ? ", bottomLeft=" + bottomLeftHorizontalRadius
						: ", bottomLeftVerticalRadius=" + bottomLeftVerticalRadius + ", bottomLeftHorizontalRadius="
								+ bottomLeftHorizontalRadius)
				+
				//                ", topLeftHorizontalRadiusAsPercentage=" + topLeftHorizontalRadiusAsPercentage +
				//                ", topLeftVerticalRadiusAsPercentage=" + topLeftVerticalRadiusAsPercentage +
				//                ", topRightVerticalRadiusAsPercentage=" + topRightVerticalRadiusAsPercentage +
				//                ", topRightHorizontalRadiusAsPercentage=" + topRightHorizontalRadiusAsPercentage +
				//                ", bottomRightHorizontalRadiusAsPercentage=" + bottomRightHorizontalRadiusAsPercentage +
				//                ", bottomRightVerticalRadiusAsPercentage=" + bottomRightVerticalRadiusAsPercentage +
				//                ", bottomLeftVerticalRadiusAsPercentage=" + bottomLeftVerticalRadiusAsPercentage +
				//                ", bottomLeftHorizontalRadiusAsPercentage=" + bottomLeftHorizontalRadiusAsPercentage +
				//                ", hasPercentBasedRadii=" + hasPercentBasedRadii +
				//                ", uniform=" + uniform +
				']';
	}
}
