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

import java.io.File;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.pircbotx.Channel;
import org.pircbotx.User;
import org.quackbot.err.QuackbotException;
import org.quackbot.hooks.Hook;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
@ToString
@EqualsAndHashCode(callSuper = true)
public abstract class Command extends Hook {
	@Getter
	private String help = "";
	@Getter
	private boolean admin = false;
	@Getter @Setter
	private boolean enabled = true;
	@Getter
	private int requiredParams = 0;
	@Getter
	private int optionalParams = 0;
	@Getter
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
	 * @param file The file that this Command came from
	 * @param name The name to use for this Command
	 */
	public Command(File file, String name) {
		super(file, name);
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
	public String onCommand(Channel chan, User user, String[] args) throws Exception {
		return null;
	}

	/**
	 * Called when command is invoked in a private message
	 * @param user The user that invoked this command
	 * @param args Any arguments the user gave, presented as an array for ease
	 *             of use
	 * @return A response to the user's command. Can be null
	 * @throws Exception Any exception you encountered <i>needs</i> to be thrown
	 *                   and not handled internally
	 */
	public String onCommandPM(User user, String[] args) throws Exception {
		return null;
	}

	/**
	 * Called when command is invoked in a channel
	 * @param chan The channel the command may of taken place in.
	 * @param user The user that invoked this command
	 * @param args Any arguments the user gave, presented as an array for ease
	 *             of use
	 * @return A response to the user's command. Can be null
	 * @throws Exception Any exception you encountered <i>needs</i> to be thrown
	 *                   and not handled internally
	 */
	public String onCommandChannel(Channel chan, User user, String[] args) throws Exception {
		return null;
	}
}
