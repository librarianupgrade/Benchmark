package javafx.scene.paint;

import webfx.platform.shared.util.Numbers;
import javafx.animation.Interpolatable;

import java.util.HashMap;
import java.util.Map;

/**
 * The Color class is used to encapsulate colors in the default sRGB color space.
 * Every color has an implicit alpha value of 1.0 or an explicit one provided
 * in the constructor. The alpha value defines the transparency of a color
 * and can be  represented by a double value in the range 0.0-1.0 or 0-255.
 * An alpha value of 1.0 or 255 means that the color is completely opaque
 * and an alpha value of 0 or 0.0 means that the color is completely transparent.
 * When constructing a {@code Color} with an explicit alpha or getting
 * the color/alpha components of a Color,
 * the color components are never premultiplied by the alpha component.
 * </p>
 *
 * <p>{@code Color}s can be created with the constructor or with one of several
 * utility methods.  The following lines of code all create the same
 * blue color:</p>
 *
 * <pre><code>
 * Color c = Color.BLUE;   //use the blue constant
 * Color c = new Color(0,0,1,1.0); // standard constructor, use 0->1.0 values, explicit alpha of 1.0
 *
 * Color c = Color.color(0,0,1.0); //use 0->1.0 values. implicit alpha of 1.0
 * Color c = Color.color(0,0,1.0,1.0); //use 0->1.0 values, explicit alpha of 1.0
 *
 * Color c = Color.rgb(0,0,255); //use 0->255 integers, implicit alpha of 1.0
 * Color c = Color.rgb(0,0,255,1.0); //use 0->255 integers, explicit alpha of 1.0
 *
 * Color c = Color.hsb(270,1.0,1.0); //hue = 270, saturation & value = 1.0. inplicit alpha of 1.0
 * Color c = Color.hsb(270,1.0,1.0,1.0); //hue = 270, saturation & value = 1.0, explicit alpha of 1.0
 *
 * Color c = Color.web("0x0000FF",1.0);// blue as a hex web value, explicit alpha
 * Color c = Color.web("0x0000FF");// blue as a hex web value, implicit alpha
 * Color c = Color.web("0x00F");// blue as a short hex web value, implicit alpha
 * Color c = Color.web("#0000FF",1.0);// blue as a hex web value, explicit alpha
 * Color c = Color.web("#0000FF");// blue as a hex web value, implicit alpha
 * Color c = Color.web("#00F");// blue as a short hex web value, implicit alpha
 * Color c = Color.web("0000FF",1.0);// blue as a hex web value, explicit alpha
 * Color c = Color.web("0000FF");// blue as a hex web value, implicit alpha
 * Color c = Color.web("00F");// blue as a short hex web value, implicit alpha
 * Color c = Color.web("rgba(0,0,255,1.0)");// blue as an rgb web value, explicit alpha
 * Color c = Color.web("rgb(0,0,255)");// blue as an rgb web value, implicit alpha
 * Color c = Color.web("rgba(0,0,100%,1.0)");// blue as an rgb percent web value, explicit alpha
 * Color c = Color.web("rgb(0,0,100%)");// blue as an rgb percent web value, implicit alpha
 * Color c = Color.web("hsla(270,100%,100%,1.0)");// blue as an hsl web value, explicit alpha
 * Color c = Color.web("hsl(270,100%,100%)");// blue as an hsl web value, implicit alpha
 * </code></pre>
 *
 * <p>
 * The creation of a {@code Color} will throw {@code IllegalArgumentException} if any
 * of the values are out of range.
 * </p>
 *
 * <p>
 * For example:
 * <pre><code>
 * Rectangle rec1 = new Rectangle(5, 5, 50, 40);
 * rec1.setFill(Color.RED);
 * rec1.setStroke(Color.GREEN);
 * rec1.setStrokeWidth(3);
 *
 * Rectangle rec2 = new Rectangle(65, 5, 50, 40);
 * rec2.setFill(Color.rgb(91, 127, 255));
 * rec2.setStroke(Color.hsb(40, 0.7, 0.8));
 * rec2.setStrokeWidth(3);
 * </code></pre>
 * </p>
 * @since JavaFX 2.0
 */
public final class Color implements Paint, Interpolatable<Color> {

	/**
	 * Brightness change factor for darker() and brighter() methods.
	 */
	private static final double DARKER_BRIGHTER_FACTOR = 0.7;

	/**
	 * Saturation change factor for saturate() and desaturate() methods.
	 */
	private static final double SATURATE_DESATURATE_FACTOR = 0.7;

	/**
	 * Creates an sRGB color with the specified red, green and blue values
	 * in the range {@code 0.0-1.0}, and a given opacity.
	 *
	 * @param red the red component, in the range {@code 0.0-1.0}
	 * @param green the green component, in the range {@code 0.0-1.0}
	 * @param blue the blue component, in the range {@code 0.0-1.0}
	 * @param opacity the opacity component, in the range {@code 0.0-1.0}
	 * @return the {@code Color}
	 * @throws IllegalArgumentException if any value is out of range
	 */
	public static Color color(double red, double green, double blue, double opacity) {
		return new Color(red, green, blue, opacity);
	}

	/**
	 * Creates an opaque sRGB color with the specified red, green and blue values
	 * in the range {@code 0.0-1.0}.
	 *
	 * @param red the red component, in the range {@code 0.0-1.0}
	 * @param green the green component, in the range {@code 0.0-1.0}
	 * @param blue the blue component, in the range {@code 0.0-1.0}
	 * @return the {@code Color}
	 * @throws IllegalArgumentException if any value is out of range
	 */
	public static Color color(double red, double green, double blue) {
		return new Color(red, green, blue, 1);
	}

	/**
	 * Creates an sRGB color with the specified RGB values in the range {@code 0-255},
	 * and a given opacity.
	 *
	 * @param red the red component, in the range {@code 0-255}
	 * @param green the green component, in the range {@code 0-255}
	 * @param blue the blue component, in the range {@code 0-255}
	 * @param opacity the opacity component, in the range {@code 0.0-1.0}
	 * @return the {@code Color}
	 * @throws IllegalArgumentException if any value is out of range
	 */
	public static Color rgb(int red, int green, int blue, double opacity) {
		checkRGB(red, green, blue);
		return new Color(red / 255.0, green / 255.0, blue / 255.0, opacity);
	}

	/**
	 * Creates an opaque sRGB color with the specified RGB values in the range {@code 0-255}.
	 *
	 * @param red the red component, in the range {@code 0-255}
	 * @param green the green component, in the range {@code 0-255}
	 * @param blue the blue component, in the range {@code 0-255}
	 * @return the {@code Color}
	 * @throws IllegalArgumentException if any value is out of range
	 */
	public static Color rgb(int red, int green, int blue) {
		checkRGB(red, green, blue);
		return new Color(red / 255.0, green / 255.0, blue / 255.0, 1.0);
	}

	/**
	 * This is a shortcut for {@code rgb(gray, gray, gray)}.
	 */
	public static Color grayRgb(int gray) {
		return rgb(gray, gray, gray);
	}

	/**
	 * This is a shortcut for {@code rgb(gray, gray, gray, opacity)}.
	 */
	public static Color grayRgb(int gray, double opacity) {
		return rgb(gray, gray, gray, opacity);
	}

	/**
	 * Creates a grey color.
	 * @param gray color on gray scale in the range
	 *             {@code 0.0} (black) - {@code 1.0} (white).
	 * @param opacity the opacity component, in the range {@code 0.0-1.0}
	 * @return the {@code Color}
	 * @throws IllegalArgumentException if any value is out of range
	 */
	public static Color gray(double gray, double opacity) {
		return new Color(gray, gray, gray, opacity);
	}

	/**
	 * Creates an opaque grey color.
	 * @param gray color on gray scale in the range
	 *             {@code 0.0} (black) - {@code 1.0} (white).
	 * @return the {@code Color}
	 * @throws IllegalArgumentException if any value is out of range
	 */
	public static Color gray(double gray) {
		return gray(gray, 1.0);
	}

	private static void checkRGB(int red, int green, int blue) {
		if (red < 0 || red > 255) {
			throw new IllegalArgumentException("Color.rgb's red parameter (" + red + ") expects color values 0-255");
		}
		if (green < 0 || green > 255) {
			throw new IllegalArgumentException(
					"Color.rgb's green parameter (" + green + ") expects color values 0-255");
		}
		if (blue < 0 || blue > 255) {
			throw new IllegalArgumentException("Color.rgb's blue parameter (" + blue + ") expects color values 0-255");
		}
	}

	/**
	 * Creates a {@code Color} based on the specified values in the HSB color model,
	 * and a given opacity.
	 *
	 * @param hue the hue, in degrees
	 * @param saturation the saturation, {@code 0.0 to 1.0}
	 * @param brightness the brightness, {@code 0.0 to 1.0}
	 * @param opacity the opacity, {@code 0.0 to 1.0}
	 * @return the {@code Color}
	 * @throws IllegalArgumentException if {@code saturation}, {@code brightness} or
	 *         {@code opacity} are out of range
	 */
	public static Color hsb(double hue, double saturation, double brightness, double opacity) {
		checkSB(saturation, brightness);
		double[] rgb = HSBtoRGB(hue, saturation, brightness);
		Color result = new Color(rgb[0], rgb[1], rgb[2], opacity);
		return result;
	}

	/**
	 * Creates an opaque {@code Color} based on the specified values in the HSB color model.
	 *
	 * @param hue the hue, in degrees
	 * @param saturation the saturation, {@code 0.0 to 1.0}
	 * @param brightness the brightness, {@code 0.0 to 1.0}
	 * @return the {@code Color}
	 * @throws IllegalArgumentException if {@code saturation} or {@code brightness} are
	 *         out of range
	 */
	public static Color hsb(double hue, double saturation, double brightness) {
		return hsb(hue, saturation, brightness, 1.0);
	}

