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

import org.pircbotx.Channel;
import org.pircbotx.User;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.ModeEvent;
import org.pircbotx.hooks.events.NickChangeEvent;
import org.pircbotx.hooks.events.PartEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.pircbotx.hooks.events.QuitEvent;
import org.quackbot.Bot;
import org.quackbot.dao.model.LogEntry;
import org.quackbot.dao.model.LogEntryType;
import org.quackbot.hooks.Hook;

/**
 * Logs all relevant IRC events in the DAO
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public class QuackbotLogHook extends Hook {
	@Override
	public void onMode(ModeEvent<Bot> event) throws Exception {
		log(event, LogEntryType.CHANNEL_MODE, event.getChannel(), event.getUser(), event.getMode());
	}

	@Override
	public void onMessage(MessageEvent<Bot> event) throws Exception {
		log(event, LogEntryType.MESSAGE, event.getChannel(), event.getUser(), event.getMessage());
	}

	@Override
	public void onPrivateMessage(PrivateMessageEvent<Bot> event) throws Exception {
		log(event, LogEntryType.PRIVATE_MESSAGE, null, event.getUser(), event.getMessage());
	}

	@Override
	public void onJoin(JoinEvent<Bot> event) throws Exception {
		log(event, LogEntryType.JOIN, event.getChannel(), event.getUser(), null);
	}

	@Override
	public void onPart(PartEvent<Bot> event) throws Exception {
		log(event, LogEntryType.PART, event.getChannel(), event.getUser(), null);
	}

	@Override
	public void onQuit(QuitEvent<Bot> event) throws Exception {
		log(event, LogEntryType.QUIT, null, event.getUser(), null);
	}

	@Override
	public void onNickChange(NickChangeEvent<Bot> event) throws Exception {
		log(event, LogEntryType.NICK_CHANGE, null, event.getUser(), event.getOldNick());
	}

	protected void log(Event event, LogEntryType type, Channel chan, User user, String message) {
		LogEntry entry = (LogEntry) getController().getLogDao().create();
		entry.setType(type);
		entry.setServer(event.getBot().getServer());
		entry.setTimestamp(event.getTimestamp());
		if (chan != null)
			entry.setChannel(chan.getName());
		if (user != null)
			entry.setUser(user.getNick());
		entry.setMessage(message);
	}
}
