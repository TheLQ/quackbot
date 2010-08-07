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
package Quackbot.plugins;

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
		String name = StringUtils.split(file.getName(), ".")[0];
		log.info("New JavaScript Plugin: " + name);

		//Make an Engine and a context to use for this plugin
		ScriptEngine jsEngine = new ScriptEngineManager().getEngineByName("JavaScript");
		jsEngine.eval(new FileReader("plugins/QuackUtils.js"));
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
		jsEngine.eval(new FileReader("plugins/QuackUtils.js"));
		jsEngine.eval(new FileReader("plugins/JSPlugin.js"));
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

	public String genQuackUtils() {
		return "var util = false;\n"
				+ "var hook = null;\n"
				+ "var ignore = false;\n"
				+ "var parameters = 0;\n"
				+ "var admin = false;\n"
				+ "var enabled = true;\n"
				+ "var help = '';\n"
				+ "var QuackUtils = {\n"
				+ "	toJavaArray: function(type, arr) {\n"
				+ "		var jArr;\n"
				+ "		if(arr.length) {\n"
				+ "			jArr = java.lang.reflect.Array.newInstance(type, arr.length);\n"
				+ "			for(var i=0;i<arr.length;i++)\n"
				+ "				jArr[i] = arr[i];\n"
				+ "		}\n"
				+ "		else {\n"
				+ "			jArr = java.lang.reflect.Array.newInstance(type, 1);\n"
				+ "			jArr[0] = arr;\n"
				+ "		}\n"
				+ "		return jArr;\n"
				+ "	},\n"
				+ "	getRequiredParams: function() {\n"
				+ "		//Is it even set?\n"
				+ "		if(typeof parameters == 'undefined')\n"
				+ "			return 0;\n"
				+ "		else if(typeof(parameters) == 'object')\n"
				+ "			if(QuackUtils.isArray(parameters))\n"
				+ "				return parameters[0]\n"
				+ "			else if(typeof parameters.required == 'undefined')\n"
				+ "				return 0;\n"
				+ "			else //Required field must be a number\n"
				+ "				return parameters.required;\n"
				+ "		//Must be a number\n"
				+ "		else\n"
				+ "			return parameters;\n"
				+ "	},\n"
				+ "	getOptionalParams: function() {\n"
				+ "		if(typeof parameters == 'undefined')\n"
				+ "			return 0;\n"
				+ "		else if(typeof(parameters) == 'object')\n"
				+ "			if(QuackUtils.isArray(parameters))\n"
				+ "				return parameters[1]\n"
				+ "			else if(typeof parameters.optional== 'undefined')\n"
				+ "				return 0;\n"
				+ "			else //Required field must be a string or a number\n"
				+ "				return parameters.optional;\n"
				+ "		//Must be a number, but only for required\n"
				+ "		else\n"
				+ "			return 0;\n"
				+ "	},\n"
				+ "	isArray: function(object) {\n"
				+ "		return object.length && typeof object != 'string';\n"
				+ "	},\n"
				+ "	pickBest: function(param, defult) {\n"
				+ "		if(typeof param == 'undefined')\n"
				+ "			return defult;\n"
				+ "		else\n"
				+ "			return param;\n"
				+ "	},\n"
				+ "	stringClass: new java.lang.String().getClass()\n"
				+ "}\n"
				+ "function getEnabled() {\n"
				+ "	return command.getEnabled();\n"
				+ "}\n"
				+ "function getBot() {\n"
				+ "	return Bot.getThreadLocal();\n"
				+ "}\n"
				+ "function setEnabled(value) {\n"
				+ "	return command.setEnabled(value);\n"
				+ "}\n"
				+ "function getAdmin() {\n"
				+ "	return command.getAdmin();\n"
				+ "}\n"
				+ "function setAdmin(value) {\n"
				+ "	return command.setAdmin(value);\n"
				+ "}\n"
				+ "function getHelp() {\n"
				+ "	return command.getHelp();\n"
				+ "}\n"
				+ "function setHelp(value) {\n"
				+ "	return command.setHelp(value);\n"
				+ "}\n"
				+ "function getEnabled() {\n"
				+ "	return command.getEnabled();\n"
				+ "}";
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
