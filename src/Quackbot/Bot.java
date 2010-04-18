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

    public boolean botLocked = false;
    final HashSet<String> PREFIXES = new HashSet<String>();
    public TreeMap<String, String> adminList;
    public TreeMap<String, String> chanLockList;
    public Controller ctrl = InstanceTracker.getCtrlInst();
    public Server serverDB;
    Logger log = Logger.getLogger(Bot.class);

    /**
     * Init bot by setting all information
     * @param mainInstance   The controller instance used to spawn this bot
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

    @Override
    public void onConnect() {
	//Init prefixes (put here in order to prevent MOTD from tripping bot)
	PREFIXES.add("?");
	PREFIXES.add(getNick() + ":");
	PREFIXES.add(getNick());

	updateServer();
	List<Channel> channels = serverDB.getChannels();
	for(Channel curChannel : channels)
	    joinChannel(curChannel.getChannel(),curChannel.getPassword());
    }

    public void updateServer() {
	try {
	    Collection<Channel> channels = ctrl.dbm.loadObjects(new ArrayList<Channel>(), Channel.class);
	    for(Channel channel : channels)
		if(channel.getServerID() == serverDB.getServerId())
		    serverDB.getChannels().add(channel);
	    Collection<Admin> admins = ctrl.dbm.loadObjects(new ArrayList<Admin>(), Admin.class);
	    for(Admin curAdmin : admins)
		if(curAdmin.getServerID() == serverDB.getServerId())
		    serverDB.getAdmins().add(curAdmin);
	}
	catch(Exception e) {
	    log.fatal("Cannot connect to channels",e);
	}
    }

    public void sendMsg(BotMessage msg) {
	sendMessage(msg.channel,msg.toString());
    }



    /*********************LISTENERS FOLLOW************************/
    @Override
    public void onJoin(String channel, String sender, String login, String hostname) {
	runListener("onJoin", new UserMessage(channel, sender, login, hostname, null));

	//If this is us, add to server info
	if (sender.equalsIgnoreCase(getNick())) {
	    //curServer.addChannel(new Channel(channel));
	}
    }

    @Override
    public void onPart(String channel, String sender, String login, String hostname) {
	runListener("onPart", new UserMessage(channel, sender, login, hostname, null));

	if (sender.equalsIgnoreCase(getNick())) {
	    //curServer.removeChannel(channel);
	    //ctrl.JRocm.update(curServer);
	}
    }

    @Override
    public void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) {
	runListener("onQuit", new UserMessage(null, sourceNick, sourceLogin, sourceHostname, reason));
    }

    /**
     * Excecutes listener
     * @param command   The name of the command
     * @param channel   The channel which somebody joined.
     * @param sender    The nick of the user who joined the channel.
     * @param login     The login of the user who joined the channel.
     * @param hostname  The hostname of the user who joined the channel.
     */
    private void runListener(String command, UserMessage msgInfo) {
	log("Attempting to run listener " + command);
	if (!JSUtils.getListeners().containsKey(command)) {
	    log.error("Listiner does not exist!!");
	    return;
	}
	new PluginExecutor(this,msgInfo,command,new String[0]);

	}

    /***************USER SUBMITTED COMMANDS FOLLOW*********************/
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
	if (!contPre) {
	    return;
	}

	//Bot activated, start command process
	activateCmd(msgInfo);
    }

    @Override
    public void onPrivateMessage(String sender, String login, String hostname, String message) {
	//Because this is a PM, just start going
	activateCmd(new UserMessage(null, sender, login, hostname, message));
    }


    /**
     * runCommand wrapper, outputs begginin and end to console and catches errors
     */
    private void activateCmd(UserMessage msgInfo) {
	log("-----------BOT ACTIVATED FROM " + msgInfo.rawmsg + "-----------");
	try {
	    runCommand(msgInfo);
	} catch (Exception e) {
	    sendMessage(msgInfo.channel, msgInfo.sender + ": ERROR " + e.getMessage());
	    log.error("Run Error", e);
	}
	log("-----------END BOT ACTIVATED FROM " + msgInfo.rawmsg + "-----------");
    }

    /**
     * Command handling takes place here purley for nice output for console. If returned, end tag still shown
     * @param channel     The channel to which the message was sent.
     * @throws Exception  If error is encountered while setting up command
     */
    private void runCommand(UserMessage msgInfo) throws InvalidCMDException, AdminException, NumArgException {
	//Is bot locked?
	if (botLocked == true && !isAdmin(msgInfo.sender)) {
	    log.info("Command ignored due to global lock in effect");
	    return;
	}

	//Is channel locked?
	if (chanLockList.containsKey(msgInfo.channel)) {
	    log.info("Command ignored due to channel lock in effect");
	    return;
	}

	String[] argArray;
	String command;
	//Parse message to get cmd and args
	if (msgInfo.rawmsg.indexOf(" ") > -1) {
	    String[] msgArray = msgInfo.rawmsg.split(" ", 2);
	    command = msgArray[0].trim();
	    argArray = msgArray[1].split(" ");
	} else {
	    command = msgInfo.rawmsg.trim();
	    argArray = new String[0];
	}

	//Build UserMessage bean
	msgInfo.setArgs(argArray);
	msgInfo.setCommand(command);

	ctrl.threadPool_js.execute(new PluginExecutor(this, msgInfo, command, argArray));
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
     * Check to see if person is an admin
     * @return True if person is an admin, false otherwise
     */
    public boolean isAdmin(String user) {
	return adminList.containsKey(user);
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
