/**
 * @(#)Channel.java
 *
 * This file is part of Quackbot
 */
package Quackbot.info;

import jpersist.Entity;
import org.apache.commons.lang.StringUtils;

/**
 * Bean that holds all Channel info
 *
 * This is usually configured by JPersist
 * @author Lord.Quackstar
 */
public class Channel extends Entity {

	/**
	 * Sterilized ID
	 */
	private static long serialVersionUID = 100L;

	/**
	 * @return the serialVersionUID
	 */
	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	/**
	 * @param aSerialVersionUID the serialVersionUID to set
	 */
	public static void setSerialVersionUID(long aSerialVersionUID) {
		serialVersionUID = aSerialVersionUID;
	}
	/**
	 * Value mapped to column in DB or manually provided
	 */
	private Integer serverID, channelID;
	/**
	 * Value mapped to column in DB or manually provided
	 */
	private String channel, password;

	/**
	 * Empty Constructor
	 */
	public Channel() {
	}

	/**
	 * From server Id
	 * @param serverID
	 */
	public Channel(Integer serverID) {
		this.serverID = serverID;
	}

	/**
	 * Create from string
	 * @param channel
	 */
	public Channel(String channel) {
		this.channel = channel;
	}

	/**
	 * Create from string and password
	 * @param channel
	 * @param password
	 */
	public Channel(String channel, String password) {
		this.channel = channel;
		this.password = password;
	}

	/**
	 * Convert to String
	 * @return String representation of Channel
	 */
	public String toString() {
		return StringUtils.join(new Object[]{getChannel(), getChannelID(), getPassword(), getServerID()}, ", ");
	}

	/**
	 * @return the serverID
	 */
	public Integer getServerID() {
		return serverID;
	}

	/**
	 * @param serverID the serverID to set
	 */
	public void setServerID(Integer serverID) {
		this.serverID = serverID;
	}

	/**
	 * @return the channelID
	 */
	public Integer getChannelID() {
		return channelID;
	}

	/**
	 * @param channelID the channelID to set
	 */
	public void setChannelID(Integer channelID) {
		this.channelID = channelID;
	}

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
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}
}
