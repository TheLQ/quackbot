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
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public class InvalidHookException extends RuntimeException {

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
