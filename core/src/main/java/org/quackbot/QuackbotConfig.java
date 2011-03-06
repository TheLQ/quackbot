/*
 * Copyright (C) 2009-2010 Leon Blakey
 *
 * Quackedbot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Quackedbot  is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.quackbot;

import ch.qos.logback.classic.Level;
import ejp.DatabaseManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import javax.sql.DataSource;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Owner
 */
public class QuackbotConfig {
	private DatabaseManager dbm = null;
	/**
	 * Global Prefixes.
	 */
	private List<String> globPrefixes = Collections.synchronizedList(new ArrayList<String>());
	/**
	 * All registered plugin types
	 */
	private TreeMap<String, PluginLoader> pluginLoaders = new TreeMap<String, PluginLoader>();
	/**
	 * Wait between sending messages
	 */
	private int msgWait = 1750;
	private boolean makeGui = true;
	private String version = "";
	private String finger = "";
	private final String suffix = "Quackbot Java IRC Framework 3.3 http://quackbot.googlecode.com/";
	private String nick = "Quackbot";
	private String name = "Quackbot";
	private Level databaseLogLevel = Level.OFF;
	

	/**
	 * Creates a blank configuration
	 */
	public QuackbotConfig() {
	}

	/**
	 * Set the log level of JPersist. By default its OFF, but can be changed for debugging
	 * @param level JUT logging level
	 */
	public void setDatabaseLogLevel(Level level) {
		databaseLogLevel = level;
	}

	public Level getDatabaseLogLevel() {
		return databaseLogLevel;
	}

	/**
	 * Register a custom plugin type with Quackbot, associating with the specified extention
	 * @param ext     Exentsion to associate Command Type with
	 * @param newType Class of Command Type
	 */
	public void addPluginLoader(PluginLoader loader, String ext) {
		addPluginLoader(loader, new String[]{ext});
	}

	/**
	 * Register a custom plugin type with Quackbot, associating with the specified extentions
	 * @param exts     Extention to associate Command Type with
	 * @param newType Class of Command Type
	 */
	public void addPluginLoader(PluginLoader loader, String[] exts) {
		for (String curExt : exts)
			getPluginLoaders().put(curExt, loader);
	}

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
		dbm = DatabaseManager.getDatabaseManager(databaseName, poolSize, dataSource);

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
		dbm = DatabaseManager.getDatabaseManager(databaseName, poolSize, jndiUri, username, password);
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
		dbm = DatabaseManager.getDatabaseManager(databaseName, poolSize, driver, url, username, password);
	}

	public DatabaseManager getDatabase() {
		return dbm;
	}

	/**
	 * @return the msgWait
	 */
	public int getMsgWait() {
		return msgWait;
	}

	/**
	 * @param msgDelay the msgWait to setMessageDelay
	 */
	public void setMessageDelay(int msgDelay) {
		msgWait = msgDelay;
	}

	public void addPrefix(String prefix) {
		globPrefixes.add(prefix);
	}

	public void removePrefix(String prefix) {
		globPrefixes.remove(prefix);
	}

	public List<String> getPrefixes() {
		return globPrefixes;
	}

	public void disableGui(boolean disable) {
		makeGui = disable;
	}

	public boolean isGuiEnabled() {
		return makeGui;
	}

	/**
	 * @return the pluginLoaders
	 */
	public TreeMap<String, PluginLoader> getPluginLoaders() {
		return pluginLoaders;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		String output = "";
		if (StringUtils.isNotBlank(version))
			output = version + " - ";
		return output + suffix;
	}

	/**
	 * @param version the version to setMessageDelay
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @return the versionSuffix
	 */
	public String getSuffix() {
		return suffix;
	}

	/**
	 * @return the nick
	 */
	public String getNick() {
		return nick;
	}

	/**
	 * @param nick the nick to setMessageDelay
	 */
	public void setNick(String nick) {
		this.nick = nick;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to setMessageDelay
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the finger
	 */
	public String getFinger() {
		String output = "";
		if (StringUtils.isNotBlank(finger))
			output = finger + " - ";
		return output + suffix;
	}

	/**
	 * @param finger the finger to setMessageDelay
	 */
	public void setFinger(String finger) {
		this.finger = finger;
	}
}
