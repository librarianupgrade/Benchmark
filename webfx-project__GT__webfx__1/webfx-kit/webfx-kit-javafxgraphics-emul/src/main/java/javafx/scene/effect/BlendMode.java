package javafx.scene.effect;

/**
 * A blending mode defines the manner in which the inputs of a Blend
 * effect are composited together or how a Node is blended into the
 * background of a scene.
 */
public enum BlendMode {

	/**
	 * The top input is blended over the bottom input.
	 * (Equivalent to the Porter-Duff "source over destination" rule.)
	 */
	SRC_OVER,

	/**
	 * The part of the top input lying inside of the bottom input
	 * is blended with the bottom input.
	 * (Equivalent to the Porter-Duff "source atop destination" rule.)
	 */
	SRC_ATOP,

	/**
	 * The color and alpha components from the top input are
	 * added to those from the bottom input.
	 * The result is clamped to 1.0 if it exceeds the logical
	 * maximum of 1.0.
	 * <p>
	 * Notes:
	 * <ul>
	 * <li>This mode is commutative (ordering of inputs
	 * does not matter).
	 * <li>This mode is sometimes referred to as "linear dodge" in
	 * imaging software packages.
	 * </ul>
	 */
	ADD,

	/**
	 * The color components from the first input are multiplied with those
	 * from the second input.
	 * The alpha components are blended according to
	 * the {@link #SRC_OVER} equation.
	 * <p>
	 * Notes:
	 * <ul>
	 * <li>This mode is commutative (ordering of inputs
	 * does not matter).
	 * <li>This mode is the mathematical opposite of
	 * the {@link #SCREEN} mode.
	 * <li>The resulting color is always at least as dark as either
	 * of the input colors.
	 * <li>Rendering with a completely black top input produces black;
	 * rendering with a completely white top input produces a result
	 * equivalent to the bottom input.
	 * </ul>
	 */
	MULTIPLY,

	/**
	 * The color components from both of the inputs are
	 * inverted, multiplied with each other, and that result
	 * is again inverted to produce the resulting color.
	 * The alpha components are blended according
	 * to the {@link #SRC_OVER} equation.
	 * <p>
	 * Notes:
	 * <ul>
	 * <li>This mode is commutative (ordering of inputs
	 * does not matter).
	 * <li>This mode is the mathematical opposite of
	 * the {@link #MULTIPLY} mode.
	 * <li>The resulting color is always at least as light as either
	 * of the input colors.
	 * <li>Rendering with a completely white top input produces white;
	 * rendering with a completely black top input produces a result
	 * equivalent to the bottom input.
	 * </ul>
	 */
	SCREEN,

	/**
	 * The input color components are either multiplied or screened,
	 * depending on the bottom input color.
	 * The alpha components are blended according
	 * to the {@link #SRC_OVER} equation.
	 * <p>
	 * Notes:
	 * <ul>
	 * <li>This mode is a combination of {@link #SCREEN} and
	 * {@link #MULTIPLY}, depending on the bottom input color.
	 * <li>This mode is the mathematical opposite of
	 * the {@link #HARD_LIGHT} mode.
	 * <li>In this mode, the top input colors "overlay" the bottom input
	 * while preserving highlights and shadows of the latter.
	 * </ul>
	 */
	OVERLAY,

	/**
	 * The darker of the color components from the two inputs are
	 * selected to produce the resulting color.
	 * The alpha components are blended according
	 * to the {@link #SRC_OVER} equation.
	 * <p>
	 * Notes:
	 * <ul>
	 * <li>This mode is commutative (ordering of inputs
	 * does not matter).
	 * <li>This mode is the mathematical opposite of
	 * the {@link #LIGHTEN} mode.
	 * </ul>
	 */
	DARKEN,

