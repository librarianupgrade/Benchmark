package org.redline_rpm;

import org.redline_rpm.header.AbstractHeader.Entry;
import org.redline_rpm.header.Format;
import org.redline_rpm.header.Header;
import org.redline_rpm.header.Header.HeaderTag;
import org.redline_rpm.payload.CpioHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;

import static org.redline_rpm.ChannelWrapper.Key;
import static org.redline_rpm.header.Header.HeaderTag.HEADERIMMUTABLE;
import static org.redline_rpm.header.Signature.SignatureTag.SIGNATURES;

/**
 * The scanner reads an RPM file and outputs useful
 * information about it's contents. The scanner will
 * output the headers of the RPM format itself, as
 * well as the individual headers for the particular
 * packaged content.
 *
 * In addition, the scanner will scan through the
 * payload and output information about each file
 * contained in the embedded CPIO payload.
 */
public class Scanner {
	private static final Logger LOGGER = LoggerFactory.getLogger(Scanner.class);
	private final PrintStream output;

	public Scanner() {
		this(null);
	}

	public Scanner(final PrintStream out) {
		this.output = out;
	}

	/**
	 * Scans a file and prints out useful information.
	 * This utility reads from standard input, and parses
	 * the binary contents of the RPM file.
	 *
	 * @param args command line arguements
	 * @throws Exception if an error occurs while
	 * reading the RPM file or it's contents
	 */
	public static void main(String[] args) throws Exception {
		InputStream fios = new FileInputStream(args[0]);
		ReadableChannelWrapper in = new ReadableChannelWrapper(Channels.newChannel(fios));
		Scanner scanner = new Scanner(System.out);
		Format format = scanner.run(in);
		scanner.log(format.toString());
		Header rpmHeader = format.getHeader();
		scanner.log("Payload compression: " + rpmHeader.getEntry(HeaderTag.PAYLOADCOMPRESSOR));
		InputStream uncompressed = Util.openPayloadStream(rpmHeader, fios);
		in = new ReadableChannelWrapper(Channels.newChannel(uncompressed));
		CpioHeader header;
		int total = 0;
		do {
			header = new CpioHeader();
			total = header.read(in, total);
			scanner.log(header.toString());
			final int skip = header.getFileSize();
			if (uncompressed.skip(skip) != skip)
				throw new RuntimeException("Skip failed.");
			total += header.getFileSize();
		} while (!header.isLast());
	}

	/**
	 * Reads the headers of an RPM and returns a description of it
	 * and it's format.
	 *
	 * @param in the channel wrapper to read input from
	 * @return information describing the RPM file
	 * @throws IOException if an error occurs reading the file
	 */
	public Format run(ReadableChannelWrapper in) throws IOException {
		Format format = new Format();
		Key<Integer> headerStartKey = in.start();

		Key<Integer> lead = in.start();
		format.getLead().read(in);
		log("Lead ended at '" + in.finish(lead) + "'.");

		Key<Integer> signature = in.start();
		int count = format.getSignature().read(in);
		Entry<?> sigEntry = format.getSignature().getEntry(SIGNATURES);
		int expected = sigEntry == null ? 0 : ByteBuffer.wrap((byte[]) sigEntry.getValues(), 8, 4).getInt() / -16;
		log("Signature ended at '" + in.finish(signature) + "' and contained '" + count + "' headers (expected '"
				+ expected + "').");

		Integer headerStartPos = in.finish(headerStartKey);
		format.getHeader().setStartPos(headerStartPos);
		Key<Integer> headerKey = in.start();
		count = format.getHeader().read(in);
		Entry<?> immutableEntry = format.getHeader().getEntry(HEADERIMMUTABLE);
		expected = immutableEntry == null ? 0
				: ByteBuffer.wrap((byte[]) immutableEntry.getValues(), 8, 4).getInt() / -16;
		Integer headerLength = in.finish(headerKey);
		format.getHeader().setEndPos(headerStartPos + headerLength);
		log("Header ended at '" + headerLength + " and contained '" + count + "' headers (expected '" + expected
				+ "').");

		return format;
	}

	private void log(final String text) {
		if (output == null) {
			LOGGER.info(text);
		} else {
			output.println(text);
		}
	}
}
