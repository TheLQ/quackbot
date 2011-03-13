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
package org.quackbot.data.db;

import ejp.DatabaseException;
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
public class AdminStoreDatabase implements AdminStore {
	/**
	 * The ID of the admin
	 */
	private Integer adminId;
	/**
	 * The username of the admin
	 */
	private String name;
	private Set<ChannelStore> channels;
	private Set<ServerStore> servers;
	protected final DatabaseStore store;
	/**
	 * Logging system
	 */
	private Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * Generate from name
	 * @param name  Name of admin
	 */
	public AdminStoreDatabase(DatabaseStore store, String name) {
		this.name = name;
		this.store = store;
	}

	@Override
	public boolean delete() {
		try {
			store.getDatabaseManager().deleteObject(this);
			return true;
		} catch (DatabaseException ex) {
			log.error("Couldn't delete AdminStore from Database", ex);
		}
		return false;
	}

	public void update() {
		//Try to save the object
		try {
			store.getDatabaseManager().saveObject(this);
		} catch (DatabaseException e) {
			log.error("Couldn't save AdminStore to database", e);
			return;
		}

		//Update ourselves with the admin ID
		try {
			AdminStore dbVersion = store.getDatabaseManager().loadObject(this);
			setAdminId(dbVersion.getAdminId());
		} catch (DatabaseException ex) {
			log.error("Can't load AdminStore from database", ex);
		}
	}
	
	public void loadAssociations() {
		try {
			store.getDatabaseManager().loadAssociations(this);
		} catch (DatabaseException ex) {
			log.error("Can't load associations from database", ex);
		}
	}

	@Override
	public Set<ChannelStore> getChannels() {
		loadAssociations();
		return channels;
	}

	@Override
	public Set<ServerStore> getServers() {
		loadAssociations();
		return servers;
	}
}
