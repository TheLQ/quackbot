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
package org.quackbot.dao;

import java.util.Set;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public interface DAOFactory {
	public AdminDAO newAdminStore(String name);
	public ChannelDAO newChannelStore(String name);
	public ServerDAO newServerStore(String address);
	public Set<ServerDAO> getServers();
	/**
	 * Get all global, server, and channel admins. This must NOT return duplicate
	 * entries. Implmentations should weed out duplicates by checking admin ID's
	 * @return 
	 */
	public Set<AdminDAO> getAllAdmins();
	/**
	 * Called when shutting down. Useful for closing any open connections and/or
	 * files
	 */
	public void close() throws Exception;
	
	/**
	 * Begin a transaction (eg unit of work). Useful for implementations that require
	 * transactional calls
	 */
	public void beginTransaction();
	
	/**
	 * End a transaction. Guaranteed to be called in the same thread that
	 * {@link #beginTransaction() } was called in. Note that this does NOT mean
	 * changes have occured!
	 */
	public void endTransaction(boolean isGood);
}
