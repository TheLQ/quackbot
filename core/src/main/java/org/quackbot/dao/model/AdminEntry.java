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

import java.util.Set;

/**
 * Bean that holds all known Admin information. This is meant to be integrated with
 * JPersist and the database. Only configure if going to add to database, otherwise let
 * JPersist configure it.
 * <p>
 * If this needs to be changed in database, call {@link #updateDB()}
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public interface AdminEntry extends GenericEntry {
	/**
	 * The username of the admin
	 * @return the user
	 */
	public String getName();

	/**
	 * The username of the admin
	 * @param user the user to set
	 */
	public void setName(String name);

	/**
	 * The Channel object that the admin might be attached to. Can be null
	 * <p>
	 * A null value indicates this isn't attached to a channel. The admin can
	 * either be server admin or global admin
	 * <p>
	 * Note that this isn't mapped by JPersist, it is simply a convience method
	 * @return the channel
	 */
	public Set<ChannelEntry> getChannels();

	/**
	 * The Server object that the admin might be attached to. Can be null
	 * <p>
	 * A null value indicates the admin isn't attached to a server. They must
	 * be a global admin.
	 * <p>
	 * Note that this isn't mapped by JPersist, it is simply a convience method
	 * @return the server
	 */
	public Set<ServerEntry> getServers();
}
