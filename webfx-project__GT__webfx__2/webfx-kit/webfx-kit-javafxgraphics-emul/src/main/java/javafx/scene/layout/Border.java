package javafx.scene.layout;

import com.sun.javafx.UnmodifiableArrayList;
import javafx.geometry.Insets;

import java.util.Collections;
import java.util.List;

/**
 * The Border of a {@link Region}. A Border is an immutable object which
 * encapsulates the entire set of data required to render the border
 * of a Region. Because this class is immutable, you can freely reuse the same
 * Border on many different Regions. Please refer to
 * {@link ../doc-files/cssref.html JavaFX CSS Reference} for a complete description
 * of the CSS rules for styling the border of a Region.
 * <p/>
 * Every Border is comprised of {@link #getStrokes() strokes} and / or
 * {@link #getImages() images}. Neither list will ever be null, but either or
 * both may be empty. When rendering, if no images are specified or no
 * image succeeds in loading, then all strokes will be rendered in order.
 * If any image is specified and succeeds in loading, then no strokes will
 * be drawn, although they will still contribute to the {@link #getInsets() insets}
 * and {@link #getOutsets() outsets} of the Border.
 * <p/>
 * The Border's {@link #getOutsets() outsets} define any extension of the drawing area of a Region
 * which is necessary to account for all border drawing and positioning. These outsets are defined
 * by both the {@link BorderStroke}s and {@link BorderImage}s specified on this Border.
 * Outsets are strictly non-negative.
 * <p/>
 * {@link #getInsets()} are used to define the inner-most edge of all of the borders. It also is
 * always strictly non-negative. The Region uses the insets of the {@link Background} and Border
 * and the {@link javafx.scene.layout.Region#getPadding() Region's padding} to determine the
 * Region {@link javafx.scene.layout.Region#getPadding() insets}, which define the content area
 * for any children of the Region. The outsets of a Border together with the outsets of a Background
 * and the width and height of the Region define the geometric bounds of the Region (which in
 * turn contribute to the {@code layoutBounds}, {@code boundsInLocal}, and {@code boundsInParent}).
 * <p/>
 * A Border is most often used in cases where you want to skin the Region with an image,
 * often used in conjunction with 9-patch scaling techniques. In such cases, you may
 * also specify a stroked border which is only used when the image fails to load for some
 * reason.
 *
 * @since JavaFX 8.0
 */
@SuppressWarnings("unchecked")
public final class Border {

	/**
	 * An empty Border, useful to use instead of null.
	 */
	public static final Border EMPTY = new Border((BorderStroke[]) null, null);

	/**
	 * The list of BorderStrokes which together define the stroked portion
	 * of this Border. This List is unmodifiable and immutable. It
	 * will never be null. It will never contain any null elements.
	 */
	public final List<BorderStroke> getStrokes() {
		return strokes;
	}

	final List<BorderStroke> strokes;

	/**
	 * The list of BorderImages which together define the images to use
	 * instead of stroke for this Border. If this list is specified and
	 * at least one image within it succeeds in loading, then any specified
	 * {@link #getStrokes strokes} are not drawn. If this list is null or no images
	 * succeeded in loading, then any specified {@code strokes} are drawn.
	 * <p>
	 * This List is unmodifiable and immutable. It will never be null.
	 * It will never contain any null elements.
	 */
	public final List<BorderImage> getImages() {
		return images;
	}

	final List<BorderImage> images;

	/**
	 * The outsets of the border define the outer-most edge of the border to be drawn.
	 * The values in these outsets are strictly non-negative.
	 */
	public final Insets getOutsets() {
		return outsets;
	}

	final Insets outsets;

	/**
	 * The insets define the distance from the edge of the Region to the inner-most edge
	 * of the border, if that distance is non-negative. The values in these outsets
	 * are strictly non-negative.
	 */
	public final Insets getInsets() {
		return insets;
	}

	final Insets insets;

	/**
	 * Gets whether the Border is empty. It is empty if there are no strokes or images.
	 * @return true if the Border is empty, false otherwise.
	 */
	public final boolean isEmpty() {
		return strokes.isEmpty() && images.isEmpty();
	}

	/**
	 * The cached hash code computation for the Border. One very big
	 * reason for making Border immutable was to make it possible to
	 * cache and reuse the same Border instance for multiple
	 * Regions. To enable efficient caching, we cache the hash.
	 */
	private final int hash;

