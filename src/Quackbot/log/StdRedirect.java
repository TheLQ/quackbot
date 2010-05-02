/**
 * @(#)StdRedirect.java
 *
 * This file is part of Quackbot
 */
package Quackbot.log;

import Quackbot.InstanceTracker;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.log4j.Logger;

/**
 * Redirects standard output to log
 *
 * @author Lord.Quackstar
 */
public class StdRedirect extends FilterOutputStream {
	private Logger log = Logger.getLogger(getClass());
	boolean error;

	/**
	 * Sets up redirect with Stream to capture and if this is the error stream
	 * @param aStream
	 * @param error
	 */
	public StdRedirect(OutputStream aStream, boolean error) {
		super(aStream);
		this.error = error;
	}

	/**
	 * Writes len bytes from the specified byte array starting at offset off to this output stream.
	 * <p>
	 * The write method of FilterOutputStream calls the write method of one argument on each byte
	 * to output.
	 *
	 * @param b            the data.
	 * @param off          the start offset in the data.
	 * @param len          the number of bytes to write.
	 * @throws IOException if an I/O error occurs. This is never thrown
	 */
	public void write(byte b[], int off, int len) throws IOException {
		String stringRep = new String(b, off, len).trim();
		if (error) {
			log.error(stringRep);
			InstanceTracker.getMain().err.print(stringRep);
		} else
			log.info(stringRep);
	}
}
