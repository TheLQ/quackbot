/**
 * Copyright (C) 2010 Leon Blakey <lord.quackstar at gmail.com>
 *
 * This file is part of PircBotX.
 *
 * PircBotX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PircBotX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PircBotX.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.quackbot.data;

import java.util.Set;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public interface DataStore {
	public AdminStore newAdminStore(String name);
	public ChannelStore newChannelStore(String name);
	public ServerStore newServerStore(String address);
	public Set<ServerStore> getServers();
	/**
	 * Get all global, server, and channel admins. This must NOT return duplicate
	 * entries. Implmentations should weed out duplicates by checking admin ID's
	 * @return 
	 */
	public Set<AdminStore> getAllAdmins();
	/**
	 * Called when shutting down. Useful for closing any open connections and/or
	 * files
	 */
	public void close() throws Exception;
}