	/**
	 * Creates a new Border by supplying an array of BorderStrokes.
	 * This array may be null, or may contain null values. Any null values
	 * will be ignored and will not contribute to the {@link #getStrokes() strokes}
	 * or {@link #getOutsets() outsets} or {@link #getInsets() insets}.
	 *
	 * @param strokes   The strokes. This may be null, and may contain nulls. Any
	 *                  contained nulls are filtered out and not included in the
	 *                  final List of strokes. A null array becomes an empty List.
	 *                  If both strokes and images are specified, and if any one
	 *                  of the images specified succeeds in loading, then no
	 *                  strokes are shown. In this way, strokes can be defined as
	 *                  a fallback in the case that an image failed to load.
	 */
	public Border(BorderStroke... strokes) {
		this(strokes, null);
	}

	/**
	 * Creates a new Border by supplying an array of BorderImages.
	 * This array may be null, or may contain null values. Any null values
	 * will be ignored and will not contribute to the {@link #getImages() images}
	 * or {@link #getOutsets() outsets} or {@link #getInsets() insets}.
	 *
	 * @param images    The images. This may be null, and may contain nulls. Any
	 *                  contained nulls are filtered out and not included in the
	 *                  final List of images. A null array becomes an empty List.
	 */
	public Border(BorderImage... images) {
		this(null, images);
	}

	/**
	 * Creates a new Border by supplying a List of BorderStrokes and BorderImages.
	 * These Lists may be null, or may contain null values. Any null values
	 * will be ignored and will not contribute to the {@link #getStrokes() strokes}
	 * or {@link #getImages() images}, {@link #getOutsets() outsets}, or
	 * {@link #getInsets() insets}.
	 *
	 * @param strokes   The strokes. This may be null, and may contain nulls. Any
	 *                  contained nulls are filtered out and not included in the
	 *                  final List of strokes. A null array becomes an empty List.
	 *                  If both strokes and images are specified, and if any one
	 *                  of the images specified succeeds in loading, then no
	 *                  strokes are shown. In this way, strokes can be defined as
	 *                  a fallback in the case that an image failed to load.
	 * @param images    The images. This may be null, and may contain nulls. Any
	 *                  contained nulls are filtered out and not included in the
	 *                  final List of images. A null array becomes an empty List.
	 */
	public Border(List<BorderStroke> strokes, List<BorderImage> images) {
		// NOTE: This constructor had to be supplied in order to cause a Builder
		// to be auto-generated, because otherwise the types of the strokes and images
		// properties didn't match the types of the array based constructor parameters.
		// So a Builder will use this constructor, while the CSS engine uses the
		// array based constructor (for speed).
		this(strokes == null ? null : strokes.toArray(new BorderStroke[strokes.size()]),
				images == null ? null : images.toArray(new BorderImage[images.size()]));
	}

