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
package org.quackbot;

import java.util.Set;
import org.pircbotx.hooks.Listener;
import org.pircbotx.hooks.events.ConnectEvent;
import org.quackbot.err.AdminException;
import org.quackbot.err.InvalidCMDException;
import org.quackbot.err.NumArgException;
import org.quackbot.info.Channel;
import org.quackbot.hook.HookManager;
import org.quackbot.hook.Hook;
import org.quackbot.info.Server;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.managers.ListenerManager;
import org.slf4j.LoggerFactory;

/**
 * Bot instance that communicates with 1 server
 *  -Initiates all commands
 *
 * Used by: Controller, spawned commands
 *
 * @version 3.0
 * @author Lord.Quackstar
 */
public class Bot extends PircBotX implements Comparable<Bot> {
	/**
	 * Says weather bot is globally locked or not
	 */
	public boolean botLocked = false;
	/**
	 * List of channels bot is locked on. Is NOT persistent!!
	 */
	public TreeSet<String> chanLockList = new TreeSet<String>();
	/**
	 * Current Server database object
	 */
	public Server serverDB;
	/**
	 * Local threadpool
	 */
	public ExecutorService threadPool;
	/**
	 * Stores variable local to this thread group
	 */
	private static ThreadGroupLocal<Bot> poolLocal = new ThreadGroupLocal<Bot>(null);
	/**
	 * Log4J logger
	 */
	private Logger log = LoggerFactory.getLogger(Bot.class);
	public UUID unique;
	public Controller controller;

	/**
	 * Init bot by setting all information
	 * @param serverDB   The persistent server object from database
	 */
	public Bot(Controller controller, final Server serverDB, ExecutorService threadPool) {
		this.serverDB = serverDB;
		this.threadPool = threadPool;
		poolLocal.set(this);
		unique = UUID.randomUUID();
		this.controller = controller;

		setName(controller.config.getName());
		setAutoNickChange(true);
		setFinger(controller.config.getFinger());
		setVersion(controller.config.getVersion());
		setMessageDelay(controller.config.getMsgWait());


		//Some debug
		StringBuilder serverDebug = new StringBuilder("Attempting to connect to " + serverDB.getAddress() + " on port " + serverDB.getPort());
		if (serverDB.getPassword() != null)
			serverDebug.append(serverDB.getPassword());
		log.info(serverDebug.toString());
		try {
			//Connect to server and join all channels (fetched from db)
			if (serverDB.getPassword() != null)
				connect(serverDB.getAddress(), serverDB.getPort(), serverDB.getPassword());
			else
				connect(serverDB.getAddress(), serverDB.getPort());
		} catch (Exception e) {
			log.error("Error in connecting", e);
		}
	}

