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

import org.quackbot.hooks.java.AdminOnly;
import org.quackbot.hooks.java.HelpDoc;
import org.quackbot.hooks.java.Optional;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.quackbot.hooks.Command;
import org.quackbot.err.InvalidCMDException;
import org.quackbot.events.CommandEvent;

/**
 *
 * @author LordQuackstar
 */
@HelpDoc("Provides list of Admin-only commands or help for specific command. Syntax: ?helpAdmin <OPTIONAL:command>")
@AdminOnly
public class AdminHelp extends Command {
	public String onCommand(CommandEvent event, @Optional String command) throws Exception {
		//Does user want command list
		if (command == null) {
			List<String> cmdList = new ArrayList<String>();

			//Add Java Plugins
			for (Command curCmd : getController().getHookManager().getCommands())
				if (curCmd.isEnabled() && curCmd.isAdmin())
					cmdList.add(curCmd.getName());

			//Send to user
			return "Possible commands: " + StringUtils.join(cmdList.toArray(), ", ");
		}
		
		//Command specified, get specific help for it
		Command result = getController().getHookManager().getCommand(command);
		if (result == null)
			throw new InvalidCMDException(command);
		else if (!result.isEnabled())
			throw new InvalidCMDException(command, "(disabled)");
		else if (StringUtils.isBlank(result.getHelp()))
			return "No help avalible";
		return result.getHelp();
	}
}
