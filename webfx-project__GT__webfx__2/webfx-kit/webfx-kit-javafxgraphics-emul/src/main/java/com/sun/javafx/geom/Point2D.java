package com.sun.javafx.geom;

/**
 * A 2D geometric point that usually represents the x, y coordinates.
 * It can also represent a relative magnitude vector's x, y magnitudes.
 */
public class Point2D {
	/**
	 * The X coordinate of this <code>Point2D</code>.
	 */
	public float x;

	/**
	 * The Y coordinate of this <code>Point2D</code>.
	 */
	public float y;

	/**
	 * Constructs and initializes a <code>Point2D</code> with
	 * coordinates (0,&nbsp;0).
	 */
	public Point2D() {
	}

	/**
	 * Constructs and initializes a <code>Point2D</code> with
	 * the specified coordinates.
	 *
	 * @param x the X coordinate of the newly
	 *          constructed <code>Point2D</code>
	 * @param y the Y coordinate of the newly
	 *          constructed <code>Point2D</code>
	 */
	public Point2D(float x, float y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Sets the location of this <code>Point2D</code> to the
	 * specified <code>float</code> coordinates.
	 *
	 * @param x the new X coordinate of this {@code Point2D}
	 * @param y the new Y coordinate of this {@code Point2D}
	 */
	public void setLocation(float x, float y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Sets the location of this <code>Point2D</code> to the same
	 * coordinates as the specified <code>Point2D</code> object.
	 * @param p the specified <code>Point2D</code> to which to set
	 * this <code>Point2D</code>
	 */
	public void setLocation(Point2D p) {
		setLocation(p.x, p.y);
	}

	/**
	 * Returns the square of the distance between two points.
	 *
	 * @param x1 the X coordinate of the first specified point
	 * @param y1 the Y coordinate of the first specified point
	 * @param x2 the X coordinate of the second specified point
	 * @param y2 the Y coordinate of the second specified point
	 * @return the square of the distance between the two
	 * sets of specified coordinates.
	 */
	public static float distanceSq(float x1, float y1, float x2, float y2) {
		x1 -= x2;
		y1 -= y2;
		return (x1 * x1 + y1 * y1);
	}

	/**
	 * Returns the distance between two points.
	 *
	 * @param x1 the X coordinate of the first specified point
	 * @param y1 the Y coordinate of the first specified point
	 * @param x2 the X coordinate of the second specified point
	 * @param y2 the Y coordinate of the second specified point
	 * @return the distance between the two sets of specified
	 * coordinates.
	 */
	public static float distance(float x1, float y1, float x2, float y2) {
		x1 -= x2;
		y1 -= y2;
		return (float) Math.sqrt(x1 * x1 + y1 * y1);
	}

	/**
	 * Returns the square of the distance from this
	 * <code>Point2D</code> to a specified point.
	 *
	 * @param px the X coordinate of the specified point to be measured
	 *           against this <code>Point2D</code>
	 * @param py the Y coordinate of the specified point to be measured
	 *           against this <code>Point2D</code>
	 * @return the square of the distance between this
	 * <code>Point2D</code> and the specified point.
	 */
	public float distanceSq(float px, float py) {
		px -= x;
		py -= y;
		return (px * px + py * py);
	}

	/**
	 * Returns the square of the distance from this
	 * <code>Point2D</code> to a specified <code>Point2D</code>.
	 *
	 * @param pt the specified point to be measured
	 *           against this <code>Point2D</code>
	 * @return the square of the distance between this
	 * <code>Point2D</code> to a specified <code>Point2D</code>.
	 */
	public float distanceSq(Point2D pt) {
		float px = pt.x - this.x;
		float py = pt.y - this.y;
		return (px * px + py * py);
	}

	/**
	 * Returns the distance from this <code>Point2D</code> to
	 * a specified point.
	 *
	 * @param px the X coordinate of the specified point to be measured
	 *           against this <code>Point2D</code>
	 * @param py the Y coordinate of the specified point to be measured
	 *           against this <code>Point2D</code>
	 * @return the distance between this <code>Point2D</code>
	 * and a specified point.
	 */
	public float distance(float px, float py) {
		px -= x;
		py -= y;
		return (float) Math.sqrt(px * px + py * py);
	}

	/**
	 * Returns the distance from this <code>Point2D</code> to a
	 * specified <code>Point2D</code>.
	 *
	 * @param pt the specified point to be measured
	 *           against this <code>Point2D</code>
	 * @return the distance between this <code>Point2D</code> and
	 * the specified <code>Point2D</code>.
	 */
	public float distance(Point2D pt) {
		float px = pt.x - this.x;
		float py = pt.y - this.y;
		return (float) Math.sqrt(px * px + py * py);
	}

	/**
	 * Returns the hashcode for this <code>Point2D</code>.
	 * @return      a hash code for this <code>Point2D</code>.
	 */
	public int hashCode() {
		int bits = java.lang.Float.floatToIntBits(x);
		bits ^= java.lang.Float.floatToIntBits(y) * 31;
		return bits;
	}

	/**
	 * Determines whether or not two points are equal. Two instances of
	 * <code>Point2D</code> are equal if the values of their
	 * <code>x</code> and <code>y</code> member fields, representing
	 * their position in the coordinate space, are the same.
	 * @param obj an object to be compared with this <code>Point2D</code>
	 * @return <code>true</code> if the object to be compared is
	 *         an instance of <code>Point2D</code> and has
	 *         the same values; <code>false</code> otherwise.
	 */
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof Point2D) {
			Point2D p2d = (Point2D) obj;
			return (x == p2d.x) && (y == p2d.y);
		}
		return false;
	}

	/**
	 * Returns a <code>String</code> that represents the value
	 * of this <code>Point2D</code>.
	 * @return a string representation of this <code>Point2D</code>.
	 */
	public String toString() {
		return "Point2D[" + x + ", " + y + "]";
	}
}
