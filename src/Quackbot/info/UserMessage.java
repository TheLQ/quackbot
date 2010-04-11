package Quackbot.info;

/**
 *
 * @author lordquackstar
 */
public class UserMessage {
	public String channel;
	public String sender;
	public String login;
	public String hostname;
	public String rawmsg;
	public String command;
	public String[] args;

	public UserMessage(String channel, String sender, String login, String hostname, String message) {
		this.channel= channel;
		this.sender = sender;
		this.login = login;
		this.hostname = hostname;
		this.rawmsg = message;
	}


	public UserMessage() {}

	/**
	 * @return the channel
	 */
	public String getChannel() {
		return channel;
	}

	/**
	 * @param channel the channel to set
	 */
	public void setChannel(String channel) {
		this.channel = channel;
	}

	/**
	 * @return the sender
	 */
	public String getSender() {
		return sender;
	}

	/**
	 * @param sender the sender to set
	 */
	public void setSender(String sender) {
		this.sender = sender;
	}

	/**
	 * @return the login
	 */
	public String getLogin() {
		return login;
	}

	/**
	 * @param login the login to set
	 */
	public void setLogin(String login) {
		this.login = login;
	}

	/**
	 * @return the hostname
	 */
	public String getHostname() {
		return hostname;
	}

	/**
	 * @param hostname the hostname to set
	 */
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	/**
	 * @return the rawmsg
	 */
	public String getRawmsg() {
		return rawmsg;
	}

	/**
	 * @param rawmsg the rawmsg to set
	 */
	public void setRawmsg(String rawmsg) {
		this.rawmsg = rawmsg;
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

	/**
	 * @return the args
	 */
	public String[] getArgs() {
		return args;
	}

	/**
	 * @param args the args to set
	 */
	public void setArgs(String[] args) {
		this.args = args;
	}
}
