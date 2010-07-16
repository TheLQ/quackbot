/**
 * @(#)Bot.java
 *
 * Copyright Leon Blakey/Lord.Quackstar, 2009-2010
 *
 * This file is part of Quackbot
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
package Quackbot;

import Quackbot.err.AdminException;
import Quackbot.err.InvalidCMDException;
import Quackbot.err.NumArgException;
import Quackbot.err.QuackbotException;
import Quackbot.hook.HookList;

import Quackbot.info.BotMessage;
import Quackbot.info.Channel;
import Quackbot.hook.Event;
import Quackbot.hook.HookManager;
import Quackbot.hook.PluginHook;
import Quackbot.info.Server;
import Quackbot.info.BotEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ArrayBlockingQueue;

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
public class Bot extends PircBot {
	/**
	 * Says weather bot is globally locked or not
	 */
	public boolean botLocked = false;
	/**
	 * Set of prefixes that will initate bot command
	 */
	public final HashSet<String> PREFIXES = new HashSet<String>();
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
	public static ThreadGroupLocal<String> threadLocal = new ThreadGroupLocal<String>("EMPTY");
	/**
	 * A custom outgoing queue used for tracking
	 */
	public final CustBlockingQueue msgQueue = new CustBlockingQueue(1);
	/**
	 * Log4J logger
	 */
	private Logger log = LoggerFactory.getLogger(Bot.class);

	/**
	 * Init bot by setting all information
	 * @param serverDB   The persistent server object from database
	 */
	public Bot(final Server serverDB, ExecutorService threadPool) {
		this.serverDB = serverDB;
		this.threadPool = threadPool;
		threadLocal.set(serverDB.getAddress());

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

		// A consumer thread
		new Thread(new Runnable() {
			public void run() {
				while (true)
					try {
						// Blocks until there is something in the queue
						MessageQueueEntry entry = msgQueue.take();
						sendRawLine(entry.msg);
						//Release lock so that put() unblocks
						entry.lock.countDown();
						//Wait before continuing
						Thread.sleep(Controller.msgWait);
					} catch (InterruptedException e) {
						log.error("Wait for sending message interrupted", e);
					}
			}
		}).start();

	}

	/**
	 * This adds the default hooks for command management
	 */
	static {
		//Default onMessage handling
		HookManager.addHook(Event.onMessage, "QuackRunCommand", new PluginHook() {
			public void run(HookList hookStack, Bot bot, BotEvent msgInfo) {
				//Look for a prefix
				Iterator preItr = bot.PREFIXES.iterator();
				Boolean contPre = false;
				String msg = msgInfo.getRawmsg();
				while (preItr.hasNext()) {
					String curPre = preItr.next().toString();
					if (curPre.length() < msg.length() && msg.substring(0, curPre.length()).equals(curPre)) {
						contPre = true;
						msgInfo.setRawmsg(msg.substring(curPre.length(), msg.length()).trim());

						//Bot activated, start command process
						bot.activateCmd(msgInfo);
						break;
					}
				}
			}
		});

		//Default onPrivateMessage handling
		HookManager.addHook(Event.onPrivateMessage, "QuackRunPMCommand", new PluginHook() {
			public void run(HookList hookStack, Bot bot, BotEvent msgInfo) {
				bot.activateCmd(msgInfo);
			}
		});

		//Default onVersion handling
		HookManager.addHook(Event.onVersion, "NativeOnVersion", new PluginHook() {
			public void run(HookList hookStack, Bot bot, BotEvent msgInfo) {
				bot.onVersionSuper(msgInfo.getSender(), msgInfo.getLogin(), msgInfo.getHostname(), msgInfo.getChannel());
			}
		});

		//Default onPing handling
		HookManager.addHook(Event.onPing, "NativeOnPing", new PluginHook() {
			public void run(HookList hookStack, Bot bot, BotEvent msgInfo) {
				bot.onPingSuper(msgInfo.getSender(), msgInfo.getLogin(), msgInfo.getHostname(), msgInfo.getChannel(), msgInfo.getRawmsg());
			}
		});

		HookManager.addHook(Event.onServerPing, "NativeOnServerPing", new PluginHook() {
			public void run(HookList hookStack, Bot bot, BotEvent msgInfo) {
				bot.onServerPingSuper(msgInfo.getRawmsg());
			}
		});

		HookManager.addHook(Event.onTime, "NativeOnTime", new PluginHook() {
			public void run(HookList hookStack, Bot bot, BotEvent msgInfo) {
				bot.onTimeSuper(msgInfo.getSender(), msgInfo.getLogin(), msgInfo.getHostname(), msgInfo.getChannel());
			}
		});

		HookManager.addHook(Event.onFinger, "NativeOnFinger", new PluginHook() {
			public void run(HookList hookStack, Bot bot, BotEvent msgInfo) {
				bot.onFingerSuper(msgInfo.getSender(), msgInfo.getLogin(), msgInfo.getHostname(), msgInfo.getChannel());
			}
		});
	}

	/**
	 * PircBot commands use this, simply redirects to Log4j. SHOULD NOT BE USED OUTSIDE OF PIRCBOT
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
	protected void onConnect() {
		HookManager.executeEvent(this, new BotEvent(Event.onConnect, null));

		//Init prefixes (put here in order to prevent MOTD from tripping bot)
		PREFIXES.add("?");
		PREFIXES.add(getNick() + ":");
		PREFIXES.add(getNick());

		List<Channel> channels = serverDB.getChannels();
		for (Channel curChannel : channels) {
			joinChannel(curChannel.getName(), curChannel.getPassword());
			log.debug("Trying to join channel using " + curChannel);
		}
	}

	/**
	 * <b>Main bot output</b> All commands should use this to communicate with server
	 * <p>
	 * Note that this method BLOCKS until message is sent!
	 * @param msg Message to send
	 */
	public void sendMsg(final BotMessage msg) {
		try {
			msgQueue.put(msg.toIrcCommand());
		} catch (InterruptedException e) {
			log.error("Wait to send message interupted", e);
		} catch (QuackbotException e) {
			log.error("Can't put message on queue", e);
		}
	}

	public synchronized void dispose() {
		threadPool.shutdown();
		super.dispose();
	}

	/**
	 * runCommand wrapper, outputs beginning and end to console and catches errors
	 * @param msgInfo BotEvent bean
	 */
	public void activateCmd(BotEvent msgInfo) {
		try {
			HookManager.executeEvent(this, msgInfo.setEvent(Event.onCommand));
			runCommand(msgInfo);
		} catch (Exception e) {
			sendMsg(new BotMessage(msgInfo, e));
			log.error("Run Error", e);
			HookManager.executeEvent(this, msgInfo.setEvent(Event.onCommandFail).setExtra(e));
		}
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
	protected void runCommand(BotEvent msgInfo) throws InvalidCMDException, AdminException, NumArgException {
		//Is bot locked?
		if (botLocked == true && !Controller.instance.adminExists(this, msgInfo)) {
			log.info("Command ignored due to global lock in effect");
			return;
		}

		//Is channel locked?
		if (msgInfo.getChannel() != null && chanLockList.contains(msgInfo.getChannel()) && !Controller.instance.adminExists(this, msgInfo)) {
			log.info("Command ignored due to channel lock in effect");
			return;
		}

		String[] argArray;
		String command;
		//Parse message to get cmd and args
		if (msgInfo.getRawmsg().indexOf(" ") > -1) {
			String[] msgArray = msgInfo.getRawmsg().split(" ", 2);
			command = msgArray[0].trim();
			argArray = msgArray[1].split(" ");
		} else {
			command = msgInfo.getRawmsg().trim();
			argArray = new String[0];
		}

		//Build BotEvent bean
		msgInfo.setArgs(argArray);
		msgInfo.setCommand(command);

		threadPool.execute(new PluginExecutor(this, msgInfo));
	}

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
			sendMsg(new BotMessage(curChan, msg));
	}

	public boolean isBot(String name) {
		return getName().equals(name);
	}

	public boolean isBot(BotEvent event) {
		return getName().equals(event.getSender());
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
	protected void onMessage(String channel, String sender, String login, String hostname, String message) {
		HookManager.executeEvent(this, new BotEvent(Event.onMessage, channel, sender, login, hostname, message));
	}

	/**
	 * This method is called whenever a private message is sent to the PircBot.
	 *
	 * @param sender The nick of the person who sent the private message.
	 * @param login The login of the person who sent the private message.
	 * @param hostname The hostname of the person who sent the private message.
	 * @param message The actual message.
	 */
	protected void onPrivateMessage(String sender, String login, String hostname, String message) {
		HookManager.executeEvent(this, new BotEvent(Event.onPrivateMessage, null, sender, login, hostname, message));
	}

	/**
	 * See {@link Event#onDisconnect}
	 */
	protected void onDisconnect() {
		HookManager.executeEvent(this, new BotEvent(Event.onDisconnect, null));
	}

	/**
	 * See {@link Event#onServerResponse}
	 *
	 * @param code The three-digit numerical code for the response. Stored as BotEvent.extra
	 * @param response The full response from the IRC server. Stored as BotEvent.rawmsg
	 *
	 * @see ReplyConstants
	 */
	protected void onServerResponse(int code, String response) {
		HookManager.executeEvent(this, new BotEvent<Integer, Void>(Event.onServerResponse, response).setExtra(code));
	}

	/**
	 * See {@link Event#onUserList}
	 *
	 * @param channel The name of the channel. Stored as BotEvent.rawmsg
	 * @param users An array of User objects belonging to this channel. Stored as BotEvent.extra
	 *
	 * @see User
	 */
	protected void onUserList(String channel, User[] users) {
		HookManager.executeEvent(this, new BotEvent<User[], Void>(Event.onUserList, channel, null, null, null, null).setExtra(users));
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
	protected void onAction(String sender, String login, String hostname, String target, String action) {
		HookManager.executeEvent(this, new BotEvent(Event.onAction, target, sender, login, hostname, action));
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
	protected void onNotice(String sourceNick, String sourceLogin, String sourceHostname, String target, String notice) {
		HookManager.executeEvent(this, new BotEvent(Event.onNotice, target, sourceNick, sourceLogin, sourceHostname, notice));
	}

	/**
	 * See {@link Event#onJoin}
	 *
	 * @param channel The channel which somebody joined.
	 * @param sender The nick of the user who joined the channel.
	 * @param login The login of the user who joined the channel.
	 * @param hostname The hostname of the user who joined the channel.
	 */
	protected void onJoin(String channel, String sender, String login, String hostname) {
		HookManager.executeEvent(this, new BotEvent(Event.onJoin, channel, sender, login, hostname, null));
	}

	/**
	 * See {@link Event#onPart}
	 *
	 * @param channel The channel which somebody parted from.
	 * @param sender The nick of the user who parted from the channel.
	 * @param login The login of the user who parted from the channel.
	 * @param hostname The hostname of the user who parted from the channel.
	 */
	protected void onPart(String channel, String sender, String login, String hostname) {
		HookManager.executeEvent(this, new BotEvent(Event.onPart, channel, sender, login, hostname, null));
	}

	/**
	 * See {@link Event#onNickChange}
	 *
	 * @param oldNick The old nick.
	 * @param login The login of the user.
	 * @param hostname The hostname of the user.
	 * @param newNick The new nick. 
	 */
	protected void onNickChange(String oldNick, String login, String hostname, String newNick) {
		HookManager.executeEvent(this, new BotEvent(Event.onNickChange, oldNick, newNick, login, hostname, null));
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
	protected void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason) {
		HookManager.executeEvent(this, new BotEvent<String, Void>(Event.onKick, channel, kickerNick, kickerLogin, kickerHostname, reason).setExtra(recipientNick));
	}

	/**
	 * See {@link Event#onQuit}
	 *
	 * @param sourceNick The nick of the user that quit from the server.
	 * @param sourceLogin The login of the user that quit from the server.
	 * @param sourceHostname The hostname of the user that quit from the server.
	 * @param reason The reason given for quitting the server.
	 */
	protected void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) {
		HookManager.executeEvent(this, new BotEvent(Event.onQuit, null, sourceNick, sourceLogin, sourceHostname, reason));
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
	protected void onTopic(String channel, String topic, String setBy, long date, boolean changed) {
		HookManager.executeEvent(this, new BotEvent<Long, Boolean>(Event.onTopic, channel, setBy, null, null, topic).setExtra(date).setExtra1(changed));
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
	protected void onChannelInfo(String channel, int userCount, String topic) {
		HookManager.executeEvent(this, new BotEvent<Integer, Void>(Event.onChannelInfo, channel, null, null, null, topic).setExtra(userCount));
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
	protected void onMode(String channel, String sourceNick, String sourceLogin, String sourceHostname, String mode) {
		HookManager.executeEvent(this, new BotEvent(Event.onMode, channel, sourceNick, sourceLogin, sourceHostname, mode));
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
	protected void onUserMode(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String mode) {
		HookManager.executeEvent(this, new BotEvent(Event.onUserMode, targetNick, sourceNick, sourceLogin, sourceHostname, mode));
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
	protected void onOp(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
		HookManager.executeEvent(this, new BotEvent(Event.onOp, channel, sourceNick, sourceLogin, sourceHostname, recipient));
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
	protected void onDeop(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
		HookManager.executeEvent(this, new BotEvent(Event.onDeop, channel, sourceNick, sourceLogin, sourceHostname, recipient));
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
	protected void onVoice(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
		HookManager.executeEvent(this, new BotEvent(Event.onVoice, channel, sourceNick, sourceLogin, sourceHostname, recipient));
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
	protected void onDeVoice(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
		HookManager.executeEvent(this, new BotEvent(Event.onDeVoice, channel, sourceNick, sourceLogin, sourceHostname, recipient));
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
	protected void onSetChannelKey(String channel, String sourceNick, String sourceLogin, String sourceHostname, String key) {
		HookManager.executeEvent(this, new BotEvent(Event.onSetChannelKey, channel, sourceNick, sourceLogin, sourceHostname, key));
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
	protected void onRemoveChannelKey(String channel, String sourceNick, String sourceLogin, String sourceHostname, String key) {
		HookManager.executeEvent(this, new BotEvent(Event.onRemoveChannelKey, channel, sourceNick, sourceLogin, sourceHostname, key));
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
	protected void onSetChannelLimit(String channel, String sourceNick, String sourceLogin, String sourceHostname, int limit) {
		HookManager.executeEvent(this, new BotEvent<Integer, Void>(Event.onSetChannelLimit, channel, sourceNick, sourceLogin, sourceHostname, null).setExtra(limit));
	}

	/**
	 * See {@link Event#onRemoveChannelLimit}
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 */
	protected void onRemoveChannelLimit(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		HookManager.executeEvent(this, new BotEvent(Event.onRemoveChannelLimit, channel, sourceNick, sourceLogin, sourceHostname, null));
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
	protected void onSetChannelBan(String channel, String sourceNick, String sourceLogin, String sourceHostname, String hostmask) {
		HookManager.executeEvent(this, new BotEvent(Event.onSetChannelBan, channel, sourceNick, sourceLogin, sourceHostname, hostmask));
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
	protected void onRemoveChannelBan(String channel, String sourceNick, String sourceLogin, String sourceHostname, String hostmask) {
		HookManager.executeEvent(this, new BotEvent(Event.onRemoveChannelBan, channel, sourceNick, sourceLogin, sourceHostname, hostmask));
	}

	/**
	 * See {@link Event#onSetTopicProtection}
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 */
	protected void onSetTopicProtection(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		HookManager.executeEvent(this, new BotEvent(Event.onSetTopicProtection, channel, sourceNick, sourceLogin, sourceHostname, null));
	}

	/**
	 * See {@link Event#onRemoveTopicProtection}
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 */
	protected void onRemoveTopicProtection(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		HookManager.executeEvent(this, new BotEvent(Event.onRemoveTopicProtection, channel, sourceNick, sourceLogin, sourceHostname, null));
	}

	/**
	 * See {@link Event#onSetNoExternalMessages}
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 */
	protected void onSetNoExternalMessages(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		HookManager.executeEvent(this, new BotEvent(Event.onSetNoExternalMessages, channel, sourceNick, sourceLogin, sourceHostname, null));
	}

	/**
	 * See {@link Event#onRemoveNoExternalMessages}
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 */
	protected void onRemoveNoExternalMessages(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		HookManager.executeEvent(this, new BotEvent(Event.onRemoveNoExternalMessages, channel, sourceNick, sourceLogin, sourceHostname, null));
	}

	/**
	 * See {@link Event#onSetInviteOnly}
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 */
	protected void onSetInviteOnly(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		HookManager.executeEvent(this, new BotEvent(Event.onSetInviteOnly, channel, sourceNick, sourceLogin, sourceHostname, null));
	}

	/**
	 * See {@link Event#onRemoveInviteOnly}
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 */
	protected void onRemoveInviteOnly(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		HookManager.executeEvent(this, new BotEvent(Event.onRemoveInviteOnly, channel, sourceNick, sourceLogin, sourceHostname, null));
	}

	/**
	 * See {@link Event#onSetModerated}
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 */
	protected void onSetModerated(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		HookManager.executeEvent(this, new BotEvent(Event.onSetModerated, channel, sourceNick, sourceLogin, sourceHostname, null));
	}

	/**
	 * See {@link Event#onRemoveModerated}
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 */
	protected void onRemoveModerated(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		HookManager.executeEvent(this, new BotEvent(Event.onRemoveModerated, channel, sourceNick, sourceLogin, sourceHostname, null));
	}

	/**
	 * See {@link Event#onSetPrivate}
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 */
	protected void onSetPrivate(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		HookManager.executeEvent(this, new BotEvent(Event.onSetPrivate, channel, sourceNick, sourceLogin, sourceHostname, null));
	}

	/**
	 * See {@link Event#onRemovePrivate}
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 */
	protected void onRemovePrivate(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		HookManager.executeEvent(this, new BotEvent(Event.onRemovePrivate, channel, sourceNick, sourceLogin, sourceHostname, null));
	}

	/**
	 * See {@link Event#onSetSecret}
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 */
	protected void onSetSecret(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		HookManager.executeEvent(this, new BotEvent(Event.onSetSecret, channel, sourceNick, sourceLogin, sourceHostname, null));
	}

	/**
	 * See {@link Event#onRemoveSecret}
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 */
	protected void onRemoveSecret(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		HookManager.executeEvent(this, new BotEvent(Event.onRemoveSecret, channel, sourceNick, sourceLogin, sourceHostname, null));
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
	protected void onInvite(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String channel) {
		HookManager.executeEvent(this, new BotEvent(Event.onInvite, targetNick, sourceNick, sourceLogin, sourceHostname, channel));
	}

	/**
	 * See {@link Event#onIncomingFileTransfer}
	 *
	 * @param transfer The DcccFileTransfer that you may accept. Stored as BotEvent.extra
	 *
	 * @see DccFileTransfer
	 *
	 */
	protected void onIncomingFileTransfer(DccFileTransfer transfer) {
		HookManager.executeEvent(this, new BotEvent<DccFileTransfer, Void>(Event.onIncomingFileTransfer, null).setExtra(transfer));
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
	protected void onFileTransferFinished(DccFileTransfer transfer, Exception e) {
		HookManager.executeEvent(this, new BotEvent<DccFileTransfer, Exception>(Event.onFileTransferFinished, null).setExtra(transfer).setExtra1(e));
	}

	/**
	 * See {@link Event#onIncomingChatRequest}
	 *
	 * @param chat A DccChat object that represents the incoming chat request. Stored as BotEvent.extra
	 *
	 * @see DccChat
	 *
	 */
	protected void onIncomingChatRequest(DccChat chat) {
		HookManager.executeEvent(this, new BotEvent<DccChat, Void>(Event.onIncomingChatRequest, null).setExtra(chat));
	}

	/**
	 * See {@link Event#onVersion}
	 * 
	 * @param sourceNick The nick of the user that sent the VERSION request.
	 * @param sourceLogin The login of the user that sent the VERSION request.
	 * @param sourceHostname The hostname of the user that sent the VERSION request.
	 * @param target The target of the VERSION request, be it our nick or a channel name.
	 */
	protected void onVersion(String sourceNick, String sourceLogin, String sourceHostname, String target) {
		HookManager.executeEvent(this, new BotEvent(Event.onVersion, target, sourceNick, sourceLogin, sourceHostname, null));
	}

	/**
	 * Calls super implementation for default behavior
	 * 
	 * @param sourceNick The nick of the user that sent the VERSION request.
	 * @param sourceLogin The login of the user that sent the VERSION request.
	 * @param sourceHostname The hostname of the user that sent the VERSION request.
	 * @param target The target of the VERSION request, be it our nick or a channel name.
	 */
	protected void onVersionSuper(String sourceNick, String sourceLogin, String sourceHostname, String target) {
		super.onVersion(sourceNick, sourceLogin, sourceHostname, target);
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
	protected void onPing(String sourceNick, String sourceLogin, String sourceHostname, String target, String pingValue) {
		HookManager.executeEvent(this, new BotEvent(Event.onPing, target, sourceNick, sourceLogin, sourceHostname, pingValue));
	}

	/**
	 * Calls super onPing for default behavior
	 *
	 * @param sourceNick The nick of the user that sent the PING request.
	 * @param sourceLogin The login of the user that sent the PING request.
	 * @param sourceHostname The hostname of the user that sent the PING request.
	 * @param target The target of the PING request, be it our nick or a channel name.
	 * @param pingValue The value that was supplied as an argument to the PING command. Stored as BotEvent.rawmsg
	 */
	protected void onPingSuper(String sourceNick, String sourceLogin, String sourceHostname, String target, String pingValue) {
		super.onPing(sourceNick, sourceLogin, sourceHostname, target, pingValue);
	}

	/**
	 * See {@link Event#onServerPing}
	 * 
	 * @param response The response that should be given back in your PONG. Stored as BotEvent.rawmsg
	 */
	protected void onServerPing(String response) {
		HookManager.executeEvent(this, new BotEvent(Event.onServerPing, response));
	}

	/**
	 * Calls super onServerPing for default behavior
	 *
	 * @param response The response that should be given back in your PONG. Stored as BotEvent.rawmsg
	 */
	protected void onServerPingSuper(String response) {
		super.onServerPing(response);
	}

	/**
	 * See {@link Event#onTime}
	 * 
	 * @param sourceNick The nick of the user that sent the TIME request.
	 * @param sourceLogin The login of the user that sent the TIME request.
	 * @param sourceHostname The hostname of the user that sent the TIME request.
	 * @param target The target of the TIME request, be it our nick or a channel name.
	 */
	protected void onTime(String sourceNick, String sourceLogin, String sourceHostname, String target) {
		HookManager.executeEvent(this, new BotEvent(Event.onTime, target, sourceNick, sourceLogin, sourceHostname, null));
	}

	/**
	 * Calls onTime super for default behavior
	 *
	 * @param sourceNick The nick of the user that sent the TIME request.
	 * @param sourceLogin The login of the user that sent the TIME request.
	 * @param sourceHostname The hostname of the user that sent the TIME request.
	 * @param target The target of the TIME request, be it our nick or a channel name.
	 */
	protected void onTimeSuper(String sourceNick, String sourceLogin, String sourceHostname, String target) {
		super.onTime(sourceNick, sourceLogin, sourceHostname, target);
	}

	/**
	 * See {@link Event#onFinger}
	 * 
	 * @param sourceNick The nick of the user that sent the FINGER request.
	 * @param sourceLogin The login of the user that sent the FINGER request.
	 * @param sourceHostname The hostname of the user that sent the FINGER request.
	 * @param target The target of the FINGER request, be it our nick or a channel name.
	 */
	protected void onFinger(String sourceNick, String sourceLogin, String sourceHostname, String target) {
		HookManager.executeEvent(this, new BotEvent(Event.onFinger, target, sourceNick, sourceLogin, sourceHostname, null));
	}

	/**
	 * Calls super onFinger for default behavior
	 *
	 * @param sourceNick The nick of the user that sent the FINGER request.
	 * @param sourceLogin The login of the user that sent the FINGER request.
	 * @param sourceHostname The hostname of the user that sent the FINGER request.
	 * @param target The target of the FINGER request, be it our nick or a channel name.
	 */
	protected void onFingerSuper(String sourceNick, String sourceLogin, String sourceHostname, String target) {
		super.onFinger(sourceNick, sourceLogin, sourceHostname, target);
	}

	/**
	 * See {@link Event#onUnknown}
	 *
	 * @param line The raw line that was received from the server. Stored as BotEvent.rawmsg
	 */
	protected void onUnknown(String line) {
		HookManager.executeEvent(this, new BotEvent(Event.onUnknown, line));
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

	public class CustBlockingQueue extends ArrayBlockingQueue<MessageQueueEntry> {
		public CustBlockingQueue(int capacity) {
			super(capacity);
		}

		public void put(String e) throws InterruptedException {
			CountDownLatch latch = new CountDownLatch(1);
			super.put(new MessageQueueEntry(e, latch));
			latch.await();
		}
	}

	protected class MessageQueueEntry {
		protected CountDownLatch lock;
		protected String msg;

		public MessageQueueEntry(String e, CountDownLatch latch) {
			msg = e;
			lock = latch;
		}
	}
}
