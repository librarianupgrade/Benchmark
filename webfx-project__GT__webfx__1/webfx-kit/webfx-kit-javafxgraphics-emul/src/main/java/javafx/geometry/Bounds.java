package javafx.geometry;

import com.sun.javafx.geom.Point2D;

/**
 * The base class for objects that are used to describe the bounds of a node or
 * other scene graph object. One interesting characteristic of a Bounds object
 * is that it may have a negative width, height, or depth. A negative value
 * for any of these indicates that the Bounds are "empty".
 *
 * @since JavaFX 2.0
 */
public abstract class Bounds {
	/**
	 * The x coordinate of the upper-left corner of this {@code Bounds}.
	 *
	 * @defaultValue 0.0
	 */
	public final double getMinX() {
		return minX;
	}

	private double minX;

	/**
	 * The y coordinate of the upper-left corner of this {@code Bounds}.
	 *
	 * @defaultValue 0.0
	 */
	public final double getMinY() {
		return minY;
	}

	private double minY;

	/**
	 * The minimum z coordinate of this {@code Bounds}.
	 *
	 * @defaultValue 0.0
	 */
	public final double getMinZ() {
		return minZ;
	}

	private double minZ;

	/**
	 * The width of this {@code Bounds}.
	 *
	 * @defaultValue 0.0
	 */
	public final double getWidth() {
		return width;
	}

	private double width;

	/**
	 * The height of this {@code Bounds}.
	 *
	 * @defaultValue 0.0
	 */
	public final double getHeight() {
		return height;
	}

	private double height;

	/**
	 * The depth of this {@code Bounds}.
	 *
	 * @defaultValue 0.0
	 */
	public final double getDepth() {
		return depth;
	}

	private double depth;

	/**
	 * The x coordinate of the lower-right corner of this {@code Bounds}.
	 *
	 * @defaultValue {@code minX + width}
	 */
	public final double getMaxX() {
		return maxX;
	}

	private double maxX;

	/**
	 * The y coordinate of the lower-right corner of this {@code Bounds}.
	 *
	 * @defaultValue {@code minY + height}
	 */
	public final double getMaxY() {
		return maxY;
	}

	private double maxY;

	/**
	 * The maximum z coordinate of this {@code Bounds}.
	 *
	 * @defaultValue {@code minZ + depth}
	 */
	public final double getMaxZ() {
		return maxZ;
	}

	private double maxZ;

	/**
	 * Indicates whether any of the dimensions(width, height or depth) of this bounds
	 * is less than zero.
	 * @return true if any of the dimensions(width, height or depth) of this bounds
	 * is less than zero.
	 */
	public abstract boolean isEmpty();

	/**
	 * Tests if the specified point is inside the boundary of {@code Bounds}.
	 *
	 * @param p the specified point to be tested
	 * @return true if the specified point is inside the boundary of this
	 * {@code Bounds}; false otherwise.
	 */
	public abstract boolean contains(Point2D p);

	/**
	 * Tests if the specified point is inside the boundary of {@code Bounds}.
	 *
	 * @param p the specified 3D point to be tested
	 * @return true if the specified point is inside the boundary of this
	 * {@code Bounds}; false otherwise.
	 */
	//public abstract boolean contains(Point3D p);

	/**
	 * Tests if the specified {@code (x, y)} coordinates are inside the boundary
	 * of {@code Bounds}.
	 *
	 * @param x the specified x coordinate to be tested
	 * @param y the specified y coordinate to be tested
	 * @return true if the specified {@code (x, y)} coordinates are inside the
	 * boundary of this {@code Bounds}; false otherwise.
	 */
	public abstract boolean contains(double x, double y);

	/**
	 * Tests if the specified {@code (x, y, z)} coordinates are inside the boundary
	 * of {@code Bounds}.
	 *
	 * @param x the specified x coordinate to be tested
	 * @param y the specified y coordinate to be tested
	 * @return true if the specified {@code (x, y)} coordinates are inside the
	 * boundary of this {@code Bounds}; false otherwise.
	 */
	public abstract boolean contains(double x, double y, double z);

