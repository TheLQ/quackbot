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
@HelpDoc("The umbrilla command for any action on the bot")
public class BotCommand extends Command {
	public String onCommand(CommandEvent event, String action, String target, @Optional String[] arg2) throws Exception {
		Bot bot = event.getBot();
		Controller controller = bot.getController();
		
		if(action.equalsIgnoreCase("reload")) {
			controller.reloadPlugins();
			return "Succesfully reloaded plugins";
		}
		
		throw new RuntimeException("Unknown operation: " + action);
	}
}
