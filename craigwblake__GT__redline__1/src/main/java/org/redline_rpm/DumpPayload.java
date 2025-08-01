package org.redline_rpm;

import java.io.FileOutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

/**
 * Convenience class for dumping the payload of an
 * RPM file to a file. This is useful in debugging
 * problems in RPM generation.
 */
public final class DumpPayload {
	private DumpPayload() {
	}

	/**
	 * Dumps the contents of the payload for an RPM file to
	 * the provided file.  This method accepts an RPM file from
	 * standard input and dumps it's payload out to the file
	 * name provided as the first argument.
	 * @param args command line arguements
	 * @throws Exception an exception occurred
	 */
	public static void main(String[] args) throws Exception {
		ReadableByteChannel in = Channels.newChannel(System.in);
		new Scanner().run(new ReadableChannelWrapper(in));
		FileOutputStream fout = new FileOutputStream(args[0]);
		FileChannel out = fout.getChannel();

		long position = 0;
		long read;
		while ((read = out.transferFrom(in, position, 1024)) > 0)
			position += read;
		fout.close();
	}
}
