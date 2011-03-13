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
package org.quackbot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.quackbot.data.DataStore;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
@Data
public class QuackbotConfig {
	/**
	 * Global Prefixes.
	 */
	@Setter(AccessLevel.NONE)
	protected List<String> prefixes = Collections.synchronizedList(new ArrayList<String>());
	/**
	 * All registered plugin types
	 */
	private TreeMap<String, HookLoader> pluginLoaders = new TreeMap<String, HookLoader>();
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
	private DataStore storage;
	private int defaultPort = 6667;

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
	public void addPluginLoader(HookLoader loader, String ext) {
		addPluginLoader(loader, new String[]{ext});
	}

	/**
	 * Register a custom plugin type with Quackbot, associating with the specified extentions
	 * @param exts     Extention to associate Command Type with
	 * @param newType Class of Command Type
	 */
	public void addPluginLoader(HookLoader loader, String[] exts) {
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
	
	public boolean addPrefix(String prefix) {
		return prefixes.add(prefix);
	}
	
	public boolean removePrefix(String prefix) {
		return prefixes.add(prefix);
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
