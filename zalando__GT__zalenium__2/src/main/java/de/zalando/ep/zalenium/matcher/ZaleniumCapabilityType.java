package de.zalando.ep.zalenium.matcher;

public interface ZaleniumCapabilityType {
	String CUSTOM_CAPABILITY_PREFIX = "zal:";
	String TEST_NAME_NO_PREFIX = "name";
	String TEST_NAME = CUSTOM_CAPABILITY_PREFIX.concat(TEST_NAME_NO_PREFIX);
	String BUILD_NAME_NO_PREFIX = "build";
	String BUILD_NAME = CUSTOM_CAPABILITY_PREFIX.concat(BUILD_NAME_NO_PREFIX);
	String IDLE_TIMEOUT_NO_PREFIX = "idleTimeout";
	String IDLE_TIMEOUT = CUSTOM_CAPABILITY_PREFIX.concat(IDLE_TIMEOUT_NO_PREFIX);
	String SCREEN_RESOLUTION_NO_PREFIX = "screenResolution";
	String SCREEN_RESOLUTION = CUSTOM_CAPABILITY_PREFIX.concat(SCREEN_RESOLUTION_NO_PREFIX);
	String RESOLUTION_NO_PREFIX = "resolution";
	String RESOLUTION = CUSTOM_CAPABILITY_PREFIX.concat(RESOLUTION_NO_PREFIX);
	String SCREEN_RESOLUTION_DASH_NO_PREFIX = "screen-resolution";
	String SCREEN_RESOLUTION_DASH = CUSTOM_CAPABILITY_PREFIX.concat(SCREEN_RESOLUTION_DASH_NO_PREFIX);
	String RECORD_VIDEO_NO_PREFIX = "recordVideo";
	String RECORD_VIDEO = CUSTOM_CAPABILITY_PREFIX.concat(RECORD_VIDEO_NO_PREFIX);
	String TIME_ZONE_NO_PREFIX = "tz";
	String TIME_ZONE = CUSTOM_CAPABILITY_PREFIX.concat(TIME_ZONE_NO_PREFIX);
}