	private static void checkSB(double saturation, double brightness) {
		if (saturation < 0.0 || saturation > 1.0) {
			throw new IllegalArgumentException(
					"Color.hsb's saturation parameter (" + saturation + ") expects values 0.0-1.0");
		}
		if (brightness < 0.0 || brightness > 1.0) {
			throw new IllegalArgumentException(
					"Color.hsb's brightness parameter (" + brightness + ") expects values 0.0-1.0");
		}
	}

	/**
	 * Creates an RGB color specified with an HTML or CSS attribute string.
	 *
	 * <p>
	 * This method supports the following formats:
	 * <ul>
	 * <li>Any standard HTML color name
	 * <li>An HTML long or short format hex string with an optional hex alpha
	 * channel.
	 * Hexadecimal values may be preceded by either {@code "0x"} or {@code "#"}
	 * and can either be 2 digits in the range {@code 00} to {@code 0xFF} or a
	 * single digit in the range {@code 0} to {@code F}.
	 * <li>An {@code rgb(r,g,b)} or {@code rgba(r,g,b,a)} format string.
	 * Each of the {@code r}, {@code g}, or {@code b} values can be an integer
	 * from 0 to 255 or a floating point percentage value from 0.0 to 100.0
	 * followed by the percent ({@code %}) character.
	 * The alpha component, if present, is a
	 * floating point value from 0.0 to 1.0.  Spaces are allowed before or
	 * after the numbers and between the percentage number and its percent
	 * sign ({@code %}).
	 * <li>An {@code hsl(h,s,l)} or {@code hsla(h,s,l,a)} format string.
	 * The {@code h} value is a floating point number from 0.0 to 360.0
	 * representing the hue angle on a color wheel in degrees with
	 * {@code 0.0} or {@code 360.0} representing red, {@code 120.0}
	 * representing green, and {@code 240.0} representing blue.  The
	 * {@code s} value is the saturation of the desired color represented
	 * as a floating point percentage from gray ({@code 0.0}) to
	 * the fully saturated color ({@code 100.0}) and the {@code l} value
	 * is the desired lightness or brightness of the desired color represented
	 * as a floating point percentage from black ({@code 0.0}) to the full
	 * brightness of the color ({@code 100.0}).
	 * The alpha component, if present, is a floating
	 * point value from 0.0 to 1.0.  Spaces are allowed before or
	 * after the numbers and between the percentage number and its percent
	 * sign ({@code %}).
	 * </ul>
	 *
	 * <p>For formats without an alpha component and for named colors, opacity
	 * is set according to the {@code opacity} argument. For colors specified
	 * with an alpha component, the resulting opacity is a combination of the
	 * parsed alpha component and the {@code opacity} argument, so a
	 * transparent color becomes more transparent by specifying opacity.</p>
	 *
	 * <p>Examples:</p>
	 * <div class="classUseContainer">
	 * <table class="overviewSummary" border="0" cellpadding="3" cellspacing="0">
	 * <tr>
	 * <th class="colFirst">Web Format String</th>
	 * <th class="colLast">Equivalent constructor or factory call</th>
	 * </tr>
	 * <tr class="rowColor">
	 * <td class="colFirst"><code>Color.web("orange", 0.5);</code></td>
	 * <td class="colLast"><code>new Color(1.0, 0xA5/255.0, 0.0, 0.5)</code></td>
	 * </tr>
	 * <tr class="altColor">
	 * <td class="colFirst"><code>Color.web("0xff66cc33", 0.5);</code></td>
	 * <td class="colLast"><code>new Color(1.0, 0.4, 0.8, 0.1)</code></td>
	 * </tr>
	 * <tr class="rowColor">
	 * <td class="colFirst"><code>Color.web("0xff66cc", 0.5);</code></td>
	 * <td class="colLast"><code>new Color(1.0, 0.4, 0.8, 0.5)</code></td>
	 * </tr>
	 * <tr class="altColor">
	 * <td class="colFirst"><code>Color.web("#ff66cc", 0.5);</code></td>
	 * <td class="colLast"><code>new Color(1.0, 0.4, 0.8, 0.5)</code></td>
	 * </tr>
	 * <tr class="rowColor">
	 * <td class="colFirst"><code>Color.web("#f68", 0.5);</code></td>
	 * <td class="colLast"><code>new Color(1.0, 0.4, 0.8, 0.5)</code></td>
	 * </tr>
	 * <tr class="altColor">
	 * <td class="colFirst"><code>Color.web("rgb(255,102,204)", 0.5);</code></td>
	 * <td class="colLast"><code>new Color(1.0, 0.4, 0.8, 0.5)</code></td>
	 * </tr>
	 * <tr class="rowColor">
	 * <td class="colFirst"><code>Color.web("rgb(100%,50%,50%)", 0.5);</code></td>
	 * <td class="colLast"><code>new Color(1.0, 0.5, 0.5, 0.5)</code></td>
	 * </tr>
	 * <tr class="altColor">
	 * <td class="colFirst"><code>Color.web("rgb(255,50%,50%,0.25)", 0.5);</code></td>
	 * <td class="colLast"><code>new Color(1.0, 0.5, 0.5, 0.125)</code></td>
	 * </tr>
	 * <tr class="rowColor">
	 * <td class="colFirst"><code>Color.web("hsl(240,100%,100%)", 0.5);</code></td>
	 * <td class="colLast"><code>Color.hsb(240.0, 1.0, 1.0, 0.5)</code></td>
	 * </tr>
	 * <tr class="altColor">
	 * <td style="border-bottom:1px solid" class="colFirst">
	 *     <code>Color.web("hsla(120,0%,0%,0.25)", 0.5);</code>
	 * </td>
	 * <td style="border-bottom:1px solid" class="colLast">
	 *     <code>Color.hsb(120.0, 0.0, 0.0, 0.125)</code>
	 * </td>
	 * </tr>
	 * </table>
	 * </div>
	 *
	 * @param colorString the name or numeric representation of the color
	 *                    in one of the supported formats
	 * @param opacity the opacity component in range from 0.0 (transparent)
	 *                to 1.0 (opaque)
	 * @throws NullPointerException if {@code colorString} is {@code null}
	 * @throws IllegalArgumentException if {@code colorString} specifies
	 *      an unsupported color name or contains an illegal numeric value
	 */
	public static Color web(String colorString, double opacity) {
		if (colorString == null) {
			throw new NullPointerException("The color components or name must be specified");
		}
		if (colorString.isEmpty()) {
			throw new IllegalArgumentException("Invalid color specification");
		}

		String color = colorString.toLowerCase();

		if (color.startsWith("#")) {
			color = color.substring(1);
		} else if (color.startsWith("0x")) {
			color = color.substring(2);
		} else if (color.startsWith("rgb")) {
			if (color.startsWith("(", 3)) {
				return parseRGBColor(color, 4, false, opacity);
			} else if (color.startsWith("a(", 3)) {
				return parseRGBColor(color, 5, true, opacity);
			}
		} else if (color.startsWith("hsl")) {
			if (color.startsWith("(", 3)) {
				return parseHSLColor(color, 4, false, opacity);
			} else if (color.startsWith("a(", 3)) {
				return parseHSLColor(color, 5, true, opacity);
			}
		} else {
			Color col = NamedColors.get(color);
			if (col != null) {
				if (opacity == 1.0) {
					return col;
				} else {
					return Color.color(col.red, col.green, col.blue, opacity);
				}
			}
		}

		int len = color.length();

		try {
			int r;
			int g;
			int b;
			int a;

			if (len == 3) {
				r = Integer.parseInt(color.substring(0, 1), 16);
				g = Integer.parseInt(color.substring(1, 2), 16);
				b = Integer.parseInt(color.substring(2, 3), 16);
				return Color.color(r / 15.0, g / 15.0, b / 15.0, opacity);
			} else if (len == 4) {
				r = Integer.parseInt(color.substring(0, 1), 16);
				g = Integer.parseInt(color.substring(1, 2), 16);
				b = Integer.parseInt(color.substring(2, 3), 16);
				a = Integer.parseInt(color.substring(3, 4), 16);
				return Color.color(r / 15.0, g / 15.0, b / 15.0, opacity * a / 15.0);
			} else if (len == 6) {
				r = Integer.parseInt(color.substring(0, 2), 16);
				g = Integer.parseInt(color.substring(2, 4), 16);
				b = Integer.parseInt(color.substring(4, 6), 16);
				return Color.rgb(r, g, b, opacity);
			} else if (len == 8) {
				r = Integer.parseInt(color.substring(0, 2), 16);
				g = Integer.parseInt(color.substring(2, 4), 16);
				b = Integer.parseInt(color.substring(4, 6), 16);
				a = Integer.parseInt(color.substring(6, 8), 16);
				return Color.rgb(r, g, b, opacity * a / 255.0);
			}
		} catch (NumberFormatException nfe) {
		}

		throw new IllegalArgumentException("Invalid color specification");
	}

	private static Color parseRGBColor(String color, int roff, boolean hasAlpha, double a) {
		try {
			int rend = color.indexOf(',', roff);
			int gend = rend < 0 ? -1 : color.indexOf(',', rend + 1);
			int bend = gend < 0 ? -1 : color.indexOf(hasAlpha ? ',' : ')', gend + 1);
			int aend = hasAlpha ? (bend < 0 ? -1 : color.indexOf(')', bend + 1)) : bend;
			if (aend >= 0) {
				double r = parseComponent(color, roff, rend, PARSE_COMPONENT);
				double g = parseComponent(color, rend + 1, gend, PARSE_COMPONENT);
				double b = parseComponent(color, gend + 1, bend, PARSE_COMPONENT);
				if (hasAlpha) {
					a *= parseComponent(color, bend + 1, aend, PARSE_ALPHA);
				}
				return new Color(r, g, b, a);
			}
		} catch (NumberFormatException nfe) {
		}

		throw new IllegalArgumentException("Invalid color specification");
	}

