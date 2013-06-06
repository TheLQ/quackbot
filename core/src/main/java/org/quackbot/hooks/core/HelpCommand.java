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
package org.quackbot.hooks.core;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import org.apache.commons.lang3.StringUtils;
import org.quackbot.AdminLevels;
import org.quackbot.hooks.Command;
import org.quackbot.err.InvalidCMDException;
import org.quackbot.hooks.events.CommandEvent;
import org.quackbot.hooks.CommandManager;
import org.quackbot.hooks.java.JavaArgument;
import org.quackbot.hooks.java.JavaCommand;

/**
 * Core plugin that provides help for a command
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public class HelpCommand {
	protected static final Joiner JOINER_COMMA = Joiner.on(", ");

	@JavaCommand(name = "help",
			minimumLevel = AdminLevels.ADMIN,
			help = "Provides list of commands or help for specific command",
			arguments =
			@JavaArgument(name = "command", argumentHelp = "", required = false))
	public String onHelpCommand(CommandEvent event, String command) throws Exception {
		//Does user want command list
		CommandManager commandManager = event.getBot().getController().getCommandManager();
		if (command == null) {
			//Add Java Plugins
			StringBuilder commandsResponse = new StringBuilder();
			for (String curLevel : commandManager.getUserAdminLevels(event.getUser())) {
				ImmutableSortedSet<Command> commandsForLevel = commandManager.getCommandsForAdminLevel(curLevel);
				if (commandsForLevel.isEmpty())
					continue;
				commandsResponse.append(" ").append(curLevel).append(": ");
				JOINER_COMMA.appendTo(commandsResponse, Iterables.transform(commandsForLevel, new Function<Command, String>() {
					public String apply(Command curCommand) {
						return curCommand.getName();
					}
				}));
			}
			//Send to user
			return "Possible commands: " + commandsResponse.toString().trim();
		} else {
			//Command specified, get specific help
			Command requestedCommand = commandManager.getCommand(command);
			if (requestedCommand == null)
				throw new InvalidCMDException(command);
			//else if (!result.isEnabled()) //TODO: Support disabled commands
			//	throw new InvalidCMDException(command, "disabled");
			else if (!commandManager.getUserAdminLevels(event.getUser()).contains(requestedCommand.getMinimumAdminLevel()))
				throw new InvalidCMDException(command, "admin only");
			else if (StringUtils.isBlank(requestedCommand.getHelp()))
				return "No help avalible";
			return requestedCommand.getHelp();
		}
	}
}
