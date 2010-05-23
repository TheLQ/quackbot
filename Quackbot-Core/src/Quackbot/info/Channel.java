/**
 * @(#)Channel.java
 *
 * This file is part of Quackbot
 */
package Quackbot.info;

import Quackbot.Controller;
import jpersist.Entity;
import org.slf4j.LoggerFactory;

/**
 * Bean that holds all known Channel information. This is meant to be integrated with
 * JPersist and the database. Only configure if going to add to database, otherwise let
 * JPersist configure it.
 * <p>
 * If this needs to be changed in database, call {@link #updateDB()}
 * @author Lord.Quackstar
 */
public class Channel extends Entity {
	/**
	 * ID of the server Channel is attached to
	 */
	private Integer serverID;
	/**
	 * ID of channel in Database
	 */
	private Integer channelID;
	/**
	 * Name of the channel
	 */
	private String channel;
	/**
	 * Password of the channel. Can be null.
	 */
	private String password;

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
		return new StringBuilder("[").append("Channel=" + getChannel() + ",").append("Password=" + getPassword() + ",").append("ChannelID=" + getChannelID() + ",").append("ServerID=" + getServerID()).append("]").toString();
	}

	/**
	 * Utility to update the database with the current Admin object.
	 * <p>
	 * WARNING: Passing an empty or null server object might destroy the
	 * database's knowledge of the server. Only JPersist generated Server
	 * objects should be passed
	 * <p>
	 * This is a convience method for
	 * <br>
	 * <code>try {
	 *		save(Controller.instance.dbm);
	 * catch (Exception e) {
	 * updating database", e);
	 *	}</code>
	 * @return Channel object with database generated info set
	 */
	public Channel updateDB() {
		try {
			save(Controller.instance.dbm);
			return Controller.instance.dbm.loadObject(this);
		} catch (Exception e) {
			LoggerFactory.getLogger(Server.class).error("Error updating or fetching database", e);
		}
		return null;
	}

	/**
	 * ID of the server Channel is attached to
	 * @return the serverID
	 */
	public Integer getServerID() {
		return serverID;
	}

	/**
	 * ID of the server Channel is attached to
	 * @param serverID the serverID to set
	 */
	public void setServerID(Integer serverID) {
		this.serverID = serverID;
	}

	/**
	 * ID of channel in Database
	 * @return the channelID
	 */
	public Integer getChannelID() {
		return channelID;
	}

	/**
	 * ID of channel in Database
	 * @param channelID the channelID to set
	 */
	public void setChannelID(Integer channelID) {
		this.channelID = channelID;
	}

	/**
	 * Name of the channel
	 * @return the channel
	 */
	public String getChannel() {
		return channel;
	}

	/**
	 * Name of the channel
	 * @param channel the channel to set
	 */
	public void setChannel(String channel) {
		this.channel = channel;
	}

	/**
	 * Password of the channel. Can be null.
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Password of the channel. Can be null.
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

}
