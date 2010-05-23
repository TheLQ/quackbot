/**
 * @(#)QuackbotException.java
 *
 * This file is part of Quackbot
 */
package Quackbot.err;

/**
 * Generic Quackbot exception, used when another exception does not exist. Seperate exception
 * purly for semantics
 *
 * @author admins
 */
public class QuackbotException extends Exception {
	/**
	 * Generates exception with specified message
	 * @param msg the detail message.
	 */
	public QuackbotException(String msg) {
		super(msg);
	}
}
