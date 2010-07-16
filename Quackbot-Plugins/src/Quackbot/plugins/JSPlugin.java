/**
 * @(#)JSPlugin.java
 *
 * Copyright Leon Blakey/Lord.Quackstar, 2009-2010
 *
 * This file is part of Quackbot
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
package Quackbot.plugins;

import Quackbot.Bot;
import Quackbot.Controller;
import Quackbot.ParameterConfig;

import Quackbot.PluginType;
import Quackbot.err.QuackbotException;
import Quackbot.hook.Event;
import Quackbot.hook.HookList;
import Quackbot.hook.HookManager;
import Quackbot.hook.PluginHook;
import Quackbot.info.BotEvent;
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
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JS utility bean, holds all information about JS plugin
 * @author Lord.Quackstar
 */
public class JSPlugin implements PluginType {
	private String name;
	private String help;
	private boolean admin = false;
	private boolean enabled = true;
	private boolean service = false;
	private boolean util = false;
	private File file;
	/**
	 * Log4j Logger
	 */
	private static Logger log = LoggerFactory.getLogger(JSPlugin.class);
	/**
	 * Scope of current engine
	 */
	private Bindings engineScope;
	/**
	 * Compiled script (for faster execution)
	 */
	private CompiledScript compiled;
	/**
	 * The engine used by this script
	 */
	private ScriptEngine jsEngine = new ScriptEngineManager().getEngineByName("JavaScript");
	/**
	 * Raw source code
	 */
	private String src;
	private ParameterConfig<String> paramConfig = new ParameterConfig<String>() {
		public void fillByObject(String object, String argument) {
			engineScope.put(object, argument);
		}
	};

	public boolean load(File file) throws Exception {
		//Basic setup
		setName(StringUtils.split(file.getName(), ".")[0]);
		setFile(file);
		log.debug("New JavaScript Plugin: " + getName());

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
		jsEngine = new ScriptEngineManager().getEngineByName("JavaScript");
		engineScope = jsEngine.getBindings(ScriptContext.ENGINE_SCOPE);
		jsEngine.eval(genQuackUtils());
		jsEngine.eval(fileContents.toString());

		//Should we just ignore this?
		if (castToBoolean(engineScope.get("ignore"))) {
			log.info("Ignore variable set, skipping");
			return false;
		}
		

		//Add the QuackUtils js utility class
		Object quackUtils = engineScope.get("QuackUtils");
		Invocable inv = (Invocable) jsEngine;

		//Is this a hook?
		Object hook = engineScope.get("hook");
		if (hook != null) {
			Event[] events = (Event[]) inv.invokeMethod(quackUtils, "toJavaArray", new Object[]{Event.class, hook});
			HookManager.addPluginHooks(events, this);
		}

		if (engineScope.get("params") != null)
			throw new QuackbotException("Should use up to date params");

		//Fill in cmd Info
		setAdmin(castToBoolean(engineScope.get("admin")));
		setService(castToBoolean(engineScope.get("service")));
		setEnabled(castToBoolean(engineScope.get("enabled")));
		setUtil(castToBoolean(engineScope.get("util")));
		setHelp((String) engineScope.get("help"));
		src = fileContents.toString();

		//Check for an invoke function
		String invType = (String) jsEngine.eval("typeof(invoke)");
		if (!invType.equals("function") && !isUtil())
			throw new QuackbotException("Invoke function not declared");

		//Add parameters
		if (jsEngine.get("parameters") != null) {
			inv.invokeMethod(quackUtils, "setOptionalParams", paramConfig);
			inv.invokeMethod(quackUtils, "setRequiredParams", paramConfig);
		}
		return true;
	}

	public void invoke(Bot bot, BotEvent msgInfo) throws Exception {
		log.info("Running Javascript Plugin " + getName());

		//Compile on load (so all utils are loaded)
		if (compiled == null) {
			//Get all utils to add to command header
			StringBuilder compSrc = new StringBuilder();
			for (PluginType curPlugin : Controller.instance.plugins)
				if (curPlugin instanceof JSPlugin) {
					JSPlugin plugin = (JSPlugin) curPlugin;
					if (plugin.isUtil())
						compSrc.append(plugin.src);
				}
			compiled = ((Compilable) jsEngine).compile(compSrc + src);
		}

		//Set script globals
		engineScope = jsEngine.getBindings(ScriptContext.ENGINE_SCOPE);
		log.trace("Msginfo: " + msgInfo.toString());
		engineScope.put("log", LoggerFactory.getLogger("Quackbot.plugins.js." + getName()));
		engineScope.remove("msgInfo");
		engineScope.remove("qb");
		if (bot != null) {
			engineScope.put("event", msgInfo);
			engineScope.put("qb", bot);
		} else {
			//Prevent "not defined" errors
			engineScope.put("event", null);
			engineScope.put("qb", null);
		}
		compiled.eval(engineScope);

		//Call invoke function with invoke
		Invocable inv = (Invocable) compiled.getEngine();
		inv.invokeFunction("invoke", (Object[]) msgInfo.getArgs());
	}

