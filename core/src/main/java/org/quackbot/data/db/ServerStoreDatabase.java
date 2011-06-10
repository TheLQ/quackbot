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
package org.quackbot.data.db;

import ejp.DatabaseException;
import ejp.DatabaseManager;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import org.quackbot.data.AdminStore;
import org.quackbot.data.ChannelStore;
import org.quackbot.data.ServerStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
@Data
public class ServerStoreDatabase implements ServerStore {
		/**
	 * Value mapped to column in DB or manually provided
	 */
	private String address, password;
	/**
	 * Value mapped to column in DB or manually provided
	 */
	private Integer serverId, port;
	/**
	 * List of all Channels, refrenced by common serverID
	 */
	private Set<ChannelStore> channels = new HashSet<ChannelStore>();
	/**
	 * List of all Admins, refrenced by common serverID
	 */
	private Set<AdminStore> admins = new HashSet<AdminStore>();
	protected final DatabaseManager dbm = DatabaseStore.databaseManager;
	private Logger log = LoggerFactory.getLogger(this.getClass());

	public ServerStoreDatabase() {
	}
	
	/**
	 * Creates Server
	 * @param address Address of server
	 */
	public ServerStoreDatabase(String address) {
		this.address = address;
	}

	/*******************************************UTILS*********************************/
	@Override
	public void addAdmin(AdminStore admin) {
		loadAssociations();
		admins.add(admin);
		update();
	}

	@Override
	public void removeAdmin(AdminStore admin) {
		loadAssociations();
		admins.remove(admin);
		update();
	}

	/**
	 * Add channel
	 * @param channel Channel name (must include prefix)
	 */
	@Override
	public void addChannel(ChannelStore channel) {
		loadAssociations();
		channels.add(channel);
		update();
	}

	/**
	 * Removes channel
	 * @param channel Channel name (must include prefix)
	 */
	@Override
	public void removeChannel(ChannelStore channel) {
		loadAssociations();
		getChannels().remove(channel);
		update();
	}


	/**
	 * Utility to update the database with the current Server object.
	 * <p>
	 * WARNING: Passing an empty or null server object might destroy the
	 * database's knowledge of the server. Only JPersist generated Server
	 * objects should be passed
	 * <p>
	 * @return Server object with database generated info set
	 */
	public void update() {
		//Try to save the object
		try {
			dbm.saveObject(this);
		} catch (DatabaseException e) {
			log.error("Couldn't save ServerStore to database", e);
			return;
		}

		//Update ourselves with the admin ID
		try {
			ServerStoreDatabase dbVersion = dbm.loadObject(this);
			setServerId(dbVersion.getServerId());
		} catch (DatabaseException ex) {
			log.error("Can't load ServerStore from database", ex);
		}
	}

	@Override
	public boolean delete() {
		try {
			dbm.deleteObject(this);
			return true;
		} catch (DatabaseException ex) {
			log.error("Couldn't delete ServerStore from Database", ex);
		}
		return false;
	}
	
	public void loadAssociations() {
		try {
			dbm.loadAssociations(this);
		} catch (DatabaseException ex) {
			log.error("Can't load associations from database", ex);
		}
	}
}
