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
package org.quackbot;

import com.google.common.base.Preconditions;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.quackbot.dao.model.ServerEntry;

/**
 * Bot instance that communicates with 1 server
 *  -Initiates all commands
 *
 * Used by: Controller, spawned commands
 *
 * @version 3.0
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class Bot extends PircBotX {
	protected final Controller controller;
	@Getter
	protected final ServerEntry serverEntry;
	/**
	 * Says weather bot is globally locked or not
	 */
	protected boolean botLocked = false;
	protected final Set<User> ignoredUsers = new HashSet<User>();
	protected final Set<Channel> ignoredChannels = new HashSet<Channel>();

	/**
	 * Init bot by setting all information
	 * @param serverDB   The persistent server object from database
	 */
	public Bot(Controller controller, ServerEntry serverEntry) {
		super(serverEntry.getConfiguration());
		this.controller = controller;
		this.serverEntry = serverEntry;
	}

	/**
	 * Checks if the bot is locked on the server
	 * @return True if the bot is locked, false if not
	 */
	public boolean isLocked() {
		return botLocked;
	}

	public boolean isIgnored(Channel chan, User user) {
		//If the user is an admin, let them through
		if (controller.isAdmin(this, user, chan))
			return false;

		//Is bot locked?
		if (isLocked()) {
			log.info("Command ignored due to server lock in effect");
			return true;
		}

		//Is channel Ignored?
		if (chan != null && ignoredChannels.contains(chan)) {
			log.info("Command ignored due to channel lock in effect");
			return true;
		}

		//Is user ignored
		if (user != null && ignoredUsers.contains(user)) {
			log.info("Command ignored due to user lock in effect");
			return true;
		}

		//All tests pass. Bot, channel, and user are not ignored
		return false;
	}

	/**
	 * Send message to ALL joined channels
	 * @param msg   Message to send
	 */
	public void sendAnnounceMessage(String msg) {
		Preconditions.checkNotNull(msg, "Msg must not be null");
		for (Channel channel : getUserChannelDao().getAllChannels())
			channel.send().message(msg);
	}

	public List<String> getPrefixes() {
		//Merge the global list and the Bot specific list
		ArrayList<String> list = new ArrayList<String>(controller.getQconfiguration().getGlobalPrefixes());
		list.add(getNick() + ":");
		list.add(getNick());
		return list;
	}
}