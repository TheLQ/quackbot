/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Quackbot.info;

import Quackbot.annotations.HelpDoc;
import Quackbot.plugins.core.BasePlugin;
import Quackbot.plugins.core.JavaTest;
import java.lang.annotation.Annotation;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author admins
 */
@HelpDoc("Generic plugin help")
public class JavaPlugin {

	/**
	 * Name of command
	 */
	private String name = "";
	/**
	 * Help for command. HIGHLY recommended to override
	 */
	private String help = "";
	/**
	 * FQCN of command, needed for dynamic reloading
	 */
	private String fqcn = "";
	/**
	 * Admin only?
	 */
	private boolean admin = false;
	/**
	 * Ignore command?
	 */
	private boolean ignore = false;
	/**
	 * Is Listener?
	 */
	private boolean listener = false;
	/**
	 * Is server?
	 */
	private boolean service = false;
	/**
	 * Is Util?
	 */
	private boolean util = false;
	/**
	 * Requires Arguments?
	 */
	private boolean reqArg = false;
	/**
	 *
	 */
	private static Logger log = Logger.getLogger(JavaPlugin.class);

	/**
	 * Empty Constructor. Shouldn't be used
	 */
	public JavaPlugin() {
	}

	/**
	 * Creates empty JavaPlugin with class name
	 * @param FQCN Fully Qualified Class Name of Java plugin
	 */
	public JavaPlugin(String className) {
		this.fqcn = className;
		String[] fqcn = StringUtils.split(className, ".");
		this.name = fqcn[fqcn.length-1];
		try {
			setHelp(newInstance().getClass().getAnnotation(HelpDoc.class).value());
		}
		catch(Exception e) {
			log.error("Cannot load help of command "+name,e);
		}
	}

	/**
	 * Help for command. HIGHLY recommended to override
	 * @return the help
	 */
	public String getHelp() {
		return help;
	}

	/**
	 * Help for command. HIGHLY recommended to override
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

	public BasePlugin newInstance() throws Exception {
		BasePlugin plugin = null;
		try {
			plugin = (BasePlugin)this.getClass().getClassLoader().loadClass(getFqcn()).newInstance();
			
		}
		catch(ClassCastException e) {
			if(StringUtils.contains(e.getMessage(),"BasePlugin"))
				throw new ClassCastException("Can't cast java plugin to BasePlugin (maybe class isn't exentding it?)");
			else
				throw e;
		}
		catch(Exception e) {
			log.error("Unable to create instance of "+getName(),e);
			throw e;
		}
		return plugin;
	}

	/**
	 * @return the fqcn
	 */
	public String getFqcn() {
		return fqcn;
	}

	/**
	 * @param fqcn the fqcn to set
	 */
	public void setFqcn(String fqcn) {
		this.fqcn = fqcn;
	}
}
