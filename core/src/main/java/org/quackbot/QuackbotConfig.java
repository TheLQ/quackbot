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
	

	/**
	 * Creates a blank configuration
	 */
	public QuackbotConfig() {
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
