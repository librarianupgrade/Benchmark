package de.zalando.ep.zalenium.matcher;

import de.zalando.ep.zalenium.proxy.DockerSeleniumStarterRemoteProxy;
import org.openqa.grid.internal.TestSlot;
import org.openqa.grid.internal.utils.DefaultCapabilityMatcher;
import org.openqa.grid.selenium.proxy.DefaultRemoteProxy;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.CapabilityType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DockerSeleniumCapabilityMatcher extends DefaultCapabilityMatcher {
	private static final List<String> ZALENIUM_CUSTOM_CAPABILITIES_NO_PREFIX = Arrays.asList(
			ZaleniumCapabilityType.TEST_NAME_NO_PREFIX, ZaleniumCapabilityType.BUILD_NAME_NO_PREFIX,
			ZaleniumCapabilityType.IDLE_TIMEOUT_NO_PREFIX, ZaleniumCapabilityType.SCREEN_RESOLUTION_NO_PREFIX,
			ZaleniumCapabilityType.RESOLUTION_NO_PREFIX, ZaleniumCapabilityType.SCREEN_RESOLUTION_DASH_NO_PREFIX,
			ZaleniumCapabilityType.RECORD_VIDEO_NO_PREFIX, ZaleniumCapabilityType.TIME_ZONE_NO_PREFIX);

	private static String chromeVersion = null;
	private static String firefoxVersion = null;
	private static AtomicBoolean browserVersionsFetched = new AtomicBoolean(false);
	private final Logger logger = Logger.getLogger(DockerSeleniumCapabilityMatcher.class.getName());
	private DefaultRemoteProxy proxy;

	public DockerSeleniumCapabilityMatcher(DefaultRemoteProxy defaultRemoteProxy) {
		super();
		proxy = defaultRemoteProxy;
	}

	@Override
	public boolean matches(Map<String, Object> nodeCapability, Map<String, Object> requestedCapability) {
		logger.log(Level.FINE,
				() -> String.format("Validating %s in node with capabilities %s", requestedCapability, nodeCapability));

		// We do this because the starter node does not have the browser versions when Zalenium starts
		if (proxy instanceof DockerSeleniumStarterRemoteProxy) {
			Map<String, Object> requestedCapabilityCopy = copyMap(requestedCapability);
			if (browserVersionsFetched.get()) {
				String browser = nodeCapability.get(CapabilityType.BROWSER_NAME).toString();
				if (BrowserType.FIREFOX.equalsIgnoreCase(browser)) {
					nodeCapability.put(CapabilityType.VERSION, firefoxVersion);
				} else if (BrowserType.CHROME.equalsIgnoreCase(browser)) {
					nodeCapability.put(CapabilityType.VERSION, chromeVersion);
				}
			} else {
				requestedCapabilityCopy.remove(CapabilityType.VERSION);
			}
			return super.matches(nodeCapability, requestedCapabilityCopy);
		}

		// DockerSeleniumRemoteProxy part
		if (super.matches(nodeCapability, requestedCapability)) {
			getChromeAndFirefoxVersions(proxy);

			// Prefix Zalenium custom capabilities here (both node and requested)
			prefixZaleniumCustomCapabilities(nodeCapability);
			prefixZaleniumCustomCapabilities(requestedCapability);

			boolean screenResolutionMatches = isScreenResolutionMatching(nodeCapability, requestedCapability);
			boolean timeZoneCapabilityMatches = isTimeZoneMatching(nodeCapability, requestedCapability);
			return screenResolutionMatches && timeZoneCapabilityMatches;
		}
		return false;
	}

	private void prefixZaleniumCustomCapabilities(Map<String, Object> capabilities) {
		for (String zaleniumCustomCapability : ZALENIUM_CUSTOM_CAPABILITIES_NO_PREFIX) {
			if (capabilities.containsKey(zaleniumCustomCapability)) {
				String prefixedCapability = ZaleniumCapabilityType.CUSTOM_CAPABILITY_PREFIX
						.concat(zaleniumCustomCapability);
				capabilities.put(prefixedCapability, capabilities.get(zaleniumCustomCapability));
				capabilities.remove(zaleniumCustomCapability);
			}
		}
	}

	// Cannot use Collectors.toMap() because it fails when there are null values.
	private Map<String, Object> copyMap(Map<String, Object> mapToCopy) {
		Map<String, Object> copiedMap = new HashMap<>();
		for (Map.Entry<String, Object> entry : mapToCopy.entrySet()) {
			copiedMap.put(entry.getKey(), entry.getValue());
		}
		return copiedMap;
	}

	private void getChromeAndFirefoxVersions(DefaultRemoteProxy proxy) {
		if (!browserVersionsFetched.getAndSet(true)) {
			for (TestSlot testSlot : proxy.getTestSlots()) {
				String browser = testSlot.getCapabilities().get(CapabilityType.BROWSER_NAME).toString();
				String browserVersion = testSlot.getCapabilities().get(CapabilityType.VERSION).toString();
				if (BrowserType.CHROME.equalsIgnoreCase(browser)) {
					chromeVersion = browserVersion;
				} else if (BrowserType.FIREFOX.equalsIgnoreCase(browser)) {
					firefoxVersion = browserVersion;
				}
			}
		}
	}

	private boolean isScreenResolutionMatching(Map<String, Object> nodeCapability,
			Map<String, Object> requestedCapability) {
		boolean screenResolutionCapabilityMatches = true;
		boolean screenSizeCapabilityIsRequested = false;

		List<String> screenResolutionNames = Arrays.asList(ZaleniumCapabilityType.SCREEN_RESOLUTION,
				ZaleniumCapabilityType.RESOLUTION, ZaleniumCapabilityType.SCREEN_RESOLUTION_DASH);

		for (String screenResolutionName : screenResolutionNames) {
			if (requestedCapability.containsKey(screenResolutionName)) {
				screenSizeCapabilityIsRequested = true;
				screenResolutionCapabilityMatches = nodeCapability.containsKey(screenResolutionName)
						&& requestedCapability.get(screenResolutionName)
								.equals(nodeCapability.get(screenResolutionName));
			}
		}

		/*
		    This node has a screen size different from the default/configured one,
		    and no special screen size was requested...
		    then this validation prevents requests using nodes that were created with specific screen sizes
		 */
		String defaultScreenResolution = String.format("%sx%s",
				DockerSeleniumStarterRemoteProxy.getConfiguredScreenSize().getWidth(),
				DockerSeleniumStarterRemoteProxy.getConfiguredScreenSize().getHeight());
		String nodeScreenResolution = nodeCapability.get(ZaleniumCapabilityType.SCREEN_RESOLUTION).toString();
		if (!screenSizeCapabilityIsRequested && !defaultScreenResolution.equalsIgnoreCase(nodeScreenResolution)) {
			screenResolutionCapabilityMatches = false;
		}
		return screenResolutionCapabilityMatches;
	}

	private boolean isTimeZoneMatching(Map<String, Object> nodeCapability, Map<String, Object> requestedCapability) {
		boolean timeZoneCapabilityMatches;

		String defaultTimeZone = DockerSeleniumStarterRemoteProxy.getConfiguredTimeZone().getID();
		String nodeTimeZone = nodeCapability.get(ZaleniumCapabilityType.TIME_ZONE).toString();

		/*
		    If a time zone is not requested in the capabilities,
		    and this node has a different time zone from the default/configured one...
		    this will prevent that a request without a time zone uses a node created with a specific time zone
		 */
		if (requestedCapability.containsKey(ZaleniumCapabilityType.TIME_ZONE)) {
			timeZoneCapabilityMatches = nodeCapability.containsKey(ZaleniumCapabilityType.TIME_ZONE)
					&& requestedCapability.get(ZaleniumCapabilityType.TIME_ZONE)
							.equals(nodeCapability.get(ZaleniumCapabilityType.TIME_ZONE));
		} else {
			timeZoneCapabilityMatches = defaultTimeZone.equalsIgnoreCase(nodeTimeZone);
		}

		return timeZoneCapabilityMatches;
	}

}
