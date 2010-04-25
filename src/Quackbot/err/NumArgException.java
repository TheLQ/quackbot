/**
 * @(#)NumArgException.java
 *
 * This file is part of Quackbot
 */
package Quackbot.err;

/**
 * Wrong number of parameters/arguments given
 *
 * @author Lord.Quackstar
 */
public class NumArgException extends Exception {

	/**
	 * Simple constructor with default text
	 * @param given  Number of given args
	 * @param req    Number of required args
	 */
	public NumArgException(int given, int req) {
		super("Wrong number of parameters specified. Given: " + given + ", Required: " + req);
	}

	/**
	 * Simple constructor with default text
	 * @param given  Number of given args
	 * @param req    Number of required args
	 */
	public NumArgException(int given, int req, int optional) {
		super("Wrong number of parameters specified. Given: " + given + ", Required: " + req + " Optional: " + optional);
	}
}