	private static Color parseHSLColor(String color, int hoff, boolean hasAlpha, double a) {
		try {
			int hend = color.indexOf(',', hoff);
			int send = hend < 0 ? -1 : color.indexOf(',', hend + 1);
			int lend = send < 0 ? -1 : color.indexOf(hasAlpha ? ',' : ')', send + 1);
			int aend = hasAlpha ? (lend < 0 ? -1 : color.indexOf(')', lend + 1)) : lend;
			if (aend >= 0) {
				double h = parseComponent(color, hoff, hend, PARSE_ANGLE);
				double s = parseComponent(color, hend + 1, send, PARSE_PERCENT);
				double l = parseComponent(color, send + 1, lend, PARSE_PERCENT);
				if (hasAlpha) {
					a *= parseComponent(color, lend + 1, aend, PARSE_ALPHA);
				}
				return Color.hsb(h, s, l, a);
			}
		} catch (NumberFormatException nfe) {
		}

		throw new IllegalArgumentException("Invalid color specification");
	}

	private static final int PARSE_COMPONENT = 0; // percent, or clamped to [0,255] => [0,1]
	private static final int PARSE_PERCENT = 1; // clamped to [0,100]% => [0,1]
	private static final int PARSE_ANGLE = 2; // clamped to [0,360]
	private static final int PARSE_ALPHA = 3; // clamped to [0.0,1.0]

	private static double parseComponent(String color, int off, int end, int type) {
		color = color.substring(off, end).trim();
		if (color.endsWith("%")) {
			if (type > PARSE_PERCENT) {
				throw new IllegalArgumentException("Invalid color specification");
			}
			type = PARSE_PERCENT;
			color = color.substring(0, color.length() - 1).trim();
		} else if (type == PARSE_PERCENT) {
			throw new IllegalArgumentException("Invalid color specification");
		}
		double c = ((type == PARSE_COMPONENT) ? Integer.parseInt(color) : Double.parseDouble(color));
		switch (type) {
		case PARSE_ALPHA:
			return (c < 0.0) ? 0.0 : ((c > 1.0) ? 1.0 : c);
		case PARSE_PERCENT:
			return (c <= 0.0) ? 0.0 : ((c >= 100.0) ? 1.0 : (c / 100.0));
		case PARSE_COMPONENT:
			return (c <= 0.0) ? 0.0 : ((c >= 255.0) ? 1.0 : (c / 255.0));
		case PARSE_ANGLE:
			return ((c < 0.0) ? ((c % 360.0) + 360.0) : ((c > 360.0) ? (c % 360.0) : c));
		}

		throw new IllegalArgumentException("Invalid color specification");
	}

	/**
	 * Creates an RGB color specified with an HTML or CSS attribute string.
	 *
	 * <p>
	 * This method supports the following formats:
	 * <ul>
	 * <li>Any standard HTML color name
	 * <li>An HTML long or short format hex string with an optional hex alpha
	 * channel.
	 * Hexadecimal values may be preceded by either {@code "0x"} or {@code "#"}
	 * and can either be 2 digits in the range {@code 00} to {@code 0xFF} or a
	 * single digit in the range {@code 0} to {@code F}.
	 * <li>An {@code rgb(r,g,b)} or {@code rgba(r,g,b,a)} format string.
	 * Each of the {@code r}, {@code g}, or {@code b} values can be an integer
	 * from 0 to 255 or a floating point percentage value from 0.0 to 100.0
	 * followed by the percent ({@code %}) character.
	 * The alpha component, if present, is a
	 * floating point value from 0.0 to 1.0.  Spaces are allowed before or
	 * after the numbers and between the percentage number and its percent
	 * sign ({@code %}).
	 * <li>An {@code hsl(h,s,l)} or {@code hsla(h,s,l,a)} format string.
	 * The {@code h} value is a floating point number from 0.0 to 360.0
	 * representing the hue angle on a color wheel in degrees with
	 * {@code 0.0} or {@code 360.0} representing red, {@code 120.0}
	 * representing green, and {@code 240.0} representing blue.  The
	 * {@code s} value is the saturation of the desired color represented
	 * as a floating point percentage from gray ({@code 0.0}) to
	 * the fully saturated color ({@code 100.0}) and the {@code l} value
	 * is the desired lightness or brightness of the desired color represented
	 * as a floating point percentage from black ({@code 0.0}) to the full
	 * brightness of the color ({@code 100.0}).
	 * The alpha component, if present, is a floating
	 * point value from 0.0 to 1.0.  Spaces are allowed before or
	 * after the numbers and between the percentage number and its percent
	 * sign ({@code %}).
	 * </ul>
	 *
	 * <p>Examples:</p>
	 * <div class="classUseContainer">
	 * <table class="overviewSummary" border="0" cellpadding="3" cellspacing="0">
	 * <tr>
	 * <th class="colFirst">Web Format String</th>
	 * <th class="colLast">Equivalent constant or factory call</th>
	 * </tr>
	 * <tr class="rowColor">
	 * <td class="colFirst"><code>Color.web("orange");</code></td>
	 * <td class="colLast"><code>Color.ORANGE</code></td>
	 * </tr>
	 * <tr class="altColor">
	 * <td class="colFirst"><code>Color.web("0xff668840");</code></td>
	 * <td class="colLast"><code>Color.rgb(255, 102, 136, 0.25)</code></td>
	 * </tr>
	 * <tr class="rowColor">
	 * <td class="colFirst"><code>Color.web("0xff6688");</code></td>
	 * <td class="colLast"><code>Color.rgb(255, 102, 136, 1.0)</code></td>
	 * </tr>
	 * <tr class="altColor">
	 * <td class="colFirst"><code>Color.web("#ff6688");</code></td>
	 * <td class="colLast"><code>Color.rgb(255, 102, 136, 1.0)</code></td>
	 * </tr>
	 * <tr class="rowColor">
	 * <td class="colFirst"><code>Color.web("#f68");</code></td>
	 * <td class="colLast"><code>Color.rgb(255, 102, 136, 1.0)</code></td>
	 * </tr>
	 * <tr class="altColor">
	 * <td class="colFirst"><code>Color.web("rgb(255,102,136)");</code></td>
	 * <td class="colLast"><code>Color.rgb(255, 102, 136, 1.0)</code></td>
	 * </tr>
	 * <tr class="rowColor">
	 * <td class="colFirst"><code>Color.web("rgb(100%,50%,50%)");</code></td>
	 * <td class="colLast"><code>Color.rgb(255, 128, 128, 1.0)</code></td>
	 * </tr>
	 * <tr class="altColor">
	 * <td class="colFirst"><code>Color.web("rgb(255,50%,50%,0.25)");</code></td>
	 * <td class="colLast"><code>Color.rgb(255, 128, 128, 0.25)</code></td>
	 * </tr>
	 * <tr class="rowColor">
	 * <td class="colFirst"><code>Color.web("hsl(240,100%,100%)");</code></td>
	 * <td class="colLast"><code>Color.hsb(240.0, 1.0, 1.0, 1.0)</code></td>
	 * </tr>
	 * <tr class="altColor">
	 * <td style="border-bottom:1px solid" class="colFirst">
	 *     <code>Color.web("hsla(120,0%,0%,0.25)");</code>
	 * </td>
	 * <td style="border-bottom:1px solid" class="colLast">
	 *     <code>Color.hsb(120.0, 0.0, 0.0, 0.25)</code>
	 * </td>
	 * </tr>
	 * </table>
	 * </div>
	 *
	 * @param colorString the name or numeric representation of the color
	 *                    in one of the supported formats
	 * @throws NullPointerException if {@code colorString} is {@code null}
	 * @throws IllegalArgumentException if {@code colorString} specifies
	 *      an unsupported color name or contains an illegal numeric value
	 */
	public static Color web(String colorString) {
		return web(colorString, 1.0);
	}

	/**
	 * Creates a color value from a string representation. The format
	 * of the string representation is the same as in {@link #web(String)}.
	 *
	 * @param value the string to convert
	 * @throws NullPointerException if the {@code value} is {@code null}
	 * @throws IllegalArgumentException if the {@code value} specifies
	 *      an unsupported color name or illegal hexadecimal value
	 * @return a {@code Color} object holding the value represented
	 * by the string argument
	 * @see #web(String)
	 * @since JavaFX 2.1
	 */
	public static Color valueOf(String value) {
		if (value == null) {
			throw new NullPointerException("color must be specified");
		}

		return web(value);
	}

	private static int to32BitInteger(int red, int green, int blue, int alpha) {
		int i = red;
		i = i << 8;
		i = i | green;
		i = i << 8;
		i = i | blue;
		i = i << 8;
		i = i | alpha;
		return i;
	}

	/**
	 * Gets the hue component of this {@code Color}.
	 * @return Hue value in the range in the range {@code 0.0-360.0}.
	 */
	public double getHue() {
		return RGBtoHSB(red, green, blue)[0];
	}

	/**
	 * Gets the saturation component of this {@code Color}.
	 * @return Saturation value in the range in the range {@code 0.0-1.0}.
	 */
	public double getSaturation() {
		return RGBtoHSB(red, green, blue)[1];
	}

	/**
	 * Gets the brightness component of this {@code Color}.
	 * @return Brightness value in the range in the range {@code 0.0-1.0}.
	 */
	public double getBrightness() {
		return RGBtoHSB(red, green, blue)[2];
	}

	/**
	 * Creates a new {@code Color} based on this {@code Color} with hue,
	 * saturation, brightness and opacity values altered. Hue is shifted
	 * about the given value and normalized into its natural range, the
	 * other components' values are multiplied by the given factors and
	 * clipped into their ranges.
	 *
	 * Increasing brightness of black color is allowed by using an arbitrary,
	 * very small source brightness instead of zero.
	 */
	public Color deriveColor(double hueShift, double saturationFactor, double brightnessFactor, double opacityFactor) {

		double[] hsb = RGBtoHSB(red, green, blue);

		/* Allow brightness increase of black color */
		double b = hsb[2];
		if (b == 0 && brightnessFactor > 1.0) {
			b = 0.05;
		}

		/* the tail "+ 360) % 360" solves shifts into negative numbers */
		double h = (((hsb[0] + hueShift) % 360) + 360) % 360;
		double s = Math.max(Math.min(hsb[1] * saturationFactor, 1.0), 0.0);
		b = Math.max(Math.min(b * brightnessFactor, 1.0), 0.0);
		double a = Math.max(Math.min(opacity * opacityFactor, 1.0), 0.0);
		return hsb(h, s, b, a);
	}

