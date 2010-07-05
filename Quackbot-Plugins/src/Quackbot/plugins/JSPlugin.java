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
import java.util.ArrayList;
import java.util.List;
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
import sun.org.mozilla.javascript.internal.NativeArray;
import sun.org.mozilla.javascript.internal.NativeFunction;
import sun.org.mozilla.javascript.internal.NativeObject;

/**
 * JS utility bean, holds all information about JS plugin
 * @author Lord.Quackstar
 */
public class JSPlugin implements PluginType {
	private String name;
	private String help;
	private boolean admin = false;
	private boolean ignore = false;
	private boolean service = false;
	private boolean util = false;
	private int params = 0;
	private int optParams = 0;
	private File file;
	/**
	 * Log4j Logger
	 */
	private static Logger log = LoggerFactory.getLogger(JSPlugin.class);
	/**
	 * Scope of current engine
	 */
	private Bindings scope;
	/**
	 * Compiled script (for faster execution)
	 */
	private CompiledScript compiled;
	/**
	 * The engine used by this script
	 */
	private ScriptEngine jsEngine = new ScriptEngineManager().getEngineByName("JavaScript");
	/**
	 * Names of variables of required variables
	 */
	private List<String> reqVars;
	/**
	 * Names of variables of optional variables
	 */
	private List<String> optVars;
	/**
	 * Raw source code
	 */
	private String src;

