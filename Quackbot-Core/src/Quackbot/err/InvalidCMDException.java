/**
 * @(#)InvalidCMDException.java
 *
 * This file is part of Quackbot
 */
package Quackbot.err;

/**
 * Exception that command does not exist with message "Command *command* does not exist"
 * 
 * @author Lord.Quackstar
 */
public class InvalidCMDException extends Exception {
	/**
	 * Generates exception with message "Command *command* does not exist"
	 * @param cmd   Name of non-existant command
	 */
	public InvalidCMDException(String cmd) {
		super("Command " + cmd + " does not exist");
	}
}
