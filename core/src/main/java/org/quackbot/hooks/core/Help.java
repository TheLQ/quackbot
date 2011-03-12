/*
 * Copyright (C) 2009-2010 Leon Blakey
 *
 * Quackedbot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Quackedbot  is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.quackbot.hooks.core;

import org.quackbot.hooks.java.HelpDoc;
import org.quackbot.hooks.java.Optional;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.quackbot.Command;
import org.quackbot.err.InvalidCMDException;
import org.quackbot.hook.HookManager;

/**
 * Core plugin that provides help for a command
 *
 * @author Lord.Quackstar
 */
@HelpDoc("Provides list of commands or help for specific command. Syntax: ?help <OPTIONAL:command>")
public class Help extends Command {
	public String onCommand(@Optional String command) throws Exception {
		//Does user want command list
		if (command == null) {
			List<String> cmdList = new ArrayList<String>();

			//Add Java Plugins
			for (Command curCmd : HookManager.getCommands())
				if (curCmd.isEnabled() && !curCmd.isAdmin())
					cmdList.add(curCmd.getName());

			//Send to user
			return "Possible commands: " + StringUtils.join(cmdList.toArray(), ", ");
		}
	
		//Command specified, get specific help
		Command result = HookManager.getCommand(command);
		if (result == null)
			throw new InvalidCMDException(command);
		else if (!result.isEnabled())
			throw new InvalidCMDException(command, "disabled");
		else if (result.isAdmin())
			throw new InvalidCMDException(command, "admin only");
		else if (StringUtils.isBlank(result.getHelp()))
			return "No help avalible";
		return result.getHelp();
	}
}
