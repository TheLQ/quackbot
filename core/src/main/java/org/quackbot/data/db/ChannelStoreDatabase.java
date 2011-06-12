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
public class ChannelStoreDatabase implements ChannelStore {
	/**
	 * ID of the server Channel is attached to
	 */
	private ServerStoreDatabase server;
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
	protected final DatabaseManager dbm = DatabaseStore.databaseManager;
	private Set<AdminStoreDatabase> admins;
	private Logger log = LoggerFactory.getLogger(this.getClass());

	public ChannelStoreDatabase() {
	}

	/**
	 * Create from string
	 * @param name
	 */
	public ChannelStoreDatabase(String name) {
		this.name = name;
	}

	/**
	 * Utility to update the database with the current Admin object.
	 * @return Channel object with database generated info set
	 */
	public void update() {
		//Try to save the object
		try {
			dbm.saveObject(this);
		} catch (DatabaseException e) {
			log.error("Couldn't save ChannelStore to database", e);
			return;
		}

		//Update ourselves with the admin ID
		try {
			ChannelStoreDatabase dbVersion = dbm.loadObject(this);
			setChannelID(dbVersion.getChannelID());
		} catch (DatabaseException ex) {
			log.error("Can't load ChannelStore from database", ex);
		}
	}
	
	public void loadAssociations() {
		try {
			dbm.loadAssociations(this);
		} catch (DatabaseException ex) {
			log.error("Can't load associations from database", ex);
		}
	}

	@Override
	public boolean delete() {
		try {
			dbm.deleteObject(this);
			return true;
		} catch (DatabaseException ex) {
			log.error("Couldn't delete ChannelStore from Database", ex);
		}
		return false;
	}

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

	public void setServer(ServerStore server) {
		this.server = (ServerStoreDatabase)server;
	}
}
