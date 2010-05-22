/**
 * @(#)InvalidCMDException.java
 *
 * This file is part of Quackbot
 */
package Quackbot.err;

/**
 * Command does not exist
 * 
 * @author Lord.Quackstar
 */
public class InvalidCMDException extends Exception {
	/**
	 * Simple constructor with default text
	 * @param cmd   Command that does not exist
	 */
	public InvalidCMDException(String cmd) {
		super("Command " + cmd + " does not exist");
	}
}