	/**
	 * Creates a new Border by supplying an array of BorderStrokes and BorderImages.
	 * These arrays may be null, or may contain null values. Any null values
	 * will be ignored and will not contribute to the {@link #getStrokes() strokes}
	 * or {@link #getImages() images}, {@link #getOutsets() outsets}, or
	 * {@link #getInsets() insets}.
	 *
	 * @param strokes   The strokes. This may be null, and may contain nulls. Any
	 *                  contained nulls are filtered out and not included in the
	 *                  final List of strokes. A null array becomes an empty List.
	 *                  If both strokes and images are specified, and if any one
	 *                  of the images specified succeeds in loading, then no
	 *                  strokes are shown. In this way, strokes can be defined as
	 *                  a fallback in the case that an image failed to load.
	 * @param images    The images. This may be null, and may contain nulls. Any
	 *                  contained nulls are filtered out and not included in the
	 *                  final List of images. A null array becomes an empty List.
	 */
	public Border(BorderStroke[] strokes, BorderImage[] images) {
		double innerTop = 0, innerRight = 0, innerBottom = 0, innerLeft = 0;
		double outerTop = 0, outerRight = 0, outerBottom = 0, outerLeft = 0;

		if (strokes == null || strokes.length == 0) {
			this.strokes = Collections.emptyList();
		} else {
			final BorderStroke[] noNulls = new BorderStroke[strokes.length];
			int size = 0;
			for (int i = 0; i < strokes.length; i++) {
				final BorderStroke stroke = strokes[i];
				if (stroke != null) {
					noNulls[size++] = stroke;

					// Calculate the insets and outsets. "insets" are the distance
					// from the edge of the region to the inmost edge of the inmost border.
					// Outsets are the distance from the edge of the region out towards the
					// outer-most edge of the outer-most border.
					final double strokeInnerTop = stroke.innerEdge.getTop();
					final double strokeInnerRight = stroke.innerEdge.getRight();
					final double strokeInnerBottom = stroke.innerEdge.getBottom();
					final double strokeInnerLeft = stroke.innerEdge.getLeft();

					innerTop = innerTop >= strokeInnerTop ? innerTop : strokeInnerTop;
					innerRight = innerRight >= strokeInnerRight ? innerRight : strokeInnerRight;
					innerBottom = innerBottom >= strokeInnerBottom ? innerBottom : strokeInnerBottom;
					innerLeft = innerLeft >= strokeInnerLeft ? innerLeft : strokeInnerLeft;

					final double strokeOuterTop = stroke.outerEdge.getTop();
					final double strokeOuterRight = stroke.outerEdge.getRight();
					final double strokeOuterBottom = stroke.outerEdge.getBottom();
					final double strokeOuterLeft = stroke.outerEdge.getLeft();

					outerTop = outerTop >= strokeOuterTop ? outerTop : strokeOuterTop;
					outerRight = outerRight >= strokeOuterRight ? outerRight : strokeOuterRight;
					outerBottom = outerBottom >= strokeOuterBottom ? outerBottom : strokeOuterBottom;
					outerLeft = outerLeft >= strokeOuterLeft ? outerLeft : strokeOuterLeft;
				}
			}
			this.strokes = new UnmodifiableArrayList<BorderStroke>(noNulls, size);
		}

		if (images == null || images.length == 0) {
			this.images = Collections.emptyList();
		} else {
			final BorderImage[] noNulls = new BorderImage[images.length];
			int size = 0;
			for (int i = 0; i < images.length; i++) {
				final BorderImage image = images[i];
				if (image != null) {
					noNulls[size++] = image;

					// The Image width + insets may contribute to the insets / outsets of
					// this border.
					final double imageInnerTop = image.innerEdge.getTop();
					final double imageInnerRight = image.innerEdge.getRight();
					final double imageInnerBottom = image.innerEdge.getBottom();
					final double imageInnerLeft = image.innerEdge.getLeft();

					innerTop = innerTop >= imageInnerTop ? innerTop : imageInnerTop;
					innerRight = innerRight >= imageInnerRight ? innerRight : imageInnerRight;
					innerBottom = innerBottom >= imageInnerBottom ? innerBottom : imageInnerBottom;
					innerLeft = innerLeft >= imageInnerLeft ? innerLeft : imageInnerLeft;

					final double imageOuterTop = image.outerEdge.getTop();
					final double imageOuterRight = image.outerEdge.getRight();
					final double imageOuterBottom = image.outerEdge.getBottom();
					final double imageOuterLeft = image.outerEdge.getLeft();

					outerTop = outerTop >= imageOuterTop ? outerTop : imageOuterTop;
					outerRight = outerRight >= imageOuterRight ? outerRight : imageOuterRight;
					outerBottom = outerBottom >= imageOuterBottom ? outerBottom : imageOuterBottom;
					outerLeft = outerLeft >= imageOuterLeft ? outerLeft : imageOuterLeft;
				}
			}
			this.images = new UnmodifiableArrayList<BorderImage>(noNulls, size);
		}

		// Both the BorderStroke and BorderImage class make sure to return the outsets
		// and insets in the right way, such that we don't have to worry about adjusting
		// the sign, etc, unlike in the Background implementation.
		outsets = new Insets(outerTop, outerRight, outerBottom, outerLeft);
		insets = new Insets(innerTop, innerRight, innerBottom, innerLeft);

		// Pre-compute the hash code. NOTE: all variables are prefixed with "this" so that we
		// do not accidentally compute the hash based on the constructor arguments rather than
		// based on the fields themselves!
		int result = this.strokes.hashCode();
		result = 31 * result + this.images.hashCode();
		hash = result;
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
		Border border = (Border) o;
		if (this.hash != border.hash)
			return false;

		if (!images.equals(border.images))
			return false;
		if (!strokes.equals(border.strokes))
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
}
