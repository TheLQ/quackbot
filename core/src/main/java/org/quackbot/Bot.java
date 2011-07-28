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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.net.Socket;
import java.util.Set;
import org.pircbotx.hooks.Listener;
import org.quackbot.hooks.Hook;
import org.quackbot.dao.ServerDAO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.pircbotx.Channel;
import org.pircbotx.InputThread;
import org.pircbotx.OutputThread;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.managers.ListenerManager;

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
	/**
	 * Says weather bot is globally locked or not
	 */
	protected boolean botLocked = false;
	protected final int serverId;
	/**
	 * Local threadpool
	 */
	protected final ExecutorService threadPool;
	/**
	 * Stores variable local to this thread group
	 */
	protected final Controller controller;
	protected final Set<User> ignoredUsers = new HashSet();
	protected final Set<Channel> ignoredChannels = new HashSet();

	/**
	 * Init bot by setting all information
	 * @param serverDB   The persistent server object from database
	 */
	public Bot(Controller controller, int serverId, ExecutorService threadPool) {
		this.serverId = serverId;
		this.threadPool = threadPool;
		this.controller = controller;

		setName(controller.getDefaultName());
		setLogin(controller.getDefaultLogin());
		setAutoNickChange(true);
		setFinger(controller.getFinger());
		setVersion(controller.getVersion());
		setMessageDelay(controller.getDefaultMessageDelay());
		setListenerManager(new WrapperListenerManager());
	}

	public void connect() {
		//Some debug
		ServerDAO serverStore = getServerStore();
		log.info("Attempting to connect to " + serverStore.getAddress() + " on port " + serverStore.getPort());
		if (serverStore.getPassword() != null)
			log.info("Using password " + serverStore.getPassword() + " to connect");

		//Connect to server. Channels are handled by onConnect listener in CoreQuackbotHook
		try {
			if (serverStore.getPassword() != null)
				connect(serverStore.getAddress(), serverStore.getPort(), serverStore.getPassword());
			else
				connect(serverStore.getAddress(), serverStore.getPort());
		} catch (Exception e) {
			log.error("Error in connecting", e);
		}
	}

	@Override
	protected InputThread createInputThread(Socket socket, BufferedReader breader) {
		InputThread inputThread = new InputThread(this, socket, breader) {
		};
		inputThread.setName("quackbot-" + getServerStore().getAddress() + "-" + getServerStore().getServerId() + "-input");
		return inputThread;
	}

	@Override
	protected OutputThread createOutputThread(BufferedWriter bwriter) {
		OutputThread outputThread = new OutputThread(this, bwriter) {
		};
		outputThread.setName("quackbot-" + getServerStore().getAddress() + "-" + getServerStore().getServerId() + "-output");
		return outputThread;
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
	 * DO NOT USE THIS! Only for redirecting internal logging by PircBotX to slf4j
	 * @param line The line to add to the log.
	 */
	@Override
	public void log(String line) {
		if (line.startsWith("###"))
			log.error(line);
		else if (line.startsWith("***"))
			log.info(line);
		else
			//This means <<<, >>>, +++, and unknown lines
			log.debug(line);
	}

	@Override
	public synchronized void dispose() {
		threadPool.shutdown();
		super.dispose();
	}

	/**
	 * Send message to ALL joined channels
	 * @param msg   Message to send
	 */
	public void sendAllMessage(String msg) {
		if (msg != null)
			for (String curChan : getChannelsNames())
				sendMessage(curChan, msg);
	}

	public boolean isBot(String name) {
		return getName().equals(name);
	}

	public List<String> getPrefixes() {
		//Merge the global list and the Bot specific list
		ArrayList<String> list = new ArrayList<String>(controller.getPrefixes());
		list.add(getNick() + ":");
		list.add(getNick());
		return list;
	}

	public ServerDAO getServerStore() {
		for (ServerDAO curServer : controller.getStorage().getServers())
			if (curServer.getServerId().equals(serverId))
				return curServer;
		throw new RuntimeException("Can't find server store of current bot ( " + getServer() + " ) with server id " + serverId);
	}

	@Override
	protected void handleLine(String line) {
		controller.getStorage().beginTransaction();
		try {
			super.handleLine(line);
			controller.getStorage().endTransaction(true);
		} catch (Throwable t) {
			controller.getStorage().endTransaction(false);
			//Simply do the logging here, much easier than wrapping to throw up
			logException(t);
		}
	}

	public class WrapperListenerManager implements ListenerManager<Bot> {
		protected HashMap<Listener, Hook> listenerTracker = new HashMap();

		public void dispatchEvent(Event<Bot> event) {
			controller.getHookManager().dispatchEvent(event);
		}

		public boolean addListener(Listener listener) {
			Hook genHook = new Hook(listener) {
			};
			listenerTracker.put(listener, genHook);
			return controller.getHookManager().addHook(genHook);
		}

		public boolean removeListener(Listener listener) {
			if (listenerTracker.containsKey(listener))
				if (controller.getHookManager().removeHook(listenerTracker.get(listener)))
					return listenerTracker.remove(listener) != null;
			return false;
		}

		public boolean listenerExists(Listener listener) {
			return listenerTracker.containsKey(listener);
		}

		public Set<Listener> getListeners() {
			return listenerTracker.keySet();
		}
	}
}