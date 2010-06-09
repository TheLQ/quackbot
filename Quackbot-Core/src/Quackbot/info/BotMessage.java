/**
 * @(#)BotMessage.java
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
package Quackbot.info;

/**
 * Bean that holds bot message
 * @author Lord.Quackstar
 */
public class BotMessage {
	/**
	 * Message to be sent (REQUIRED)
	 */
	private String message;
	/**
	 * Channel to send message on (REQUIRED)
	 */
	private String channel;
	/**
	 * User to recieve message (Optional)
	 */
	private String user;

	/**
	 * Message will be sent to user in channel who sent message will be in prefix
	 * @param usrMsg  BotEvent bean from bot
	 * @param message Message to send
	 */
	public BotMessage(BotEvent usrMsg, String message) {
		this.message = message;
		this.channel = usrMsg.getChannel();
		this.user = usrMsg.getSender();
	}

	/**
	 * Send message contained in Throwable to user and channel in BotEvent
	 * @param usrMsg  BotEvent bean from bot
	 * @param t       Exception
	 */
	public BotMessage(BotEvent usrMsg, Throwable t) {
		this.message = "ERROR: " + t.getMessage();
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
		StringBuilder sb = new StringBuilder();
		if (getUser() != null)
			sb.append(getUser()).append(": ");
		return sb.append(getMessage()).toString();
	}

	/**
	 * Message to be sent (REQUIRED)
	 * @return the message, null being represented by an EMPTY STRING
	 */
	public String getMessage() {
		return (message == null) ? "" : message;
	}

	/**
	 * Message to be sent (REQUIRED)
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * Channel to send message on (REQUIRED)
	 * @return the channel, null being represented by an EMPTY STRING
	 */
	public String getChannel() {
		return (channel == null) ? "" : channel;
	}

	/**
	 * Channel to send message on (REQUIRED)
	 * @param channel the channel to set
	 */
	public void setChannel(String channel) {
		this.channel = channel;
	}

	/**
	 * User to recieve message (Optional)
	 * @return the user, null being represented by an EMPTY STRING
	 */
	public String getUser() {
		return (user == null) ? "" : user;
	}

	/**
	 * User to recieve message (Optional)
	 * @param user the user to set
	 */
	public void setUser(String user) {
		this.user = user;
	}
}
