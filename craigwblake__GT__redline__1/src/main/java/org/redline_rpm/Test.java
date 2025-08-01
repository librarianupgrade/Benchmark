package org.redline_rpm;

import java.io.File;

import static org.redline_rpm.header.RpmType.BINARY;
import static org.redline_rpm.header.Architecture.NOARCH;
import static org.redline_rpm.header.Os.LINUX;

/**
 * Simple test class to validate that a basic
 * RPM file can be correctly generated. The
 * results of this test class can be read in
 * by the scanner to validate it's format.
 */
public final class Test {
	private Test() {
	}

	/**
	 * Builds a test RPM file using the provided optional arguments.
	 * The first argument is the target path of a file to add to the
	 * RPM, and the second argument is the path to the actual file
	 * content.
	 *
	 * @param args command line arguments
	 * @throws Exception if an error occurrs generating the test
	 * RPM file
	 */
	public static void main(String[] args) throws Exception {

		// Set required fields for the RPM package.
		final Builder builder = new Builder();
		builder.setPackage("test", "0.0.1", "1");
		builder.setType(BINARY);
		builder.setPlatform(NOARCH, LINUX);
		builder.setSummary("Test RPM");
		builder.setDescription("A test RPM with a few packaged files.");
		builder.setBuildHost("localhost");
		builder.setLicense("MIT");
		builder.setGroup("Miscellaneous");
		builder.setDistribution("FreeCompany");
		builder.setVendor("Redline RPM Repository http://redline-rpm.org/rpms");
		builder.setPackager("Jane Doe");
		builder.setUrl("http://redline-rpm.org");
		builder.setProvides("test");

		// Adds one file passed as an argument to the package.
		if (args.length == 2)
			builder.addFile(args[1], new File(args[0]));

		// This generates a RPM file in the current directory named by the package and type settings.
		builder.build(new File("."));
	}
}