	/**
	 * Tests if the interior of this {@code Bounds} entirely contains the
	 * specified Bounds, {@code b}.
	 *
	 * @param b The specified Bounds
	 * @return true if the specified Bounds, {@code b}, is inside the
	 * boundary of this {@code Bounds}; false otherwise.
	 */
	public abstract boolean contains(Bounds b);

	/**
	 * Tests if the interior of this {@code Bounds} entirely contains the
	 * specified rectangular area.
	 *
	 * @param x the x coordinate of the upper-left corner of the specified
	 * rectangular area
	 * @param y the y coordinate of the upper-left corner of the specified
	 * rectangular area
	 * @param w the width of the specified rectangular area
	 * @param h the height of the specified rectangular area
	 * @return true if the interior of this {@code Bounds} entirely contains
	 * the specified rectangular area; false otherwise.
	 */
	public abstract boolean contains(double x, double y, double w, double h);

	/**
	 * Tests if the interior of this {@code Bounds} entirely contains the
	 * specified rectangular area.
	 *
	 * @param x the x coordinate of the upper-left corner of the specified
	 * rectangular volume
	 * @param y the y coordinate of the upper-left corner of the specified
	 * rectangular volume
	 * @param z the z coordinate of the upper-left corner of the specified
	 * rectangular volume
	 * @param w the width of the specified rectangular volume
	 * @param h the height of the specified rectangular volume
	 * @param d the depth of the specified rectangular volume
	 * @return true if the interior of this {@code Bounds} entirely contains
	 * the specified rectangular area; false otherwise.
	 */
	public abstract boolean contains(double x, double y, double z, double w, double h, double d);

	/**
	 * Tests if the interior of this {@code Bounds} intersects the interior
	 * of a specified Bounds, {@code b}.
	 *
	 * @param b The specified Bounds
	 * @return true if the interior of this {@code Bounds} and the interior
	 * of the specified Bounds, {@code b}, intersect.
	 */
	public abstract boolean intersects(Bounds b);

	/**
	 * Tests if the interior of this {@code Bounds} intersects the interior
	 * of a specified rectangular area.
	 *
	 * @param x the x coordinate of the upper-left corner of the specified
	 * rectangular area
	 * @param y the y coordinate of the upper-left corner of the specified
	 * rectangular area
	 * @param w the width of the specified rectangular area
	 * @param h the height of the specified rectangular area
	 * @return true if the interior of this {@code Bounds} and the interior
	 * of the rectangular area intersect.
	 */
	public abstract boolean intersects(double x, double y, double w, double h);

	/**
	 * Tests if the interior of this {@code Bounds} intersects the interior
	 * of a specified rectangular area.
	 *
	 * @param x the x coordinate of the upper-left corner of the specified
	 * rectangular volume
	 * @param y the y coordinate of the upper-left corner of the specified
	 * rectangular volume
	 * @param z the z coordinate of the upper-left corner of the specified
	 * rectangular volume
	 * @param w the width of the specified rectangular volume
	 * @param h the height of the specified rectangular volume
	 * @param d the depth of the specified rectangular volume
	 * @return true if the interior of this {@code Bounds} and the interior
	 * of the rectangular area intersect.
	 */
	public abstract boolean intersects(double x, double y, double z, double w, double h, double d);

	/**
	 * Creates a new instance of {@code Bounds} class.
	 * @param minX the X coordinate of the upper-left corner
	 * @param minY the Y coordinate of the upper-left corner
	 * @param minZ the minimum z coordinate of the {@code Bounds}
	 * @param width the width of the {@code Bounds}
	 * @param height the height of the {@code Bounds}
	 * @param depth the depth of the {@code Bounds}
	 */
	protected Bounds(double minX, double minY, double minZ, double width, double height, double depth) {
		this.minX = minX;
		this.minY = minY;
		this.minZ = minZ;
		this.width = width;
		this.height = height;
		this.depth = depth;
		this.maxX = minX + width;
		this.maxY = minY + height;
		this.maxZ = minZ + depth;
	}
}
