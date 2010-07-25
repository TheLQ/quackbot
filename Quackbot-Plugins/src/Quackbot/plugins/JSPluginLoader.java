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

import Quackbot.Command;
import Quackbot.CommandManager;

import Quackbot.PluginLoader;
import Quackbot.err.QuackbotException;
import Quackbot.hook.BaseHook;
import Quackbot.hook.HookManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import org.apache.commons.lang.ArrayUtils;
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
		//Basic setup

		/**
		 * Compiled script (for faster execution)
		 */
		String name = StringUtils.split(file.getName(), ".")[0];
		log.debug("New JavaScript Plugin: " + name);

		//Read File Line By Line
		BufferedReader input = new BufferedReader(new FileReader(file));
		StringBuilder fileContents = new StringBuilder();
		fileContents.append("importPackage(Packages.Quackbot);");
		fileContents.append("importPackage(Packages.Quackbot.info);");
		fileContents.append("importPackage(Packages.Quackbot.hook);");
		fileContents.append("importPackage(Packages.Quackbot.err);");
		fileContents.append("importClass(Packages.java.lang.Thread);");
		String strLine;
		while ((strLine = input.readLine()) != null)
			fileContents.append(strLine).append(System.getProperty("line.separator"));
		input.close();

		//Make an Engine and a context to use for this plugin
		final ScriptEngine jsEngine = new ScriptEngineManager().getEngineByName("JavaScript");
		final Bindings scope = jsEngine.getBindings(ScriptContext.ENGINE_SCOPE);
		final Invocable inv = (Invocable) jsEngine;
		jsEngine.eval(genQuackUtils());
		jsEngine.eval(fileContents.toString());

		//Should we just ignore this?
		if (castToBoolean(scope.get("ignore"))) {
			log.info("Ignore variable set, skipping");
			return;
		}


		//Add the QuackUtils js utility class
		Object quackUtils = scope.get("QuackUtils");

		//Get metho

		//Is this a hook?
		for (String curFunction : scope.keySet())
			if (StringUtils.startsWithAny(curFunction, new String[]{"get", "set", "is"}) && HookManager.getNames().contains(curFunction)) {
				//It contains a hook method, assume that the whole thing is a hook
				HookManager.addPluginHook(((Invocable) jsEngine).getInterface(BaseHook.class));
				return;
			}

		if (scope.get("params") != null)
			throw new QuackbotException("Should use up to date params");

		//Fill in cmd Info
		boolean admin = castToBoolean(scope.get("admin"));
		boolean enabled = castToBoolean(scope.get("enabled"));
		boolean util = castToBoolean(scope.get("util"));
		String help = (String) scope.get("help");
		String src = fileContents.toString();


		int optionalParams = ((Double) inv.invokeMethod(quackUtils, "getOptionalParams")).intValue();
		int requiredParams = ((Double) inv.invokeMethod(quackUtils, "getRequiredParams")).intValue();


		scope.put("log", LoggerFactory.getLogger("Quackbot.plugins.js." + name));
		scope.put("file", file);
		scope.put("name", name);

		CommandManager.addCommand(new JSPlugin(name, help, admin, enabled, file, requiredParams, optionalParams, jsEngine, util, src));
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

	private static class JSPlugin extends Command {
		private final ScriptEngine jsEngine;
		private final boolean util;
		private final String src;
		private final Bindings scope;
		public static CompiledScript compiled;

		public JSPlugin(String name, String help, boolean admin, boolean enabled, File file, int optionalParams, int requiredParams, ScriptEngine jsEngine, boolean util, String src) {
			super(name, help, admin, enabled, file, optionalParams, requiredParams);
			this.jsEngine = jsEngine;
			this.util = util;
			this.src = src;
			this.scope = jsEngine.getBindings(ScriptContext.ENGINE_SCOPE);
		}

		@Override
		public void onCommand(String channel, String sender, String login, String hostname, String[] args) throws Exception {
			Object[] baseArgs = new Object[]{channel, sender, login, hostname};
			if (jsEngine.eval("typeof(onCommand)").equals("function"))
				if (castToBoolean(scope.get("expandArgs")))
					handle().invokeFunction("onCommand", ArrayUtils.addAll(baseArgs, args));
				else
					handle().invokeFunction("onCommand", ArrayUtils.add(baseArgs, args));
		}

		@Override
		public void onCommandChannel(String channel, String sender, String login, String hostname, String[] args) throws Exception {
			Object[] baseArgs = new Object[]{channel, sender, login, hostname};
			if (jsEngine.eval("typeof(onCommandChannel)").equals("function"))
				if (castToBoolean(scope.get("expandArgs")))
					handle().invokeFunction("onCommandChannel", ArrayUtils.addAll(baseArgs, args));
				else
					handle().invokeFunction("onCommandChannel", ArrayUtils.add(baseArgs, args));
		}

		@Override
		public void onCommandPM(String sender, String login, String hostname, String[] args) throws Exception {
			Object[] baseArgs = new Object[]{sender, login, hostname};
			if (jsEngine.eval("typeof(onCommandPM)").equals("function"))
				if (castToBoolean(scope.get("expandArgs")))
					handle().invokeFunction("onCommandPM", ArrayUtils.addAll(baseArgs, args));
				else
					handle().invokeFunction("onCommandPM", ArrayUtils.add(baseArgs, args));
		}

		protected Invocable handle() throws Exception {
			if (compiled == null) {
				//Get all utils to add to command header
				StringBuilder compSrc = new StringBuilder();
				for (Command curCmd : CommandManager.getCommands())
					if (curCmd instanceof JSPlugin && util)
						compSrc.append(src);
				compiled = ((Compilable) jsEngine).compile(compSrc + src);
			}
			scope.put("log", LoggerFactory.getLogger("Quackbot.plugins.js." + getName()));
			scope.put("command", this);
			compiled.eval(scope);
			return (Invocable) compiled.getEngine();
		}
	}
}
