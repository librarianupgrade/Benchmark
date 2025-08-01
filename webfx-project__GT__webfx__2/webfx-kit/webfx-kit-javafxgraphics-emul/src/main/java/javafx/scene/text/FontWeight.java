package javafx.scene.text;

/**
 * Specifies different font weights which can be used when searching for a
 * font on the system.
 * The names correspond to pre-defined weights in the OS/2 table of the
 * <A HREF=http://www.microsoft.com/typography/otspec/os2.htm#wtc>
 * OpenType font specification</A>.
 * The CSS 3 specification references the same as a sequence of values.
 */
public enum FontWeight {

	/**
	 * represents Thin font weight (100).
	 */
	THIN(100, "Thin"),

	/**
	 * represents 'Extra Light' font weight (200).
	 */
	EXTRA_LIGHT(200, "Extra Light", "Ultra Light"),

	/**
	 * represents Light font weight (300).
	 */
	LIGHT(300, "Light"),

	/**
	 * represents Normal font weight (400).
	 */
	NORMAL(400, "Normal", "Regular"),

	/**
	 * represents Medium font weight (500).
	 */
	MEDIUM(500, "Medium"),

	/**
	 * represents 'Demi Bold' font weight (600).
	 */
	SEMI_BOLD(600, "Semi Bold", "Demi Bold"),

	/**
	 * represents Bold font weight (700).
	 */
	BOLD(700, "Bold"),

	/**
	 * represents 'Extra Bold' font weight (800).
	 */
	EXTRA_BOLD(800, "Extra Bold", "Ultra Bold"),

	/**
	 * represents Black font weight (900).
	 */
	BLACK(900, "Black", "Heavy");

	private final int weight;
	private final String[] names;

	FontWeight(int weight, String... names) {
		this.weight = weight;
		this.names = names;
	}

	/**
	 * Return the visual weight (degree of blackness or thickness)
	 * specified by this {@code FontWeight}.
	 * @return weight
	 */
	public int getWeight() {
		return weight;
	}

	/**
	 * Returns {@code FontWeight} by its name.
	 *
	 * @param name name of the {@code FontWeight}
	 */
	public static FontWeight findByName(String name) {
		if (name == null)
			return null;

		for (FontWeight w : FontWeight.values())
			for (String n : w.names)
				if (n.equalsIgnoreCase(name))
					return w;

		return null;
	}

	/**
	 * Returns the closest {@code FontWeight} for a weight
	 * value as defined by the CSS and OpenType specifications.
	 * Where the specified value  is equidistant between two
	 * {@code FontWeight} values, then the implementation may
	 * select either at its discretion.
	 * This lookup is without reference to a font, so this is
	 * purely a mapping to the set of {@code FontWeight} instances
	 * and does not mean that a font of that weight will be available.
	 * @return closest {@code FontWeight}
	 */
	public static FontWeight findByWeight(int weight) {
		if (weight <= 150)
			return THIN;
		if (weight <= 250)
			return EXTRA_LIGHT;
		if (weight < 350)
			return LIGHT;
		if (weight <= 450)
			return NORMAL;
		if (weight <= 550)
			return MEDIUM;
		if (weight < 650)
			return SEMI_BOLD;
		if (weight <= 750)
			return BOLD;
		if (weight <= 850)
			return EXTRA_BOLD;
		return BLACK;
	}
}
