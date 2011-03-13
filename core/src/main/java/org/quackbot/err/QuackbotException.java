/**
 * Copyright (C) 2010 Leon Blakey <lord.quackstar at gmail.com>
 *
 * This file is part of PircBotX.
 *
 * PircBotX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PircBotX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PircBotX.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.quackbot.err;

/**
 * Generic Quackbot exception, used when another exception does not exist. Seperate exception
 * purly for semantics
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
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
