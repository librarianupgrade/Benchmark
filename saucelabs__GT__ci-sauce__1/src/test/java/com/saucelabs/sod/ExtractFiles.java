package com.saucelabs.sod;

import com.saucelabs.ci.sauceconnect.SauceConnectFourManager;
import org.junit.Test;

import java.io.File;

/**
 * @author Ross Rowe
 */
public class ExtractFiles {

	private SauceConnectFourManager manager = new SauceConnectFourManager();

	@Test
	public void linux() throws Exception {

		File workingDirectory = new File(System.getProperty("java.io.tmpdir"));
		manager.extractZipFile(workingDirectory, SauceConnectFourManager.OperatingSystem.LINUX);
	}

	@Test
	public void windows() throws Exception {
		File workingDirectory = new File(System.getProperty("java.io.tmpdir"));
		manager.extractZipFile(workingDirectory, SauceConnectFourManager.OperatingSystem.WINDOWS);
	}
}
