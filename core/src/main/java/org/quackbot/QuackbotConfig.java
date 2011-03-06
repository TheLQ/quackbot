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
import lombok.Data;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Owner
 */
@Data
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
	private int outputThrottleMs = 1750;
	private boolean startGui = true;
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
	 * @return the version
	 */
	public String getVersion() {
		String output = "";
		if (StringUtils.isNotBlank(version))
			output = version + " - ";
		return output + suffix;
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
	 * Start the bot
	 * @return The Quackbot Controller
	 */
	public Controller start() {
		Controller controller = new Controller(this);
		controller.start();
		return controller;
	}
}
