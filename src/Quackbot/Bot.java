/**
 * @(#)Bot.java
 *
 * This file is part of Quackbot
 */
package Quackbot;

import Quackbot.err.AdminException;
import Quackbot.err.InvalidCMDException;
import Quackbot.err.NumArgException;

import Quackbot.info.Admin;
import Quackbot.info.BotMessage;
import Quackbot.info.Channel;
import Quackbot.info.Server;
import Quackbot.info.UserMessage;

import Quackbot.log.BotAppender;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import org.jibble.pircbot.PircBot;
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
	public TreeMap<String, String> chanLockList;
	/**
	 * Current {@link Controller} instance
	 */
	public Controller ctrl = InstanceTracker.getCtrlInst();
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

		//Init channel block list
		chanLockList = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);

		setName("Quackbot");
		setAutoNickChange(true);
		setFinger("Quackbot IRC bot by Lord.Quackstar. Source: http://quackbot.googlecode.com/");
		setMessageDelay(500);
		setVersion("Quackbot 0.5");
		try {
			//Connect to server and join all channels (fetched from db)
			connect(serverDB.getAddress(), 6665);
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
		//Init prefixes (put here in order to prevent MOTD from tripping bot)
		PREFIXES.add("?");
		PREFIXES.add(getNick() + ":");
		PREFIXES.add(getNick());

		updateServer();
		List<Channel> channels = serverDB.getChannels();
		for (Channel curChannel : channels)
			joinChannel(curChannel.getChannel(), curChannel.getPassword());
	}

	/**
	 * Utility method updates persistent server object with new Channel and Admin info from database
	 */
	public void updateServer() {
		try {
			Collection<Channel> channels = ctrl.dbm.loadObjects(new ArrayList<Channel>(), Channel.class);
			for (Channel channel : channels)
				if (channel.getServerID() == serverDB.getServerId())
					serverDB.getChannels().add(channel);
			Collection<Admin> admins = ctrl.dbm.loadObjects(new ArrayList<Admin>(), Admin.class);
			for (Admin curAdmin : admins)
				if (curAdmin.getServerID() == serverDB.getServerId())
					serverDB.getAdmins().add(curAdmin);
		} catch (Exception e) {
			log.fatal("Cannot connect to channels", e);
		}
	}

	/**
	 * <b>Main bot output</b> All commands should use this to communicate with server
	 * @param msg Message to send
	 */
	public void sendMsg(BotMessage msg) {
		sendMessage(msg.channel, msg.toString());
	}

	/*********************LISTENERS FOLLOW************************/
	/**
	 * This method is called whenever someone (possibly us) joins a channel
	 * which we are on.
	 *
	 * @param channel The channel which somebody joined.
	 * @param sender The nick of the user who joined the channel.
	 * @param login The login of the user who joined the channel.
	 * @param hostname The hostname of the user who joined the channel.
	 */
	@Override
	public void onJoin(String channel, String sender, String login, String hostname) {

		runListener("onJoin", new UserMessage(channel, sender, login, hostname, null,"onJoin"));

		//If this is us, add to server info
		if (sender.equalsIgnoreCase(getNick())) {
			//curServer.addChannel(new Channel(channel));
		}
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
	@Override
	public void onPart(String channel, String sender, String login, String hostname) {
		runListener("onPart", new UserMessage(channel, sender, login, hostname, null,"onPart"));

		if (sender.equalsIgnoreCase(getNick())) {
			//curServer.removeChannel(channel);
			//ctrl.JRocm.update(curServer);
		}
	}

	/**
	 * This method is called whenever someone (possibly us) quits from the
	 * server.  We will only observe this if the user was in one of the
	 * channels to which we are connected.
	 *  <p>
	 * The implementation of this method in the PircBot abstract class
	 * performs no actions and may be overridden as required.
	 *
	 * @param sourceNick The nick of the user that quit from the server.
	 * @param sourceLogin The login of the user that quit from the server.
	 * @param sourceHostname The hostname of the user that quit from the server.
	 * @param reason The reason given for quitting the server.
	 */
	@Override
	public void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) {
		runListener("onQuit", new UserMessage(null, sourceNick, sourceLogin, sourceHostname, reason,"onQuit"));
	}

	/**
	 * Executes listener
	 * @param command The command to run (just the name of the method that is calling this)
	 * @param msgInfo UserMessage bean
	 */
	private void runListener(String command, UserMessage msgInfo) {
		log("Attempting to run listener " + command);
		ctrl.threadPool_js.execute(new PluginExecutor(this, msgInfo));
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
		//Look for a prefix
		Iterator preItr = PREFIXES.iterator();
		Boolean contPre = false;
		while (preItr.hasNext()) {
			String curPre = preItr.next().toString();
			if (curPre.length() < message.length() && message.substring(0, curPre.length()).equals(curPre)) {
				contPre = true;
				message = message.substring(curPre.length(), message.length()).trim().toLowerCase();
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
		//Because this is a PM, just start going
		activateCmd(new UserMessage(null, sender, login, hostname, message));
	}

	/**
	 * runCommand wrapper, outputs begginin and end to console and catches errors
	 * @param msgInfo UserMessage bean
	 */
	private void activateCmd(UserMessage msgInfo) {
		log("-----------BOT ACTIVATED FROM " + msgInfo.getRawmsg() + "-----------");
		try {
			runCommand(msgInfo);
		} catch (Exception e) {
			sendMessage(msgInfo.getChannel(), msgInfo.getSender() + ": ERROR " + e.getMessage());
			log.error("Run Error", e);
		}
		log("-----------END BOT ACTIVATED FROM " + msgInfo.getRawmsg() + "-----------");
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
		if (chanLockList.containsKey(msgInfo.getChannel())) {
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

		ctrl.threadPool_js.execute(new PluginExecutor(this, msgInfo));
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
}
