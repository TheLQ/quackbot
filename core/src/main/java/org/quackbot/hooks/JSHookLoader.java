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
package org.quackbot.hooks;

import java.io.File;
import java.io.FileReader;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import org.apache.commons.lang.StringUtils;
import org.pircbotx.Channel;
import org.pircbotx.User;
import org.pircbotx.hooks.Event;
import org.quackbot.Command;
import org.quackbot.HookLoader;
import org.quackbot.hook.Hook;
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
	public Hook load(File file) throws Exception {
		if (file.getName().equals("JS_Template.js") || file.getName().equals("QuackUtils.js"))
			//Ignore this
			return null;

		String name = StringUtils.split(file.getName(), ".")[0];
		log.info("New JavaScript Plugin: " + name);

		//Add utilities and wrappings
		ScriptEngine jsEngine = new ScriptEngineManager().getEngineByName("JavaScript");
		jsEngine.put("log", LoggerFactory.getLogger("JSPlugins." + name));
		jsEngine.eval(new FileReader(new File(getClass().getResource("/JSPluginResources/QuackUtils.js").toURI())));
		jsEngine.eval(new FileReader(new File(getClass().getResource("/JSPluginResources/JSPlugin.js").toURI())));
		jsEngine.eval(new FileReader(file));

		//Should we just ignore this?
		if (castToBoolean(jsEngine.get("ignore"))) {
			log.debug("Ignore variable set, skipping");
			return null;
		}

		//Return the appropiate hook
		if (jsEngine.get("onCommand") != null || jsEngine.get("onCommandPM") != null || jsEngine.get("onCommandChannel") != null)
			//Has Command functions, return command
			return new JSCommandWrapper(jsEngine, file, name);

		//Assume hook
		return new JSHookWrapper(jsEngine, file, name);
	}

	public boolean castToBoolean(Object obj) {
		if (obj == null || !(obj instanceof Boolean))
			return false;
		return (Boolean) obj;
	}

	public class JSHookWrapper extends Hook {
		protected ScriptEngine jsEngine;

		public JSHookWrapper(ScriptEngine jsEngine, File file, String name) {
			super(file, name);
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

		public JSCommandWrapper(ScriptEngine jsEngine, File file, String name) {
			super(file, name);
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
				return (Integer)(((Invocable) jsEngine).invokeFunction("getOptionalParams", new Object[]{}));
			} catch (Exception ex) {
				throw new RuntimeException("Error encountered when executing Javascript function getOptionalParams");
			}
		}

		@Override
		public int getRequiredParams() {
			try {
				return (Integer)(((Invocable) jsEngine).invokeFunction("getRequiredParams", new Object[]{}));
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
