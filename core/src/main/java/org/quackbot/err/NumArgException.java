/**
 * Copyright (C) 2011 Leon Blakey <lord.quackstar at gmail.com>
 *
 * This file is part of Quackbot.
 *
 * Quackbot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Quackbot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Quackbot.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.quackbot.err;

/**
 * Wrong number of parameters/arguments given.
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
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
