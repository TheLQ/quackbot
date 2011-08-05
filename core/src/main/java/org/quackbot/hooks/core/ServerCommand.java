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

import java.util.ArrayList;
import org.apache.commons.lang.StringUtils;
import org.quackbot.Bot;
import org.quackbot.Controller;
import org.quackbot.events.CommandEvent;
import org.quackbot.hooks.Command;
import org.quackbot.hooks.java.AdminOnly;
import org.quackbot.hooks.java.HelpDoc;
import org.quackbot.hooks.java.Optional;
import org.springframework.stereotype.Component;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
@Component
@AdminOnly
@HelpDoc("The umbrilla command for any action to a server")
public class ServerCommand extends Command {
	public String onCommand(CommandEvent event, String action, String target, @Optional String[] arg2) throws Exception {
		Bot bot = event.getBot();
		Controller controller = bot.getController();

		if (action.equalsIgnoreCase("list")) {
			ArrayList<String> serverNames = new ArrayList(controller.getBots().size());
			for (Bot curBot : controller.getBots())
				serverNames.add(curBot.getServer());
			return "Connected to servers: " + serverNames.toString().substring(0, serverNames.toString().length());
		} else if (action.equalsIgnoreCase("add")) {
			controller.addServer(target);
			return "Attempting to join server: " + target;
		} else if (action.equalsIgnoreCase("quit")) {
			String quitMessage = "Bot killed by user " + event.getUser().getNick();
			if (StringUtils.isBlank(target))
				//Assume quitting this server
				bot.quitServer(quitMessage);
			else {
				//Trying to quit another server
				for (Bot curBot : controller.getBots())
					if (curBot.getServer().equalsIgnoreCase(target)) {
						curBot.quitServer(quitMessage);
						return "Quit server " + target;
					}
				//Getting here means the server wasn't found
				throw new RuntimeException("Can't find server " + target);
			}
		} else if (action.equalsIgnoreCase("lock")) {
			bot.setBotLocked(true);
			return "Bot locked on this server";
		} else if (action.equalsIgnoreCase("unlock")) {
			bot.setBotLocked(false);
			return "Bot unlocked on this server";
		} else if (action.equalsIgnoreCase("lockStatus"))
			return "Bot locked status: " + ((bot.isBotLocked()) ? "Locked" : "Unlocked");
		else
			return "Unknown operation: " + action;

		return null;
	}
}
