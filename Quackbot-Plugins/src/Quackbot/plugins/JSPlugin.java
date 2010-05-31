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
import Quackbot.InitHook;

import Quackbot.PluginType;
import Quackbot.info.Hooks;
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

	/**
	 * Name of command
	 */
	private String name;
	/**
	 * Raw source code (used for versioning)
	 */
	private String src;
	/**
	 * Help for command
	 */
	private String help;
	/**
	 * Admin only?
	 */
	private boolean admin;
	/**
	 * Ignore command?
	 */
	private boolean ignore;
	/**
	 * Hook?
	 */
	private Hooks hook;
	/**
	 * Is server?
	 */
	private boolean service;
	/**
	 * Is Util?
	 */
	private boolean util;
	/**
	 * Requires Arguments?
	 */
	private boolean reqArg;
	/**
	 * Number of parameters
	 */
	private int params;
	/**
	 * Optional params? This is currently not implemented
	 */
	private int optParams;
	/**
	 * Current JS context
	 */
	private ScriptContext context;
	/**
	 * Scope
	 */
	private Bindings scope;
	private File file;
	private CompiledScript compiled;
	/**
	 * Log4j Logger
	 */
	private static Logger log = LoggerFactory.getLogger(JSPlugin.class);

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
		fileContents.append("importClass(Packages.java.lang.Thread);");
		String strLine;
		while ((strLine = input.readLine()) != null) {
			fileContents.append(strLine + System.getProperty("line.separator"));
		}
		input.close();

		//Make new context
		ScriptEngine jsEngine = new ScriptEngineManager().getEngineByName("JavaScript");
		Bindings engineScope = jsEngine.getBindings(ScriptContext.ENGINE_SCOPE);
		jsEngine.eval(fileContents.toString());

		//Fill in cmd Info
		setAdmin((engineScope.get("admin") == null) ? false : true);
		setService((engineScope.get("service") == null) ? false : true);
		setHook((engineScope.get("hook") == null) ? null : (Hooks) engineScope.get("hook"));
		setIgnore((engineScope.get("ignore") == null) ? false : true);
		setUtil((engineScope.get("util") == null) ? false : true);
		setSrc(fileContents.toString());
		setHelp((String) engineScope.get("help"));
		setReqArg(((engineScope.get("ReqArg") == null) ? false : true));
		if (engineScope.get("param") != null) {
			setParams((int) Double.parseDouble(engineScope.get("param").toString()));
		} else {
			setParams(0);
		}
	}

	public void invoke(String[] args, Bot bot, BotEvent msgInfo) throws Exception {
		log.info("Running Javascript Plugin " + getName());
		ScriptEngine jsEngine = new ScriptEngineManager().getEngineByName("JavaScript");

		//Compile script on first execution for faster run time
		if (getCompiled() == null) {
			//Get all utils to add to command header
			StringBuilder compSrc = new StringBuilder();
			for (PluginType curPlugin : Controller.instance.plugins) {
				if (curPlugin instanceof JSPlugin) {
					JSPlugin plugin = (JSPlugin) curPlugin;
					if (plugin.isUtil()) {
						compSrc.append(plugin.getSrc());
					}
				}
			}
			compSrc.append(getSrc());
			Compilable compilingEngine = (Compilable) jsEngine;
			setCompiled(compilingEngine.compile(compSrc.toString()));
		}

		//Set script globals
		Bindings engineScope = jsEngine.getBindings(ScriptContext.ENGINE_SCOPE);
		engineScope.put("log", LoggerFactory.getLogger("Quackbot.plugins.js." + getName()));
		if (bot != null) {
			engineScope.put("msgInfo", msgInfo);
			engineScope.put("qb", bot);
		} else {
			//Prevent "not defined" errors
			engineScope.put("msgInfo", null);
			engineScope.put("qb", null);
		}
		getCompiled().eval(engineScope);

		//Call invoke function with invoke
		Invocable inv = (Invocable) getCompiled().getEngine();
		inv.invokeFunction("invoke", (Object[]) args);
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
	 * Requires Arguments?
	 * @return the reqArg
	 */
	public boolean isReqArg() {
		return reqArg;
	}

	/**
	 * Requires Arguments?
	 * @param reqArg the reqArg to set
	 */
	public void setReqArg(boolean reqArg) {
		this.reqArg = reqArg;
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
	 * Current JS context
	 * @return the context
	 */
	public ScriptContext getContext() {
		return context;
	}

	/**
	 * Current JS context
	 * @param context the context to set
	 */
	public void setContext(ScriptContext context) {
		this.context = context;
	}

	/**
	 * Scope
	 * @return the scope
	 */
	public Bindings getScope() {
		return scope;
	}

	/**
	 * Scope
	 * @param scope the scope to set
	 */
	public void setScope(Bindings scope) {
		this.scope = scope;
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
	 * @return the compiled
	 */
	public CompiledScript getCompiled() {
		return compiled;
	}

	/**
	 * @param compiled the compiled to set
	 */
	public void setCompiled(CompiledScript compiled) {
		this.compiled = compiled;
	}

	/**
	 * Hook?
	 * @return the hook
	 */
	public Hooks getHook() {
		return hook;
	}

	/**
	 * Hook?
	 * @param hook the hook to set
	 */
	public void setHook(Hooks hook) {
		this.hook = hook;
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