	/**
	 * This adds the default hooks for command management
	 */
	static {
		//Default onPrivateMessage handling
		HookManager.addHook(new Hook("QuackbotCore") {
			private Logger log = LoggerFactory.getLogger(Bot.class);

			@Override
			public void onConnect(ConnectEvent event) {
				List<Channel> channels = getBot().serverDB.getChannels();
				for (Channel curChannel : channels) {
					getBot().joinChannel(curChannel.getName(), curChannel.getPassword());
					log.debug("Trying to join channel using " + curChannel);
				}
			}

			@Override
			public void onMessage(String channel, String sender, String login, String hostname, String message) {
				int cmdNum = getController().addCmdNum();

				String command = "";

				if (getBot().isLocked(channel, sender, true)) {
					log.warn("Bot locked");
					return;
				}

				//Look for a prefix
				for (String curPrefix : getBot().getPrefixes())
					if (curPrefix.length() < message.length() && message.substring(0, curPrefix.length()).equalsIgnoreCase(curPrefix))
						try {
							log.info("-----------Begin execution of command #" + cmdNum + ",  from channel " + channel + " using message " + message + "-----------");
							message = message.substring(curPrefix.length(), message.length()).trim();
							command = message.split(" ", 2)[0];
							BaseCommand cmd = setupCommand(command, channel, sender, login, hostname, message);
							getBot().sendMessage(channel, sender, cmd.onCommandGiven(channel, sender, login, hostname, getArgs(message)));
							getBot().sendMessage(channel, sender, cmd.onCommandChannel(channel, sender, login, hostname, getArgs(message)));
							getBot().sendMessage(channel, sender, executeOnCommand(cmd, getArgs(message)));
							break;
						} catch (Exception e) {
							log.error("Error encountered when running command " + command, e);
							getBot().sendMessage(channel, sender, "ERROR: " + e.getMessage());
						} finally {
							log.info("-----------End execution of command #" + cmdNum + ",  from channel " + channel + " using message " + message + "-----------");
						}
			}

			@Override
			public void onPrivateMessage(String sender, String login, String hostname, String message) {
				int cmdNum = getController().addCmdNum();
				log.debug("-----------Begin execution of command #" + cmdNum + ",  from a PM from " + sender + " using message " + message + "-----------");
				String command = "";

				try {
					if (getBot().isLocked(null, sender, true)) {
						log.warn("Bot locked");
						return;
					}

					//Look for a prefix
					command = message.split(" ", 2)[0];
					BaseCommand cmd = setupCommand(command, null, sender, login, hostname, message);
					getBot().sendMessage(sender, cmd.onCommandGiven(sender, sender, login, hostname, getArgs(message)));
					getBot().sendMessage(sender, cmd.onCommandPM(sender, login, hostname, getArgs(message)));
					getBot().sendMessage(sender, executeOnCommand(cmd, getArgs(message)));
				} catch (Exception e) {
					log.error("Error encountered when running command " + command, e);
					getBot().sendMessage(sender, "ERROR: " + e.getMessage());
				} finally {
					log.debug("-----------End execution of command #" + cmdNum + ",  from a PM from " + sender + " using message " + message + "-----------");
				}
			}

			public String executeOnCommand(BaseCommand cmd, String[] args) throws Exception {
				try {
					Class clazz = cmd.getClass();
					for (Method curMethod : clazz.getMethods())
						if (curMethod.getName().equalsIgnoreCase("onCommand") && curMethod.getReturnType().equals(String.class)) {
							//Pad the args with null values
							args = Arrays.copyOf(args, curMethod.getParameterTypes().length);
							log.trace("Args: " + StringUtils.join(args, ","));

							return (String) curMethod.invoke(cmd, (Object[]) args);
						}
				} catch (InvocationTargetException e) {
					//Unrwap if nessesary
					Throwable cause = e.getCause();
					if (cause != null && cause instanceof Exception)
						throw (Exception) e.getCause();
					throw e;
				}
				return null;
			}

			public String[] getArgs(String message) {
				message = message.trim();
				String[] args;
				if (message.contains(" "))
					args = (String[]) ArrayUtils.remove(message.split(" "), 0);
				else
					args = new String[0];
				return args;
			}

			public BaseCommand setupCommand(String command, String channel, String sender, String login, String hostname, String message) throws Exception {
				//Parse message to get cmd and args
				String[] args = getArgs(message);

				BaseCommand plugin = CommandManager.getCommand(command);
				//Is this a valid plugin?
				if (plugin == null || !plugin.isEnabled())
					throw new InvalidCMDException(command);
				//Is this an admin function? If so, is the person an admin?
				if (plugin.isAdmin() && !getController().isAdmin(sender, getBot(), channel))
					throw new AdminException();

				//Does the required number of args exist?
				int given = args.length;
				int required = plugin.getRequiredParams();
				int optional = plugin.getOptionalParams();
				log.debug("User Args: " + given + " | Req Args: " + required + " | Optional: " + optional);
				if (given > required + optional) //Do we have too many?
					throw new NumArgException(given, required, optional);
				else if (given < required) //Do we not have enough?
					throw new NumArgException(given, required);
				return plugin;
			}
		});
	}

	public static Bot getPoolLocal() {
		Bot bot = poolLocal.get();
		return bot;
	}

	public boolean isLocked(String channel, String sender) {
		return isLocked(channel, sender, false);
	}

	public boolean isLocked(String channel, String sender, boolean sayError) {
		//Is bot locked?
		if (botLocked == true && !controller.isAdmin(getServer(), channel, sender)) {
			if (sayError)
				log.info("Command ignored due to global lock in effect");
			return true;
		}

		//Is channel locked?
		if (channel != null && chanLockList.contains(channel) && !controller.isAdmin(getServer(), channel, sender)) {
			if (sayError)
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

	public void sendMessage(String channel, String user, String message) {
		if (message != null)
			sendMessage(channel, user + ": " + message);
	}

	public boolean isBot(String name) {
		return getName().equals(name);
	}

	public List<String> getPrefixes() {
		//Merge the global list and the Bot specific list
		ArrayList<String> list = new ArrayList<String>(controller.config.getPrefixes());
		list.add(getNick() + ":");
		list.add(getNick());
		return list;
	}

	@Override
	public int compareTo(Bot bot) {
		return unique.compareTo(bot.unique);
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
