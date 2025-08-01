package com.algocrafts.browsers;

import com.algocrafts.selenium.CachedWebDriverSupplier;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class ChromeDriverSupplier implements CachedWebDriverSupplier<ChromeDriver> {

	@Override
	public ChromeDriver init() {
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--test-type");
		return new ChromeDriver(options);
	}
}
