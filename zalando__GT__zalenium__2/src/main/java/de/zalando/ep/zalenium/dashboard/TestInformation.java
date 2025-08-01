package de.zalando.ep.zalenium.dashboard;

import de.zalando.ep.zalenium.util.CommonProxyUtilities;

import java.util.List;
import java.util.Optional;

/**
 * The purpose of this class is to gather the test information that can be used to render the dashboard.
 */
@SuppressWarnings("WeakerAccess")
public class TestInformation {
	private static final String TEST_FILE_NAME_TEMPLATE = "{buildName}{proxyName}_{testName}_{browser}_{platform}_{timestamp}";
	private static final String FILE_NAME_TEMPLATE = "{fileName}{fileExtension}";
	private static final String ZALENIUM_PROXY_NAME = "Zalenium";
	private static final String SAUCE_LABS_PROXY_NAME = "SauceLabs";
	private static final String BROWSER_STACK_PROXY_NAME = "BrowserStack";
	private static final CommonProxyUtilities commonProxyUtilities = new CommonProxyUtilities();
	private String seleniumSessionId;
	private String testName;
	private String proxyName;
	private String browser;
	private String browserVersion;
	private String platform;
	private String platformVersion;
	private String fileName;
	private String fileExtension;
	private String videoUrl;
	private List<String> logUrls;
	private String videoFolderPath;
	private String logsFolderPath;
	private String testNameNoExtension;
	private String screenDimension;
	private String timeZone;
	private String build;
	private TestStatus testStatus;
	private boolean videoRecorded;

	public boolean isVideoRecorded() {
		return videoRecorded;
	}

	public void setVideoRecorded(boolean videoRecorded) {
		this.videoRecorded = videoRecorded;
	}

	public String getVideoFolderPath() {
		return videoFolderPath;
	}

	public String getTestName() {
		return Optional.ofNullable(testName).orElse(seleniumSessionId);
	}

	public String getProxyName() {
		return proxyName;
	}

	public String getFileName() {
		return fileName;
	}

	public List<String> getLogUrls() {
		return logUrls;
	}

	public String getVideoUrl() {
		return videoUrl;
	}

	public String getLogsFolderPath() {
		return logsFolderPath;
	}

	public String getScreenDimension() {
		return screenDimension;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public String getBuild() {
		return build;
	}

	public TestStatus getTestStatus() {
		return testStatus;
	}

	public void setTestStatus(TestStatus testStatus) {
		this.testStatus = testStatus;
	}

	public String getSeleniumLogFileName() {
		String seleniumLogFileName = Dashboard.LOGS_FOLDER_NAME + "/" + testNameNoExtension + "/";
		if (ZALENIUM_PROXY_NAME.equalsIgnoreCase(proxyName)) {
			return seleniumLogFileName.concat("selenium-multinode-stderr.log");
		}
		if (SAUCE_LABS_PROXY_NAME.equalsIgnoreCase(proxyName)) {
			return seleniumLogFileName.concat("selenium-server.log");
		}
		return seleniumLogFileName.concat("not_implemented.log");
	}

	public String getBrowserDriverLogFileName() {
		String browserDriverLogFileName = Dashboard.LOGS_FOLDER_NAME + "/" + testNameNoExtension + "/";
		if (ZALENIUM_PROXY_NAME.equalsIgnoreCase(proxyName)) {
			return browserDriverLogFileName.concat(String.format("%s_driver.log", browser.toLowerCase()));
		}
		if (SAUCE_LABS_PROXY_NAME.equalsIgnoreCase(proxyName)) {
			return browserDriverLogFileName.concat("log.json");
		}
		return browserDriverLogFileName.concat("not_implemented.log");
	}

	@SuppressWarnings("SameParameterValue")
	public void setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
		buildVideoFileName();
	}

	public void buildVideoFileName() {
		String buildName;
		if ("N/A".equalsIgnoreCase(this.build) || this.build.trim().isEmpty()) {
			buildName = "";
		} else {
			buildName = this.build.replace(" ", "_").replace("/", "_") + "/";
		}

		this.testNameNoExtension = TEST_FILE_NAME_TEMPLATE.replace("{proxyName}", this.proxyName.toLowerCase())
				.replace("{testName}", getTestName()).replace("{browser}", this.browser)
				.replace("{platform}", this.platform)
				.replace("{timestamp}", commonProxyUtilities.getCurrentDateAndTimeFormatted()).replace(" ", "_")
				.replace("/", "_");
		this.testNameNoExtension = this.testNameNoExtension.replace("{buildName}", buildName);

		this.fileName = FILE_NAME_TEMPLATE.replace("{fileName}", testNameNoExtension).replace("{fileExtension}",
				fileExtension);

		this.videoFolderPath = commonProxyUtilities.currentLocalPath() + "/" + Dashboard.VIDEOS_FOLDER_NAME;
		this.logsFolderPath = commonProxyUtilities.currentLocalPath() + "/" + Dashboard.VIDEOS_FOLDER_NAME + "/"
				+ Dashboard.LOGS_FOLDER_NAME + "/" + testNameNoExtension;
	}

