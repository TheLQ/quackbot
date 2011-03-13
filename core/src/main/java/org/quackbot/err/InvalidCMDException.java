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
 * Exception that command does not exist with message "Command *command* does not exist"
 * 
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public class InvalidCMDException extends Exception {
	/**
	 * Generates exception with message "Command *command* does not exist"
	 * @param cmd   Name of non-existant command
	 */
	public InvalidCMDException(String cmd) {
		super("Command " + cmd + " does not exist");
	}

	public InvalidCMDException(String cmd, String reason) {
		super("Command " + cmd + " does not exist (" + reason + ")");
	}
}
