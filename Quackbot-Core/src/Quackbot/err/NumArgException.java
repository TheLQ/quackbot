/**
 * @(#)NumArgException.java
 *
 * This file is part of Quackbot
 */
package Quackbot.err;

/**
 * Wrong number of parameters/arguments given.
 *
 * @author Lord.Quackstar
 */
public class NumArgException extends Exception {
	/**
	 * Generates message "Wrong number of parameters specified. Given: *given*, Required: *required*
	 * @param given  Number of given args
	 * @param req    Number of required args
	 */
	public NumArgException(int given, int req) {
		super("Wrong number of parameters specified. Given: " + given + ", Required: " + req);
	}

	/**
	 * Generates message "Wrong number of parameters specified. Given: *given*, Required: *required*, Optional: *optional*
	 * @param given    Number of given args
	 * @param req      Number of required args
	 * @param optional Number of optional args
	 */
	public NumArgException(int given, int req, int optional) {
		super("Wrong number of parameters specified. Given: " + given + ", Required: " + req + " Optional: " + optional);
	}
}