	/**
	 * Creates a new Color that is a brighter version of this Color.
	 */
	public Color brighter() {
		return deriveColor(0, 1.0, 1.0 / DARKER_BRIGHTER_FACTOR, 1.0);
	}

	/**
	 * Creates a new Color that is a darker version of this Color.
	 */
	public Color darker() {
		return deriveColor(0, 1.0, DARKER_BRIGHTER_FACTOR, 1.0);
	}

	/**
	 * Creates a new Color that is a more saturated version of this Color.
	 */
	public Color saturate() {
		return deriveColor(0, 1.0 / SATURATE_DESATURATE_FACTOR, 1.0, 1.0);
	}

	/**
	 * Creates a new Color that is a less saturated version of this Color.
	 */
	public Color desaturate() {
		return deriveColor(0, SATURATE_DESATURATE_FACTOR, 1.0, 1.0);
	}

	/**
	 * Creates a new Color that is grayscale equivalent of this Color.
	 * Opacity is preserved.
	 */
	public Color grayscale() {
		double gray = 0.21 * red + 0.71 * green + 0.07 * blue;
		return Color.color(gray, gray, gray, opacity);
	}

	/**
	 * Creates a new Color that is inversion of this Color.
	 * Opacity is preserved.
	 */
	public Color invert() {
		return Color.color(1.0 - red, 1.0 - green, 1.0 - blue, opacity);
	}

	/**
	 * A fully transparent color with an ARGB value of #00000000.
	 */
	public static final Color TRANSPARENT = new Color(0f, 0f, 0f, 0f);

