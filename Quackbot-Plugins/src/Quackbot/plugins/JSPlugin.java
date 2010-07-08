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
		jsEngine.eval(fileContents.toString());

		//Should we just ignore this?
		if(engineScope.get("ignore") != null && (Boolean)engineScope.get("ignore") == true) {
			log.info("Ignore variable set, skipping");
			return false;
		}

		//Add the QuackUtils js utility class
		jsEngine.eval(new FileReader("plugins/QuackUtils.js"));
		Object quackUtils = engineScope.get("QuackUtils");
		Invocable inv = (Invocable) jsEngine;

		//Is this a hook?
		Object hook = engineScope.get("hook");
		if (hook != null) {
			Object[] obj = new Object[]{Event.class, hook};
			Event[] events = (Event[]) inv.invokeMethod(quackUtils, "toJavaArray", obj);
			for (Event curEvent : events)
				HookManager.addHook(curEvent, new PluginHook() {
					public void run(HookList hookStack, Bot bot, BotEvent msgInfo) throws Exception {
						JSPlugin.this.invoke(bot, msgInfo);
					}
				});
		}

		if ((engineScope.get("params") != null))
			throw new QuackbotException("Should use up to date params");

		//Fill in cmd Info
		setAdmin((engineScope.get("admin") == null) ? false : true);
		setService((engineScope.get("service") == null) ? false : true);
		setEnabled((engineScope.get("enabled") == null) ? true : false);
		setUtil((engineScope.get("util") == null) ? false : true);
		setHelp((String) engineScope.get("help"));
		src = fileContents.toString();

		//Check for an invoke function
		String invType = (String) jsEngine.eval("typeof(invoke)");
		if (!invType.equals("function") && !isUtil())
			throw new QuackbotException("Invoke function not declared");

		//Add parameters
		Object[] paramArgs = new Object[]{paramConfig};
		if (jsEngine.get("parameters") != null) {
			inv.invokeMethod(quackUtils, "setOptionalParams", paramArgs);
			inv.invokeMethod(quackUtils, "setRequiredParams", paramArgs);
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
		inv.invokeFunction("invoke", new Object[]{});
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
