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

import Quackbot.info.BotMessage;
import Quackbot.info.Channel;
import Quackbot.info.Hooks;
import Quackbot.info.Server;
import Quackbot.info.BotEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

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
	 * Says wheather bot is globally locked or not
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
	public final CustBlockingQueue<BotMessage> msgQueue = new CustBlockingQueue<BotMessage>(1);
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
		StringBuilder serverDebug = new StringBuilder().append("Attempting to connect to " + serverDB.getAddress() + " on port " + serverDB.getPort());
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
						BotMessage msg = msgQueue.take();
						sendRawLine("PRIVMSG " + msg.getChannel() + " :" + msg.getMessage());
						//Release lock so that put() unblocks
						msgQueue.lock.lockInterruptibly();
						msgQueue.doneProcessing.signal();
						msgQueue.lock.unlock();
						//Wait before continuing
						Thread.sleep(Controller.msgWait);
					} catch (InterruptedException e) {
						log.error("Wait for sending message interrupted", e);
					}
			}
		}).start();

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
		runHook(Hooks.onConnect, null);

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
	Date mostRecentUpdate = new Date();

	/**
	 * <b>Main bot output</b> All commands should use this to communicate with server
	 * <p>
	 * Note that this method BLOCKS until message is sent!
	 * @param msg Message to send
	 */
	public void sendMsg(final BotMessage msg) {
		try {
			msgQueue.put(msg);
		} catch (InterruptedException e) {
			log.error("Wait to send message interupted", e);
		}
	}

	public synchronized void dispose() {
		super.dispose();
		threadPool.shutdown();
	}

	/***************USER SUBMITTED COMMANDS FOLLOW*********************/
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
		runHook(Hooks.onMessage, new BotEvent(channel, sender, login, hostname, message));
		//Look for a prefix
		Iterator preItr = PREFIXES.iterator();
		Boolean contPre = false;
		while (preItr.hasNext()) {
			String curPre = preItr.next().toString();
			if (curPre.length() < message.length() && message.substring(0, curPre.length()).equals(curPre)) {
				contPre = true;
				message = message.substring(curPre.length(), message.length()).trim();
				break;
			}
		}

		//Create BotEvent from modified message
		BotEvent msgInfo = new BotEvent(channel, sender, login, hostname, message);

		//Is there a prefix?
		if (!contPre)
			return;

		//Bot activated, start command process
		activateCmd(msgInfo);
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
	protected void onPrivateMessage(String sender, String login, String hostname, String message) {
		runHook(Hooks.onPrivateMessage, new BotEvent(null, sender, login, hostname, message));
		//Because this is a PM, just start going
		activateCmd(new BotEvent(null, sender, login, hostname, message));
	}

	/**
	 * runCommand wrapper, outputs begginin and end to console and catches errors
	 * @param msgInfo BotEvent bean
	 */
	private void activateCmd(BotEvent msgInfo) {
		try {
			runCommand(msgInfo);
		} catch (Exception e) {
			sendMsg(new BotMessage(msgInfo.getChannel(), msgInfo.getSender() + ": ERROR " + e.getMessage()));
			log.error("Run Error", e);
		}
	}

	/**
	 * Executes command
	 *
	 * Command handling takes place here purley for nice output for console. If returned, end tag still shown
	 * @param msgInfo                The BotEvent bean
	 * @throws InvalidCMDException   If command does not exist
	 * @throws AdminException        If command is admin only and user is not admin
	 * @throws NumArgException       If impropper amount of arguments were given
	 */
	private void runCommand(BotEvent msgInfo) throws InvalidCMDException, AdminException, NumArgException {
		//Is bot locked?
		if (botLocked == true && !Controller.instance.adminExists(this, msgInfo)) {
			log.info("Command ignored due to global lock in effect");
			return;
		}

		//Is channel locked?
		if (chanLockList.contains(msgInfo.getChannel()) && !Controller.instance.adminExists(this, msgInfo)) {
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
	 * Executes Hook
	 * @param command The command to run (just the name of the method that is calling this)
	 * @param msgInfo BotEvent bean
	 */
	private void runHook(Hooks command, BotEvent msgInfo) {
		List<PluginType> pluginHooks = new ArrayList<PluginType>();
		for (PluginType curPlugin : Controller.instance.plugins) {
			Hooks curHook = curPlugin.getHook();
			if (curHook != null && curHook == command)
				pluginHooks.add(curPlugin);
		}
		if (pluginHooks.size() > 0) {
			log.trace("Attempting to run hook " + command);
			for (PluginType curPlugin : pluginHooks) {
				msgInfo.setCommand(curPlugin.getName());
				threadPool.execute(new PluginExecutor(this, msgInfo));
			}
		} //Run default command if no hook exists
		else
			switch (command) {
				case onVersion:
					super.onVersion(msgInfo.getSender(), msgInfo.getLogin(), msgInfo.getHostname(), msgInfo.getRawmsg());
					break;
				case onPing:
					super.onPing(msgInfo.getSender(), msgInfo.getLogin(), msgInfo.getHostname(), msgInfo.getChannel(), msgInfo.getRawmsg());
					break;
				case onServerPing:
					super.onServerPing(msgInfo.getRawmsg());
					break;
				case onTime:
					super.onTime(msgInfo.getSender(), msgInfo.getLogin(), msgInfo.getHostname(), msgInfo.getChannel());
					break;
				case onFinger:
					super.onFinger(msgInfo.getChannel(), msgInfo.getSender(), msgInfo.getLogin(), msgInfo.getHostname());
					break;
			}
	}

	/**
	 * This method carries out the actions to be performed when the PircBot
	 * gets disconnected.  This may happen if the PircBot quits from the
	 * server, or if the connection is unexpectedly lost.
	 *  <p>
	 * Disconnection from the IRC server is detected immediately if either
	 * we or the server close the connection normally. If the connection to
	 * the server is lost, but neither we nor the server have explicitly closed
	 * the connection, then it may take a few minutes to detect (this is
	 * commonly referred to as a "ping timeout").
	 *  <p>
	 * If you wish to get your IRC bot to automatically rejoin a server after
	 * the connection has been lost, then this is probably the ideal method to
	 * override to implement such functionality.
	 */
	protected void onDisconnect() {
		runHook(Hooks.onDisconnect, null);
	}

	/**
	 * This method is called when we receive a numeric response from the
	 * IRC server.
	 *  <p>
	 * Numerics in the range from 001 to 099 are used for client-server
	 * connections only and should never travel between servers.  Replies
	 * generated in response to commands are found in the range from 200
	 * to 399.  Error replies are found in the range from 400 to 599.
	 *  <p>
	 * For example, we can use this method to discover the topic of a
	 * channel when we join it.  If we join the channel #test which
	 * has a topic of &quot;I am King of Test&quot; then the response
	 * will be &quot;<code>PircBot #test :I Am King of Test</code>&quot;
	 * with a code of 332 to signify that this is a topic.
	 * (This is just an example - note that overriding the
	 * <code>onTopic</code> method is an easier way of finding the
	 * topic for a channel). Check the IRC RFC for the full list of other
	 * command response codes.
	 *  <p>
	 * PircBot implements the interface ReplyConstants, which contains
	 * contstants that you may find useful here.
	 *
	 * @param code The three-digit numerical code for the response. Stored as BotEvent.extra
	 * @param response The full response from the IRC server. Stored as BotEvent.rawmsg
	 *
	 * @see ReplyConstants
	 */
	protected void onServerResponse(int code, String response) {
		runHook(Hooks.onServerResponse, new BotEvent(response, code));
	}

	/**
	 * This method is called when we receive a user list from the server
	 * after joining a channel.
	 *  <p>
	 * Shortly after joining a channel, the IRC server sends a list of all
	 * users in that channel. The PircBot collects this information and
	 * calls this method as soon as it has the full list.
	 *  <p>
	 * To obtain the nick of each user in the channel, call the getNick()
	 * method on each User object in the array.
	 *  <p>
	 * At a later time, you may call the getUsers method to obtain an
	 * up to date list of the users in the channel.
	 *
	 * @param channel The name of the channel. Stored as BotEvent.rawmsg
	 * @param users An array of User objects belonging to this channel. Stored as BotEvent.extra
	 *
	 * @see User
	 */
	protected void onUserList(String channel, User[] users) {
		runHook(Hooks.onUserList, new BotEvent(channel, users));
	}

	/**
	 * This method is called whenever an ACTION is sent from a user.  E.g.
	 * such events generated by typing "/me goes shopping" in most IRC clients.
	 *
	 * @param sender The nick of the user that sent the action.
	 * @param login The login of the user that sent the action.
	 * @param hostname The hostname of the user that sent the action.
	 * @param target The target of the action, be it a channel or our nick.
	 * @param action The action carried out by the user.
	 */
	protected void onAction(String sender, String login, String hostname, String target, String action) {
		runHook(Hooks.onAction, new BotEvent(target, sender, login, hostname, action));
	}

	/**
	 * This method is called whenever we receive a notice.
	 *
	 * @param sourceNick The nick of the user that sent the notice.
	 * @param sourceLogin The login of the user that sent the notice.
	 * @param sourceHostname The hostname of the user that sent the notice.
	 * @param target The target of the notice, be it our nick or a channel name.
	 * @param notice The notice message.
	 */
	protected void onNotice(String sourceNick, String sourceLogin, String sourceHostname, String target, String notice) {
		runHook(Hooks.onNotice, new BotEvent(target, sourceNick, sourceLogin, sourceHostname, notice));
	}

	/**
	 * This method is called whenever someone (possibly us) joins a channel
	 * which we are on.
	 *
	 * @param channel The channel which somebody joined.
	 * @param sender The nick of the user who joined the channel.
	 * @param login The login of the user who joined the channel.
	 * @param hostname The hostname of the user who joined the channel.
	 */
	protected void onJoin(String channel, String sender, String login, String hostname) {
		runHook(Hooks.onJoin, new BotEvent(channel, sender, login, hostname, null));
	}

	/**
	 * This method is called whenever someone (possibly us) parts a channel
	 * which we are on.
	 *
	 * @param channel The channel which somebody parted from.
	 * @param sender The nick of the user who parted from the channel.
	 * @param login The login of the user who parted from the channel.
	 * @param hostname The hostname of the user who parted from the channel.
	 */
	protected void onPart(String channel, String sender, String login, String hostname) {
		runHook(Hooks.onPart, new BotEvent(channel, sender, login, hostname, null));
	}

	/**
	 * This method is called whenever someone (possibly us) changes nick on any
	 * of the channels that we are on.
	 *
	 * @param oldNick The old nick.
	 * @param login The login of the user.
	 * @param hostname The hostname of the user.
	 * @param newNick The new nick. Stored as BotEvent.rawmsg
	 */
	protected void onNickChange(String oldNick, String login, String hostname, String newNick) {
		runHook(Hooks.onNickChange, new BotEvent(login, newNick, login, hostname, newNick));
	}

	/**
	 * This method is called whenever someone (possibly us) is kicked from
	 * any of the channels that we are in.
	 *
	 * @param channel The channel from which the recipient was kicked.
	 * @param kickerNick The nick of the user who performed the kick. Stored as BotEvent.sender
	 * @param kickerLogin The login of the user who performed the kick. Stored as BotEvent.login
	 * @param kickerHostname The hostname of the user who performed the kick. Stored as BotEvent.hostName
	 * @param recipientNick The unfortunate recipient of the kick. Stored as BotEvent.rawmsg
	 * @param reason The reason given by the user who performed the kick. Stored as BotEvent.extra
	 */
	protected void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason) {
		runHook(Hooks.onKick, new BotEvent(channel, kickerNick, kickerLogin, kickerHostname, recipientNick, reason));
	}

	/**
	 * This method is called whenever someone (possibly us) quits from the
	 * server.  We will only observe this if the user was in one of the
	 * channels to which we are connected.
	 *
	 * @param sourceNick The nick of the user that quit from the server.
	 * @param sourceLogin The login of the user that quit from the server.
	 * @param sourceHostname The hostname of the user that quit from the server.
	 * @param reason The reason given for quitting the server.
	 */
	protected void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) {
		runHook(Hooks.onQuit, new BotEvent(null, sourceNick, sourceLogin, sourceHostname, reason));
	}

	/**
	 * This method is called whenever a user sets the topic, or when
	 * PircBot joins a new channel and discovers its topic.
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
		runHook(Hooks.onTopic, new BotEvent(channel, setBy, null, null, topic, date, changed));
	}

	/**
	 * After calling the listChannels() method in PircBot, the server
	 * will start to send us information about each channel on the
	 * server.  You may override this method in order to receive the
	 * information about each channel as soon as it is received.
	 *  <p>
	 * Note that certain channels, such as those marked as hidden,
	 * may not appear in channel listings.
	 *
	 * @param channel The name of the channel.
	 * @param userCount The number of users visible in this channel. Stored as BotEvent.extra
	 * @param topic The topic for this channel. Stored as BotEvent.rawmsg
	 *
	 * @see #listChannels() listChannels
	 */
	protected void onChannelInfo(String channel, int userCount, String topic) {
		runHook(Hooks.onChannelInfo, new BotEvent(channel, null, null, null, topic, userCount));
	}

	/**
	 * Called when the mode of a channel is set.
	 *  <p>
	 * You may find it more convenient to decode the meaning of the mode
	 * string by overriding the onOp, onDeOp, onVoice, onDeVoice,
	 * onChannelKey, onDeChannelKey, onChannelLimit, onDeChannelLimit,
	 * onChannelBan or onDeChannelBan methods as appropriate.
	 *
	 * @param channel The channel that the mode operation applies to.
	 * @param sourceNick The nick of the user that set the mode.
	 * @param sourceLogin The login of the user that set the mode.
	 * @param sourceHostname The hostname of the user that set the mode.
	 * @param mode The mode that has been set. Stored as BotEvent.rawmsg
	 *
	 */
	protected void onMode(String channel, String sourceNick, String sourceLogin, String sourceHostname, String mode) {
		runHook(Hooks.onMode, new BotEvent(channel, sourceNick, sourceLogin, sourceHostname, mode));
	}

	/**
	 * Called when the mode of a user is set.
	 *
	 * @param targetNick The nick that the mode operation applies to. Stored as BotEvent.extra
	 * @param sourceNick The nick of the user that set the mode.
	 * @param sourceLogin The login of the user that set the mode.
	 * @param sourceHostname The hostname of the user that set the mode.
	 * @param mode The mode that has been set. Stored as BotEvent.rawmsg
	 *
	 */
	protected void onUserMode(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String mode) {
		runHook(Hooks.onUserMode, new BotEvent(null, sourceNick, sourceLogin, sourceHostname, mode, targetNick));
	}

	/**
	 * Called when a user (possibly us) gets granted operator status for a channel.
	 *  <p>
	 * This is a type of mode change and is also passed to the onMode
	 * method in the PircBot class.
	 *
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 * @param recipient The nick of the user that got 'opped'. Stored as BotEvent.rawmsg
	 */
	protected void onOp(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
		runHook(Hooks.onOp, new BotEvent(channel, sourceNick, sourceLogin, sourceHostname, recipient));
	}

	/**
	 * Called when a user (possibly us) gets operator status taken away.
	 *  <p>
	 * This is a type of mode change and is also passed to the onMode
	 * method in the PircBot class.
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 * @param recipient The nick of the user that got 'deopped'. Stored as BotEvent.rawmsg
	 */
	protected void onDeop(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
		runHook(Hooks.onDeop, new BotEvent(channel, sourceNick, sourceLogin, sourceHostname, recipient));
	}

	/**
	 * Called when a user (possibly us) gets voice status granted in a channel.
	 *  <p>
	 * This is a type of mode change and is also passed to the onMode
	 * method in the PircBot class.
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 * @param recipient The nick of the user that got 'voiced'. Stored as BotEvent.rawmsg
	 */
	protected void onVoice(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
		runHook(Hooks.onVoice, new BotEvent(channel, sourceNick, sourceLogin, sourceHostname, recipient));
	}

	/**
	 * Called when a user (possibly us) gets voice status removed.
	 *  <p>
	 * This is a type of mode change and is also passed to the onMode
	 * method in the PircBot class.
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 * @param recipient The nick of the user that got 'devoiced'. Stored as BotEvent.rawmsg
	 */
	protected void onDeVoice(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
		runHook(Hooks.onDeVoice, new BotEvent(channel, sourceNick, sourceLogin, sourceHostname, recipient));
	}

	/**
	 * Called when a channel key is set.  When the channel key has been set,
	 * other users may only join that channel if they know the key.  Channel keys
	 * are sometimes referred to as passwords.
	 *  <p>
	 * This is a type of mode change and is also passed to the onMode
	 * method in the PircBot class.
	 *

	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 * @param key The new key for the channel. Stored as BotEvent.key
	 */
	protected void onSetChannelKey(String channel, String sourceNick, String sourceLogin, String sourceHostname, String key) {
		runHook(Hooks.onSetChannelKey, new BotEvent(channel, sourceNick, sourceLogin, sourceHostname, key));
	}

	/**
	 * Called when a channel key is removed.
	 *  <p>
	 * This is a type of mode change and is also passed to the onMode
	 * method in the PircBot class.
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 * @param key The key that was in use before the channel key was removed. Stored as BotEvent.key
	 */
	protected void onRemoveChannelKey(String channel, String sourceNick, String sourceLogin, String sourceHostname, String key) {
		runHook(Hooks.onRemoveChannelKey, new BotEvent(channel, sourceNick, sourceLogin, sourceHostname, key));
	}

	/**
	 * Called when a user limit is set for a channel.  The number of users in
	 * the channel cannot exceed this limit.
	 *  <p>
	 * This is a type of mode change and is also passed to the onMode
	 * method in the PircBot class.
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 * @param limit The maximum number of users that may be in this channel at the same time. Stored as BotEvent.extra
	 */
	protected void onSetChannelLimit(String channel, String sourceNick, String sourceLogin, String sourceHostname, int limit) {
		runHook(Hooks.onSetChannelLimit, new BotEvent(channel, sourceNick, sourceLogin, sourceHostname, null, limit));
	}

	/**
	 * Called when the user limit is removed for a channel.
	 *  <p>
	 * This is a type of mode change and is also passed to the onMode
	 * method in the PircBot class.
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 */
	protected void onRemoveChannelLimit(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		runHook(Hooks.onRemoveChannelLimit, new BotEvent(channel, sourceNick, sourceLogin, sourceHostname, null));
	}

	/**
	 * Called when a user (possibly us) gets banned from a channel.  Being
	 * banned from a channel prevents any user with a matching hostmask from
	 * joining the channel.  For this reason, most bans are usually directly
	 * followed by the user being kicked :-)
	 *  <p>
	 * This is a type of mode change and is also passed to the onMode
	 * method in the PircBot class.
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 * @param hostmask The hostmask of the user that has been banned. Stored  as BotEvent.rawmsg
	 */
	protected void onSetChannelBan(String channel, String sourceNick, String sourceLogin, String sourceHostname, String hostmask) {
		runHook(Hooks.onSetChannelBan, new BotEvent(channel, sourceNick, sourceLogin, sourceHostname, hostmask));
	}

	/**
	 * Called when a hostmask ban is removed from a channel.
	 *  <p>
	 * This is a type of mode change and is also passed to the onMode
	 * method in the PircBot class.
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 * @param hostmask The hostmask of the user that has been banned. Stored  as BotEvent.rawmsg
	 */
	protected void onRemoveChannelBan(String channel, String sourceNick, String sourceLogin, String sourceHostname, String hostmask) {
		runHook(Hooks.onRemoveChannelBan, new BotEvent(channel, sourceNick, sourceLogin, sourceHostname, hostmask));
	}

	/**
	 * Called when topic protection is enabled for a channel.  Topic protection
	 * means that only operators in a channel may change the topic.
	 *  <p>
	 * This is a type of mode change and is also passed to the onMode
	 * method in the PircBot class.
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 */
	protected void onSetTopicProtection(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		runHook(Hooks.onSetTopicProtection, new BotEvent(channel, sourceNick, sourceLogin, sourceHostname, null));
	}

	/**
	 * Called when topic protection is removed for a channel.
	 *  <p>
	 * This is a type of mode change and is also passed to the onMode
	 * method in the PircBot class.
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 */
	protected void onRemoveTopicProtection(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		runHook(Hooks.onRemoveTopicProtection, new BotEvent(channel, sourceNick, sourceLogin, sourceHostname, null));
	}

	/**
	 * Called when a channel is set to only allow messages from users that
	 * are in the channel.
	 *  <p>
	 * This is a type of mode change and is also passed to the onMode
	 * method in the PircBot class.
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 */
	protected void onSetNoExternalMessages(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		runHook(Hooks.onSetNoExternalMessages, new BotEvent(channel, sourceNick, sourceLogin, sourceHostname, null));
	}

	/**
	 * Called when a channel is set to allow messages from any user, even
	 * if they are not actually in the channel.
	 *  <p>
	 * This is a type of mode change and is also passed to the onMode
	 * method in the PircBot class.
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 */
	protected void onRemoveNoExternalMessages(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		runHook(Hooks.onRemoveNoExternalMessages, new BotEvent(channel, sourceNick, sourceLogin, sourceHostname, null));
	}

	/**
	 * Called when a channel is set to 'invite only' mode.  A user may only
	 * join the channel if they are invited by someone who is already in the
	 * channel.
	 *  <p>
	 * This is a type of mode change and is also passed to the onMode
	 * method in the PircBot class.
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 */
	protected void onSetInviteOnly(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		runHook(Hooks.onSetInviteOnly, new BotEvent(channel, sourceNick, sourceLogin, sourceHostname, null));
	}

	/**
	 * Called when a channel has 'invite only' removed.
	 *  <p>
	 * This is a type of mode change and is also passed to the onMode
	 * method in the PircBot class.
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 */
	protected void onRemoveInviteOnly(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		runHook(Hooks.onRemoveInviteOnly, new BotEvent(channel, sourceNick, sourceLogin, sourceHostname, null));
	}

	/**
	 * Called when a channel is set to 'moderated' mode.  If a channel is
	 * moderated, then only users who have been 'voiced' or 'opped' may speak
	 * or change their nicks.
	 *  <p>
	 * This is a type of mode change and is also passed to the onMode
	 * method in the PircBot class.
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 */
	protected void onSetModerated(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		runHook(Hooks.onSetModerated, new BotEvent(channel, sourceNick, sourceLogin, sourceHostname, null));
	}

	/**
	 * Called when a channel has moderated mode removed.
	 *  <p>
	 * This is a type of mode change and is also passed to the onMode
	 * method in the PircBot class.
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 */
	protected void onRemoveModerated(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		runHook(Hooks.onRemoveModerated, new BotEvent(channel, sourceNick, sourceLogin, sourceHostname, null));
	}

	/**
	 * Called when a channel is marked as being in private mode.
	 *  <p>
	 * This is a type of mode change and is also passed to the onMode
	 * method in the PircBot class.
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 */
	protected void onSetPrivate(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		runHook(Hooks.onSetPrivate, new BotEvent(channel, sourceNick, sourceLogin, sourceHostname, null));
	}

	/**
	 * Called when a channel is marked as not being in private mode.
	 *  <p>
	 * This is a type of mode change and is also passed to the onMode
	 * method in the PircBot class.
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 */
	protected void onRemovePrivate(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		runHook(Hooks.onRemovePrivate, new BotEvent(channel, sourceNick, sourceLogin, sourceHostname, null));
	}

	/**
	 * Called when a channel is set to be in 'secret' mode.  Such channels
	 * typically do not appear on a server's channel listing.
	 *  <p>
	 * This is a type of mode change and is also passed to the onMode
	 * method in the PircBot class.
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 */
	protected void onSetSecret(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		runHook(Hooks.onSetSecret, new BotEvent(channel, sourceNick, sourceLogin, sourceHostname, null));
	}

	/**
	 * Called when a channel has 'secret' mode removed.
	 *  <p>
	 * This is a type of mode change and is also passed to the onMode
	 * method in the PircBot class.
	 *
	 * @param channel The channel in which the mode change took place.
	 * @param sourceNick The nick of the user that performed the mode change.
	 * @param sourceLogin The login of the user that performed the mode change.
	 * @param sourceHostname The hostname of the user that performed the mode change.
	 */
	protected void onRemoveSecret(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
		runHook(Hooks.onRemoveSecret, new BotEvent(channel, sourceNick, sourceLogin, sourceHostname, null));
	}

	/**
	 * Called when we are invited to a channel by a user.
	 *
	 * @param targetNick The nick of the user being invited - should be us!
	 * @param sourceNick The nick of the user that sent the invitation.
	 * @param sourceLogin The login of the user that sent the invitation.
	 * @param sourceHostname The hostname of the user that sent the invitation.
	 * @param channel The channel that we're being invited to.
	 */
	protected void onInvite(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String channel) {
		runHook(Hooks.onInvite, new BotEvent(channel, sourceNick, sourceLogin, sourceHostname, null));
	}

	/**
	 * This method is called whenever a DCC SEND request is sent to the PircBot.
	 * This means that a client has requested to send a file to us.
	 * This abstract implementation performs no action, which means that all
	 * DCC SEND requests will be ignored by default. If you wish to receive
	 * the file, then you may override this method and call the receive method
	 * on the DccFileTransfer object, which connects to the sender and downloads
	 * the file.
	 *  <p>
	 * Example:
	 * <pre> public void onIncomingFileTransfer(DccFileTransfer transfer) {
	 *     // Use the suggested file name.
	 *     File file = transfer.getFile();
	 *     // Receive the transfer and save it to the file, allowing resuming.
	 *     transfer.receive(file, true);
	 * }</pre>
	 *  <p>
	 * <b>Warning:</b> Receiving an incoming file transfer will cause a file
	 * to be written to disk. Please ensure that you make adequate security
	 * checks so that this file does not overwrite anything important!
	 *  <p>
	 * Each time a file is received, it happens within a new Thread
	 * in order to allow multiple files to be downloaded by the PircBot
	 * at the same time.
	 *  <p>
	 * If you allow resuming and the file already partly exists, it will
	 * be appended to instead of overwritten.  If resuming is not enabled,
	 * the file will be overwritten if it already exists.
	 *  <p>
	 * You can throttle the speed of the transfer by calling the setPacketDelay
	 * method on the DccFileTransfer object, either before you receive the
	 * file or at any moment during the transfer.
	 *
	 * @param transfer The DcccFileTransfer that you may accept. Stored as BotEvent.extra
	 *
	 * @see DccFileTransfer
	 *
	 */
	protected void onIncomingFileTransfer(DccFileTransfer transfer) {
		runHook(Hooks.onIncomingFileTransfer, new BotEvent(null, transfer));
	}

	/**
	 * This method gets called when a DccFileTransfer has finished.
	 * If there was a problem, the Exception will say what went wrong.
	 * If the file was sent successfully, the Exception will be null.
	 *  <p>
	 * Both incoming and outgoing file transfers are passed to this method.
	 * You can determine the type by calling the isIncoming or isOutgoing
	 * methods on the DccFileTransfer object.
	 *
	 * @param transfer The DccFileTransfer that has finished. Stored as BotEvent.extra
	 * @param e null if the file was transfered successfully, otherwise this
	 *          will report what went wrong. Stored as BotEvent.extra1
	 *
	 * @see DccFileTransfer
	 *
	 */
	protected void onFileTransferFinished(DccFileTransfer transfer, Exception e) {
		runHook(Hooks.onFileTransferFinished, new BotEvent(null, transfer, e));
	}

	/**
	 * This method will be called whenever a DCC Chat request is received.
	 * This means that a client has requested to chat to us directly rather
	 * than via the IRC server. This is useful for sending many lines of text
	 * to and from the bot without having to worry about flooding the server
	 * or any operators of the server being able to "spy" on what is being
	 * said. This abstract implementation performs no action, which means
	 * that all DCC CHAT requests will be ignored by default.
	 *  <p>
	 * If you wish to accept the connection, then you may override this
	 * method and call the accept() method on the DccChat object, which
	 * connects to the sender of the chat request and allows lines to be
	 * sent to and from the bot.
	 *  <p>
	 * Your bot must be able to connect directly to the user that sent the
	 * request.
	 *  <p>
	 * Example:
	 * <pre> public void onIncomingChatRequest(DccChat chat) {
	 *     try {
	 *         // Accept all chat, whoever it's from.
	 *         chat.accept();
	 *         chat.sendLine("Hello");
	 *         String response = chat.readLine();
	 *         chat.close();
	 *     }
	 *     catch (IOException e) {}
	 * }</pre>
	 *
	 * Each time this method is called, it is called from within a new Thread
	 * so that multiple DCC CHAT sessions can run concurrently.
	 *
	 * @param chat A DccChat object that represents the incoming chat request. Stored as BotEvent.extra
	 *
	 * @see DccChat
	 *
	 */
	protected void onIncomingChatRequest(DccChat chat) {
		runHook(Hooks.onIncomingChatRequest, new BotEvent(null, chat));
	}

	/**
	 * This method is called whenever we receive a VERSION request.
	 * This abstract implementation responds with the PircBot's _version string,
	 * so if you override this method, be sure to either mimic its functionality
	 * or to call super.onVersion(...);
	 * <p>
	 * <b>WARNING:</b> By hooking this it is up to the Hooking to send the Version response.
	 * default version response is <b>not</b> sent.
	 * @param sourceNick The nick of the user that sent the VERSION request.
	 * @param sourceLogin The login of the user that sent the VERSION request.
	 * @param sourceHostname The hostname of the user that sent the VERSION request.
	 * @param target The target of the VERSION request, be it our nick or a channel name.
	 */
	protected void onVersion(String sourceNick, String sourceLogin, String sourceHostname, String target) {
		runHook(Hooks.onVersion, new BotEvent(target, sourceNick, sourceLogin, sourceHostname, null));
	}

	/**
	 * This method is called whenever we receive a PING request from another
	 * user.
	 *  <p>
	 * This abstract implementation responds correctly, so if you override this
	 * method, be sure to either mimic its functionality or to call
	 * super.onPing(...);
	 *  <p>
	 * <b>WARNING:</b> By hooking this it is up to the Hooking to send the ping response.
	 * default ping response is <b>not</b> sent.
	 * @param sourceNick The nick of the user that sent the PING request.
	 * @param sourceLogin The login of the user that sent the PING request.
	 * @param sourceHostname The hostname of the user that sent the PING request.
	 * @param target The target of the PING request, be it our nick or a channel name.
	 * @param pingValue The value that was supplied as an argument to the PING command. Stored as BotEvent.rawmsg
	 */
	protected void onPing(String sourceNick, String sourceLogin, String sourceHostname, String target, String pingValue) {
		runHook(Hooks.onPing, new BotEvent(target, sourceNick, sourceLogin, sourceHostname, pingValue));
	}

	/**
	 * The actions to perform when a PING request comes from the server.
	 *  <p>
	 * This sends back a correct response, so if you override this method,
	 * be sure to either mimic its functionality or to call
	 * super.onServerPing(response);
	 *  <p>
	 * <b>WARNING:</b> By hooking this it is up to the Hooking to send the PONG response.
	 * PONG version response is <b>not</b> sent. Failure to implement this will make
	 * server think the bot is disconnected
	 * @param response The response that should be given back in your PONG. Stored as BotEvent.rawmsg
	 */
	protected void onServerPing(String response) {
		runHook(Hooks.onServerPing, new BotEvent(response, null));
	}

	/**
	 * This method is called whenever we receive a TIME request.
	 *  <p>
	 * This abstract implementation responds correctly, so if you override this
	 * method, be sure to either mimic its functionality or to call
	 * super.onTime(...);
	 *  <p>
	 * <b>WARNING:</b> By hooking this it is up to the Hooking to send the time response.
	 * default time response is <b>not</b> sent.
	 * @param sourceNick The nick of the user that sent the TIME request.
	 * @param sourceLogin The login of the user that sent the TIME request.
	 * @param sourceHostname The hostname of the user that sent the TIME request.
	 * @param target The target of the TIME request, be it our nick or a channel name.
	 */
	protected void onTime(String sourceNick, String sourceLogin, String sourceHostname, String target) {
		runHook(Hooks.onTime, new BotEvent(target, sourceNick, sourceLogin, sourceHostname, null));
	}

	/**
	 * This method is called whenever we receive a FINGER request.
	 *  <p>
	 * This abstract implementation responds correctly, so if you override this
	 * method, be sure to either mimic its functionality or to call
	 * super.onFinger(...);
	 *  <p>
	 * <b>WARNING:</b> By hooking this it is up to the Hooking to send the finger response.
	 * default finger response is <b>not</b> sent.
	 * @param sourceNick The nick of the user that sent the FINGER request.
	 * @param sourceLogin The login of the user that sent the FINGER request.
	 * @param sourceHostname The hostname of the user that sent the FINGER request.
	 * @param target The target of the FINGER request, be it our nick or a channel name.
	 */
	protected void onFinger(String sourceNick, String sourceLogin, String sourceHostname, String target) {
		runHook(Hooks.onFinger, new BotEvent(target, sourceNick, sourceLogin, sourceHostname, null));
	}

	/**
	 * This method is called whenever we receive a line from the server that
	 * the PircBot has not been programmed to recognise.
	 *
	 * @param line The raw line that was received from the server. Stored as BotEvent.rawmsg
	 */
	protected void onUnknown(String line) {
		runHook(Hooks.onUnknown, new BotEvent(line, null));
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

	public class CustBlockingQueue<E> extends ArrayBlockingQueue<E> {
		public ReentrantLock lock = new ReentrantLock(false);
		public Condition doneProcessing = lock.newCondition();

		public CustBlockingQueue(int capacity) {
			super(capacity);
		}

		public void put(E e) throws InterruptedException {
			lock.lockInterruptibly();
			super.put(e);
			doneProcessing.await();
			lock.unlock();
		}
	}
}
