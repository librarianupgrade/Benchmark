package io.github.zeroone3010.yahueapi;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * The Hue Bridge recognizes the concept of an "alert" as blinking the lights. This enumeration holds
 * the different alert types so that one may activate a short alert, a long alert, or stop alerting altogether.
 *
 * @since 2.1.0
 */
public enum AlertType {
	/**
	 * A short blink.
	 */
	SHORT_ALERT("select"),

	/**
	 * Blinking for 15 seconds.
	 */
	LONG_ALERT("lselect"),

	/**
	 * No alert. Disables an ongoing alert.
	 */
	NONE("none"),

	/**
	 * A type of alert not recognized by this version of the library.
	 */
	UNKNOWN(null);

	private String apiString;

	AlertType(final String apiString) {
		this.apiString = apiString;
	}

	public static AlertType parseTypeString(final String type) {
		if (type == null) {
			return null;
		}
		return Stream.of(values()).filter(alertType -> Objects.equals(alertType.getApiString(), type)).findFirst()
				.orElse(AlertType.UNKNOWN);
	}

	@JsonValue
	public String getApiString() {
		return apiString;
	}
}
