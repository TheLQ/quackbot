/**
 * @(#)BotEvent.java
 *
 * This file is part of Quackbot
 */
package Quackbot.info;

/**
 * Bean that holds BotEvent and cmd breakdown info
 * @author Lord.Quackstar
 */
public class BotEvent {

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
	private int cmdNum;
	private Object extra;
	private Object extra1;

	/**
	 * Empty constructor
	 */
	public BotEvent() {
	}

	/**
	 * Usually call this, sets general info
	 * @param channel  Channel message was sent on
	 * @param sender   User name
	 * @param login    User login
	 * @param hostname User hostname
	 * @param message  Message user sent
	 */
	public BotEvent(String channel, String sender, String login, String hostname, String message) {
		this(channel, sender, login, hostname, message, null, null);
	}

	public BotEvent(String rawmsg, Object extra) {
		this(rawmsg,extra,null);
	}

	public BotEvent(String rawmsg, Object extra, Object extra1) {
		this.rawmsg = rawmsg;
		this.extra = extra;
		this.extra1 = extra1;
	}
	
	public BotEvent(String channel, String sender, String login, String hostname, String message, Object extra) {
		this(channel, sender, login, hostname, message, extra, null);
	}

	public BotEvent(String channel, String sender, String login, String hostname, String message, Object extra, Object extra1) {
		this.channel = channel;
		this.sender = sender;
		this.login = login;
		this.hostname = hostname;
		this.rawmsg = message;
		this.extra = extra;
		this.extra1 = extra1;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Channel: "+channel+" ");
		sb.append("Sender: "+sender+" ");
		sb.append("Login: "+login+" ");
		sb.append("Hostname: "+hostname+" ");
		sb.append("RawMsg: "+rawmsg+" ");
		return sb.toString();
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

	/**
	 * @return the cmdNum
	 */
	public int getCmdNum() {
		return cmdNum;
	}

	/**
	 * @param cmdNum the cmdNum to set
	 */
	public void setCmdNum(int cmdNum) {
		this.cmdNum = cmdNum;
	}

	/**
	 * @return the extra
	 */
	public Object getExtra() {
		return extra;
	}

	/**
	 * @param extra the extra to set
	 */
	public void setExtra(Object extra) {
		this.extra = extra;
	}

	/**
	 * @return the extra1
	 */
	public Object getExtra1() {
		return extra1;
	}

	/**
	 * @param extra1 the extra1 to set
	 */
	public void setExtra1(Object extra1) {
		this.extra1 = extra1;
	}

	/**
	 * @return the command
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * @param command the command to set
	 */
	public void setCommand(String command) {
		this.command = command;
	}
}
