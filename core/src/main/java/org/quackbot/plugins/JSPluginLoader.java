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
package org.quackbot.plugins;

import Quackbot.BaseCommand;
import Quackbot.CommandManager;

import Quackbot.PluginLoader;
import Quackbot.err.QuackbotException;
import Quackbot.hook.HookManager;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JS utility bean, holds all information about JS plugin
 * @author Lord.Quackstar
 */
public class JSPluginLoader implements PluginLoader {
	/**
	 * Log4j Logger
	 */
	private static Logger log = LoggerFactory.getLogger(JSPluginLoader.class);

	@Override
	public void load(File file) throws Exception {
		if(file.getName().equals("JS_Template.js") || file.getName().equals("QuackUtils.js"))
			//Ignore this
			return;

		String name = StringUtils.split(file.getName(), ".")[0];
		log.info("New JavaScript Plugin: " + name);

		//Make an Engine and a context to use for this plugin
		ScriptEngine jsEngine = new ScriptEngineManager().getEngineByName("JavaScript");
		jsEngine.eval(new FileReader(new File(getClass().getResource("/JSPluginResources/QuackUtils.js").toURI())));
		jsEngine.eval(new FileReader(file));

		//Should we just ignore this?
		if (castToBoolean(jsEngine.get("ignore"))) {
			log.debug("Ignore variable set, skipping");
			return;
		}

		//Add the QuackUtils js utility class
		//Object quackUtils = scope.get("QuackUtils");

		//Is this a hook?
		for (String curFunction : jsEngine.getBindings(ScriptContext.ENGINE_SCOPE).keySet())
			if (HookManager.getNames().contains(curFunction))
				//It contains a hook method, assume that the whole thing is a hook
				//HookManager.addPluginHook(((Invocable) jsEngine).getInterface(BaseHook.class));
				//return;
				throw new QuackbotException("Hooks not supported");

		//Must be a Command
		jsEngine = new ScriptEngineManager().getEngineByName("JavaScript");
		jsEngine.put("log", LoggerFactory.getLogger("JSPlugins." + name));
		jsEngine.eval(new FileReader(new File(getClass().getResource("/JSPluginResources/QuackUtils.js").toURI())));
		jsEngine.eval(new FileReader(new File(getClass().getResource("/JSPluginResources/JSPlugin.js").toURI())));
		jsEngine.eval(new FileReader(file));
		if (jsEngine.get("onCommand") == null)
			jsEngine.eval("function onCommand() {return null;}");

		BaseCommand cmd = JSPluginProxy.newInstance(((Invocable) jsEngine).getInterface(BaseCommand.class), jsEngine);
		cmd.setup(name, null, true, true, file, 0, 0);
		CommandManager.addCommand(cmd);
	}

	public static boolean castToBoolean(Object obj) {
		if (obj == null || !(obj instanceof Boolean))
			return false;
		return (Boolean) obj;
	}

	public static class JSPluginProxy implements InvocationHandler {
		BaseCommand obj;
		ScriptEngine jsEngine;

		public JSPluginProxy(BaseCommand obj, ScriptEngine jsEngine) {
			this.obj = obj;
			this.jsEngine = jsEngine;
		}

		public static BaseCommand newInstance(BaseCommand obj, ScriptEngine jsEngine) {
			return (BaseCommand) Proxy.newProxyInstance(obj.getClass().getClassLoader(), obj.getClass().getInterfaces(), new JSPluginProxy(obj, jsEngine));
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			Object returned = null;
			try {
				if (method.getName().equalsIgnoreCase("onCommand"))
					//Throw away calls since onCommand is called indirectly
					return null;
				if (method.getName().equalsIgnoreCase("onCommandPM"))
					obj.getBot().sendMessage((String) args[0], (String) ((Invocable) jsEngine).invokeFunction("onCommand", (Object[]) args[3]));
				if (method.getName().equalsIgnoreCase("onCommandChannel"))
					obj.getBot().sendMessage((String) args[0], (String) args[1], (String) ((Invocable) jsEngine).invokeFunction("onCommand", (Object[]) args[4]));
				returned = method.invoke(obj, args);
			} catch (InvocationTargetException e) {
				//Unwrap several times
				throw e.getCause().getCause().getCause();
			}
			return returned;
		}
	}
}