	/**
	 * The lighter of the color components from the two inputs are
	 * selected to produce the resulting color.
	 * The alpha components are blended according
	 * to the {@link #SRC_OVER} equation.
	 * <p>
	 * Notes:
	 * <ul>
	 * <li>This mode is commutative (ordering of inputs
	 * does not matter).
	 * <li>This mode is the mathematical opposite of
	 * the {@link #DARKEN} mode.
	 * </ul>
	 */
	LIGHTEN,

	/**
	 * The bottom input color components are divided by the inverse
	 * of the top input color components to produce the resulting color.
	 * The alpha components are blended according
	 * to the {@link #SRC_OVER} equation.
	 */
	COLOR_DODGE,

	/**
	 * The inverse of the bottom input color components are divided by
	 * the top input color components, all of which is then inverted
	 * to produce the resulting color.
	 * The alpha components are blended according
	 * to the {@link #SRC_OVER} equation.
	 */
	COLOR_BURN,

	/**
	 * The input color components are either multiplied or screened,
	 * depending on the top input color.
	 * The alpha components are blended according
	 * to the {@link #SRC_OVER} equation.
	 * <p>
	 * Notes:
	 * <ul>
	 * <li>This mode is a combination of {@link #SCREEN} and
	 * {@link #MULTIPLY}, depending on the top input color.
	 * <li>This mode is the mathematical opposite of
	 * the {@link #OVERLAY} mode.
	 * </ul>
	 */
	HARD_LIGHT,

	/**
	 * The input color components are either darkened or lightened,
	 * depending on the top input color.
	 * The alpha components are blended according
	 * to the {@link #SRC_OVER} equation.
	 * <p>
	 * Notes:
	 * <ul>
	 * <li>This mode is similar to {@link #OVERLAY}, but generally
	 * produces "lighter" results than {@code OVERLAY}.
	 * </ul>
	 */
	SOFT_LIGHT,

	/**
	 * The darker of the color components from the two inputs are
	 * subtracted from the lighter ones to produce the resulting color.
	 * The alpha components are blended according
	 * to the {@link #SRC_OVER} equation.
	 * <p>
	 * Notes:
	 * <ul>
	 * <li>This mode is commutative (ordering of inputs
	 * does not matter).
	 * <li>This mode can be used to invert parts of the bottom input
	 * image, or to quickly compare two images (equal pixels will result
	 * in black).
	 * <li>Rendering with a completely white top input inverts the
	 * bottom input; rendering with a completely black top input produces
	 * a result equivalent to the bottom input.
	 * </ul>
	 */
	DIFFERENCE,

	/**
	 * The color components from the two inputs are multiplied and
	 * doubled, and then subtracted from the sum of the bottom input
	 * color components, to produce the resulting color.
	 * The alpha components are blended according
	 * to the {@link #SRC_OVER} equation.
	 * <p>
	 * Notes:
	 * <ul>
	 * <li>This mode is commutative (ordering of inputs
	 * does not matter).
	 * <li>This mode can be used to invert parts of the bottom input.
	 * <li>This mode produces results that are similar to those of
	 * {@link #DIFFERENCE}, except with lower contrast.
	 * <li>Rendering with a completely white top input inverts the
	 * bottom input; rendering with a completely black top input produces
	 * a result equivalent to the bottom input.
	 * </ul>
	 */
	EXCLUSION,

	/**
	 * The red component of the bottom input is replaced with the
	 * red component of the top input; the other color components
	 * are unaffected.
	 * The alpha components are blended according
	 * to the {@link #SRC_OVER} equation.
	 */
	RED,

	/**
	 * The green component of the bottom input is replaced with the
	 * green component of the top input; the other color components
	 * are unaffected.
	 * The alpha components are blended according
	 * to the {@link #SRC_OVER} equation.
	 */
	GREEN,

	/**
	 * The blue component of the bottom input is replaced with the
	 * blue component of the top input; the other color components
	 * are unaffected.
	 * The alpha components are blended according
	 * to the {@link #SRC_OVER} equation.
	 */
	BLUE
}