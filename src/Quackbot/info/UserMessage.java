/**
 * @(#)UserMessage.java
 *
 * This file is part of Quackbot
 */
package Quackbot.info;

/**
 * Bean that holds UserMessage and cmd breakdown info
 * @author Lord.Quackstar
 */
public class UserMessage {

	/**
	 * Args passed by user
	 */
	private static long serialVersionUID = 100L;

	/**
	 * Sterilizer info
	 * @return the serialVersionUID
	 */
	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	/**
	 * Sterilizer info
	 * @param aSerialVersionUID the serialVersionUID to set
	 */
	public static void setSerialVersionUID(long aSerialVersionUID) {
		serialVersionUID = aSerialVersionUID;
	}
	private String channel;
	private String sender;
	private String login;
	private String hostname;
	private String rawmsg;
	private String command;
	private String[] args = new String[0];

	/**
	 * Empty constructor
	 */
	public UserMessage() {
	}

	/**
	 * Usually call this, sets general info
	 * @param channel  Channel message was sent on
	 * @param sender   User name
	 * @param login    User login
	 * @param hostname User hostname
	 * @param message  Message user sent
	 */
	public UserMessage(String channel, String sender, String login, String hostname, String message) {
		this.channel = channel;
		this.sender = sender;
		this.login = login;
		this.hostname = hostname;
		this.rawmsg = message;
	}

	/**
	 * Bot listeners will call this
	 * @param channel  Channel message was sent on
	 * @param sender   User name
	 * @param login    User login
	 * @param hostname User hostname
	 * @param message  Message user sent
	 * @param command  Command to run
	 */
	public UserMessage(String channel, String sender, String login, String hostname, String message, String command) {
		this.channel = channel;
		this.sender = sender;
		this.login = login;
		this.hostname = hostname;
		this.rawmsg = message;
		this.command = command;
	}

	/**
	 * Utility method, checks if this is sent from this bot
	 * @param username Username of bot
	 * @return         True if it is, false otherwise
	 */
	public boolean isBot(String username) {
		return getSender().equalsIgnoreCase(username);
	}

	/**
	 * Channel message was sent on
	 * @return the channel
	 */
	public String getChannel() {
		return channel;
	}

	/**
	 * Channel message was sent on
	 * @param channel the channel to set
	 */
	public void setChannel(String channel) {
		this.channel = channel;
	}

	/**
	 * Name of sender
	 * @return the sender
	 */
	public String getSender() {
		return sender;
	}

	/**
	 * Name of sender
	 * @param sender the sender to set
	 */
	public void setSender(String sender) {
		this.sender = sender;
	}

	/**
	 * Login of sender
	 * @return the login
	 */
	public String getLogin() {
		return login;
	}

	/**
	 * Login of sender
	 * @param login the login to set
	 */
	public void setLogin(String login) {
		this.login = login;
	}

	/**
	 * Hostname of sender
	 * @return the hostname
	 */
	public String getHostname() {
		return hostname;
	}

	/**
	 * Hostname of sender
	 * @param hostname the hostname to set
	 */
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	/**
	 * Raw Message of sender
	 * @return the rawmsg
	 */
	public String getRawmsg() {
		return rawmsg;
	}

	/**
	 * Raw Message of sender
	 * @param rawmsg the rawmsg to set
	 */
	public void setRawmsg(String rawmsg) {
		this.rawmsg = rawmsg;
	}

	/**
	 * Command user called
	 * @return the command
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * Command user called
	 * @param command the command to set
	 */
	public void setCommand(String command) {
		this.command = command;
	}

	/**
	 * Args passed by user
	 * @return the args
	 */
	public String[] getArgs() {
		return args;
	}

	/**
	 * Args passed by user
	 * @param args the args to set
	 */
	public void setArgs(String[] args) {
		this.args = args;
	}
}
