package com.nepxion.discovery.console.desktop.workspace.type;

/**
 * <p>Title: Nepxion Discovery</p>
 * <p>Description: Nepxion Discovery</p>
 * <p>Copyright: Copyright (c) 2017-2050</p>
 * <p>Company: Nepxion</p>
 * @author Haojun Ren
 * @version 1.0
 */

public enum ProtocolType {
	HTTP("http://"), HTTPS("https://");

	private String value;

	private ProtocolType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static ProtocolType fromString(String value) {
		for (ProtocolType type : ProtocolType.values()) {
			if (type.getValue().equalsIgnoreCase(value)) {
				return type;
			}
		}

		throw new IllegalArgumentException("No matched type with value=" + value);
	}

	@Override
	public String toString() {
		return value;
	}
}