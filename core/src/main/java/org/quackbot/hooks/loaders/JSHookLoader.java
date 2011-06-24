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
package org.quackbot.hooks.loaders;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import lombok.Cleanup;
import org.apache.commons.lang.StringUtils;
import org.pircbotx.Channel;
import org.pircbotx.User;
import org.pircbotx.hooks.Event;
import org.quackbot.err.QuackbotException;
import org.quackbot.hooks.Command;
import org.quackbot.hooks.HookLoader;
import org.quackbot.hooks.Hook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JS utility bean, holds all information about JS plugin
 * @author Lord.Quackstar
 */
public class JSHookLoader implements HookLoader {
	/**
	 * Log4j Logger
	 */
	private static Logger log = LoggerFactory.getLogger(JSHookLoader.class);

	@Override
	public Hook load(String fileLocation) throws Exception {
		if (fileLocation.endsWith("JS_Template.js") || fileLocation.endsWith("QuackUtils.js"))
			//Ignore this
			return null;

		String[] pathParts = StringUtils.split(fileLocation, System.getProperty("file.separator"));
		String name = StringUtils.split(pathParts[pathParts.length - 1], ".")[0];
		log.info("New JavaScript Plugin: " + name);

		//Add utilities and wrappings
		ScriptEngine jsEngine = new ScriptEngineManager().getEngineByName("JavaScript");
		jsEngine.put("log", LoggerFactory.getLogger("JSPlugins." + name));
		LinkedHashMap<String, String> sourceMap = new LinkedHashMap();
		evalResource(jsEngine, sourceMap, "JSPluginResources/QuackUtils.js");
		evalResource(jsEngine, sourceMap, "JSPluginResources/JSPlugin.js");
		evalResource(jsEngine, sourceMap, fileLocation);

		//Should we just ignore this?
		if (castToBoolean(jsEngine.get("ignore"))) {
			log.debug("Ignore variable set, skipping");
			return null;
		}

		//Return the appropiate hook
		if (jsEngine.get("onCommand") != null || jsEngine.get("onCommandPM") != null || jsEngine.get("onCommandChannel") != null)
			//Has Command functions, return command
			return new JSCommandWrapper(jsEngine, fileLocation, name);

		//Assume hook
		return new JSHookWrapper(jsEngine, fileLocation, name);
	}

	protected void evalResource(ScriptEngine jsEngine, Map<String, String> sourceMap, String fileLocation) throws QuackbotException {
		BufferedReader reader = null;
		try {
			InputStream stream = getClass().getClassLoader().getResourceAsStream(fileLocation);
			if (stream == null)
				//Try opening it as a file
				reader = new BufferedReader(new FileReader(fileLocation));
			reader = new BufferedReader(new InputStreamReader(stream));
			jsEngine.eval(reader);

			//Dump contents
			StringBuilder contentBuilder = new StringBuilder();
			String curLine = "";
			while ((curLine = reader.readLine()) != null)
				contentBuilder.append(curLine);
			sourceMap.put(fileLocation, contentBuilder.toString());
		} catch (Exception e) {
			throw new QuackbotException("Can't load Javascript file at " + fileLocation, e);
		}
	}

	public boolean castToBoolean(Object obj) {
		if (obj == null || !(obj instanceof Boolean))
			return false;
		return (Boolean) obj;
	}

	public class JSHookWrapper extends Hook {
		protected ScriptEngine jsEngine;
		
		public JSHookWrapper(ScriptEngine jsEngine, String fileLocation, String name) {
			super(fileLocation, name);
			this.jsEngine = jsEngine;
		}

		@Override
		public void onEvent(Event event) throws Exception {
			String name = StringUtils.removeEnd(event.getClass().getSimpleName(), "Event");
			((Invocable) jsEngine).invokeFunction("on" + name, new Object[]{event});
		}
	}

	public class JSCommandWrapper extends Command {
		protected ScriptEngine jsEngine;

		public JSCommandWrapper(ScriptEngine jsEngine, String fileLocation, String name) {
			super(fileLocation, name);
			this.jsEngine = jsEngine;
		}

		@Override
		public void onEvent(Event event) throws Exception {
			String name = StringUtils.removeEnd(event.getClass().getSimpleName(), "Event");
			((Invocable) jsEngine).invokeFunction("on" + name, new Object[]{event});
		}

		@Override
		public int getOptionalParams() {
			try {
				return (Integer) (((Invocable) jsEngine).invokeFunction("getOptionalParams", new Object[]{}));
			} catch (Exception ex) {
				throw new RuntimeException("Error encountered when executing Javascript function getOptionalParams");
			}
		}

		@Override
		public int getRequiredParams() {
			try {
				return (Integer) (((Invocable) jsEngine).invokeFunction("getRequiredParams", new Object[]{}));
			} catch (Exception ex) {
				throw new RuntimeException("Error encountered when executing Javascript function getRequiredParams");
			}
		}

		@Override
		public String onCommand(Channel chan, User user, String[] args) throws Exception {
			return (String) (((Invocable) jsEngine).invokeFunction("onCommand", (Object[]) args));
		}

		@Override
		public String onCommandChannel(Channel chan, User user, String[] args) throws Exception {
			return (String) (((Invocable) jsEngine).invokeFunction("onCommandChannel", (Object[]) args));
		}

		@Override
		public String onCommandPM(User user, String[] args) throws Exception {
			return (String) (((Invocable) jsEngine).invokeFunction("onCommandPM", (Object[]) args));
		}
	}
}
