package net.sourceforge.lhadecompressor;

import java.io.*;

/**
 * Signals that a lzh exception of some sort has occurred.
 * 
 * @author Nobuyasu SUEHIRO <nosue@users.sourceforge.net>
 */
public class LhaException extends IOException {
	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = -8799459685839682440L;

	/**
	 * Constructs an LzhException with <code>null</code> as its error detail
	 * message
	 */
	public LhaException() {
		super();
	}

	/**
	 * Constructs an LzhException with the specified detail message.
	 * 
	 * @param s
	 *            the detail message
	 */
	public LhaException(String s) {
		super(s);
	}
}
