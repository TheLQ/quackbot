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

package org.quackbot.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.Event;

/**
 * Event that represents a command being sent.
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CommandEvent extends Event {
	protected final Channel channel;
	protected final User user;
	protected final String command;
	protected final String[] args;
	protected final String originalLine;
	protected final Event parentEvent;

	public CommandEvent(Event parentEvent, Channel channel, User user, String origionalLine, String command, String[] args) {
		super(parentEvent.getBot());
		this.channel = channel;
		this.user = user;
		this.originalLine = origionalLine;
		this.command = command;
		this.args = args;
		this.parentEvent = parentEvent;
	}

	@Override
	public void respond(String response) {
		parentEvent.respond(response);
	}
}
