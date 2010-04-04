/**
 * @(#)Bot.java
 *
 * This file is part of Quackbot
 */
package Quackbot;

import Quackbot.info.Channel;
import Quackbot.info.Server;
import Quackbot.log.BotAppender;

import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;

import javax.script.Bindings;
import javax.script.ScriptContext;
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

	public boolean botLocked = false;
	String channel, sender;
	final HashSet<String> PREFIXES = new HashSet<String>();
	public TreeMap<String, String> adminList;
	public TreeMap<String, String> chanLockList;
	public Controller mainInst = null;
	public Server curServer;
	public Logger log = Logger.getLogger(Bot.class);

	/**
	 * Init bot by setting all information
	 * @param mainInstance   The controller instance used to spawn this bot
	 */
	public Bot(Controller mainInstance, String hostname, int port) {
		log.addAppender(new BotAppender(mainInstance.gui,hostname));
		mainInst = mainInstance;
		setName("Quackbot");
		setAutoNickChange(true);
		setFinger("Quackbot IRC bot by Lord.Quackstar. Source: http://github.com/LBlakey/Quackbot");
		setMessageDelay(500);
		setVersion("Quackbot 0.5");
		try {
			connect(hostname, 6665);
		} catch (Exception e) {
			log.error("Error in connecting", e);
		}

	}

	/**
	 * Bot out stream wrapper, prefixes with server
	 * @param line   Line to be outputed
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
	 * Setup bot when fully connected
	 */
	@Override
	public void onConnect() {
		//Add admins
		adminList = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
		adminList.put("LordQuackstar", "True");

		//Init channel block list
		chanLockList = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);

		//Init prefixes
		PREFIXES.add("?");
		PREFIXES.add(getNick() + ":");
		PREFIXES.add(getNick());

		//Get current server database object
		curServer = (Server) mainInst.JRocm.getObject("/servers/" + getServer());
	}

	/*********************LISTENERS FOLLOW************************/
	@Override
	public void onJoin(String channel, String sender, String login, String hostname) {
		runListener("onJoin", channel, sender, login, hostname);

		//If this is us, add to server info
		if (sender.equalsIgnoreCase(getNick())) {
			curServer.addChannel(new Channel(channel));
			mainInst.JRocm.update(curServer);
		}
	}

	@Override
	public void onPart(String channel, String sender, String login, String hostname) {
		runListener("onPart", channel, sender, login, hostname);

		if (sender.equalsIgnoreCase(getNick())) {
			curServer.removeChannel(channel);
			mainInst.JRocm.update(curServer);
		}
	}

	@Override
	public void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) {
		runListener("onQuit", null, sourceNick, sourceLogin, sourceHostname);
	}

	/**
	 * Excecutes listener
	 * @param command   The name of the command
	 * @param channel   The channel which somebody joined.
	 * @param sender    The nick of the user who joined the channel.
	 * @param login     The login of the user who joined the channel.
	 * @param hostname  The hostname of the user who joined the channel.
	 */
	private void runListener(String command, String channel, String sender, String login, String hostname) {
		log("Attempting to run listener " + command);
		if (!mainInst.listeners.containsKey(command)) {
			log.error("Listiner does not exist!!");
			return;
		}
		TreeMap<String, Object> cmdinfo = mainInst.listeners.get(command);

		//Make it aware of a few parameters
		ScriptContext newContext = (ScriptContext) cmdinfo.get("context");
		Bindings engineScope = (Bindings) cmdinfo.get("scope");
		engineScope.put("channel", channel);
		engineScope.put("sender", sender);
		engineScope.put("login", login);
		engineScope.put("hostname", hostname);
		engineScope.put("qb", this);

		//Run command in thread pool
		String jsCmd = "invoke();";
		log.debug("JS cmd: " + jsCmd);
		mainInst.threadPool_js.execute(new threadCmdRun(jsCmd, newContext, this, channel, sender));
	}

	/***************USER SUBMITTED COMMANDS FOLLOW*********************/
	@Override
	public void onMessage(String channel, String sender, String login, String hostname, String message) {
		//Make class aware of a few parameters
		this.channel = channel;
		this.sender = sender;

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

		//Is there a prefix?
		if (!contPre) {
			return;
		}

		//Bot activated, start command process
		activateCmd(channel, sender, login, hostname, message);
	}

	@Override
	public void onPrivateMessage(String sender, String login, String hostname, String message) {
		//Make the class aware of a few parameters
		this.channel = sender;
		this.sender = sender;

		//Because this is a PM, just start going
		activateCmd(sender, sender, login, hostname, message);
	}

	//
	/**
	 * runCommand wrapper, outputs begginin and end to console and catches errors
	 * @param channel     The channel to which the message was sent.
	 * @param sender      The nick of the person who sent the message.
	 * @param login       The login of the person who sent the message.
	 * @param hostname    The hostname of the person who sent the message.
	 * @param message      The actual message sent to the channel.
	 */
	private void activateCmd(String channel, String sender, String login, String hostname, String message) {
		log("-----------BOT ACTIVATED FROM " + message + "-----------");
		try {
			runCommand(channel, sender, login, hostname, message);
		} catch (Exception e) {
			sendMessage(channel, sender + ": RUN ERROR: " + e.toString());
			log.error("Run Error", e);
		}
		log("-----------END BOT ACTIVATED FROM " + message + "-----------");
	}

	/**
	 * Command handling takes place here purley for nice output for console. If returned, end tag still shown
	 * @param channel     The channel to which the message was sent.
	 * @param sender      The nick of the person who sent the message.
	 * @param login       The login of the person who sent the message.
	 * @param hostname    The hostname of the person who sent the message.
	 * @param rawmsg      The actual message sent to the channel.
	 * @throws Exception  If error is encountered while setting up command
	 */
	private void runCommand(String channel, String sender, String login, String hostname, String rawmsg) throws Exception {
		//Is bot locked?
		if (botLocked == true && !isAdmin()) {
			log.info("Command ignored due to global lock in effect");
			return;
		}

		//Is channel locked?
		if (chanLockList.containsKey(channel)) {
			log.info("Command ignored due to channel lock in effect");
			return;
		}

		String[] argArray;
		String command;
		//Parse message to get cmd and args
		if (rawmsg.indexOf(" ") > -1) {
			String[] msgArray = rawmsg.split(" ", 2);
			command = msgArray[0].trim();
			argArray = msgArray[1].split(" ");
		} else {
			command = rawmsg.trim();
			argArray = new String[0];
		}

		//Does this method exist?
		if (!methodExists(command)) {
			return;
		}
		TreeMap<String, Object> cmdinfo = mainInst.cmds.get(command);

		//Is this an admin function? If so, is the person an admin?
		if (Boolean.parseBoolean(cmdinfo.get("admin").toString()) == true && !isAdmin()) {
			sendMessage(channel, sender + ": Admin only command");
			return;
		}

		//Does this method require args?
		if (Boolean.parseBoolean(cmdinfo.get("ReqArg").toString()) == true && argArray.length == 0) {
			log.debug("Method does require args, passing length 1 array");
			argArray = new String[1];
		}

		//Does the required number of args exist?
		int user_args = argArray.length;
		int method_args = Integer.parseInt(cmdinfo.get("param").toString());
		log.debug("User Args: " + user_args + " | Req Args: " + method_args);
		if (user_args != method_args) {
			sendMessage(channel, sender + ": Wrong number of parameters specified. Given: " + user_args + ", Required: " + method_args);
			return;
		}

		//All requirements are met, excecute method
		log.info("All tests passed, running method");
		ScriptContext newContext = (ScriptContext) cmdinfo.get("context");
		Bindings engineScope = (Bindings) cmdinfo.get("scope");
		engineScope.put("channel", channel);
		engineScope.put("sender", sender);
		engineScope.put("login", login);
		engineScope.put("hostname", hostname);
		engineScope.put("rawmsg", rawmsg);
		engineScope.put("qb", this);

		//build command string
		StringBuilder jsCmd = new StringBuilder();
		jsCmd.append("invoke( ");
		for (String arg : argArray) {
			jsCmd.append(" '" + arg + "',");
		}
		jsCmd.deleteCharAt(jsCmd.length() - 1);
		jsCmd.append(");");

		log.debug("JS cmd: " + jsCmd.toString());

		//Run command in thread pool
		mainInst.threadPool_js.execute(new threadCmdRun(jsCmd.toString(), newContext, this, channel, sender));
	}

	/**
	 * Utility method to get a specific user from channel
	 * @param channel  Channel to search in
	 * @param reqUser  User to search for
	 * @return         User object of user, null if not found
	 */
	public User getUser(String channel, String reqUser) {
		User[] listUsers = getUsers(channel);
		for (User curUser : listUsers) {
			if (curUser.getNick().equalsIgnoreCase(reqUser)) {
				return curUser;
			}
		}
		return null;
	}

	/**
	 * Check cmd array for method name
	 * @param method
	 * @return True if command exists, false otherwise
	 */
	public boolean methodExists(String method) {
		if (!mainInst.cmds.containsKey(method)) {
			sendMessage(channel, sender + ": Command " + method + " dosen't exist");
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Check to see if person is an admin
	 * @return True if person is an admin, false otherwise
	 */
	public boolean isAdmin() {
		if (adminList.containsKey(sender)) {
			log.info("Calling user is admin!");
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Send message to ALL joined channels
	 * @param msg   Message to send
	 */
	public void sendAllMessage(String msg) {
		String[] channels = getChannels();
		for (String curChan : channels) {
			sendMessage(curChan, msg);
		}
	}
}
