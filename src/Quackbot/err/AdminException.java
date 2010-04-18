/**
 * @(#)AdminException.java
 *
 * This file is part of Quackbot
 */
package Quackbot.err;

/**
 * Notify user that command is admin only
 *
 * @author Lord.Quackstar
 */
public class AdminException extends Exception {

	/**
	 * Simple constuctor with default text
	 */
	public AdminException() {
		super("Admin Only Command");
	}
}
