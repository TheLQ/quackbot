/**
 * @(#)Bot.java
 *
 * This file is part of Quackbot
 */
 
package Quackbot;

 
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;

import javax.script.Bindings;
import javax.script.ScriptContext;

import org.jibble.pircbot.PircBot;

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
	PrintStream errorStream = new PrintStream(new BotErrorStream(new ByteArrayOutputStream()));

	final HashSet<String> PREFIXES = new HashSet<String>();
	public TreeMap<String,String> adminList;
	public TreeMap<String,String> chanLockList;
	public Controller mainInst = null;

	/**
	 * Init bot by setting all information
	 * @param mainInstance   The controller instance used to spawn this bot
	 */
	public Bot(Controller mainInstance) {
		mainInst = mainInstance;
		setName("Quackbot");
		setAutoNickChange(true);
		setFinger("Quackbot IRC bot by Lord.Quackstar. Source: http://github.com/LBlakey/Quackbot");
		setMessageDelay(500);
		setVersion("Quackbot 0.5");
	}

	/**
	 * Bot out stream wrapper, prefixes with server
	 * @param line   Line to be outputed
	 */
	@Override
	public void log(String line) {
		System.out.println(getServer()+" "+line);
	}

	/**
	 * Bot error stream wrapper, prefixes with server
	 * @param line   Line to be outputted
	 */
	public void logErr(String line) {
		System.err.println(getServer()+" "+line);
	}

	/**
	 * Setup bot when fully connected
	 */
	@Override
	public void onConnect() {
		//Add admins
		adminList = new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
		adminList.put("LordQuackstar","True");

		//Init channel block list
		chanLockList = new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);

		//Init prefixes
		PREFIXES.add("?");
		PREFIXES.add(getNick()+":");
		PREFIXES.add(getNick());
	}

	/**
	 * Activated when someone types a message on a channel
	 * @param channel   The channel to which the message was sent.
	 * @param sender    The nick of the person who sent the message.
	 * @param login     The login of the person who sent the message.
	 * @param hostname  The hostname of the person who sent the message.
	 * @param message   The actual message sent to the channel.
	 */
	@Override
	public void onMessage(String channel, String sender, String login, String hostname, String message) {
		//Make class aware of a few parameters
		this.channel = channel;
		this.sender = sender;
		
		//Look for a prefix
		Iterator preItr = PREFIXES.iterator();
		Boolean contPre = false;
		while(preItr.hasNext()) {
			String curPre = preItr.next().toString();
			if(curPre.length() < message.length() && message.substring(0,curPre.length()).equals(curPre)) {
				contPre = true;
				message = message.substring(curPre.length(),message.length()).trim().toLowerCase();
				break;
			}
		}

		//Is there a prefix?
		if(!contPre)
			return;

		//Bot activated, start command process
		activateCmd(channel, sender, login, hostname, message);
	}

	/**
	 * PircBot method, activated when someone PM's the bot
	 * @param sender   The nick of the person who sent the private message.
	 * @param login    The login of the person who sent the private message.
	 * @param hostname The hostname of the person who sent the private message.
	 * @param message  The actual message.
	 */
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
		log("-----------BOT ACTIVATED FROM "+message+"-----------");
		try {
			runCommand(channel, sender, login, hostname, message);
		}
		catch(Exception e) {
			sendMessage(channel, sender+": RUN ERROR: "+e.toString());
			e.printStackTrace(errorStream);
		}
		log("-----------END BOT ACTIVATED FROM "+message+"-----------");
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
		if(botLocked == true && !isAdmin()) {
			log("Command ignored due to global lock in effect");
			return;
		}

		//Is channel locked?
		if(chanLockList.containsKey(channel)) {
 		log("Command ignored due to channel lock in effect");
			return;
		}

		String[] argArray;
		String command;
		//Parse message to get cmd and args
		if(rawmsg.indexOf(" ") > -1) {
			String[] msgArray = rawmsg.split(" ",2);
			command = msgArray[0].trim();
			argArray = msgArray[1].split(",");
		}
		else {
			command = rawmsg.trim();
			argArray = new String[0];
		}

		//Does this method exist?
		if(!methodExists(command)) return;
		TreeMap<String,Object> cmdinfo = mainInst.cmds.get(command);

		//Is this an admin function? If so, is the person an admin?
		if(Boolean.parseBoolean(cmdinfo.get("admin").toString())==true && !isAdmin()) {
			sendMessage(channel, sender+": Admin only command");
			return;
		}

		//Does this method require args?
		if(Boolean.parseBoolean(cmdinfo.get("ReqArg").toString()) == true && argArray.length == 0) {
			log("Method does require args, passing length 1 array");
			argArray = new String[1];
		}

		//Does the required number of args exist?
		int user_args = argArray.length;
		int method_args = Integer.parseInt(cmdinfo.get("param").toString());
		log("User Args: "+user_args+" | Req Args: "+method_args);
		if(user_args != method_args) {
			sendMessage(channel, sender+": Wrong number of parameters specified. Given: "+user_args+", Required: "+method_args);
			return;
		}

		//All requirements are met, excecute method
		log("All tests passed, running method");
		ScriptContext newContext = (ScriptContext)cmdinfo.get("context");
	 	Bindings engineScope = (Bindings)cmdinfo.get("scope");;
		engineScope.put("channel",channel);
		engineScope.put("sender",sender);
		engineScope.put("login",login);
		engineScope.put("hostname",hostname);
		engineScope.put("rawmsg",rawmsg);
		engineScope.put("qb",this);
		engineScope.put("out",System.out);

		//build command string
		StringBuilder jsCmd = new StringBuilder();
		jsCmd.append("invoke( ");
		for(String arg : argArray) {
			jsCmd.append(" '"+arg+"',");
		}
		jsCmd.deleteCharAt(jsCmd.length()-1);
		jsCmd.append(");");
		
		log("JS cmd: "+jsCmd.toString());

		//Run command in thread pool
		mainInst.threadPool_js.execute(new threadCmdRun(jsCmd.toString(),newContext));
	}

	/**
	 * Check cmd array for method name
	 * @param method
	 * @return True if command exists, false otherwise
	 */
	public boolean methodExists(String method) {
		if(!mainInst.cmds.containsKey(method)) {
			sendMessage(channel, sender+": Command "+method+" dosen't exist");
			return false;
		}
		else
			return true;
	}

	/**
	 * Check to see if person is an admin
	 * @return True if person is an admin, false otherwise
	 */
	public boolean isAdmin() {
		if(adminList.containsKey(sender)) {
			log("Calling user is admin!");
			return true;
		}
		else
			return false;
	}

	/**
	 * Send message to ALL joined channels
	 * @param msg   Message to send
	 */
	public void sendAllMessage(String msg) {
		String[] channels = getChannels();
		for(String channel : channels)
			sendMessage(channel,msg);
	}

	/**
	 * Simple Runnable to run command in seperate thread
	 */
	class threadCmdRun implements Runnable {
		String jsCmd;
		ScriptContext context;

		/**
		 * Setup runnable
		 * @param jsCmd
		 * @param context
		 */
		public threadCmdRun(String jsCmd, ScriptContext context) {
			this.jsCmd = jsCmd;
			this.context = context;
		}

		/**
		 * Run in background
		 */
		public void run() {
			try {
				mainInst.jsEngine.eval(jsCmd,context);
			}
			catch(Exception e) {
				Throwable cause = e.getCause();
				//cause.printStackTrace(errorStream);
				e.printStackTrace(errorStream);
				sendMessage(channel, sender+": CMD ERROR: "+e.toString());
			}
		}
	}

	/**
	 * Bot error stream wrapper. Passed to stackTrace dumps so errors can be prefixed
	 */
	class BotErrorStream extends FilterOutputStream {
		public BotErrorStream(ByteArrayOutputStream stream) {
			super(stream);
		}

		public void write(byte b[], int off, int len) throws IOException {
			String aString = new String(b , off , len).trim();

			//don't print empty strings
			if(aString.length()==0)
				return;

			logErr(aString.toString());
		}
	}

}