	/**
	 * The color alice blue with an RGB value of #F0F8FF
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#F0F8FF;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color ALICEBLUE = new Color(0.9411765f, 0.972549f, 1.0f);

	/**
	 * The color antique white with an RGB value of #FAEBD7
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#FAEBD7;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color ANTIQUEWHITE = new Color(0.98039216f, 0.92156863f, 0.84313726f);

	/**
	 * The color aqua with an RGB value of #00FFFF
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#00FFFF;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color AQUA = new Color(0.0f, 1.0f, 1.0f);

	/**
	 * The color aquamarine with an RGB value of #7FFFD4
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#7FFFD4;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color AQUAMARINE = new Color(0.49803922f, 1.0f, 0.83137256f);

	/**
	 * The color azure with an RGB value of #F0FFFF
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#F0FFFF;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color AZURE = new Color(0.9411765f, 1.0f, 1.0f);

	/**
	 * The color beige with an RGB value of #F5F5DC
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#F5F5DC;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color BEIGE = new Color(0.9607843f, 0.9607843f, 0.8627451f);

	/**
	 * The color bisque with an RGB value of #FFE4C4
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#FFE4C4;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color BISQUE = new Color(1.0f, 0.89411765f, 0.76862746f);

	/**
	 * The color black with an RGB value of #000000
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#000000;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color BLACK = new Color(0.0f, 0.0f, 0.0f);

	/**
	 * The color blanched almond with an RGB value of #FFEBCD
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#FFEBCD;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color BLANCHEDALMOND = new Color(1.0f, 0.92156863f, 0.8039216f);

	/**
	 * The color blue with an RGB value of #0000FF
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#0000FF;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color BLUE = new Color(0.0f, 0.0f, 1.0f);

	/**
	 * The color blue violet with an RGB value of #8A2BE2
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#8A2BE2;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color BLUEVIOLET = new Color(0.5411765f, 0.16862746f, 0.8862745f);

	/**
	 * The color brown with an RGB value of #A52A2A
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#A52A2A;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color BROWN = new Color(0.64705884f, 0.16470589f, 0.16470589f);

	/**
	 * The color burly wood with an RGB value of #DEB887
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#DEB887;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color BURLYWOOD = new Color(0.87058824f, 0.72156864f, 0.5294118f);

	/**
	 * The color cadet blue with an RGB value of #5F9EA0
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#5F9EA0;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color CADETBLUE = new Color(0.37254903f, 0.61960787f, 0.627451f);

	/**
	 * The color chartreuse with an RGB value of #7FFF00
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#7FFF00;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color CHARTREUSE = new Color(0.49803922f, 1.0f, 0.0f);

	/**
	 * The color chocolate with an RGB value of #D2691E
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#D2691E;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color CHOCOLATE = new Color(0.8235294f, 0.4117647f, 0.11764706f);

	/**
	 * The color coral with an RGB value of #FF7F50
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#FF7F50;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color CORAL = new Color(1.0f, 0.49803922f, 0.3137255f);

	/**
	 * The color cornflower blue with an RGB value of #6495ED
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#6495ED;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color CORNFLOWERBLUE = new Color(0.39215687f, 0.58431375f, 0.92941177f);

	/**
	 * The color cornsilk with an RGB value of #FFF8DC
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#FFF8DC;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color CORNSILK = new Color(1.0f, 0.972549f, 0.8627451f);

	/**
	 * The color crimson with an RGB value of #DC143C
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#DC143C;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color CRIMSON = new Color(0.8627451f, 0.078431375f, 0.23529412f);

	/**
	 * The color cyan with an RGB value of #00FFFF
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#00FFFF;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color CYAN = new Color(0.0f, 1.0f, 1.0f);

	/**
	 * The color dark blue with an RGB value of #00008B
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#00008B;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color DARKBLUE = new Color(0.0f, 0.0f, 0.54509807f);

	/**
	 * The color dark cyan with an RGB value of #008B8B
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#008B8B;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color DARKCYAN = new Color(0.0f, 0.54509807f, 0.54509807f);

	/**
	 * The color dark goldenrod with an RGB value of #B8860B
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#B8860B;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color DARKGOLDENROD = new Color(0.72156864f, 0.5254902f, 0.043137256f);

	/**
	 * The color dark gray with an RGB value of #A9A9A9
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#A9A9A9;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color DARKGRAY = new Color(0.6627451f, 0.6627451f, 0.6627451f);

	/**
	 * The color dark green with an RGB value of #006400
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#006400;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color DARKGREEN = new Color(0.0f, 0.39215687f, 0.0f);

	/**
	 * The color dark grey with an RGB value of #A9A9A9
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#A9A9A9;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color DARKGREY = DARKGRAY;

	/**
	 * The color dark khaki with an RGB value of #BDB76B
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#BDB76B;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color DARKKHAKI = new Color(0.7411765f, 0.7176471f, 0.41960785f);

	/**
	 * The color dark magenta with an RGB value of #8B008B
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#8B008B;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color DARKMAGENTA = new Color(0.54509807f, 0.0f, 0.54509807f);

	/**
	 * The color dark olive green with an RGB value of #556B2F
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#556B2F;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color DARKOLIVEGREEN = new Color(0.33333334f, 0.41960785f, 0.18431373f);

	/**
	 * The color dark orange with an RGB value of #FF8C00
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#FF8C00;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color DARKORANGE = new Color(1.0f, 0.54901963f, 0.0f);

	/**
	 * The color dark orchid with an RGB value of #9932CC
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#9932CC;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color DARKORCHID = new Color(0.6f, 0.19607843f, 0.8f);

	/**
	 * The color dark red with an RGB value of #8B0000
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#8B0000;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color DARKRED = new Color(0.54509807f, 0.0f, 0.0f);

	/**
	 * The color dark salmon with an RGB value of #E9967A
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#E9967A;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color DARKSALMON = new Color(0.9137255f, 0.5882353f, 0.47843137f);

	/**
	 * The color dark sea green with an RGB value of #8FBC8F
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#8FBC8F;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color DARKSEAGREEN = new Color(0.56078434f, 0.7372549f, 0.56078434f);

	/**
	 * The color dark slate blue with an RGB value of #483D8B
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#483D8B;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color DARKSLATEBLUE = new Color(0.28235295f, 0.23921569f, 0.54509807f);

	/**
	 * The color dark slate gray with an RGB value of #2F4F4F
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#2F4F4F;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color DARKSLATEGRAY = new Color(0.18431373f, 0.30980393f, 0.30980393f);

	/**
	 * The color dark slate grey with an RGB value of #2F4F4F
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#2F4F4F;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color DARKSLATEGREY = DARKSLATEGRAY;

	/**
	 * The color dark turquoise with an RGB value of #00CED1
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#00CED1;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color DARKTURQUOISE = new Color(0.0f, 0.80784315f, 0.81960785f);

	/**
	 * The color dark violet with an RGB value of #9400D3
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#9400D3;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color DARKVIOLET = new Color(0.5803922f, 0.0f, 0.827451f);

	/**
	 * The color deep pink with an RGB value of #FF1493
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#FF1493;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color DEEPPINK = new Color(1.0f, 0.078431375f, 0.5764706f);

	/**
	 * The color deep sky blue with an RGB value of #00BFFF
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#00BFFF;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color DEEPSKYBLUE = new Color(0.0f, 0.7490196f, 1.0f);

	/**
	 * The color dim gray with an RGB value of #696969
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#696969;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color DIMGRAY = new Color(0.4117647f, 0.4117647f, 0.4117647f);

	/**
	 * The color dim grey with an RGB value of #696969
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#696969;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color DIMGREY = DIMGRAY;

	/**
	 * The color dodger blue with an RGB value of #1E90FF
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#1E90FF;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color DODGERBLUE = new Color(0.11764706f, 0.5647059f, 1.0f);

	/**
	 * The color firebrick with an RGB value of #B22222
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#B22222;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color FIREBRICK = new Color(0.69803923f, 0.13333334f, 0.13333334f);

	/**
	 * The color floral white with an RGB value of #FFFAF0
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#FFFAF0;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color FLORALWHITE = new Color(1.0f, 0.98039216f, 0.9411765f);

	/**
	 * The color forest green with an RGB value of #228B22
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#228B22;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color FORESTGREEN = new Color(0.13333334f, 0.54509807f, 0.13333334f);

	/**
	 * The color fuchsia with an RGB value of #FF00FF
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#FF00FF;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color FUCHSIA = new Color(1.0f, 0.0f, 1.0f);

	/**
	 * The color gainsboro with an RGB value of #DCDCDC
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#DCDCDC;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color GAINSBORO = new Color(0.8627451f, 0.8627451f, 0.8627451f);

	/**
	 * The color ghost white with an RGB value of #F8F8FF
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#F8F8FF;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color GHOSTWHITE = new Color(0.972549f, 0.972549f, 1.0f);

	/**
	 * The color gold with an RGB value of #FFD700
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#FFD700;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color GOLD = new Color(1.0f, 0.84313726f, 0.0f);

	/**
	 * The color goldenrod with an RGB value of #DAA520
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#DAA520;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color GOLDENROD = new Color(0.85490197f, 0.64705884f, 0.1254902f);

	/**
	 * The color gray with an RGB value of #808080
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#808080;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color GRAY = new Color(0.5019608f, 0.5019608f, 0.5019608f);

	/**
	 * The color green with an RGB value of #008000
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#008000;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color GREEN = new Color(0.0f, 0.5019608f, 0.0f);

	/**
	 * The color green yellow with an RGB value of #ADFF2F
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#ADFF2F;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color GREENYELLOW = new Color(0.6784314f, 1.0f, 0.18431373f);

	/**
	 * The color grey with an RGB value of #808080
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#808080;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color GREY = GRAY;

	/**
	 * The color honeydew with an RGB value of #F0FFF0
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#F0FFF0;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color HONEYDEW = new Color(0.9411765f, 1.0f, 0.9411765f);

	/**
	 * The color hot pink with an RGB value of #FF69B4
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#FF69B4;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color HOTPINK = new Color(1.0f, 0.4117647f, 0.7058824f);

	/**
	 * The color indian red with an RGB value of #CD5C5C
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#CD5C5C;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color INDIANRED = new Color(0.8039216f, 0.36078432f, 0.36078432f);

	/**
	 * The color indigo with an RGB value of #4B0082
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#4B0082;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color INDIGO = new Color(0.29411766f, 0.0f, 0.50980395f);

	/**
	 * The color ivory with an RGB value of #FFFFF0
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#FFFFF0;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color IVORY = new Color(1.0f, 1.0f, 0.9411765f);

	/**
	 * The color khaki with an RGB value of #F0E68C
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#F0E68C;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color KHAKI = new Color(0.9411765f, 0.9019608f, 0.54901963f);

	/**
	 * The color lavender with an RGB value of #E6E6FA
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#E6E6FA;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color LAVENDER = new Color(0.9019608f, 0.9019608f, 0.98039216f);

	/**
	 * The color lavender blush with an RGB value of #FFF0F5
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#FFF0F5;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color LAVENDERBLUSH = new Color(1.0f, 0.9411765f, 0.9607843f);

	/**
	 * The color lawn green with an RGB value of #7CFC00
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#7CFC00;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color LAWNGREEN = new Color(0.4862745f, 0.9882353f, 0.0f);

	/**
	 * The color lemon chiffon with an RGB value of #FFFACD
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#FFFACD;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color LEMONCHIFFON = new Color(1.0f, 0.98039216f, 0.8039216f);

	/**
	 * The color light blue with an RGB value of #ADD8E6
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#ADD8E6;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color LIGHTBLUE = new Color(0.6784314f, 0.84705883f, 0.9019608f);

	/**
	 * The color light coral with an RGB value of #F08080
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#F08080;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color LIGHTCORAL = new Color(0.9411765f, 0.5019608f, 0.5019608f);

	/**
	 * The color light cyan with an RGB value of #E0FFFF
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#E0FFFF;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color LIGHTCYAN = new Color(0.8784314f, 1.0f, 1.0f);

	/**
	 * The color light goldenrod yellow with an RGB value of #FAFAD2
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#FAFAD2;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color LIGHTGOLDENRODYELLOW = new Color(0.98039216f, 0.98039216f, 0.8235294f);

	/**
	 * The color light gray with an RGB value of #D3D3D3
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#D3D3D3;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color LIGHTGRAY = new Color(0.827451f, 0.827451f, 0.827451f);

	/**
	 * The color light green with an RGB value of #90EE90
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#90EE90;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color LIGHTGREEN = new Color(0.5647059f, 0.93333334f, 0.5647059f);

	/**
	 * The color light grey with an RGB value of #D3D3D3
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#D3D3D3;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color LIGHTGREY = LIGHTGRAY;

	/**
	 * The color light pink with an RGB value of #FFB6C1
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#FFB6C1;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color LIGHTPINK = new Color(1.0f, 0.7137255f, 0.75686276f);

	/**
	 * The color light salmon with an RGB value of #FFA07A
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#FFA07A;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color LIGHTSALMON = new Color(1.0f, 0.627451f, 0.47843137f);

	/**
	 * The color light sea green with an RGB value of #20B2AA
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#20B2AA;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color LIGHTSEAGREEN = new Color(0.1254902f, 0.69803923f, 0.6666667f);

	/**
	 * The color light sky blue with an RGB value of #87CEFA
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#87CEFA;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color LIGHTSKYBLUE = new Color(0.5294118f, 0.80784315f, 0.98039216f);

	/**
	 * The color light slate gray with an RGB value of #778899
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#778899;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color LIGHTSLATEGRAY = new Color(0.46666667f, 0.53333336f, 0.6f);

	/**
	 * The color light slate grey with an RGB value of #778899
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#778899;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color LIGHTSLATEGREY = LIGHTSLATEGRAY;

	/**
	 * The color light steel blue with an RGB value of #B0C4DE
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#B0C4DE;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color LIGHTSTEELBLUE = new Color(0.6901961f, 0.76862746f, 0.87058824f);

	/**
	 * The color light yellow with an RGB value of #FFFFE0
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#FFFFE0;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color LIGHTYELLOW = new Color(1.0f, 1.0f, 0.8784314f);

	/**
	 * The color lime with an RGB value of #00FF00
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#00FF00;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color LIME = new Color(0.0f, 1.0f, 0.0f);

	/**
	 * The color lime green with an RGB value of #32CD32
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#32CD32;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color LIMEGREEN = new Color(0.19607843f, 0.8039216f, 0.19607843f);

	/**
	 * The color linen with an RGB value of #FAF0E6
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#FAF0E6;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color LINEN = new Color(0.98039216f, 0.9411765f, 0.9019608f);

	/**
	 * The color magenta with an RGB value of #FF00FF
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#FF00FF;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color MAGENTA = new Color(1.0f, 0.0f, 1.0f);

	/**
	 * The color maroon with an RGB value of #800000
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#800000;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color MAROON = new Color(0.5019608f, 0.0f, 0.0f);

	/**
	 * The color medium aquamarine with an RGB value of #66CDAA
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#66CDAA;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color MEDIUMAQUAMARINE = new Color(0.4f, 0.8039216f, 0.6666667f);

	/**
	 * The color medium blue with an RGB value of #0000CD
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#0000CD;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color MEDIUMBLUE = new Color(0.0f, 0.0f, 0.8039216f);

	/**
	 * The color medium orchid with an RGB value of #BA55D3
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#BA55D3;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color MEDIUMORCHID = new Color(0.7294118f, 0.33333334f, 0.827451f);

	/**
	 * The color medium purple with an RGB value of #9370DB
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#9370DB;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color MEDIUMPURPLE = new Color(0.5764706f, 0.4392157f, 0.85882354f);

	/**
	 * The color medium sea green with an RGB value of #3CB371
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#3CB371;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color MEDIUMSEAGREEN = new Color(0.23529412f, 0.7019608f, 0.44313726f);

	/**
	 * The color medium slate blue with an RGB value of #7B68EE
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#7B68EE;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color MEDIUMSLATEBLUE = new Color(0.48235294f, 0.40784314f, 0.93333334f);

	/**
	 * The color medium spring green with an RGB value of #00FA9A
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#00FA9A;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color MEDIUMSPRINGGREEN = new Color(0.0f, 0.98039216f, 0.6039216f);

	/**
	 * The color medium turquoise with an RGB value of #48D1CC
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#48D1CC;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color MEDIUMTURQUOISE = new Color(0.28235295f, 0.81960785f, 0.8f);

	/**
	 * The color medium violet red with an RGB value of #C71585
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#C71585;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color MEDIUMVIOLETRED = new Color(0.78039217f, 0.08235294f, 0.52156866f);

	/**
	 * The color midnight blue with an RGB value of #191970
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#191970;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color MIDNIGHTBLUE = new Color(0.09803922f, 0.09803922f, 0.4392157f);

	/**
	 * The color mint cream with an RGB value of #F5FFFA
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#F5FFFA;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color MINTCREAM = new Color(0.9607843f, 1.0f, 0.98039216f);

	/**
	 * The color misty rose with an RGB value of #FFE4E1
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#FFE4E1;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color MISTYROSE = new Color(1.0f, 0.89411765f, 0.88235295f);

	/**
	 * The color moccasin with an RGB value of #FFE4B5
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#FFE4B5;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color MOCCASIN = new Color(1.0f, 0.89411765f, 0.70980394f);

	/**
	 * The color navajo white with an RGB value of #FFDEAD
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#FFDEAD;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color NAVAJOWHITE = new Color(1.0f, 0.87058824f, 0.6784314f);

	/**
	 * The color navy with an RGB value of #000080
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#000080;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color NAVY = new Color(0.0f, 0.0f, 0.5019608f);

	/**
	 * The color old lace with an RGB value of #FDF5E6
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#FDF5E6;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color OLDLACE = new Color(0.99215686f, 0.9607843f, 0.9019608f);

	/**
	 * The color olive with an RGB value of #808000
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#808000;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color OLIVE = new Color(0.5019608f, 0.5019608f, 0.0f);

	/**
	 * The color olive drab with an RGB value of #6B8E23
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#6B8E23;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color OLIVEDRAB = new Color(0.41960785f, 0.5568628f, 0.13725491f);

	/**
	 * The color orange with an RGB value of #FFA500
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#FFA500;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color ORANGE = new Color(1.0f, 0.64705884f, 0.0f);

	/**
	 * The color orange red with an RGB value of #FF4500
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#FF4500;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color ORANGERED = new Color(1.0f, 0.27058825f, 0.0f);

	/**
	 * The color orchid with an RGB value of #DA70D6
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#DA70D6;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color ORCHID = new Color(0.85490197f, 0.4392157f, 0.8392157f);

	/**
	 * The color pale goldenrod with an RGB value of #EEE8AA
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#EEE8AA;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color PALEGOLDENROD = new Color(0.93333334f, 0.9098039f, 0.6666667f);

	/**
	 * The color pale green with an RGB value of #98FB98
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#98FB98;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color PALEGREEN = new Color(0.59607846f, 0.9843137f, 0.59607846f);

	/**
	 * The color pale turquoise with an RGB value of #AFEEEE
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#AFEEEE;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color PALETURQUOISE = new Color(0.6862745f, 0.93333334f, 0.93333334f);

	/**
	 * The color pale violet red with an RGB value of #DB7093
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#DB7093;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color PALEVIOLETRED = new Color(0.85882354f, 0.4392157f, 0.5764706f);

	/**
	 * The color papaya whip with an RGB value of #FFEFD5
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#FFEFD5;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color PAPAYAWHIP = new Color(1.0f, 0.9372549f, 0.8352941f);

	/**
	 * The color peach puff with an RGB value of #FFDAB9
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#FFDAB9;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color PEACHPUFF = new Color(1.0f, 0.85490197f, 0.7254902f);

	/**
	 * The color peru with an RGB value of #CD853F
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#CD853F;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color PERU = new Color(0.8039216f, 0.52156866f, 0.24705882f);

	/**
	 * The color pink with an RGB value of #FFC0CB
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#FFC0CB;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color PINK = new Color(1.0f, 0.7529412f, 0.79607844f);

	/**
	 * The color plum with an RGB value of #DDA0DD
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#DDA0DD;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color PLUM = new Color(0.8666667f, 0.627451f, 0.8666667f);

	/**
	 * The color powder blue with an RGB value of #B0E0E6
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#B0E0E6;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color POWDERBLUE = new Color(0.6901961f, 0.8784314f, 0.9019608f);

	/**
	 * The color purple with an RGB value of #800080
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#800080;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color PURPLE = new Color(0.5019608f, 0.0f, 0.5019608f);

	/**
	 * The color red with an RGB value of #FF0000
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#FF0000;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color RED = new Color(1.0f, 0.0f, 0.0f);

	/**
	 * The color rosy brown with an RGB value of #BC8F8F
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#BC8F8F;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color ROSYBROWN = new Color(0.7372549f, 0.56078434f, 0.56078434f);

	/**
	 * The color royal blue with an RGB value of #4169E1
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#4169E1;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color ROYALBLUE = new Color(0.25490198f, 0.4117647f, 0.88235295f);

	/**
	 * The color saddle brown with an RGB value of #8B4513
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#8B4513;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color SADDLEBROWN = new Color(0.54509807f, 0.27058825f, 0.07450981f);

	/**
	 * The color salmon with an RGB value of #FA8072
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#FA8072;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color SALMON = new Color(0.98039216f, 0.5019608f, 0.44705883f);

	/**
	 * The color sandy brown with an RGB value of #F4A460
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#F4A460;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color SANDYBROWN = new Color(0.95686275f, 0.6431373f, 0.3764706f);

	/**
	 * The color sea green with an RGB value of #2E8B57
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#2E8B57;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color SEAGREEN = new Color(0.18039216f, 0.54509807f, 0.34117648f);

	/**
	 * The color sea shell with an RGB value of #FFF5EE
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#FFF5EE;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color SEASHELL = new Color(1.0f, 0.9607843f, 0.93333334f);

	/**
	 * The color sienna with an RGB value of #A0522D
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#A0522D;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color SIENNA = new Color(0.627451f, 0.32156864f, 0.1764706f);

	/**
	 * The color silver with an RGB value of #C0C0C0
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#C0C0C0;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color SILVER = new Color(0.7529412f, 0.7529412f, 0.7529412f);

	/**
	 * The color sky blue with an RGB value of #87CEEB
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#87CEEB;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color SKYBLUE = new Color(0.5294118f, 0.80784315f, 0.92156863f);

	/**
	 * The color slate blue with an RGB value of #6A5ACD
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#6A5ACD;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color SLATEBLUE = new Color(0.41568628f, 0.3529412f, 0.8039216f);

	/**
	 * The color slate gray with an RGB value of #708090
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#708090;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color SLATEGRAY = new Color(0.4392157f, 0.5019608f, 0.5647059f);

	/**
	 * The color slate grey with an RGB value of #708090
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#708090;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color SLATEGREY = SLATEGRAY;

	/**
	 * The color snow with an RGB value of #FFFAFA
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#FFFAFA;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color SNOW = new Color(1.0f, 0.98039216f, 0.98039216f);

	/**
	 * The color spring green with an RGB value of #00FF7F
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#00FF7F;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color SPRINGGREEN = new Color(0.0f, 1.0f, 0.49803922f);

	/**
	 * The color steel blue with an RGB value of #4682B4
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#4682B4;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color STEELBLUE = new Color(0.27450982f, 0.50980395f, 0.7058824f);

	/**
	 * The color tan with an RGB value of #D2B48C
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#D2B48C;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color TAN = new Color(0.8235294f, 0.7058824f, 0.54901963f);

	/**
	 * The color teal with an RGB value of #008080
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#008080;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color TEAL = new Color(0.0f, 0.5019608f, 0.5019608f);

	/**
	 * The color thistle with an RGB value of #D8BFD8
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#D8BFD8;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color THISTLE = new Color(0.84705883f, 0.7490196f, 0.84705883f);

	/**
	 * The color tomato with an RGB value of #FF6347
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#FF6347;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color TOMATO = new Color(1.0f, 0.3882353f, 0.2784314f);

	/**
	 * The color turquoise with an RGB value of #40E0D0
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#40E0D0;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color TURQUOISE = new Color(0.2509804f, 0.8784314f, 0.8156863f);

	/**
	 * The color violet with an RGB value of #EE82EE
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#EE82EE;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color VIOLET = new Color(0.93333334f, 0.50980395f, 0.93333334f);

	/**
	 * The color wheat with an RGB value of #F5DEB3
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#F5DEB3;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color WHEAT = new Color(0.9607843f, 0.87058824f, 0.7019608f);

	/**
	 * The color white with an RGB value of #FFFFFF
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#FFFFFF;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color WHITE = new Color(1.0f, 1.0f, 1.0f);

	/**
	 * The color white smoke with an RGB value of #F5F5F5
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#F5F5F5;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color WHITESMOKE = new Color(0.9607843f, 0.9607843f, 0.9607843f);

	/**
	 * The color yellow with an RGB value of #FFFF00
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#FFFF00;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color YELLOW = new Color(1.0f, 1.0f, 0.0f);

	/**
	 * The color yellow green with an RGB value of #9ACD32
	 * <div style="border:1px solid black;width:40px;height:20px;background-color:#9ACD32;float:right;margin: 0 10px 0 0"></div><br/><br/>
	 */
	public static final Color YELLOWGREEN = new Color(0.6039216f, 0.8039216f, 0.19607843f);

