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
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.pircbotx.hooks.Event;
import org.quackbot.err.QuackbotException;
import org.quackbot.events.CommandEvent;
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
		evalResource(jsEngine, sourceMap, "JSPluginBase/QuackUtils.js");
		evalResource(jsEngine, sourceMap, "JSPluginBase/JSPlugin.js");
		evalResource(jsEngine, sourceMap, fileLocation);

		//Should we just ignore this?
		if (castToBoolean(jsEngine.get("ignore"))) {
			log.debug("Ignore variable set, skipping");
			return null;
		}

		//Return the appropiate hook
		if (jsEngine.get("onCommand") != null)
			//Has Command functions, return command
			return new JSCommandWrapper(jsEngine, sourceMap, fileLocation, name);

		//Assume hook
		return new JSHookWrapper(jsEngine, sourceMap, fileLocation, name);
	}

	protected void evalResource(ScriptEngine jsEngine, Map<String, String> sourceMap, String fileLocation) throws QuackbotException {
		BufferedReader reader = null;
		try {
			InputStream stream = getClass().getClassLoader().getResourceAsStream(fileLocation);
			if (stream == null)
				//Try opening it as a file
				reader = new BufferedReader(new FileReader(fileLocation));
			else
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

	protected Object invokeFunction(ScriptEngine jsEngine, Map<String, String> sourceMap, String functionName, Object... args) throws JSHookException {
		try {
			return ((Invocable) jsEngine).invokeFunction(functionName, args);
		} catch (ScriptException ex) {
			//Calculate where the exception occured at
			int lastLine = 0;
			for (Map.Entry<String, String> curEntry : sourceMap.entrySet()) {
				int fileLen = curEntry.getValue().split(System.getProperty("line.separator")).length;
				if (lastLine <= ex.getLineNumber() && ex.getLineNumber() >= fileLen)
					throw new JSHookException("Exception encountered when invoking function " + functionName, curEntry.getKey(), ex.getLineNumber() - lastLine, ex.getColumnNumber(), ex);
				else
					lastLine += fileLen;
			}
			throw new JSHookException("Exception encountered when invoking function " + functionName, "unknown", ex.getLineNumber(), ex.getColumnNumber(), ex);
		} catch (NoSuchMethodException ex) {
			throw new JSHookException("Can't find function " + functionName + " in file(s) " + StringUtils.join(sourceMap.keySet().toArray()), ex);
		}
	}

	public class JSHookWrapper extends Hook {
		protected ScriptEngine jsEngine;
		protected Map<String, String> sourceMap;

		public JSHookWrapper(ScriptEngine jsEngine, Map<String, String> sourceMap, String fileLocation, String name) {
			super(fileLocation, name);
			this.jsEngine = jsEngine;
			this.sourceMap = sourceMap;
		}

		@Override
		public void onEvent(Event event) throws Exception {
			invokeFunction(jsEngine, sourceMap, "on" + StringUtils.removeEnd(event.getClass().getSimpleName(), "Event"), event);
		}
	}

	public class JSCommandWrapper extends Command {
		protected ScriptEngine jsEngine;
		protected Map<String, String> sourceMap;

		public JSCommandWrapper(ScriptEngine jsEngine, Map<String, String> sourceMap, String fileLocation, String name) {
			super(fileLocation, name);
			this.jsEngine = jsEngine;
			this.sourceMap = sourceMap;
		}

		@Override
		public void onEvent(Event event) throws Exception {
			invokeFunction(jsEngine, sourceMap, "on" + StringUtils.removeEnd(event.getClass().getSimpleName(), "Event"), event);
		}

		@Override
		public int getOptionalParams() {
			return (Integer) invokeFunction(jsEngine, sourceMap, "getOptionalParams");
		}

		@Override
		public int getRequiredParams() {
			return (Integer) invokeFunction(jsEngine, sourceMap, "getRequiredParams");
		}

		@Override
		public String onCommand(CommandEvent event) throws Exception {
			return (String) invokeFunction(jsEngine, sourceMap, "onCommand", ArrayUtils.add(event.getArgs(), 0 , event));
		}
	}

	public class JSHookException extends RuntimeException {
		public JSHookException(String message, String fileLocation, int lineNumber, int column, Throwable cause) {
			super(message + " - Error Source: File location: " + fileLocation + " | Line number: " + lineNumber + " | Column: " + column, cause);
		}

		public JSHookException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
