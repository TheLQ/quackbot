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
package org.quackbot.dao.model;

import java.io.Serializable;
import java.util.Set;
import org.quackbot.dao.GenericDAO;

/**
 * This is the Server bean mapped to the Database by JPersist. Used by {@link Quackbot.Bot}
 *
 * This is usually configured by JPersist
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public interface ServerEntry extends GenericEntry {
	/**
	 * Gets admin by name
	 * @param name Name of admin
	 * @return     Admin object
	 */
	public Set<AdminEntry> getAdmins();

	/**
	 * Gets channel object by name
	 * @param channel Channel name (must include prefix)
	 * @return        Channel object
	 */
	public Set<ChannelEntry> getChannels();

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
}
