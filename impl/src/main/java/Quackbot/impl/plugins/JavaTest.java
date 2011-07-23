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
package Quackbot.impl.plugins;

import org.pircbotx.Channel;
import org.pircbotx.User;
import org.quackbot.events.CommandEvent;
import org.quackbot.hooks.Command;
import org.quackbot.hooks.java.HelpDoc;

/**
 * Simple Java cmd test
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
@HelpDoc("This is JavaTest Help")
public class JavaTest extends Command {
	@Override
	public String onCommand(CommandEvent event) throws Exception {
		StringBuilder users = new StringBuilder();
		for (User curUser : event.getChannel().getUsers())
			users.append(curUser.toString()).append("\n");

		return users.toString();
	}
}