	/*
	 * Named colors moved to nested class to initialize them only when they
	 * are needed.
	 */
	private static final class NamedColors {
		private static final Map<String, Color> namedColors = createNamedColors();

		private NamedColors() {
		}

		private static Color get(String name) {
			return namedColors.get(name);
		}

		private static Map<String, Color> createNamedColors() {
			Map<String, Color> colors = new HashMap<String, Color>(256);

			colors.put("aliceblue", ALICEBLUE);
			colors.put("antiquewhite", ANTIQUEWHITE);
			colors.put("aqua", AQUA);
			colors.put("aquamarine", AQUAMARINE);
			colors.put("azure", AZURE);
			colors.put("beige", BEIGE);
			colors.put("bisque", BISQUE);
			colors.put("black", BLACK);
			colors.put("blanchedalmond", BLANCHEDALMOND);
			colors.put("blue", BLUE);
			colors.put("blueviolet", BLUEVIOLET);
			colors.put("brown", BROWN);
			colors.put("burlywood", BURLYWOOD);
			colors.put("cadetblue", CADETBLUE);
			colors.put("chartreuse", CHARTREUSE);
			colors.put("chocolate", CHOCOLATE);
			colors.put("coral", CORAL);
			colors.put("cornflowerblue", CORNFLOWERBLUE);
			colors.put("cornsilk", CORNSILK);
			colors.put("crimson", CRIMSON);
			colors.put("cyan", CYAN);
			colors.put("darkblue", DARKBLUE);
			colors.put("darkcyan", DARKCYAN);
			colors.put("darkgoldenrod", DARKGOLDENROD);
			colors.put("darkgray", DARKGRAY);
			colors.put("darkgreen", DARKGREEN);
			colors.put("darkgrey", DARKGREY);
			colors.put("darkkhaki", DARKKHAKI);
			colors.put("darkmagenta", DARKMAGENTA);
			colors.put("darkolivegreen", DARKOLIVEGREEN);
			colors.put("darkorange", DARKORANGE);
			colors.put("darkorchid", DARKORCHID);
			colors.put("darkred", DARKRED);
			colors.put("darksalmon", DARKSALMON);
			colors.put("darkseagreen", DARKSEAGREEN);
			colors.put("darkslateblue", DARKSLATEBLUE);
			colors.put("darkslategray", DARKSLATEGRAY);
			colors.put("darkslategrey", DARKSLATEGREY);
			colors.put("darkturquoise", DARKTURQUOISE);
			colors.put("darkviolet", DARKVIOLET);
			colors.put("deeppink", DEEPPINK);
			colors.put("deepskyblue", DEEPSKYBLUE);
			colors.put("dimgray", DIMGRAY);
			colors.put("dimgrey", DIMGREY);
			colors.put("dodgerblue", DODGERBLUE);
			colors.put("firebrick", FIREBRICK);
			colors.put("floralwhite", FLORALWHITE);
			colors.put("forestgreen", FORESTGREEN);
			colors.put("fuchsia", FUCHSIA);
			colors.put("gainsboro", GAINSBORO);
			colors.put("ghostwhite", GHOSTWHITE);
			colors.put("gold", GOLD);
			colors.put("goldenrod", GOLDENROD);
			colors.put("gray", GRAY);
			colors.put("green", GREEN);
			colors.put("greenyellow", GREENYELLOW);
			colors.put("grey", GREY);
			colors.put("honeydew", HONEYDEW);
			colors.put("hotpink", HOTPINK);
			colors.put("indianred", INDIANRED);
			colors.put("indigo", INDIGO);
			colors.put("ivory", IVORY);
			colors.put("khaki", KHAKI);
			colors.put("lavender", LAVENDER);
			colors.put("lavenderblush", LAVENDERBLUSH);
			colors.put("lawngreen", LAWNGREEN);
			colors.put("lemonchiffon", LEMONCHIFFON);
			colors.put("lightblue", LIGHTBLUE);
			colors.put("lightcoral", LIGHTCORAL);
			colors.put("lightcyan", LIGHTCYAN);
			colors.put("lightgoldenrodyellow", LIGHTGOLDENRODYELLOW);
			colors.put("lightgray", LIGHTGRAY);
			colors.put("lightgreen", LIGHTGREEN);
			colors.put("lightgrey", LIGHTGREY);
			colors.put("lightpink", LIGHTPINK);
			colors.put("lightsalmon", LIGHTSALMON);
			colors.put("lightseagreen", LIGHTSEAGREEN);
			colors.put("lightskyblue", LIGHTSKYBLUE);
			colors.put("lightslategray", LIGHTSLATEGRAY);
			colors.put("lightslategrey", LIGHTSLATEGREY);
			colors.put("lightsteelblue", LIGHTSTEELBLUE);
			colors.put("lightyellow", LIGHTYELLOW);
			colors.put("lime", LIME);
			colors.put("limegreen", LIMEGREEN);
			colors.put("linen", LINEN);
			colors.put("magenta", MAGENTA);
			colors.put("maroon", MAROON);
			colors.put("mediumaquamarine", MEDIUMAQUAMARINE);
			colors.put("mediumblue", MEDIUMBLUE);
			colors.put("mediumorchid", MEDIUMORCHID);
			colors.put("mediumpurple", MEDIUMPURPLE);
			colors.put("mediumseagreen", MEDIUMSEAGREEN);
			colors.put("mediumslateblue", MEDIUMSLATEBLUE);
			colors.put("mediumspringgreen", MEDIUMSPRINGGREEN);
			colors.put("mediumturquoise", MEDIUMTURQUOISE);
			colors.put("mediumvioletred", MEDIUMVIOLETRED);
			colors.put("midnightblue", MIDNIGHTBLUE);
			colors.put("mintcream", MINTCREAM);
			colors.put("mistyrose", MISTYROSE);
			colors.put("moccasin", MOCCASIN);
			colors.put("navajowhite", NAVAJOWHITE);
			colors.put("navy", NAVY);
			colors.put("oldlace", OLDLACE);
			colors.put("olive", OLIVE);
			colors.put("olivedrab", OLIVEDRAB);
			colors.put("orange", ORANGE);
			colors.put("orangered", ORANGERED);
			colors.put("orchid", ORCHID);
			colors.put("palegoldenrod", PALEGOLDENROD);
			colors.put("palegreen", PALEGREEN);
			colors.put("paleturquoise", PALETURQUOISE);
			colors.put("palevioletred", PALEVIOLETRED);
			colors.put("papayawhip", PAPAYAWHIP);
			colors.put("peachpuff", PEACHPUFF);
			colors.put("peru", PERU);
			colors.put("pink", PINK);
			colors.put("plum", PLUM);
			colors.put("powderblue", POWDERBLUE);
			colors.put("purple", PURPLE);
			colors.put("red", RED);
			colors.put("rosybrown", ROSYBROWN);
			colors.put("royalblue", ROYALBLUE);
			colors.put("saddlebrown", SADDLEBROWN);
			colors.put("salmon", SALMON);
			colors.put("sandybrown", SANDYBROWN);
			colors.put("seagreen", SEAGREEN);
			colors.put("seashell", SEASHELL);
			colors.put("sienna", SIENNA);
			colors.put("silver", SILVER);
			colors.put("skyblue", SKYBLUE);
			colors.put("slateblue", SLATEBLUE);
			colors.put("slategray", SLATEGRAY);
			colors.put("slategrey", SLATEGREY);
			colors.put("snow", SNOW);
			colors.put("springgreen", SPRINGGREEN);
			colors.put("steelblue", STEELBLUE);
			colors.put("tan", TAN);
			colors.put("teal", TEAL);
			colors.put("thistle", THISTLE);
			colors.put("tomato", TOMATO);
			colors.put("transparent", TRANSPARENT);
			colors.put("turquoise", TURQUOISE);
			colors.put("violet", VIOLET);
			colors.put("wheat", WHEAT);
			colors.put("white", WHITE);
			colors.put("whitesmoke", WHITESMOKE);
			colors.put("yellow", YELLOW);
			colors.put("yellowgreen", YELLOWGREEN);

			return colors;
		}
	}

