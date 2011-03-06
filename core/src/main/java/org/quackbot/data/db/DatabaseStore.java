

package org.quackbot.data.db;

import ejp.DatabaseManager;
import javax.sql.DataSource;
import lombok.Data;
import org.quackbot.Controller;
import org.quackbot.data.AdminStore;
import org.quackbot.data.ChannelStore;
import org.quackbot.data.DataStore;
import org.quackbot.data.ServerStore;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
@Data
public class DatabaseStore implements DataStore {
	protected DatabaseManager databaseManager = null;
	
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
	
	public AdminStore newAdminStore(String name) {
		return new AdminStoreDatabase(this, name);
	}

	public ChannelStore newChannelStore(String name) {
		return new ChannelStoreDatabase(this, name);
	}

	public ServerStore newServerStore(String address) {
		return new ServerStoreDatabase(this, address);
	}
	
}
