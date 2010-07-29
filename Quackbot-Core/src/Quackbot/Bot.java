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
package Quackbot;

import Quackbot.err.AdminException;
import Quackbot.err.InvalidCMDException;
import Quackbot.err.NumArgException;

import Quackbot.info.Channel;
import Quackbot.hook.HookManager;
import Quackbot.hook.Hook;
import Quackbot.info.Server;
import java.awt.Event;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import java.util.List;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import org.slf4j.Logger;
import org.jibble.pircbot.DccChat;
import org.jibble.pircbot.DccFileTransfer;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.ReplyConstants;

import org.jibble.pircbot.User;
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
public class Bot extends PircBot implements Comparable<Bot> {
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

	/**
	 * Init bot by setting all information
	 * @param serverDB   The persistent server object from database
	 */
	public Bot(final Server serverDB, ExecutorService threadPool) {
		this.serverDB = serverDB;
		this.threadPool = threadPool;
		poolLocal.set(this);
		unique = UUID.randomUUID();

		setName("Quackbot");
		setAutoNickChange(true);
		setFinger("Quackbot IRC bot by Lord.Quackstar. Source: http://quackbot.googlecode.com/");
		setMessageDelay(0);
		setVersion("Quackbot 3.3");

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
		HookManager.addPluginHook(new Hook("QuackBotNative") {
			private Logger log = LoggerFactory.getLogger(Bot.class);

			@Override
			public void onMessage(String channel, String sender, String login, String hostname, String message) {
				int cmdNum = Controller.instance.addCmdNum();

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
							Command cmd = setupCommand(command, channel, sender, login, hostname, message);
							cmd.onCommandGiven(channel, sender, login, hostname, getArgs(message));
							cmd.onCommandChannel(channel, sender, login, hostname, getArgs(message));

							String response = executeOnCommand(cmd, getArgs(message));
							if (response != null)
								getBot().sendMessage(channel, sender, response);
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
				int cmdNum = Controller.instance.addCmdNum();
				log.debug("-----------Begin execution of command #" + cmdNum + ",  from a PM from " + sender + " using message " + message + "-----------");
				String command = "";

				try {
					if (getBot().isLocked(null, sender, true)) {
						log.warn("Bot locked");
						return;
					}

					//Look for a prefix
					command = message.split(" ", 2)[0];
					Command cmd = setupCommand(command, null, sender, login, hostname, message);
					cmd.onCommandGiven(sender, sender, login, hostname, getArgs(message));
					cmd.onCommandPM(sender, login, hostname, getArgs(message));
					String response = executeOnCommand(cmd, getArgs(message));
					if (response != null)
						getBot().sendMessage(sender, response);
				} catch (Exception e) {
					log.error("Error encountered when running command " + command, e);
					getBot().sendMessage(sender, "ERROR: " + e.getMessage());
				} finally {
					log.debug("-----------End execution of command #" + cmdNum + ",  from a PM from " + sender + " using message " + message + "-----------");
				}
			}

			public String executeOnCommand(Command cmd, String[] args) throws Exception {
				try {
					Class clazz = cmd.getClass();
					for (Method curMethod : clazz.getMethods())
						if (curMethod.getName().equalsIgnoreCase("onCommand") && curMethod.getReturnType().equals(String.class)) {
							//Pad the args with null values
							args = Arrays.copyOf(args, curMethod.getParameterTypes().length);
							return (String)curMethod.invoke(cmd, (Object[]) args);
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

			public Command setupCommand(String command, String channel, String sender, String login, String hostname, String message) throws Exception {
				//Parse message to get cmd and args
				String[] args = getArgs(message);

				Command plugin = CommandManager.getCommand(command);
				//Is this a valid plugin?
				if (plugin == null || !plugin.isEnabled())
					throw new InvalidCMDException(command);
				//Is this an admin function? If so, is the person an admin?
				if (plugin.isAdmin() && !Controller.instance.isAdmin(sender, getBot(), channel))
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

		HookManager.addPluginHook(new Hook("IRCNative") {
			@Override
			public void onFinger(String sourceNick, String sourceLogin, String sourceHostname, String target) {
				getBot().sendRawLine("NOTICE " + sourceNick + " :\u0001FINGER " + getBot().getFinger() + "\u0001");
			}

			@Override
			public void onPing(String sourceNick, String sourceLogin, String sourceHostname, String target, String pingValue) {
				getBot().sendRawLine("NOTICE " + sourceNick + " :\u0001PING " + pingValue + "\u0001");
			}

			@Override
			public void onServerPing(String response) throws Exception {
				getBot().sendRawLine("PONG " + response);
			}

			@Override
			public void onTime(String sourceNick, String sourceLogin, String sourceHostname, String target) {
				getBot().sendRawLine("NOTICE " + sourceNick + " :\u0001TIME " + new Date().toString() + "\u0001");
			}

			@Override
			public void onVersion(String sourceNick, String sourceLogin, String sourceHostname, String target) {
				getBot().sendRawLine("NOTICE " + sourceNick + " :\u0001VERSION " + getBot().getVersion() + "\u0001");
			}
		});
	}

	public static Bot getPoolLocal() {
		return poolLocal.get();
	}

	public boolean isLocked(String channel, String sender) {
		return isLocked(channel, sender, false);
	}

	public boolean isLocked(String channel, String sender, boolean sayError) {
		//Is bot locked?
		if (botLocked == true && !Controller.instance.isAdmin(getServer(), channel, sender)) {
			if (sayError)
				log.info("Command ignored due to global lock in effect");
			return true;
		}

		//Is channel locked?
		if (channel != null && chanLockList.contains(channel) && !Controller.instance.isAdmin(getServer(), channel, sender)) {
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

	/**
	 * This method is called once the PircBot has successfully connected to
	 * the IRC server.
	 *
	 * @since PircBot 0.9.6 & Quackbot 3.0
	 */
	@Override
	public void onConnect() {
		HookManager.getList("onConnect").execute();

		List<Channel> channels = serverDB.getChannels();
		for (Channel curChannel : channels) {
			joinChannel(curChannel.getName(), curChannel.getPassword());
			log.debug("Trying to join channel using " + curChannel);
		}
	}

	@Override
	public synchronized void dispose() {
		threadPool.shutdown();
		super.dispose();
	}

	/**
	 * Executes command
	 *
	 * Command handling takes place here purely for nice output for console. If returned, end tag still shown
	 * @param msgInfo                The BotEvent bean
	 * @throws InvalidCMDException   If command does not exist
	 * @throws AdminException        If command is admin only and user is not admin
	 * @throws NumArgException       If improper amount of arguments were given
	 */
	/**
	 * Utility method to get a specific user from channel
	 * @param channel  Channel to search in
	 * @param reqUser  User to search for
	 * @return         User object of user, null if not found
	 */
	public User getUser(String channel, String reqUser) {
		User[] listUsers = getUsers(channel);
		for (User curUser : listUsers)
			if (curUser.getNick().equalsIgnoreCase(reqUser))
				return curUser;
		return null;
	}

	/**
	 * Send message to ALL joined channels
	 * @param msg   Message to send
	 */
	public void sendAllMessage(String msg) {
		String[] channels = getChannels();
		for (String curChan : channels)
			sendMessage(curChan, msg);
	}

	public void sendMessage(String channel, String user, String message) {
		sendMessage(channel, user + ": " + message);
	}

	public boolean isBot(String name) {
		return getName().equals(name);
	}

	public List<String> getPrefixes() {
		//Merge the global list and the Bot specific list
		ArrayList<String> list = new ArrayList<String>(Controller.instance.globPrefixes);
		list.add(getNick() + ":");
		list.add(getNick());
		return list;
	}

	@Override
	public int compareTo(Bot bot) {
		return unique.compareTo(bot.unique);
	}

	/****************************************HOOKS************************************************************/
	/**
	 * This method is called whenever a message is sent to a channel.
	 *
	 * @param channel The channel to which the message was sent.
	 * @param sender The nick of the person who sent the message.
	 * @param login The login of the person who sent the message.
	 * @param hostname The hostname of the person who sent the message.
	 * @param message The actual message sent to the channel.
	 */
	@Override
	public void onMessage(String channel, String sender, String login, String hostname, String message) {
		HookManager.getList("onMessage").execute(channel, sender, login, hostname, message);
	}

	/**
	 * This method is called whenever a private message is sent to the PircBot.
	 *
	 * @param sender The nick of the person who sent the private message.
	 * @param login The login of the person who sent the private message.
	 * @param hostname The hostname of the person who sent the private message.
	 * @param message The actual message.
	 */
	@Override
	public void onPrivateMessage(String sender, String login, String hostname, String message) {
		HookManager.getList("onPrivateMessage").execute(sender, login, hostname, message);
	}

	/**
	 * See {@link Event#onDisconnect}
	 */
	@Override
	public void onDisconnect() {
		HookManager.getList("onDisconnect").execute();
	}

	/**
	 * See {@link Event#onServerResponse}
	 *
	 * @param code The three-digit numerical code for the response. Stored as BotEvent.extra
	 * @param response The full response from the IRC server. Stored as BotEvent.rawmsg
	 *
	 * @see ReplyConstants
	 */
	@Override
	public void onServerResponse(int code, String response) {
		HookManager.getList("onServerResponse").execute(code, response);
	}

	/**
	 * See {@link Event#onUserList}
	 *
	 * @param channel The name of the channel. Stored as BotEvent.rawmsg
	 * @param users An array of User objects belonging to this channel. Stored as BotEvent.extra
	 *
	 * @see User
	 */
	@Override
	public void onUserList(String channel, User[] users) {
		HookManager.getList("onUserList").execute(channel, users);
	}

	/**
	 * See {@link Event#onAction}
	 *
	 * @param sender The nick of the user that sent the action.
	 * @param login The login of the user that sent the action.
	 * @param hostname The hostname of the user that sent the action.
	 * @param target The target of the action, be it a channel or our nick.
	 * @param action The action carried out by the user.
	 */
	@Override
	public void onAction(String sender, String login, String hostname, String target, String action) {
		HookManager.getList("onAction").execute(sender, login, hostname, target, action);
	}

	/**
	 * See {@link Event#onNotice}
	 *
	 * @param sourceNick The nick of the user that sent the notice.
	 * @param sourceLogin The login of the user that sent the notice.
	 * @param sourceHostname The hostname of the user that sent the notice.
	 * @param target The target of the notice, be it our nick or a channel name.
	 * @param notice The notice message.
	 */
	@Override
	public void onNotice(String sourceNick, String sourceLogin, String sourceHostname, String target, String notice) {
		HookManager.getList("onNotice").execute(sourceNick, sourceLogin, sourceHostname, target, notice);
	}

	/**
	 * See {@link Event#onJoin}
	 *
	 * @param channel The channel which somebody joined.
	 * @param sender The nick of the user who joined the channel.
	 * @param login The login of the user who joined the channel.
	 * @param hostname The hostname of the user who joined the channel.
	 */
	@Override
	public void onJoin(String channel, String sender, String login, String hostname) {
		HookManager.getList("onJoin").execute(channel, sender, login, hostname);
	}

	/**
	 * See {@link Event#onPart}
	 *
	 * @param channel The channel which somebody parted from.
	 * @param sender The nick of the user who parted from the channel.
	 * @param login The login of the user who parted from the channel.
	 * @param hostname The hostname of the user who parted from the channel.
	 */
	@Override
	public void onPart(String channel, String sender, String login, String hostname) {
		HookManager.getList("onPart").execute(channel, sender, login, hostname);
	}

	/**
	 * See {@link Event#onNickChange}
	 *
	 * @param oldNick The old nick.
	 * @param login The login of the user.
	 * @param hostname The hostname of the user.
	 * @param newNick The new nick. 
	 */
	@Override
	public void onNickChange(String oldNick, String login, String hostname, String newNick) {
		HookManager.getList("onNickChange").execute(oldNick, login, hostname, newNick);
	}

	/**
	 * See {@link Event#onKick}
	 *
	 * @param channel The channel from which the recipient was kicked.
	 * @param kickerNick The nick of the user who performed the kick. Stored as BotEvent.sender
	 * @param kickerLogin The login of the user who performed the kick. Stored as BotEvent.login
	 * @param kickerHostname The hostname of the user who performed the kick. Stored as BotEvent.hostName
	 * @param recipientNick The unfortunate recipient of the kick. Stored as BotEvent.extra
	 * @param reason The reason given by the user who performed the kick. Stored as BotEvent.rawmsg
	 */
	@Override
	public void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason) {
		HookManager.getList("onKick").execute(channel, kickerNick, kickerLogin, kickerHostname, recipientNick, reason);
	}

	/**
	 * See {@link Event#onQuit}
	 *
	 * @param sourceNick The nick of the user that quit from the server.
	 * @param sourceLogin The login of the user that quit from the server.
	 * @param sourceHostname The hostname of the user that quit from the server.
	 * @param reason The reason given for quitting the server.
	 */
	@Override
	public void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) {
		HookManager.getList("onQuit").execute(sourceNick, sourceLogin, sourceHostname, reason);
	}

	/**
	 * See {@link Event#onTopic}
	 *
	 * @param channel The channel that the topic belongs to.
	 * @param topic The topic for the channel. Stored as BotEvent.rawmsg
	 * @param setBy The nick of the user that set the topic. Stored as BotEvent.sender
	 * @param date When the topic was set (milliseconds since the epoch). Stored as BotEvent.extra
	 * @param changed True if the topic has just been changed, false if
	 *                the topic was already there. Stored as BotEvent.extra1
	 *
	 */
	@Override
	public void onTopic(String channel, String topic, String setBy, long date, boolean changed) {
		HookManager.getList("onTopic").execute(channel, topic, setBy, date, changed);
	}

	/**
	 * See {@link Event#onChannelInfo}
	 *
	 * @param channel The name of the channel.
	 * @param userCount The number of users visible in this channel. Stored as BotEvent.extra
	 * @param topic The topic for this channel. Stored as BotEvent.rawmsg
	 *
	 * @see #listChannels() listChannels
	 */
	@Override
	public void onChannelInfo(String channel, int userCount, String topic) {
		HookManager.getList("onChannelInfo").execute(channel, userCount, topic);
	}

	/**
	 * See {@link Event#onMode}
	 *
	 * @param channel The channel that the mode operation applies to.
	 * @param sourceNick The nick of the user that set the mode.
	 * @param sourceLogin The login of the user that set the mode.
	 * @param sourceHostname The hostname of the user that set the mode.
	 * @param mode The mode that has been set. Stored as BotEvent.rawmsg
	 *
	 */
	@Override
	public void onMode(String channel, String sourceNick, String sourceLogin, String sourceHostname, String mode) {
		HookManager.getList("onMode").execute(channel, sourceNick, sourceLogin, sourceHostname, mode);
	}

	/**
	 * See {@link Event#onUserMode}
	 *
	 * @param targetNick The nick that the mode operation applies to.
	 * @param sourceNick The nick of the user that set the mode.
	 * @param sourceLogin The login of the user that set the mode.
	 * @param sourceHostname The hostname of the user that set the mode.
	 * @param mode The mode that has been set. Stored as BotEvent.rawmsg
	 *
	 */
	@Override
	public void onUserMode(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String mode) {
		HookManager.getList("onUserMode").execute(targetNick, sourceNick, sourceLogin, sourceHostname, mode);
	}

	/**
	 * See {@link Event#onOp}
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 * @param recipient The nick of the user that got 'opped'. Stored as BotEvent.rawmsg
	 */
	@Override
	public void onOp(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
		HookManager.getList("onOp").execute(channel, sourceNick, sourceLogin, sourceHostname, recipient);
	}

	/**
	 * See {@link Event#onDeop}
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 * @param recipient The nick of the user that got 'deopped'. Stored as BotEvent.rawmsg
	 */
	@Override
	public void onDeop(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
		HookManager.getList("onDeop").execute(channel, sourceNick, sourceLogin, sourceHostname, recipient);
	}

	/**
	 * See {@link Event#onVoice}
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 * @param recipient The nick of the user that got 'voiced'. Stored as BotEvent.rawmsg
	 */
	@Override
	public void onVoice(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
		HookManager.getList("onVoice").execute(channel, sourceNick, sourceLogin, sourceHostname, recipient);
	}

	/**
	 * See {@link Event#onDeVoice}
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 * @param recipient The nick of the user that got 'devoiced'. Stored as BotEvent.rawmsg
	 */
	@Override
	public void onDeVoice(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
		HookManager.getList("onDeVoice").execute(channel, sourceNick, sourceLogin, sourceHostname, recipient);
	}

	/**
	 * See {@link Event#onSetChannelKey}
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 * @param key The new key for the channel. Stored as BotEvent.rawmsg
	 */
	@Override
	public void onSetChannelKey(String channel, String sourceNick, String sourceLogin, String sourceHostname, String key) {
		HookManager.getList("onSetChannelKey").execute(channel, sourceNick, sourceLogin, sourceHostname, key);
	}

	/**
	 * See {@link Event#onRemoveChannelKey}
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 * @param key The key that was in use before the channel key was removed. Stored as BotEvent.key
	 */
	@Override
	public void onRemoveChannelKey(String channel, String sourceNick, String sourceLogin, String sourceHostname, String key) {
		HookManager.getList("onRemoveChannelKey").execute(channel, sourceNick, sourceLogin, sourceHostname, key);
	}

	/**
	 * See {@link Event#onSetChannelLimit}
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 * @param limit The maximum number of users that may be in this channel at the same time. Stored as BotEvent.extra
	 */
	@Override
	public void onSetChannelLimit(String channel, String sourceNick, String sourceLogin, String sourceHostname, int limit) {
		HookManager.getList("onSetChannelLimit").execute(channel, sourceNick, sourceLogin, sourceHostname, limit);
	}

	/**
	 * See {@link Event#onRemoveChannelLimit}
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 */
	@Override
	public void onRemoveChannelLimit(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		HookManager.getList("onRemoveChannelLimit").execute(channel, sourceNick, sourceLogin, sourceHostname);
	}

	/**
	 * See {@link Event#onSetChannelBan}
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 * @param hostmask The hostmask of the user that has been banned. Stored  as BotEvent.rawmsg
	 */
	@Override
	public void onSetChannelBan(String channel, String sourceNick, String sourceLogin, String sourceHostname, String hostmask) {
		HookManager.getList("onSetChannelBan").execute(channel, sourceNick, sourceLogin, sourceHostname, hostmask);
	}

	/**
	 * See {@link Event#onRemoveChannelBan}
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 * @param hostmask The hostmask of the user that has been banned. Stored  as BotEvent.rawmsg
	 */
	@Override
	public void onRemoveChannelBan(String channel, String sourceNick, String sourceLogin, String sourceHostname, String hostmask) {
		HookManager.getList("onRemoveChannelBan").execute(channel, sourceNick, sourceLogin, sourceHostname, hostmask);
	}

	/**
	 * See {@link Event#onSetTopicProtection}
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 */
	@Override
	public void onSetTopicProtection(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		HookManager.getList("onSetTopicProtection").execute(channel, sourceNick, sourceLogin, sourceHostname);
	}

	/**
	 * See {@link Event#onRemoveTopicProtection}
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 */
	@Override
	public void onRemoveTopicProtection(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		HookManager.getList("onRemoveTopicProtection").execute(channel, sourceNick, sourceLogin, sourceHostname);
	}

	/**
	 * See {@link Event#onSetNoExternalMessages}
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 */
	@Override
	public void onSetNoExternalMessages(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		HookManager.getList("onSetNoExternalMessages").execute(channel, sourceNick, sourceLogin, sourceHostname);
	}

	/**
	 * See {@link Event#onRemoveNoExternalMessages}
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 */
	@Override
	public void onRemoveNoExternalMessages(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		HookManager.getList("onRemoveNoExternalMessages").execute(channel, sourceNick, sourceLogin, sourceHostname);
	}

	/**
	 * See {@link Event#onSetInviteOnly}
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 */
	@Override
	public void onSetInviteOnly(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		HookManager.getList("onSetInviteOnly").execute(channel, sourceNick, sourceLogin, sourceHostname);
	}

	/**
	 * See {@link Event#onRemoveInviteOnly}
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 */
	@Override
	public void onRemoveInviteOnly(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		HookManager.getList("onRemoveInviteOnly").execute(channel, sourceNick, sourceLogin, sourceHostname);
	}

	/**
	 * See {@link Event#onSetModerated}
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 */
	@Override
	public void onSetModerated(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		HookManager.getList("onSetModerated").execute(channel, sourceNick, sourceLogin, sourceHostname);
	}

	/**
	 * See {@link Event#onRemoveModerated}
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 */
	@Override
	public void onRemoveModerated(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		HookManager.getList("onRemoveModerated").execute(channel, sourceNick, sourceLogin, sourceHostname);
	}

	/**
	 * See {@link Event#onSetPrivate}
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 */
	@Override
	public void onSetPrivate(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		HookManager.getList("onSetPrivate").execute(channel, sourceNick, sourceLogin, sourceHostname);
	}

	/**
	 * See {@link Event#onRemovePrivate}
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 */
	@Override
	public void onRemovePrivate(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		HookManager.getList("onRemovePrivate").execute(channel, sourceNick, sourceLogin, sourceHostname);
	}

	/**
	 * See {@link Event#onSetSecret}
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 */
	@Override
	public void onSetSecret(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		HookManager.getList("onSetSecret").execute(channel, sourceNick, sourceLogin, sourceHostname);
	}

	/**
	 * See {@link Event#onRemoveSecret}
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 */
	@Override
	public void onRemoveSecret(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		HookManager.getList("onRemoveSecret").execute(channel, sourceNick, sourceLogin, sourceHostname);
	}

	/**
	 * See {@link Event#onInvite}
	 *
	 * @param targetNick The nick of the user being invited - should be us!
	 * @param sourceNick The nick of the user that sent the invitation.
	 * @param sourceLogin The login of the user that sent the invitation.
	 * @param sourceHostname The hostname of the user that sent the invitation.
	 * @param channel The channel that we're being invited to. Stored as rawmsg
	 */
	@Override
	public void onInvite(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String channel) {
		HookManager.getList("onInvite").execute(targetNick, sourceNick, sourceLogin, sourceHostname, channel);
	}

	/**
	 * See {@link Event#onIncomingFileTransfer}
	 *
	 * @param transfer The DcccFileTransfer that you may accept. Stored as BotEvent.extra
	 *
	 * @see DccFileTransfer
	 *
	 */
	@Override
	public void onIncomingFileTransfer(DccFileTransfer transfer) {
		HookManager.getList("onIncomingFileTransfer").execute(transfer);
	}

	/**
	 * See {@link Event#onFileTransferFinished}
	 *
	 * @param transfer The DccFileTransfer that has finished. Stored as BotEvent.extra
	 * @param e null if the file was transfered successfully, otherwise this
	 *          will report what went wrong. Stored as BotEvent.extra1
	 *
	 * @see DccFileTransfer
	 *
	 */
	@Override
	public void onFileTransferFinished(DccFileTransfer transfer, Exception e) {
		HookManager.getList("onFileTransferFinished").execute(transfer, e);
	}

	/**
	 * See {@link Event#onIncomingChatRequest}
	 *
	 * @param chat A DccChat object that represents the incoming chat request. Stored as BotEvent.extra
	 *
	 * @see DccChat
	 *
	 */
	@Override
	public void onIncomingChatRequest(DccChat chat) {
		HookManager.getList("onIncomingChatRequest").execute(chat);
	}

	/**
	 * See {@link Event#onVersion}
	 * 
	 * @param sourceNick The nick of the user that sent the VERSION request.
	 * @param sourceLogin The login of the user that sent the VERSION request.
	 * @param sourceHostname The hostname of the user that sent the VERSION request.
	 * @param target The target of the VERSION request, be it our nick or a channel name.
	 */
	@Override
	public void onVersion(String sourceNick, String sourceLogin, String sourceHostname, String target) {
		HookManager.getList("onVersion").execute(sourceNick, sourceLogin, sourceHostname, target);
	}

	/**
	 * See {@link Event#onPing}
	 * 
	 * @param sourceNick The nick of the user that sent the PING request.
	 * @param sourceLogin The login of the user that sent the PING request.
	 * @param sourceHostname The hostname of the user that sent the PING request.
	 * @param target The target of the PING request, be it our nick or a channel name.
	 * @param pingValue The value that was supplied as an argument to the PING command. Stored as BotEvent.rawmsg
	 */
	@Override
	public void onPing(String sourceNick, String sourceLogin, String sourceHostname, String target, String pingValue) {
		HookManager.getList("onPing").execute(sourceNick, sourceLogin, sourceHostname, target, pingValue);
	}

	/**
	 * See {@link Event#onServerPing}
	 * 
	 * @param response The response that should be given back in your PONG. Stored as BotEvent.rawmsg
	 */
	@Override
	public void onServerPing(String response) {
		HookManager.getList("onServerPing").execute(response);
	}

	/**
	 * See {@link Event#onTime}
	 * 
	 * @param sourceNick The nick of the user that sent the TIME request.
	 * @param sourceLogin The login of the user that sent the TIME request.
	 * @param sourceHostname The hostname of the user that sent the TIME request.
	 * @param target The target of the TIME request, be it our nick or a channel name.
	 */
	@Override
	public void onTime(String sourceNick, String sourceLogin, String sourceHostname, String target) {
		HookManager.getList("onTime").execute(sourceNick, sourceLogin, sourceHostname, target);
	}

	/**
	 * See {@link Event#onFinger}
	 * 
	 * @param sourceNick The nick of the user that sent the FINGER request.
	 * @param sourceLogin The login of the user that sent the FINGER request.
	 * @param sourceHostname The hostname of the user that sent the FINGER request.
	 * @param target The target of the FINGER request, be it our nick or a channel name.
	 */
	@Override
	public void onFinger(String sourceNick, String sourceLogin, String sourceHostname, String target) {
		HookManager.getList("onFinger").execute(sourceNick, sourceLogin, sourceHostname, target);
	}

	/**
	 * See {@link Event#onUnknown}
	 *
	 * @param line The raw line that was received from the server. Stored as BotEvent.rawmsg
	 */
	@Override
	public void onUnknown(String line) {
		HookManager.getList("onUnknown").execute(line);
	}
	//And then it was done :-)

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
}
