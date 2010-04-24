/**
 * @(#)StdRedirect.java
 *
 * This file is part of Quackbot
 */
package Quackbot.log;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.log4j.Logger;

/**
 * Redirects standard output to log
 *
 * @author admins
 */
public class StdRedirect extends FilterOutputStream {
	private Logger log = Logger.getLogger(getClass());
	boolean error;

	public StdRedirect(OutputStream aStream,boolean error) {
		super(aStream);
		this.error = error;
	}

	public void write(byte b[], int off, int len) throws IOException {
		String stringRep = new String(b, off, len).trim();
		if(error)
			log.error(stringRep);
		else
			log.info(stringRep);
	}
}