	/**
	 * The red component of the {@code Color}, in the range {@code 0.0-1.0}.
	 *
	 * @defaultValue 0.0
	 */
	public final double getRed() {
		return red;
	}

	private float red;

	/**
	 * The green component of the {@code Color}, in the range {@code 0.0-1.0}.
	 *
	 * @defaultValue 0.0
	 */
	public final double getGreen() {
		return green;
	}

	private float green;

	/**
	 * The blue component of the {@code Color}, in the range {@code 0.0-1.0}.
	 *
	 * @defaultValue 0.0
	 */
	public final double getBlue() {
		return blue;
	}

	private float blue;

	/**
	 * The opacity of the {@code Color}, in the range {@code 0.0-1.0}.
	 *
	 * @defaultValue 1.0
	 */
	public final double getOpacity() {
		return opacity;
	}

	private float opacity = 1;

	/**
	 * @inheritDoc
	 * @since JavaFX 8.0
	 */
	@Override
	public final boolean isOpaque() {
		return opacity >= 1f;
	}

	//private Object platformPaint;

	/**
	 * Creates a new instance of color
	 * @param red red component ranging from {@code 0} to {@code 1}
	 * @param green green component ranging from {@code 0} to {@code 1}
	 * @param blue blue component ranging from {@code 0} to {@code 1}
	 * @param opacity opacity ranging from {@code 0} to {@code 1}
	 */
	public Color(double red, double green, double blue, double opacity) {
		if (red < 0 || red > 1) {
			throw new IllegalArgumentException("Color's red value (" + red + ") must be in the range 0.0-1.0");
		}
		if (green < 0 || green > 1) {
			throw new IllegalArgumentException("Color's green value (" + green + ") must be in the range 0.0-1.0");
		}
		if (blue < 0 || blue > 1) {
			throw new IllegalArgumentException("Color's blue value (" + blue + ") must be in the range 0.0-1.0");
		}
		if (opacity < 0 || opacity > 1) {
			throw new IllegalArgumentException("Color's opacity value (" + opacity + ") must be in the range 0.0-1.0");
		}

		this.red = (float) red;
		this.green = (float) green;
		this.blue = (float) blue;
		this.opacity = (float) opacity;
	}

	/**
	 * Creates a new instance of color. This constructor performs no integrity
	 * checks, and thus should ONLY be used by internal code in Color which
	 * knows that the hard-coded values are correct.
	 *
	 * @param red red component ranging from {@code 0} to {@code 1}
	 * @param green green component ranging from {@code 0} to {@code 1}
	 * @param blue blue component ranging from {@code 0} to {@code 1}
	 */
	private Color(float red, float green, float blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;
	}

	/*
	@Override
	Object acc_getPlatformPaint() {
	    if (platformPaint == null) {
	        platformPaint = Toolkit.getToolkit().getPaint(this);
	    }
	    return platformPaint;
	}
	
	*/
	/**
	 * @inheritDoc
	 */

	@Override
	public Color interpolate(Color endValue, double t) {
		if (t <= 0.0)
			return this;
		if (t >= 1.0)
			return endValue;
		float ft = (float) t;
		return new Color(red + (endValue.red - red) * ft, green + (endValue.green - green) * ft,
				blue + (endValue.blue - blue) * ft, opacity + (endValue.opacity - opacity) * ft);
	}

	/**
	 * Indicates whether some other object is "equal to" this one.
	 * @param obj the reference object with which to compare.
	 * @return {@code true} if this object is equal to the {@code obj} argument; {@code false} otherwise.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof Color) {
			Color other = (Color) obj;
			return red == other.red && green == other.green && blue == other.blue && opacity == other.opacity;
		} else
			return false;
	}

	/**
	 * Returns a hash code for this {@code Color} object.
	 * @return a hash code for this {@code Color} object.
	 */
	@Override
	public int hashCode() {
		// construct the 32bit integer representation of this color
		int r = (int) Math.round(red * 255.0);
		int g = (int) Math.round(green * 255.0);
		int b = (int) Math.round(blue * 255.0);
		int a = (int) Math.round(opacity * 255.0);
		return to32BitInteger(r, g, b, a);
	}