	public boolean castToBoolean(Object obj) {
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
				+ "var service = false;\n"
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
				+ "	setRequiredParams: function(paramConfig) {\n"
				+ "		//Is it even set?\n"
				+ "		if(typeof parameters == 'undefined')\n"
				+ "			return null;\n"
				+ "		else if(typeof(parameters) == 'object')\n"
				+ "			if(QuackUtils.isArray(parameters))   //Is this an array?\n"
				+ "				for(i in parameters)\n"
				+ "					paramConfig.addRequiredObject(parameters[i]);\n"
				+ "			else if(typeof parameters.required == 'undefined') \n"
				+ "				return null;\n"
				+ "			else if(QuackUtils.isArray(parameters.required)) //Is the required field an array?\n"
				+ "				for(i in parameters.required)\n"
				+ "					paramConfig.addRequiredObject(parameters.required[i]);\n"
				+ "			else //Required field must be a string or a number\n"
				+ "				QuackUtils.handleStringNum(paramConfig, true, parameters.required);\n"
				+ "		//Must be a string or a number\n"
				+ "		else\n"
				+ "			QuackUtils.handleStringNum(paramConfig, true, parameters);\n"
				+ "	},\n"
				+ "	setOptionalParams: function(paramConfig) {\n"
				+ "		if(typeof(parameters) == 'object')\n"
				+ "			//Is this an array or non-existant?\n"
				+ "			if(parameters.length || typeof parameters.optional == 'undefined')\n"
				+ "				return null;\n"
				+ "			//Is the optional field an array?\n"
				+ "			else if(parameters.optional.length && typeof parameters.optional != 'string')\n"
				+ "				for(i in parameters.optional)\n"
				+ "					paramConfig.addOptionalObject(parameters.optional[i]);\n"
				+ "			//Must be a string or a number\n"
				+ "			else\n"
				+ "				QuackUtils.handleStringNum(paramConfig,false,parameters.optional);\n"
				+ "		else\n"
				+ "			return null;\n"
				+ "	},\n"
				+ "	handleStringNum: function(paramConfig,required,object) {\n"
				+ "		if(typeof(object) == 'number')\n"
				+ "			if(required)\n"
				+ "				paramConfig.setRequiredCount(object);\n"
				+ "			else\n"
				+ "				paramConfig.setOptionalCount(object);\n"
				+ "		else if(typeof(object) == 'string')\n"
				+ "			if(required)\n"
				+ "				paramConfig.addRequiredObject(object);\n"
				+ "			else\n"
				+ "				paramConfig.addOptionalObject(object);\n"
				+ "		else\n"
				+ "			throw 'Unknown type in parameters for plugin, is '+typeof(object);\n"
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
				+ "}";
	}

	/**
	 * Name of command
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Name of command
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Help for command
	 * @return the help
	 */
	public String getHelp() {
		return help;
	}

	/**
	 * Help for command
	 * @param help the help to set
	 */
	public void setHelp(String help) {
		this.help = help;
	}

	/**
	 * Admin only?
	 * @return the admin
	 */
	public boolean isAdmin() {
		return admin;
	}

	/**
	 * Admin only?
	 * @param admin the admin to set
	 */
	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	/**
	 * Ignore command?
	 * @return the ignore
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Ignore command?
	 * @param ignore the ignore to set
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Is server?
	 * @return the service
	 */
	public boolean isService() {
		return service;
	}

	/**
	 * Is server?
	 * @param service the service to set
	 */
	public void setService(boolean service) {
		this.service = service;
	}

	/**
	 * Is Util?
	 * @return the util
	 */
	public boolean isUtil() {
		return util;
	}

	/**
	 * Is Util?
	 * @param util the util to set
	 */
	public void setUtil(boolean util) {
		this.util = util;
	}

	/**
	 * @return the file
	 */
	public File getFile() {
		return file;
	}

	/**
	 * @param file the file to set
	 */
	public void setFile(File file) {
		this.file = file;
	}

	public ParameterConfig getParamConfig() {
		return paramConfig;
	}
}
