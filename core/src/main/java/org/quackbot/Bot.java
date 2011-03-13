/**
 * Copyright (C) 2010 Leon Blakey <lord.quackstar at gmail.com>
 *
 * This file is part of PircBotX.
 *
 * PircBotX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PircBotX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PircBotX.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.quackbot;

import org.quackbot.hooks.core.CoreQuackbotHook;
import java.util.Set;
import java.util.logging.Level;
import org.pircbotx.hooks.Listener;
import org.quackbot.hook.HookManager;
import org.quackbot.hook.Hook;
import org.quackbot.data.ServerStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.pircbotx.Channel;
import org.slf4j.Logger;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.managers.ListenerManager;
import org.quackbot.hooks.JavaHookLoader;
import org.quackbot.hooks.core.AdminHelp;
import org.quackbot.hooks.core.Help;
import org.slf4j.LoggerFactory;

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
@EqualsAndHashCode(callSuper=true)
public class Bot extends PircBotX implements Comparable<Bot> {
	/**
	 * Says weather bot is globally locked or not
	 */
	protected boolean botLocked = false;
	/**
	 * Current Server database object
	 */
	protected final ServerStore serverStore;
	/**
	 * Local threadpool
	 */
	protected final ExecutorService threadPool;
	/**
	 * Stores variable local to this thread group
	 */
	protected final static ThreadGroupLocal<Bot> poolLocal = new ThreadGroupLocal<Bot>(null);
	@Getter(AccessLevel.NONE) @Setter(AccessLevel.NONE)
	private static final Logger log = LoggerFactory.getLogger(Bot.class);
	protected final UUID uniqueId = UUID.randomUUID();
	protected final Controller controller;
	protected final Set<User> lockedUsers = new HashSet();
	protected final Set<Channel> lockedChannels = new HashSet();

	static {
		try {
			//Add our default hooks
			HookManager.addHook(new CoreQuackbotHook());
			HookManager.addHook(JavaHookLoader.load(new Help()));
			HookManager.addHook(JavaHookLoader.load(new AdminHelp()));
		} catch (Exception ex) {
			log.error("Exception encountered when loading default plugins", ex);
		}
	}
	
	/**
	 * Init bot by setting all information
	 * @param serverDB   The persistent server object from database
	 */
	public Bot(Controller controller, final ServerStore serverStore, ExecutorService threadPool) {
		this.serverStore = serverStore;
		this.threadPool = threadPool;
		poolLocal.set(this);
		this.controller = controller;

		setName(controller.getConfig().getName());
		setAutoNickChange(true);
		setFinger(controller.getConfig().getFinger());
		setVersion(controller.getConfig().getVersion());
		setMessageDelay(controller.getConfig().getOutputThrottleMs());


		//Some debug
		StringBuilder serverDebug = new StringBuilder("Attempting to connect to " + this.serverStore.getAddress() + " on port " + this.serverStore.getPort());
		if (this.serverStore.getPassword() != null)
			serverDebug.append(this.serverStore.getPassword());
		log.info(serverDebug.toString());
		try {
			//Connect to server and join all channels (fetched from db)
			if (this.serverStore.getPassword() != null)
				connect(this.serverStore.getAddress(), this.serverStore.getPort(), this.serverStore.getPassword());
			else
				connect(this.serverStore.getAddress(), this.serverStore.getPort());
		} catch (Exception e) {
			log.error("Error in connecting", e);
		}
	}

	public static Bot getPoolLocal() {
		Bot bot = poolLocal.get();
		return bot;
	}

	/**
	 * Checks if the bot is locked on the server
	 * @return True if the bot is locked, false if not
	 */
	public boolean isLocked() {
		return botLocked;
	}

	public boolean isLocked(Channel chan, User user) {
		//If the user is an admin, let them through
		if(controller.isAdmin(this, user, chan))
			return false;
		
		//Is bot locked?
		if (isLocked()) {
			log.info("Command ignored due to server lock in effect");
			return true;
		}

		//Is channel locked?
		if (chan != null && lockedChannels.contains(chan)) {
			log.info("Command ignored due to channel lock in effect");
			return true;
		}
		return false;
	}

	/**
	 * PircBot commands use simply redirects to Log4j. SHOULD NOT BE USED OUTSIDE OF PIRCBOT
	 * <p>
	 * Each line in the log begins with a number which
	 * represents the logging time (as the number of milliseconds since the
	 * epoch).  This timestamp and the following log entry are separated by
	 * a single space character, " ".  Outgoing messages are distinguishable
	 * by a log entry that has ">>>" immediately following the space character
	 * after the timestamp.  DCC events use "+++" and warnings about unhandled
	 * Exceptions and Errors use "###".
	 *  <p>
	 * This implementation of the method will only cause log entries to be
	 * output if the PircBot has had its verbose mode turned on by calling
	 * setVerbose(true);
	 *
	 * @param line The line to add to the log.
	 */
	@Override
	public void log(String line) {
		if (!line.startsWith(">>>") && !line.startsWith("###") && !line.startsWith("+++"))
			line = "@@@" + line;
		log.info(line);
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
		ArrayList<String> list = new ArrayList<String>(controller.getConfig().getPrefixes());
		list.add(getNick() + ":");
		list.add(getNick());
		return list;
	}

	@Override
	public int compareTo(Bot bot) {
		return uniqueId.compareTo(bot.getUniqueId());
	}

	/**
	 * Static class that holds variable local to the entire thread group.
	 * Used mainly for logging, but avalible for any other purpose.
	 * <p>
	 * Thanks to the jkad open source project for providing most of the code.
	 * Source: http://code.google.com/p/jkad/source/browse/trunk/JKad/src/jkad/controller/ThreadGroupLocal.java
	 * @param <T>
	 */
	public static class ThreadGroupLocal<T> {
		/**
		 * Map storing all variables with ThreadGroup
		 */
		private final HashMap<ThreadGroup, T> map = new HashMap<ThreadGroup, T>();
		private T initValue;

		public ThreadGroupLocal(T initValue) {
			this.initValue = initValue;
		}

		/**
		 * Get object for current ThreadGroup
		 * @return Requested Object
		 */
		public T get() {
			T result = null;
			ThreadGroup group = Thread.currentThread().getThreadGroup();
			synchronized (map) {
				result = map.get(group);
				if (result == null) {
					result = initValue;
					map.put(group, result);
				}
			}
			return result;
		}

		/**
		 * Sets object for current ThreadGroup
		 * @param obj Object to store
		 */
		public void set(T obj) {
			map.put(Thread.currentThread().getThreadGroup(), obj);
		}
	}

	public class WrapperListenerManager implements ListenerManager<Bot> {
		protected HashMap<Listener, Hook> listenerTracker = new HashMap();

		public void dispatchEvent(Event<Bot> event) {
			HookManager.dispatchEvent(event);
		}

		public boolean addListener(Listener listener) {
			Hook genHook = new Hook(listener) {
			};
			listenerTracker.put(listener, genHook);
			return HookManager.addHook(genHook);
		}

		public boolean removeListener(Listener listener) {
			if (listenerTracker.containsKey(listener))
				if (HookManager.removeHook(listenerTracker.get(listener)))
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