	/**
	 * Returns a string representation of this {@code Color}.
	 * This method is intended to be used only for informational purposes.
	 * The content and format of the returned string might vary between implementations.
	 * The returned string might be empty but cannot be {@code null}.
	 *
	 * @return the string representation
	 */
	@Override
	public String toString() {
		int r = (int) Math.round(red * 255.0);
		int g = (int) Math.round(green * 255.0);
		int b = (int) Math.round(blue * 255.0);
		int o = (int) Math.round(opacity * 255.0);
		return "0x" + Numbers.twoDigits(r) + 'x' + Numbers.twoDigits(g) + 'x' + Numbers.twoDigits(b) + 'x'
				+ Numbers.twoDigits(o);
	}

	/***************************************************************************
	 *                                                                         *
	 * Color-related utilities                                                 *
	 *                                                                         *
	 **************************************************************************/

	/**
	 * Calculates a perceptual brightness for a color between 0.0 black and 1.0 while
	 */
	private static double calculateBrightness(Color color) {
		return (0.3 * color.getRed()) + (0.59 * color.getGreen()) + (0.11 * color.getBlue());
	}

	/**
	 * Derives a lighter or darker of a given color.
	 *
	 * @param c           The color to derive from
	 * @param brightness  The brightness difference for the new color -1.0 being 100% dark which is always black, 0.0 being
	 *                    no change and 1.0 being 100% lighter which is always white
	 */
	private static Color deriveColor(Color c, double brightness) {
		double baseBrightness = calculateBrightness(c);
		double calcBrightness = brightness;
		// Fine adjustments to colors in ranges of brightness to adjust the contrast for them
		if (brightness > 0) {
			if (baseBrightness > 0.85) {
				calcBrightness = calcBrightness * 1.6;
			} else if (baseBrightness > 0.6) {
				// no change
			} else if (baseBrightness > 0.5) {
				calcBrightness = calcBrightness * 0.9;
			} else if (baseBrightness > 0.4) {
				calcBrightness = calcBrightness * 0.8;
			} else if (baseBrightness > 0.3) {
				calcBrightness = calcBrightness * 0.7;
			} else {
				calcBrightness = calcBrightness * 0.6;
			}
		} else {
			if (baseBrightness < 0.2) {
				calcBrightness = calcBrightness * 0.6;
			}
		}
		// clamp brightness
		if (calcBrightness < -1) {
			calcBrightness = -1;
		} else if (calcBrightness > 1) {
			calcBrightness = 1;
		}
		// window two take the calculated brightness multiplyer and derive color based on source color
		double[] hsb = RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue());
		// change brightness
		if (calcBrightness > 0) { // brighter
			hsb[1] *= 1 - calcBrightness;
			hsb[2] += (1 - hsb[2]) * calcBrightness;
		} else { // darker
			hsb[2] *= calcBrightness + 1;
		}
		// clip saturation and brightness
		if (hsb[1] < 0) {
			hsb[1] = 0;
		} else if (hsb[1] > 1) {
			hsb[1] = 1;
		}
		if (hsb[2] < 0) {
			hsb[2] = 0;
		} else if (hsb[2] > 1) {
			hsb[2] = 1;
		}
		// convert back to color
		Color c2 = Color.hsb((int) hsb[0], hsb[1], hsb[2], c.getOpacity());
		return Color.hsb((int) hsb[0], hsb[1], hsb[2], c.getOpacity());

		/*   var hsb:Number[] = RGBtoHSB(c.red,c.green,c.blue);
		// change brightness
		if (brightness > 0) {
		    //var bright:Number = brightness * (1-calculateBrightness(c));
		    var bright:Number = if (calculateBrightness(c)<0.65 and brightness > 0.5) {
		            if (calculateBrightness(c)<0.2) then brightness * 0.55 else brightness * 0.7
		    } else brightness;
		    // brighter
		    hsb[1] *= 1 - bright;
		    hsb[2] += (1 - hsb[2]) * bright;
		} else {
		    // darker
		    hsb[2] *= brightness+1;
		}
		// clip saturation and brightness
		if (hsb[1] < 0) { hsb[1] = 0;} else if (hsb[1] > 1) {hsb[1] = 1}
		if (hsb[2] < 0) { hsb[2] = 0;} else if (hsb[2] > 1) {hsb[2] = 1}
		// convert back to color
		return Color.hsb(hsb[0],hsb[1],hsb[2]) */
	}

	/**
	 * interpolate at a set {@code position} between two colors {@code color1} and {@code color2}.
	 * The interpolation is done is linear RGB color space not the default sRGB color space.
	 */
	private static Color interpolateLinear(double position, Color color1, Color color2) {
		Color c1Linear = convertSRGBtoLinearRGB(color1);
		Color c2Linear = convertSRGBtoLinearRGB(color2);
		return convertLinearRGBtoSRGB(
				Color.color(c1Linear.getRed() + (c2Linear.getRed() - c1Linear.getRed()) * position,
						c1Linear.getGreen() + (c2Linear.getGreen() - c1Linear.getGreen()) * position,
						c1Linear.getBlue() + (c2Linear.getBlue() - c1Linear.getBlue()) * position,
						c1Linear.getOpacity() + (c2Linear.getOpacity() - c1Linear.getOpacity()) * position));
	}

	/**
	 * Get the color at the give {@code position} in the ladder of color stops
	 */
	private static Color ladder(final double position, final Stop[] stops) {
		Stop prevStop = null;
		for (int i = 0; i < stops.length; i++) {
			Stop stop = stops[i];
			if (position <= stop.getOffset()) {
				if (prevStop == null) {
					return stop.getColor();
				} else {
					return interpolateLinear(
							(position - prevStop.getOffset()) / (stop.getOffset() - prevStop.getOffset()),
							prevStop.getColor(), stop.getColor());
				}
			}
			prevStop = stop;
		}
		// position is greater than biggest stop, so will we biggest stop's color
		return prevStop.getColor();
	}

	/**
	 * Get the color at the give {@code position} in the ladder of color stops
	 */
	private static Color ladder(final Color color, final Stop[] stops) {
		return ladder(calculateBrightness(color), stops);
	}

	private static double[] HSBtoRGB(double hue, double saturation, double brightness) {
		// normalize the hue
		double normalizedHue = ((hue % 360) + 360) % 360;
		hue = normalizedHue / 360;

		double r = 0, g = 0, b = 0;
		if (saturation == 0) {
			r = g = b = brightness;
		} else {
			double h = (hue - Math.floor(hue)) * 6.0;
			double f = h - java.lang.Math.floor(h);
			double p = brightness * (1.0 - saturation);
			double q = brightness * (1.0 - saturation * f);
			double t = brightness * (1.0 - (saturation * (1.0 - f)));
			switch ((int) h) {
			case 0:
				r = brightness;
				g = t;
				b = p;
				break;
			case 1:
				r = q;
				g = brightness;
				b = p;
				break;
			case 2:
				r = p;
				g = brightness;
				b = t;
				break;
			case 3:
				r = p;
				g = q;
				b = brightness;
				break;
			case 4:
				r = t;
				g = p;
				b = brightness;
				break;
			case 5:
				r = brightness;
				g = p;
				b = q;
				break;
			}
		}
		double[] f = new double[3];
		f[0] = r;
		f[1] = g;
		f[2] = b;
		return f;
	}

	private static double[] RGBtoHSB(double r, double g, double b) {
		double hue, saturation, brightness;
		double[] hsbvals = new double[3];
		double cmax = (r > g) ? r : g;
		if (b > cmax)
			cmax = b;
		double cmin = (r < g) ? r : g;
		if (b < cmin)
			cmin = b;

		brightness = cmax;
		if (cmax != 0)
			saturation = (cmax - cmin) / cmax;
		else
			saturation = 0;

		if (saturation == 0) {
			hue = 0;
		} else {
			double redc = (cmax - r) / (cmax - cmin);
			double greenc = (cmax - g) / (cmax - cmin);
			double bluec = (cmax - b) / (cmax - cmin);
			if (r == cmax)
				hue = bluec - greenc;
			else if (g == cmax)
				hue = 2.0 + redc - bluec;
			else
				hue = 4.0 + greenc - redc;
			hue = hue / 6.0;
			if (hue < 0)
				hue = hue + 1.0;
		}
		hsbvals[0] = hue * 360;
		hsbvals[1] = saturation;
		hsbvals[2] = brightness;
		return hsbvals;
	}

	/**
	 * Helper function to convert a color in sRGB space to linear RGB space.
	 */
	private static Color convertSRGBtoLinearRGB(Color color) {
		double[] colors = new double[] { color.getRed(), color.getGreen(), color.getBlue() };
		for (int i = 0; i < colors.length; i++) {
			if (colors[i] <= 0.04045) {
				colors[i] = colors[i] / 12.92;
			} else {
				colors[i] = Math.pow((colors[i] + 0.055) / 1.055, 2.4);
			}
		}
		return Color.color(colors[0], colors[1], colors[2], color.getOpacity());
	}

	/**
	 * Helper function to convert a color in linear RGB space to SRGB space.
	 */
	private static Color convertLinearRGBtoSRGB(Color color) {
		double[] colors = new double[] { color.getRed(), color.getGreen(), color.getBlue() };
		for (int i = 0; i < colors.length; i++) {
			if (colors[i] <= 0.0031308) {
				colors[i] = colors[i] * 12.92;
			} else {
				colors[i] = (1.055 * Math.pow(colors[i], (1.0 / 2.4))) - 0.055;
			}
		}
		return Color.color(colors[0], colors[1], colors[2], color.getOpacity());
	}
}
