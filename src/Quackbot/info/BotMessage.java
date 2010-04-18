/**
 * @(#)BotMessage.java
 *
 * This file is part of Quackbot
 */
package Quackbot.info;

/**
 * Bean that holds bot message
 * @author Lord.Quackstar
 */
public class BotMessage {

	/**
	 * Message to be sent (REQUIRED)
	 */
	public String message;
	/**
	 * Channel to send message on (Optional)
	 */
	public String channel;
	/**
	 * User to recieve message (Optional)
	 */
	public String user;

	/**
	 * Message will be sent to user in channel who sent message will be in prefix
	 * @param usrMsg  UserMessage bean from bot
	 * @param message Message to send
	 */
	public BotMessage(UserMessage usrMsg, String message) {
		this.message = message;
		this.channel = usrMsg.getChannel();
		this.user = usrMsg.getSender();
	}

	/**
	 * Send message contained in Throwable to user and channel in UserMessage
	 * @param usrMsg  UserMessage bean from bot
	 * @param t       Exception
	 */
	public BotMessage(UserMessage usrMsg, Throwable t) {
		this.message = t.getMessage();
		this.channel = usrMsg.getChannel();
		this.user = usrMsg.getSender();
	}

	/**
	 * Custom bot message
	 * @param channel  Channel to send to
	 * @param user     Username
	 * @param message  Message to send
	 */
	public BotMessage(String channel, String user, String message) {
		this.message = message;
		this.channel = channel;
		this.user = user;
	}

	/**
	 * Send message to channel
	 * @param channel Channel to send to
	 * @param message Message to send
	 */
	public BotMessage(String channel, String message) {
		this.message = message;
		this.channel = channel;
	}

	/**
	 * Convert message to string
	 * @return Message in string format
	 */
	public String toString() {
		//Use StringBuilder just in case one of the values is null
		return new StringBuilder().append(user).append(": ").append(message).toString();
	}
}
