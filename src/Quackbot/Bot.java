/**
 * @(#)Bot.java
 *
 * This file is part of Quackbot
 */
package Quackbot;

import Quackbot.err.AdminException;
import Quackbot.err.InvalidCMDException;
import Quackbot.err.NumArgException;

import Quackbot.info.BotMessage;
import Quackbot.info.Channel;
import Quackbot.info.Hooks;
import Quackbot.info.Server;
import Quackbot.info.UserMessage;

import Quackbot.log.BotAppender;
import java.util.ArrayList;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.jibble.pircbot.DccChat;
import org.jibble.pircbot.DccFileTransfer;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.ReplyConstants;

import org.jibble.pircbot.User;

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
	 * Log4J logger
	 */
	private Logger log = Logger.getLogger(Bot.class);

	/**
	 * Init bot by setting all information
	 * @param serverDB   The persistent server object from database
	 */
	public Bot(Server serverDB) {
		this.serverDB = serverDB;
		log.addAppender(new BotAppender(serverDB.getAddress()));

		setName("Quackbot");
		setAutoNickChange(true);
		setFinger("Quackbot IRC bot by Lord.Quackstar. Source: http://quackbot.googlecode.com/");
		setMessageDelay(500);
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
	 * Bot error stream wrapper, prefixes with server
	 * @param line   Line to be outputted
	 */
	public void logErr(String line) {
		log.error(line);
	}

	/**
	 * This method is called once the PircBot has successfully connected to
	 * the IRC server.
	 *
	 * @since PircBot 0.9.6 & Quackbot 3.0
	 */
	@Override
	public void onConnect() {
		runHook(Hooks.onConnect, null);

		//Init prefixes (put here in order to prevent MOTD from tripping bot)
		PREFIXES.add("?");
		PREFIXES.add(getNick() + ":");
		PREFIXES.add(getNick());

		List<Channel> channels = serverDB.getChannels();
		log.debug("Channel length: " + channels.size());
		for (Channel curChannel : channels) {
			joinChannel(curChannel.getChannel(), curChannel.getPassword());
			log.debug("Trying to join channel using " + curChannel);
		}
	}

	/**
	 * <b>Main bot output</b> All commands should use this to communicate with server
	 * @param msg Message to send
	 */
	public void sendMsg(BotMessage msg) {
		sendMessage(msg.channel, msg.toString());
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
	public void onMessage(String channel, String sender, String login, String hostname, String message) {
		runHook(Hooks.onMessage, new UserMessage(channel, sender, login, hostname, message));
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

		//Create UserMessage from modified message
		UserMessage msgInfo = new UserMessage(channel, sender, login, hostname, message);

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
	public void onPrivateMessage(String sender, String login, String hostname, String message) {
		runHook(Hooks.onPrivateMessage, new UserMessage(null, sender, login, hostname, message));
		//Because this is a PM, just start going
		activateCmd(new UserMessage(null, sender, login, hostname, message));
	}

	/**
	 * runCommand wrapper, outputs begginin and end to console and catches errors
	 * @param msgInfo UserMessage bean
	 */
	private void activateCmd(UserMessage msgInfo) {
		try {
			runCommand(msgInfo);
		} catch (Exception e) {
			sendMessage(msgInfo.getChannel(), msgInfo.getSender() + ": ERROR " + e.getMessage());
			log.error("Run Error", e);
		}
	}

	/**
	 * Executes command
	 *
	 * Command handling takes place here purley for nice output for console. If returned, end tag still shown
	 * @param msgInfo                The UserMessage bean
	 * @throws InvalidCMDException   If command does not exist
	 * @throws AdminException        If command is admin only and user is not admin
	 * @throws NumArgException       If impropper amount of arguments were given
	 */
	private void runCommand(UserMessage msgInfo) throws InvalidCMDException, AdminException, NumArgException {
		//Is bot locked?
		if (botLocked == true && !serverDB.adminExists(msgInfo.getSender())) {
			log.info("Command ignored due to global lock in effect");
			return;
		}

		//Is channel locked?
		if (chanLockList.contains(msgInfo.getChannel()) && !serverDB.adminExists(msgInfo.getSender())) {
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

		//Build UserMessage bean
		msgInfo.setArgs(argArray);
		msgInfo.setCommand(command);

		ThreadPoolManager.addPlugin(new PluginExecutor(this, msgInfo));
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
			sendMessage(curChan, msg);
	}

	/****************************************HOOKS************************************************************/
	/**
	 * Executes Hook
	 * @param command The command to run (just the name of the method that is calling this)
	 * @param msgInfo UserMessage bean
	 */
	public void runHook(Hooks command, UserMessage msgInfo) {
		List<PluginType> pluginHooks = new ArrayList<PluginType>();
		for (PluginType curPlugin : InstanceTracker.getController().plugins) {
			Hooks curHook = curPlugin.getHook();
			if (curHook != null && curHook == command)
				pluginHooks.add(curPlugin);
		}
		if (pluginHooks.size() > 0) {
			log.trace("Attempting to run hook " + command);
			for (PluginType curPlugin : pluginHooks) {
				msgInfo.setCommand(curPlugin.getName());
				ThreadPoolManager.addPlugin(new PluginExecutor(this, msgInfo));
			}
		} //Run default command if no hook exists
		else
			switch (command) {
				case onVersion:
					super.onVersion( msgInfo.getSender(), msgInfo.getLogin(), msgInfo.getHostname(), msgInfo.getRawmsg());
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
	 * @param code The three-digit numerical code for the response. Stored as UserMessage.extra
	 * @param response The full response from the IRC server. Stored as UserMessage.rawmsg
	 *
	 * @see ReplyConstants
	 */
	protected void onServerResponse(int code, String response) {
		runHook(Hooks.onServerResponse, new UserMessage(response, code));
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
	 * @param channel The name of the channel. Stored as UserMessage.rawmsg
	 * @param users An array of User objects belonging to this channel. Stored as UserMessage.extra
	 *
	 * @see User
	 */
	protected void onUserList(String channel, User[] users) {
		runHook(Hooks.onUserList, new UserMessage(channel, users));
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
		runHook(Hooks.onAction, new UserMessage(target, sender, login, hostname, action));
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
		runHook(Hooks.onNotice, new UserMessage(target, sourceNick, sourceLogin, sourceHostname, notice));
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
		runHook(Hooks.onJoin, new UserMessage(channel, sender, login, hostname, null));
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
		runHook(Hooks.onPart, new UserMessage(channel, sender, login, hostname, null));
	}

	/**
	 * This method is called whenever someone (possibly us) changes nick on any
	 * of the channels that we are on.
	 *
	 * @param oldNick The old nick.
	 * @param login The login of the user.
	 * @param hostname The hostname of the user.
	 * @param newNick The new nick. Stored as UserMessage.rawmsg
	 */
	protected void onNickChange(String oldNick, String login, String hostname, String newNick) {
		runHook(Hooks.onNickChange, new UserMessage(login, newNick, login, hostname, newNick));
	}

	/**
	 * This method is called whenever someone (possibly us) is kicked from
	 * any of the channels that we are in.
	 *
	 * @param channel The channel from which the recipient was kicked.
	 * @param kickerNick The nick of the user who performed the kick. Stored as UserMessage.sender
	 * @param kickerLogin The login of the user who performed the kick. Stored as UserMessage.login
	 * @param kickerHostname The hostname of the user who performed the kick. Stored as UserMessage.hostName
	 * @param recipientNick The unfortunate recipient of the kick. Stored as UserMessage.rawmsg
	 * @param reason The reason given by the user who performed the kick. Stored as UserMessage.extra
	 */
	protected void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason) {
		runHook(Hooks.onKick, new UserMessage(channel, kickerNick, kickerLogin, kickerHostname, recipientNick, reason));
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
		runHook(Hooks.onQuit, new UserMessage(null, sourceNick, sourceLogin, sourceHostname, reason));
	}

	/**
	 * This method is called whenever a user sets the topic, or when
	 * PircBot joins a new channel and discovers its topic.
	 *
	 * @param channel The channel that the topic belongs to.
	 * @param topic The topic for the channel. Stored as UserMessage.rawmsg
	 * @param setBy The nick of the user that set the topic. Stored as UserMessage.sender
	 * @param date When the topic was set (milliseconds since the epoch). Stored as UserMessage.extra
	 * @param changed True if the topic has just been changed, false if
	 *                the topic was already there. Stored as UserMessage.extra1
	 *
	 */
	protected void onTopic(String channel, String topic, String setBy, long date, boolean changed) {
		runHook(Hooks.onTopic, new UserMessage(channel, setBy, null, null, topic, date, changed));
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
	 * @param userCount The number of users visible in this channel. Stored as UserMessage.extra
	 * @param topic The topic for this channel. Stored as UserMessage.rawmsg
	 *
	 * @see #listChannels() listChannels
	 */
	protected void onChannelInfo(String channel, int userCount, String topic) {
		runHook(Hooks.onChannelInfo, new UserMessage(channel, null, null, null, topic, userCount));
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
	 * @param mode The mode that has been set. Stored as UserMessage.rawmsg
	 *
	 */
	protected void onMode(String channel, String sourceNick, String sourceLogin, String sourceHostname, String mode) {
		runHook(Hooks.onMode, new UserMessage(channel, sourceNick, sourceLogin, sourceHostname, mode));
	}

	/**
	 * Called when the mode of a user is set.
	 *
	 * @param targetNick The nick that the mode operation applies to. Stored as UserMessage.extra
	 * @param sourceNick The nick of the user that set the mode.
	 * @param sourceLogin The login of the user that set the mode.
	 * @param sourceHostname The hostname of the user that set the mode.
	 * @param mode The mode that has been set. Stored as UserMessage.rawmsg
	 *
	 */
	protected void onUserMode(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String mode) {
		runHook(Hooks.onUserMode, new UserMessage(null, sourceNick, sourceLogin, sourceHostname, mode, targetNick));
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
	 * @param recipient The nick of the user that got 'opped'. Stored as UserMessage.rawmsg
	 */
	protected void onOp(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
		runHook(Hooks.onOp, new UserMessage(channel, sourceNick, sourceLogin, sourceHostname, recipient));
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
	 * @param recipient The nick of the user that got 'deopped'. Stored as UserMessage.rawmsg
	 */
	protected void onDeop(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
		runHook(Hooks.onDeop, new UserMessage(channel, sourceNick, sourceLogin, sourceHostname, recipient));
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
	 * @param recipient The nick of the user that got 'voiced'. Stored as UserMessage.rawmsg
	 */
	protected void onVoice(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
		runHook(Hooks.onVoice, new UserMessage(channel, sourceNick, sourceLogin, sourceHostname, recipient));
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
	 * @param recipient The nick of the user that got 'devoiced'. Stored as UserMessage.rawmsg
	 */
	protected void onDeVoice(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
		runHook(Hooks.onDeVoice, new UserMessage(channel, sourceNick, sourceLogin, sourceHostname, recipient));
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
	 * @param key The new key for the channel. Stored as UserMessage.key
	 */
	protected void onSetChannelKey(String channel, String sourceNick, String sourceLogin, String sourceHostname, String key) {
		runHook(Hooks.onSetChannelKey, new UserMessage(channel, sourceNick, sourceLogin, sourceHostname, key));
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
	 * @param key The key that was in use before the channel key was removed. Stored as UserMessage.key
	 */
	protected void onRemoveChannelKey(String channel, String sourceNick, String sourceLogin, String sourceHostname, String key) {
		runHook(Hooks.onRemoveChannelKey, new UserMessage(channel, sourceNick, sourceLogin, sourceHostname, key));
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
	 * @param limit The maximum number of users that may be in this channel at the same time. Stored as UserMessage.extra
	 */
	protected void onSetChannelLimit(String channel, String sourceNick, String sourceLogin, String sourceHostname, int limit) {
		runHook(Hooks.onSetChannelLimit, new UserMessage(channel, sourceNick, sourceLogin, sourceHostname, null, limit));
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
		runHook(Hooks.onRemoveChannelLimit, new UserMessage(channel, sourceNick, sourceLogin, sourceHostname, null));
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
	 * @param hostmask The hostmask of the user that has been banned. Stored  as UserMessage.rawmsg
	 */
	protected void onSetChannelBan(String channel, String sourceNick, String sourceLogin, String sourceHostname, String hostmask) {
		runHook(Hooks.onSetChannelBan, new UserMessage(channel, sourceNick, sourceLogin, sourceHostname, hostmask));
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
	 * @param hostmask The hostmask of the user that has been banned. Stored  as UserMessage.rawmsg
	 */
	protected void onRemoveChannelBan(String channel, String sourceNick, String sourceLogin, String sourceHostname, String hostmask) {
		runHook(Hooks.onRemoveChannelBan, new UserMessage(channel, sourceNick, sourceLogin, sourceHostname, hostmask));
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
		runHook(Hooks.onSetTopicProtection, new UserMessage(channel, sourceNick, sourceLogin, sourceHostname, null));
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
		runHook(Hooks.onRemoveTopicProtection, new UserMessage(channel, sourceNick, sourceLogin, sourceHostname, null));
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
		runHook(Hooks.onSetNoExternalMessages, new UserMessage(channel, sourceNick, sourceLogin, sourceHostname, null));
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
		runHook(Hooks.onRemoveNoExternalMessages, new UserMessage(channel, sourceNick, sourceLogin, sourceHostname, null));
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
		runHook(Hooks.onSetInviteOnly, new UserMessage(channel, sourceNick, sourceLogin, sourceHostname, null));
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
		runHook(Hooks.onRemoveInviteOnly, new UserMessage(channel, sourceNick, sourceLogin, sourceHostname, null));
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
		runHook(Hooks.onSetModerated, new UserMessage(channel, sourceNick, sourceLogin, sourceHostname, null));
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
		runHook(Hooks.onRemoveModerated, new UserMessage(channel, sourceNick, sourceLogin, sourceHostname, null));
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
		runHook(Hooks.onSetPrivate, new UserMessage(channel, sourceNick, sourceLogin, sourceHostname, null));
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
		runHook(Hooks.onRemovePrivate, new UserMessage(channel, sourceNick, sourceLogin, sourceHostname, null));
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
		runHook(Hooks.onSetSecret, new UserMessage(channel, sourceNick, sourceLogin, sourceHostname, null));
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
		runHook(Hooks.onRemoveSecret, new UserMessage(channel, sourceNick, sourceLogin, sourceHostname, null));
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
		runHook(Hooks.onInvite, new UserMessage(channel, sourceNick, sourceLogin, sourceHostname, null));
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
	 * @param transfer The DcccFileTransfer that you may accept. Stored as UserMessage.extra
	 *
	 * @see DccFileTransfer
	 *
	 */
	protected void onIncomingFileTransfer(DccFileTransfer transfer) {
		runHook(Hooks.onIncomingFileTransfer, new UserMessage(null, transfer));
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
	 * @param transfer The DccFileTransfer that has finished. Stored as UserMessage.extra
	 * @param e null if the file was transfered successfully, otherwise this
	 *          will report what went wrong. Stored as UserMessage.extra1
	 *
	 * @see DccFileTransfer
	 *
	 */
	protected void onFileTransferFinished(DccFileTransfer transfer, Exception e) {
		runHook(Hooks.onFileTransferFinished, new UserMessage(null, transfer, e));
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
	 * @param chat A DccChat object that represents the incoming chat request. Stored as UserMessage.extra
	 *
	 * @see DccChat
	 *
	 */
	protected void onIncomingChatRequest(DccChat chat) {
		runHook(Hooks.onIncomingChatRequest, new UserMessage(null, chat));
	}

	/**
	 * This method is called whenever we receive a VERSION request.
	 * This abstract implementation responds with the PircBot's _version string,
	 * so if you override this method, be sure to either mimic its functionality
	 * or to call super.onVersion(...);
	 * <p>
	 * <b>WARNING:</b> By hooking this it is up to the Hooking to send the Version response.
	 *	The default version response is <b>not</b> sent.
	 * @param sourceNick The nick of the user that sent the VERSION request.
	 * @param sourceLogin The login of the user that sent the VERSION request.
	 * @param sourceHostname The hostname of the user that sent the VERSION request.
	 * @param target The target of the VERSION request, be it our nick or a channel name.
	 */
	protected void onVersion(String sourceNick, String sourceLogin, String sourceHostname, String target) {
		runHook(Hooks.onVersion, new UserMessage(target, sourceNick, sourceLogin, sourceHostname, null));
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
	 *	The default ping response is <b>not</b> sent.
	 * @param sourceNick The nick of the user that sent the PING request.
	 * @param sourceLogin The login of the user that sent the PING request.
	 * @param sourceHostname The hostname of the user that sent the PING request.
	 * @param target The target of the PING request, be it our nick or a channel name.
	 * @param pingValue The value that was supplied as an argument to the PING command. Stored as UserMessage.rawmsg
	 */
	protected void onPing(String sourceNick, String sourceLogin, String sourceHostname, String target, String pingValue) {
		runHook(Hooks.onPing, new UserMessage(target, sourceNick, sourceLogin, sourceHostname, pingValue));
	}

	/**
	 * The actions to perform when a PING request comes from the server.
	 *  <p>
	 * This sends back a correct response, so if you override this method,
	 * be sure to either mimic its functionality or to call
	 * super.onServerPing(response);
	 *  <p>
	 * <b>WARNING:</b> By hooking this it is up to the Hooking to send the PONG response.
	 *	The PONG version response is <b>not</b> sent. Failure to implement this will make
	 *	the server think the bot is disconnected
	 * @param response The response that should be given back in your PONG. Stored as UserMessage.rawmsg
	 */
	protected void onServerPing(String response) {
		runHook(Hooks.onServerPing, new UserMessage(response, null));
	}

	/**
	 * This method is called whenever we receive a TIME request.
	 *  <p>
	 * This abstract implementation responds correctly, so if you override this
	 * method, be sure to either mimic its functionality or to call
	 * super.onTime(...);
	 *  <p>
	 * <b>WARNING:</b> By hooking this it is up to the Hooking to send the time response.
	 *	The default time response is <b>not</b> sent.
	 * @param sourceNick The nick of the user that sent the TIME request.
	 * @param sourceLogin The login of the user that sent the TIME request.
	 * @param sourceHostname The hostname of the user that sent the TIME request.
	 * @param target The target of the TIME request, be it our nick or a channel name.
	 */
	protected void onTime(String sourceNick, String sourceLogin, String sourceHostname, String target) {
		runHook(Hooks.onTime, new UserMessage(target, sourceNick, sourceLogin, sourceHostname, null));
	}

	/**
	 * This method is called whenever we receive a FINGER request.
	 *  <p>
	 * This abstract implementation responds correctly, so if you override this
	 * method, be sure to either mimic its functionality or to call
	 * super.onFinger(...);
	 *  <p>
	 * <b>WARNING:</b> By hooking this it is up to the Hooking to send the finger response.
	 *	The default finger response is <b>not</b> sent.
	 * @param sourceNick The nick of the user that sent the FINGER request.
	 * @param sourceLogin The login of the user that sent the FINGER request.
	 * @param sourceHostname The hostname of the user that sent the FINGER request.
	 * @param target The target of the FINGER request, be it our nick or a channel name.
	 */
	protected void onFinger(String sourceNick, String sourceLogin, String sourceHostname, String target) {
		runHook(Hooks.onFinger, new UserMessage(target, sourceNick, sourceLogin, sourceHostname, null));
	}

	/**
	 * This method is called whenever we receive a line from the server that
	 * the PircBot has not been programmed to recognise.
	 *
	 * @param line The raw line that was received from the server. Stored as UserMessage.rawmsg
	 */
	protected void onUnknown(String line) {
		runHook(Hooks.onUnknown, new UserMessage(line, null));
	}
	//And then it was done :-)
}
