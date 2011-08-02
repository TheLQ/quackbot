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
package org.quackbot.hooks;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import org.quackbot.err.QuackbotException;
import org.quackbot.events.CommandEvent;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Setter(AccessLevel.NONE)
public abstract class Command extends Hook {
	private String help = "";
	private boolean admin = false;
	@Setter
	private boolean enabled = true;
	private int requiredParams = 0;
	private int optionalParams = 0;
	private boolean setup;

	public Command() {
		super();
	}
	
	/**
	 * Create a Command with the given name. File is null
	 * @param name The name to use for this Command
	 */
	public Command(String name) {
		super(name);
	}

	/**
	 * Create a Command with the given file and name
	 * @param fileLocation The file that this Command came from
	 * @param name The name to use for this Command
	 */
	public Command(String fileLocation, String name) {
		super(fileLocation, name);
	}
	
	public void setup(String help, boolean admin, boolean enabled, int requiredParams, int optionalParams) throws QuackbotException {
		if(setup)
			throw new QuackbotException("This hook has already been setup");
		this.help = help;
		this.admin = admin;
		this.enabled = enabled;
		this.requiredParams = requiredParams;
		this.optionalParams = optionalParams;
	}

	/**
	 * Called when invoked in a channel or private message
	 * @param chan The channel the command may of taken place in. <b>Null means
	 *             this command was invoked in a PM</b>
	 * @param user The user that invoked this command
	 * @param args Any arguments the user gave, presented as an array for ease
	 *             of use
	 * @return A response to the user's command. Can be null
	 * @throws Exception Any exception you encountered <i>needs</i> to be thrown
	 *                   and not handled internally
	 */
	public String onCommand(CommandEvent event) throws Exception {
		return null;
	}
}