	public String getBrowserAndPlatform() {
		if (BROWSER_STACK_PROXY_NAME.equalsIgnoreCase(proxyName)) {
			return String.format("%s %s, %s %s", browser, browserVersion, platform, platformVersion);
		}
		return String.format("%s %s, %s", browser, browserVersion, platform);
	}

	public enum TestStatus {
		COMPLETED("Completed", "primary", " 'Zalenium', 'TEST COMPLETED', --icon=/home/seluser/images/completed.png"),
		TIMEOUT("Timeout", "warning", " 'Zalenium', 'TEST TIMED OUT', --icon=/home/seluser/images/timeout.png"),
		SUCCESS("Success", "success", " 'Zalenium', 'TEST PASSED', --icon=/home/seluser/images/success.png"),
		FAILED("Failed", "danger", " 'Zalenium', 'TEST FAILED', --icon=/home/seluser/images/failure.png");

		private String testStatus;
		private String testBadge;
		private String testNotificationMessage;

		TestStatus(String testStatus, String testBadge, String testNotificationMessage) {
			this.testStatus = testStatus;
			this.testBadge = testBadge;
			this.testNotificationMessage = testNotificationMessage;
		}

		public String getTestStatus() {
			return testStatus;
		}

		public String getTestBadge() {
			return testBadge;
		}

		public String getTestNotificationMessage() {
			return testNotificationMessage;
		}
	}

	private TestInformation(TestInformationBuilder builder) {
		this.seleniumSessionId = builder.seleniumSessionId;
		this.testName = builder.testName;
		this.proxyName = builder.proxyName;
		this.browser = builder.browser;
		this.browserVersion = builder.browserVersion;
		this.platform = builder.platform;
		this.platformVersion = builder.platformVersion;
		this.videoUrl = builder.videoUrl;
		this.fileExtension = Optional.ofNullable(builder.fileExtension).orElse("");
		this.logUrls = builder.logUrls;
		this.screenDimension = Optional.ofNullable(builder.screenDimension).orElse("");
		this.timeZone = Optional.ofNullable(builder.timeZone).orElse("");
		this.build = Optional.ofNullable(builder.build).orElse("");
		this.testStatus = builder.testStatus;
		this.videoRecorded = true;
		buildVideoFileName();
	}

	public static class TestInformationBuilder {
		private String seleniumSessionId;
		private String testName;
		private String proxyName;
		private String browser;
		private String browserVersion;
		private String platform;
		private String platformVersion;
		private String fileExtension;
		private String videoUrl;
		private List<String> logUrls;
		private String screenDimension;
		private String timeZone;
		private String build;
		private TestStatus testStatus;

		public TestInformationBuilder withSeleniumSessionId(String seleniumSessionId) {
			this.seleniumSessionId = seleniumSessionId;
			return this;
		}

		public TestInformationBuilder withTestName(String testName) {
			this.testName = testName;
			return this;
		}

		public TestInformationBuilder withProxyName(String proxyName) {
			this.proxyName = proxyName;
			return this;
		}

		public TestInformationBuilder withBrowser(String browser) {
			this.browser = browser;
			return this;
		}

		public TestInformationBuilder withBrowserVersion(String browserVersion) {
			this.browserVersion = browserVersion;
			return this;
		}

		public TestInformationBuilder withPlatform(String platform) {
			this.platform = platform;
			return this;
		}

		public TestInformationBuilder withPlatformVersion(String platformVersion) {
			this.platformVersion = platformVersion;
			return this;
		}

		public TestInformationBuilder withFileExtension(String fileExtension) {
			this.fileExtension = fileExtension;
			return this;
		}

		public TestInformationBuilder withVideoUrl(String videoUrl) {
			this.videoUrl = videoUrl;
			return this;
		}

		public TestInformationBuilder withLogUrls(List<String> logUrls) {
			this.logUrls = logUrls;
			return this;
		}

		public TestInformationBuilder withScreenDimension(String screenDimension) {
			this.screenDimension = screenDimension;
			return this;
		}

		public TestInformationBuilder withTimeZone(String timeZone) {
			this.timeZone = timeZone;
			return this;
		}

		public TestInformationBuilder withBuild(String build) {
			this.build = build;
			return this;
		}

		public TestInformationBuilder withTestStatus(TestStatus testStatus) {
			this.testStatus = testStatus;
			return this;
		}

		public TestInformation build() {
			return new TestInformation(this);
		}

	}
}
