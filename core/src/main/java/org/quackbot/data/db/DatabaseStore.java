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
import ejp.DatabaseManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javax.sql.DataSource;
import lombok.Data;
import org.quackbot.data.AdminStore;
import org.quackbot.data.ChannelStore;
import org.quackbot.data.DataStore;
import org.quackbot.data.ServerStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
@Data
public class DatabaseStore implements DataStore {
	protected DatabaseManager databaseManager = null;
	private Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * Create a DatabaseManager instance using a supplied DataSource.
	 * <p>
	 * This simply calls
	 * <code>dbm = new DatabaseManager(databaseName, poolSize, dataSource, catalogPattern, schemaPattern);</code>
	 *
	 * @param databaseName the name to associate with the DatabaseManager instance
	 * @param poolSize the number of instances to manage
	 * @param dataSource the data source that supplies connections
	 */
	public void connectDB(String databaseName, int poolSize, DataSource dataSource) {
		databaseManager = DatabaseManager.getDatabaseManager(databaseName, poolSize, dataSource);
	}

	/**
	 * Connect to Database using using JNDI.
	 * <p>
	 * This simply calls
	 * <code>dbm = new DatabaseManager(databaseName, poolSize,jndiUri, catalogPattern, schemaPattern);</code>
	 *
	 * @param databaseName the name to associate with the DatabaseManager instance
	 * @param poolSize the number of instances to manage
	 * @param jndiUri the JNDI URI
	 * @param username The username to use
	 * @param password The password to use
	 */
	public void connectDB(String databaseName, int poolSize, String jndiUri, String username, String password) {
		databaseManager = DatabaseManager.getDatabaseManager(databaseName, poolSize, jndiUri, username, password);
	}

	/**
	 * Create a DatabaseManager instance using a supplied database driver.
	 * <p>
	 * This simply calls
	 * <code>dbm = new DatabaseManager(databaseName, poolSize, driver, url, catalogPattern, schemaPattern);</code>
	 *
	 * @param databaseName the name to associate with the DatabaseManager instance
	 * @param poolSize the number of instances to manage
	 * @param driver the database driver class name
	 * @param url the driver oriented database url
	 * @param username The username to use
	 * @param password The password to use
	 */
	public void connectDB(String databaseName, int poolSize, String driver, String url, String username, String password) {
		databaseManager = DatabaseManager.getDatabaseManager(databaseName, poolSize, driver, url, username, password);
	}

	@Override
	public AdminStore newAdminStore(String name) {
		return new AdminStoreDatabase(this, name);
	}

	@Override
	public ChannelStore newChannelStore(String name) {
		return new ChannelStoreDatabase(this, name);
	}

	@Override
	public ServerStore newServerStore(String address) {
		return new ServerStoreDatabase(this, address);
	}

	@Override
	public Set<ServerStore> getServers() {
		try {
			return new HashSet(databaseManager.loadObjects(new ArrayList<ServerStoreDatabase>(), ServerStoreDatabase.class));
		} catch (DatabaseException ex) {
			log.error("Can't load Server's from database", ex);
		}
		return null;
	}

	@Override
	public Set<AdminStore> getAllAdmins() {
		try {
			return (HashSet<AdminStore>) databaseManager.loadObjects(new HashSet<AdminStore>(), AdminStore.class);
		} catch (DatabaseException ex) {
			log.error("Can't load Admin's from database", ex);
		}
		return null;
	}

	@Override
	public void close() throws Exception {
		databaseManager.close();
	}
}