	public void load(File file) throws Exception {
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
		fileContents.append("importClass(Packages.java.lang.Thread);");
		String strLine;
		while ((strLine = input.readLine()) != null)
			fileContents.append(strLine).append(System.getProperty("line.separator"));
		input.close();

		//Make an Engine and a context to use for this plugin
		jsEngine = new ScriptEngineManager().getEngineByName("JavaScript");
		Bindings engineScope = jsEngine.getBindings(ScriptContext.ENGINE_SCOPE);
		jsEngine.eval(fileContents.toString());

		//Check for an invoke function
		Object invFuncSuper = engineScope.get("invoke");
		if (invFuncSuper == null)
			throw new QuackbotException("No invoke function defined!");
		if (!(invFuncSuper instanceof NativeFunction))
			throw new QuackbotException("invoke name overwritten by variable. Must rename invoke variable to something else");

		//Is this a hook?
		Object hook = engineScope.get("hook");
		if (hook != null) {
			List<Event> events = new ArrayList<Event>();
			//Load specified Events
			if (hook instanceof NativeArray) {
				//Must get from JavaScript array object
				NativeArray arr = (NativeArray) hook;
				for (Object o : arr.getIds())
					events.add((Event) arr.get((Integer) o, null));
			} //Is this just an Event?
			else if (hook instanceof Event)
				events.add((Event) hook);
			else
				throw new QuackbotException("Unkown Hook variable type in " + file.getName() + ". Must be either an Event or an array of events.");

			for (Event curEvent : events)
				HookManager.addHook(curEvent, new PluginHook() {
					public void run(HookList hookStack, Bot bot, BotEvent msgInfo) throws Exception {
						JSPlugin.this.invoke(bot, msgInfo);
					}
				});
		}

		if((engineScope.get("params") != null) ) {
			throw new QuackbotException("Should use up to date params");
		}

		//Fill in cmd Info
		setAdmin((engineScope.get("admin") == null) ? false : true);
		setService((engineScope.get("service") == null) ? false : true);
		setIgnore((engineScope.get("ignore") == null) ? false : true);
		setUtil((engineScope.get("util") == null) ? false : true);
		setSrc(fileContents.toString());
		setHelp((String) engineScope.get("help"));

		//Parse paramConfig and paramCount handling the many avalible types (plain,
		//array, and object with required and optional arrays) over two configurations
		Object paramConfigSuper = engineScope.get("paramConfig");
		Object paramCountSuper = engineScope.get("paramCount");
		NativeFunction invokeFunc = (NativeFunction)engineScope.get("invoke");
		if (paramConfigSuper != null && paramCountSuper != null)
			throw new QuackbotException("JS Plugin " + getName() + " can only use one parameter configuration. Must remove one and reload.");
		if (paramConfigSuper != null) {
			if (paramConfigSuper instanceof String)
				reqVars.add((String) paramConfigSuper);
			else if (paramConfigSuper instanceof NativeArray)
				reqVars.addAll(convertToStringList("paramConfig", paramConfigSuper));
			else if (paramConfigSuper instanceof NativeObject) {
				NativeObject paramConfig = (NativeObject) paramConfigSuper;
				Object requiredSuper = paramConfig.get("required", null);
				Object optionalSuper = paramConfig.get("optional", null);
				if (optionalSuper != null)
					if (optionalSuper instanceof String)
						optVars.add((String) optionalSuper);
					else if (optionalSuper instanceof NativeArray)
						optVars.addAll(convertToStringList("optional in paramConfig", optionalSuper));
					else
						throw new QuackbotException("Optional section in paramConfig can only be an array or String");
				else if (requiredSuper != null)
					if (requiredSuper instanceof String)
						reqVars.add((String) requiredSuper);
					else if (requiredSuper instanceof NativeArray)
						reqVars.addAll(convertToStringList("required in paramConfig", requiredSuper));
					else
						throw new QuackbotException("Required section in paramConfig can only be an array or String");
			} else
				throw new QuackbotException("paramConfig can only be a String, array, or object");
			setParams(reqVars.size());
			setOptParams(optVars.size());
		} else if (paramCountSuper != null)
			if (paramCountSuper instanceof Integer)
				setParams((Integer) paramCountSuper);
			else if (paramCountSuper instanceof NativeArray)
				reqVars.addAll(convertToStringList("paramCount", paramCountSuper));
			else if (paramCountSuper instanceof NativeObject) {
				NativeObject paramConfig = (NativeObject) paramCountSuper;
				Object requiredSuper = paramConfig.get("required", null);
				Object optionalSuper = paramConfig.get("optional", null);
				if (optionalSuper != null)
					if (optionalSuper instanceof NativeArray) {
						List<Integer> countList = convertToIntList("optional in paramCount", optionalSuper);
						setParams(countList.get(0));
						if (countList.size() > 1)
							setOptParams(countList.get(0));
						if (countList.size() > 2)
							throw new QuackbotException("optional array in paramCount larger than 2. Must be int his format: [requiredCount, optionalCount]");
					} else
						throw new QuackbotException("Optional section in paramCount can only be an array or String");
				else if (requiredSuper != null) {
					List<Integer> countList = convertToIntList("optional in paramCount", optionalSuper);
					setParams(countList.get(0));
					if (countList.size() > 1)
						setOptParams(countList.get(0));
					if (countList.size() > 2)
						throw new QuackbotException("required array in paramCount larger than 2. Must be int his format: [requiredCount, optionalCount]");
				} else
					throw new QuackbotException("Required section in paramCount can only be an array or String");
			} else
				throw new QuackbotException("paramCount can only be a String, array, or object");
		else if(invokeFunc.getArity() > 0)
			setParams(invokeFunc.getArity());
		else {
			setOptParams(0);
			setParams(0);
		}
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
						compSrc.append(plugin.getSrc());
				}
			compiled = ((Compilable) jsEngine).compile(compSrc + getSrc());
		}

		//Set script globals
		Bindings engineScope = jsEngine.getBindings(ScriptContext.ENGINE_SCOPE);
		log.trace("Msginfo: " + msgInfo.toString());
		engineScope.put("log", LoggerFactory.getLogger("Quackbot.plugins.js." + getName()));
		engineScope.remove("msgInfo");
		engineScope.remove("qb");
		if (bot != null) {
			engineScope.put("msgInfo", msgInfo);
			engineScope.put("qb", bot);
		} else {
			//Prevent "not defined" errors
			engineScope.put("msgInfo", null);
			engineScope.put("qb", null);
		}

		//Setup parameters if required
		List<String> combinedList = new ArrayList<String>(reqVars);
		combinedList.addAll(optVars);
		if (combinedList.size() > 0)
			for (int i = 0; i <= combinedList.size(); i++)
				engineScope.put(combinedList.get(i), msgInfo.getArgs()[i]);
		compiled.eval(engineScope);

		//Call invoke function with invoke
		Invocable inv = (Invocable) compiled.getEngine();
		if (((NativeFunction) engineScope.get("invoke")).getArity() >= msgInfo.getArgs().length)
			inv.invokeFunction("invoke", (Object[]) msgInfo.getArgs());
		else
			inv.invokeFunction("invoke", new Object[]{});
	}

	public List<String> convertToStringList(String name, Object arr) throws QuackbotException {
		NativeArray narr = (NativeArray) arr;
		List<String> nativeList = new ArrayList<String>();
		for (Object o : narr.getIds()) {
			if (!(o instanceof String))
				throw new QuackbotException("Array " + name + " can only contain strings");
			nativeList.add((String) narr.get((Integer) o, null));
		}
		return nativeList;
	}

	public List<Integer> convertToIntList(String name, Object arr) throws QuackbotException {
		NativeArray narr = (NativeArray) arr;
		List<Integer> nativeList = new ArrayList<Integer>();
		for (Object o : narr.getIds()) {
			if (!(o instanceof Integer))
				throw new QuackbotException("Array " + name + " can only contain numbers");
			nativeList.add((Integer) narr.get((Integer) o, null));
		}
		return nativeList;
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
	 * Raw source code (used for versioning)
	 * @return the src
	 */
	public String getSrc() {
		return src;
	}

	/**
	 * Raw source code (used for versioning)
	 * @param src the src to set
	 */
	public void setSrc(String src) {
		this.src = src;
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
	public boolean isIgnore() {
		return ignore;
	}

	/**
	 * Ignore command?
	 * @param ignore the ignore to set
	 */
	public void setIgnore(boolean ignore) {
		this.ignore = ignore;
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
	 * Number of parameters
	 * @return the params
	 */
	public int getParams() {
		return this.params;
	}

	/**
	 * Number of parameters
	 * @param params the params to set
	 */
	public void setParams(int params) {
		this.params = params;
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

	/**
	 * Optional params? This is currently not implemented
	 * @return the optParams
	 */
	public int getOptParams() {
		return optParams;
	}

	/**
	 * Optional params? This is currently not implemented
	 * @param optParams the optParams to set
	 */
	public void setOptParams(int optParams) {
		this.optParams = optParams;
	}
}
