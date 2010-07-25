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
package Quackbot.plugins.core;

import Quackbot.Command;
import Quackbot.CommandManager;
import Quackbot.Controller;

import Quackbot.plugins.java.HelpDoc;
import Quackbot.err.InvalidCMDException;
import Quackbot.plugins.java.Parameters;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Core plugin that provides help for a command
 *
 * @author Lord.Quackstar
 */
@Parameters(optional = 1)
@HelpDoc("Provides list of commands or help for specific command. Syntax: ?help <OPTIONAL:command>")
public class Help extends Command {
	private static Logger log = LoggerFactory.getLogger(Help.class);
	Controller ctrl = Controller.instance;

	@Override
	public void onCommand(String channel, String sender, String login, String hostname, String[] args) throws Exception {
		//Does user want command list
		if (args.length == 0) {
			List<String> cmdList = new ArrayList<String>();

			//Add Java Plugins
			for (Command curCmd : CommandManager.getCommands())
				if (curCmd.isEnabled() && !curCmd.isAdmin())
					cmdList.add(curCmd.getName());

			//Send to user
			getBot().sendMessage(channel, "Possible commands: " + StringUtils.join(cmdList.toArray(), ", "));
		} else {
			Command result = CommandManager.getCommand(args[0]);
			if (result == null)
				throw new InvalidCMDException(args[0]);
			else if (!result.isEnabled())
				throw new InvalidCMDException(args[0], "(disabled)");
			else if (!result.isAdmin())
				throw new InvalidCMDException(args[0], "(admin only)");
			else if (StringUtils.isBlank(result.getHelp()))
				getBot().sendMessage(channel, sender, "No help avalible");
			else
				getBot().sendMessage(channel, sender, result.getHelp());
		}
	}
}
