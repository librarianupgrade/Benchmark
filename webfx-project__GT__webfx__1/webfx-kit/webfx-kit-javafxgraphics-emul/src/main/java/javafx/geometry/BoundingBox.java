package javafx.geometry;

import com.sun.javafx.geom.Point2D;

/**
 * A rectangular bounding box which is used to describe the bounds of a node
 * or other scene graph object.
 * @since JavaFX 2.0
 */
public class BoundingBox extends Bounds {
	/**
	 * Cache the hash code to make computing hashes faster.
	 */
	private int hash = 0;

	/**
	 * Creates a new instance of 3D {@code BoundingBox}.
	 * @param minX the X coordinate of the upper-left corner
	 * @param minY the Y coordinate of the upper-left corner
	 * @param minZ the minimum z coordinate of the {@code BoundingBox}
	 * @param width the width of the {@code BoundingBox}
	 * @param height the height of the {@code BoundingBox}
	 * @param depth the depth of the {@code BoundingBox}
	 */
	public BoundingBox(double minX, double minY, double minZ, double width, double height, double depth) {
		super(minX, minY, minZ, width, height, depth);
	}

	/**
	 * Creates a new instance of 2D {@code BoundingBox}.
	 * @param minX the X coordinate of the upper-left corner
	 * @param minY the Y coordinate of the upper-left corner
	 * @param width the width of the {@code BoundingBox}
	 * @param height the height of the {@code BoundingBox}
	 */
	public BoundingBox(double minX, double minY, double width, double height) {
		super(minX, minY, 0, width, height, 0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEmpty() {
		return getMaxX() < getMinX() || getMaxY() < getMinY() || getMaxZ() < getMinZ();
	}

	/**
	 * {@inheritDoc}
	 * The points on the boundary are considered to lie inside the {@code BoundingBox}.
	 */
	@Override
	public boolean contains(Point2D p) {
		if (p == null)
			return false;
		return contains(p.x, p.y, 0.0f);
	}

	/**
	 * {@inheritDoc}
	 * The points on the boundary are considered to lie inside the {@code BoundingBox}.
	 */
	/*
	@Override public boolean contains(Point3D p) {
	    if (p == null) return false;
	    return contains(p.getX(), p.getY(), p.getZ());
	}
	*/

	/**
	 * {@inheritDoc}
	 * The points on the boundary are considered to lie inside the {@code BoundingBox}.
	 */
	@Override
	public boolean contains(double x, double y) {
		return contains(x, y, 0.0f);
	}

	/**
	 * {@inheritDoc}
	 * The points on the boundary are considered to lie inside the {@code BoundingBox}.
	 */
	@Override
	public boolean contains(double x, double y, double z) {
		if (isEmpty())
			return false;
		return x >= getMinX() && x <= getMaxX() && y >= getMinY() && y <= getMaxY() && z >= getMinZ() && z <= getMaxZ();
	}

	/**
	 * {@inheritDoc}
	 * The points on the boundary are considered to lie inside the {@code BoundingBox}.
	 */
	@Override
	public boolean contains(Bounds b) {
		if ((b == null) || b.isEmpty())
			return false;
		return contains(b.getMinX(), b.getMinY(), b.getMinZ(), b.getWidth(), b.getHeight(), b.getDepth());
	}

	/**
	 * {@inheritDoc}
	 * The points on the boundary are considered to lie inside the {@code BoundingBox}.
	 */
	@Override
	public boolean contains(double x, double y, double w, double h) {
		return contains(x, y) && contains(x + w, y + h);

	}

	/**
	 * {@inheritDoc}
	 * The points on the boundary are considered to lie inside the {@code BoundingBox}.
	 */
	@Override
	public boolean contains(double x, double y, double z, double w, double h, double d) {
		return contains(x, y, z) && contains(x + w, y + h, z + d);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean intersects(Bounds b) {
		if ((b == null) || b.isEmpty())
			return false;
		return intersects(b.getMinX(), b.getMinY(), b.getMinZ(), b.getWidth(), b.getHeight(), b.getDepth());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean intersects(double x, double y, double w, double h) {
		return intersects(x, y, 0, w, h, 0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean intersects(double x, double y, double z, double w, double h, double d) {
		if (isEmpty() || w < 0 || h < 0 || d < 0)
			return false;
		return (x + w >= getMinX() && y + h >= getMinY() && z + d >= getMinZ() && x <= getMaxX() && y <= getMaxY()
				&& z <= getMaxZ());
	}

	/**
	 * Indicates whether some other object is "equal to" this one.
	 *
	 * @param obj the reference object with which to compare
	 * @return true if this object is the same as the obj argument; false otherwise
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof BoundingBox) {
			BoundingBox other = (BoundingBox) obj;
			return getMinX() == other.getMinX() && getMinY() == other.getMinY() && getMinZ() == other.getMinZ()
					&& getWidth() == other.getWidth() && getHeight() == other.getHeight()
					&& getDepth() == other.getDepth();
		} else
			return false;
	}

	/**
	 * Returns a hash code value for the object.
	 * @return a hash code value for the object.
	 */
	@Override
	public int hashCode() {
		if (hash == 0) {
			long bits = 7L;
			bits = 31L * bits + Double.doubleToLongBits(getMinX());
			bits = 31L * bits + Double.doubleToLongBits(getMinY());
			bits = 31L * bits + Double.doubleToLongBits(getMinZ());
			bits = 31L * bits + Double.doubleToLongBits(getWidth());
			bits = 31L * bits + Double.doubleToLongBits(getHeight());
			bits = 31L * bits + Double.doubleToLongBits(getDepth());
			hash = (int) (bits ^ (bits >> 32));
		}
		return hash;
	}

	/**
	 * Returns a string representation of this {@code BoundingBox}.
	 * This method is intended to be used only for informational purposes.
	 * The content and format of the returned string might getMary between
	 * implementations.
	 * The returned string might be empty but cannot be {@code null}.
	 */
	@Override
	public String toString() {
		return "BoundingBox [" + "minX:" + getMinX() + ", minY:" + getMinY() + ", minZ:" + getMinZ() + ", width:"
				+ getWidth() + ", height:" + getHeight() + ", depth:" + getDepth() + ", maxX:" + getMaxX() + ", maxY:"
				+ getMaxY() + ", maxZ:" + getMaxZ() + "]";
	}
}
