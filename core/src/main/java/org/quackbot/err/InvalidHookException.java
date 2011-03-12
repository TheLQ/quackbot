package org.quackbot.err;

/**
 *
 * @author Leon Blakey
 */
public class InvalidHookException extends Exception {

	/**
	 * Creates a new instance of <code>InvalidHookException</code> without detail message.
	 */
	public InvalidHookException() {
	}

	/**
	 * Constructs an instance of <code>InvalidHookException</code> with the specified detail message.
	 * @param msg the detail message.
	 */
	public InvalidHookException(String msg) {
		super(msg);
	}
}
