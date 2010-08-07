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

import Quackbot.BaseCommand;
import Quackbot.Command;
import Quackbot.CommandManager;
import Quackbot.Controller;
import Quackbot.err.InvalidCMDException;
import Quackbot.plugins.java.AdminOnly;
import Quackbot.plugins.java.HelpDoc;
import Quackbot.plugins.java.Optional;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author LordQuackstar
 */
@HelpDoc("Provides list of Admin-only commands or help for specific command. Syntax: ?helpAdmin <OPTIONAL:command>")
@AdminOnly
public class AdminHelp extends Command {
	private static Logger log = LoggerFactory.getLogger(AdminHelp.class);
	Controller ctrl = Controller.instance;

	public String onCommand(@Optional String command) throws Exception {
		//Does user want command list
		if (command == null) {
			List<String> cmdList = new ArrayList<String>();

			//Add Java Plugins
			for (BaseCommand curCmd : CommandManager.getCommands())
				if (curCmd.isEnabled() && curCmd.isAdmin())
					cmdList.add(curCmd.getName());

			//Send to user
			return "Possible commands: " + StringUtils.join(cmdList.toArray(), ", ");
		}
		BaseCommand result = CommandManager.getCommand(command);
		if (result == null)
			throw new InvalidCMDException(command);
		else if (!result.isEnabled())
			throw new InvalidCMDException(command, "(disabled)");
		else if (StringUtils.isBlank(result.getHelp()))
			return "No help avalible";
		return result.getHelp();
	}
}
