/*
 * Copyright (C) 2009-2010 Leon Blakey
 *
 * Quackedbot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Quackedbot  is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.quackbot.data;

import org.quackbot.Controller;
import org.slf4j.LoggerFactory;

/**
 * Bean that holds all known Channel information. This is meant to be integrated with
 * JPersist and the database. Only configure if going to add to database, otherwise let
 * JPersist configure it.
 * <p>
 * If this needs to be changed in database, call {@link #updateDB()}
 * @author Lord.Quackstar
 */
public class Channel  {
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
	private String name;
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
	 * From channel ID
	 * @param channelID
	 */
	public Channel(Integer channelID) {
		this.channelID = channelID;
	}

	/**
	 * Create from string
	 * @param name
	 */
	public Channel(String name) {
		this.name = name;
	}

	/**
	 * Create from string and password
	 * @param name The channel that this object should represent
	 * @param password The password of the channel
	 */
	public Channel(String name, String password) {
		this.name = name;
		this.password = password;
	}

	/**
	 * Convert to String
	 * @return String representation of Channel
	 */
	@Override
	public String toString() {
		return new StringBuilder("[").append("Channel=").append(getName()).append(",").
				append("Password=").append(getPassword()).append(",").
				append("ChannelID=").append(getChannelID()).append(",").
				append("ServerID=").append(getServerID()).append("]").
				toString();
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
	public Channel updateDB(Controller controller) {
		try {
			controller.getDatabase().saveObject(this);
			return controller.getDatabase().loadObject(this);
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
	public String getName() {
		return name;
	}

	/**
	 * Name of the channel
	 * @param name the channel to set
	 */
	public void setName(String name) {
		this.name = name;
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
