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

import java.util.Set;

/**
 * Bean that holds all known Channel information. This is meant to be integrated with
 * JPersist and the database. Only configure if going to add to database, otherwise let
 * JPersist configure it.
 * <p>
 * If this needs to be changed in database, call {@link #updateDB()}
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public interface ChannelStore  {
	/**
	 * Delete this Channel
	 */
	public boolean delete();
	
	/*********** Admin Management ***********/
	
	/**
	 * Adds admin
	 * @param admin An admin object
	 */
	public void addAdmin(AdminStore admin);

	/**
	 * Remove an admin from this channel
	 */
	public void removeAdmin(AdminStore admin);
	
	/**
	 * Gets admin by name
	 * @param name Name of admin
	 * @return     Admin object
	 */
	public Set<AdminStore> getAdmins();
	
	/************ Channel Info ***************/
	
	/**
	 * ID of the server Channel is attached to
	 * @return the serverID
	 */
	public Integer getServerID();

	/**
	 * ID of the server Channel is attached to
	 * @param serverID the serverID to set
	 */
	public void setServerID(Integer serverID);
	/**
	 * ID of channel in Database
	 * @return the channelID
	 */
	public Integer getChannelID();

	/**
	 * ID of channel in Database
	 * @param channelID the channelID to set
	 */
	public void setChannelID(Integer channelID);

	/**
	 * Name of the channel
	 * @return the channel
	 */
	public String getName();

	/**
	 * Name of the channel
	 * @param name the channel to set
	 */
	public void setName(String name);

	/**
	 * Password of the channel. Can be null.
	 * @return the password
	 */
	public String getPassword();

	/**
	 * Password of the channel. Can be null.
	 * @param password the password to set
	 */
	public void setPassword(String password);
}