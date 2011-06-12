/**
 * Copyright (C) 2011 Leon Blakey <lord.quackstar at gmail.com>
 *
 * This file is part of Quackbot.
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
package org.quackbot.data;


import java.util.Set;


/**
 * This is the Server bean mapped to the Database by JPersist. Used by {@link Quackbot.Bot}
 *
 * This is usually configured by JPersist
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public interface ServerStore {
	/**
	 * Delete the server
	 * @return True if delete was successful, false if not
	 */
	public boolean delete();
	
	/*********** Admin and Channel Management **************************/
	/**
	 * Adds admin
	 * @param admin An admin object
	 */
	public void addAdmin(AdminStore admin);

	/**
	 * Remove an admin from this server
	 * @param admin 
	 */
	public void removeAdmin(AdminStore admin);
	
	/**
	 * Gets admin by name
	 * @param name Name of admin
	 * @return     Admin object
	 */
	public Set<? extends AdminStore> getAdmins();

	/**
	 * Add channel
	 * @param channel Channel name (must include prefix)
	 */
	public void addChannel(ChannelStore channel);

	public void removeChannel(ChannelStore channel);
	
	/**
	 * Gets channel object by name
	 * @param channel Channel name (must include prefix)
	 * @return        Channel object
	 */
	public Set<? extends ChannelStore> getChannels();

	/******************************* Server Info *************************/
	/**
	 * Value mapped to column in DB or manually provided
	 * @return the address
	 */
	public String getAddress();

	/**
	 * Value mapped to column in DB or manually provided
	 * @param address the address to set
	 */
	public void setAddress(String address);

	/**
	 * Value mapped to column in DB or manually provided
	 * @return the password
	 */
	public String getPassword();

	/**
	 * Value mapped to column in DB or manually provided
	 * @param password the password to set
	 */
	public void setPassword(String password);

	/**
	 * Value mapped to column in DB or manually provided
	 * @return the port
	 */
	public Integer getPort();

	/**
	 * Value mapped to column in DB or manually provided
	 * @param port the port to set
	 */
	public void setPort(Integer port);

	/**
	 * Value mapped to column in DB or manually provided
	 * @return the serverId
	 */
	public Integer getServerId();

	/**
	 * Value mapped to column in DB or manually provided
	 * @param serverId the serverId to set
	 */
	public void setServerId(Integer serverId);
}

