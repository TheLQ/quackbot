/**
 * @(#)JSPlugin.java
 *
 * This file is part of Quackbot
 */
package Quackbot.info;

import javax.script.Bindings;
import javax.script.ScriptContext;

/**
 * JS utility bean, holds all information about JS plugin
 * @author Lord.Quackstar
 */
public class JSPlugin {

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
	 * Is Listener?
	 */
	private boolean listener;
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
	 * Current JS context
	 */
	private ScriptContext context;
	/**
	 * Scope
	 */
	private Bindings scope;

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
	 * Is Listener?
	 * @return the listener
	 */
	public boolean isListener() {
		return listener;
	}

	/**
	 * Is Listener?
	 * @param listener the listener to set
	 */
	public void setListener(boolean listener) {
		this.listener = listener;
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
		return params;
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
}
