/**
 * @(#)AdminException.java
 *
 * This file is part of Quackbot
 */
package Quackbot.err;

/**
 * Notify user that command is admin. Exception message is "Admin Only Command"/
 *
 * @author Lord.Quackstar
 */
public class AdminException extends Exception {
	/**
	 * Generates exception with message "Admin Only Command".
	 */
	public AdminException() {
		super("Admin Only Command");
	}
}
