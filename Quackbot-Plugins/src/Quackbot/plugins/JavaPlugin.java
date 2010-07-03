/**
 * @(#)JavaPlugin.java
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

import Quackbot.hook.HookList;
import Quackbot.plugins.java.JavaBase;
import Quackbot.Bot;
import Quackbot.Controller;

import Quackbot.PluginType;
import Quackbot.plugins.java.HelpDoc;
import Quackbot.plugins.java.ParamConfig;
import Quackbot.plugins.java.ParamCount;
import Quackbot.err.NumArgException;
import Quackbot.err.QuackbotException;
import Quackbot.hook.Event;
import Quackbot.hook.HookManager;
import Quackbot.hook.PluginHook;
import Quackbot.info.BotEvent;
import Quackbot.plugins.core.AdminHelp;
import Quackbot.plugins.core.Help;
import Quackbot.plugins.java.AdminOnly;
import Quackbot.plugins.java.Ignore;
import Quackbot.plugins.java.Hooks;
import Quackbot.plugins.java.ReqArg;
import Quackbot.plugins.java.Service;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the global JavaBean/Utility for all Java written plugins
 *
 * @author Lord.Quackstar
 */
public class JavaPlugin implements PluginType {
	static {
		HookManager.addHook(Event.onInit, new PluginHook() {
			public void run(HookList hookStack, Bot bot, BotEvent msgInfo) {
				Controller ctrl = Controller.instance;
				ctrl.addPluginType("java", JavaPlugin.class);
				ctrl.addPluginType("js", JSPlugin.class);
				ctrl.addPlugin(new JavaPlugin(Help.class), true);
				ctrl.addPlugin(new JavaPlugin(AdminHelp.class), true);
			}
		});
	}
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
	 * Is Hooks?
	 */
	private Event hook;
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
	 * Params
	 */
	private int params = 0;
	/**
	 * Optional params
	 */
	private int optParams = 0;
	/**
	 * Log4j logger
	 */
	private static Logger log = LoggerFactory.getLogger(JavaPlugin.class);
	protected List<Field> paramFields = new ArrayList<Field>();
	protected Class<?> clazz;

	public JavaPlugin(Class<?> clazz) {
		this(clazz.getName());
	}

	public JavaPlugin(String className) {
		setFqcn(className);
		String[] fqcna = StringUtils.split(className, ".");
		setName(fqcna[fqcna.length - 1]);
		log.info("New Java Plugin " + getName());
		try {
			clazz = this.getClass().getClassLoader().loadClass(getFqcn());

			//Set all fields acessable
			Field[] fields = clazz.getDeclaredFields();
			for (Field curField : fields)
				curField.setAccessible(true);

			//Plugin info creation
			if (clazz.isAnnotationPresent(ReqArg.class))
				setReqArg(true);
			if (clazz.isAnnotationPresent(AdminOnly.class))
				setAdmin(true);
			if (clazz.isAnnotationPresent(Ignore.class))
				setIgnore(true);
			if (clazz.isAnnotationPresent(Hooks.class))
				setHook(clazz.getAnnotation(Hooks.class).value());
			if (clazz.isAnnotationPresent(Service.class))
				setService(true);

			//Param and syntax generation
			if (clazz.isAnnotationPresent(ParamCount.class) && clazz.isAnnotationPresent(ParamConfig.class))
				throw new QuackbotException("Class " + name + " cannot use both parameter annotations, please remove one and restart");
			else if (clazz.isAnnotationPresent(ParamCount.class)) {
			
				setParams(clazz.getAnnotation(ParamCount.class).value());
				setOptParams(clazz.getAnnotation(ParamCount.class).optional());
			} else if (clazz.isAnnotationPresent(ParamConfig.class)) {
			
				//Get required names
				for (String curName : clazz.getAnnotation(ParamConfig.class).value()) {
					paramFields.add(clazz.getDeclaredField(curName));
					setParams(++params);
				}
			
				//Get optionals
				for (String curName : clazz.getAnnotation(ParamConfig.class).optional()) {
				
					paramFields.add(clazz.getDeclaredField(curName));
					setOptParams(++optParams);
				}
				
			}

			//Help generation
			if (clazz.isAnnotationPresent(HelpDoc.class))
				setHelp(clazz.getAnnotation(HelpDoc.class).value());
			else
				setHelp("No help avalible");
		} catch (Exception e) {
			log.error("Cannot load help of command " + name, e);
		}
	}

	public void load(File file) {
		throw new UnsupportedOperationException("Java plugins cannot be loaded. Attempted to load " + file.getAbsolutePath());
	}

	public JavaBase newInstance() throws Exception {
		JavaBase javaCmd = null;
		try {
			javaCmd = (JavaBase)clazz.newInstance();
		} catch (ClassCastException e) {
			if (StringUtils.contains(e.getMessage(), "BasePlugin"))
				throw new ClassCastException("Can't cast java plugin to BasePlugin (maybe class isn't exentding it?)");
			else
				throw e;
		} catch (Exception e) {
			log.error("Unable to create instance of " + getName(), e);
			throw e;
		}
		return javaCmd;
	}

	/**
	 * {@inheritDoc}
	 * @param bot
	 * @param msgInfo
	 * @throws Exception
	 */
	public void invoke(String[] args, Bot bot, BotEvent msgInfo) throws Exception {
		JavaBase javaCmd = newInstance();

		log.debug("Running Java Plugin " + msgInfo.getCommand());
		int paramLen = paramFields.size();
		int userParams = msgInfo.getArgs().length;

		//Set fields if the plugin uses ParamConfig
		if (!paramFields.isEmpty())
			for (int i = 0; i < userParams; i++)
				paramFields.get(i).set(javaCmd, msgInfo.getArgs()[i]);

		javaCmd.invoke(bot, msgInfo);
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

	/**
	 * @return the params
	 */
	public int getParams() {
		return params;
	}

	/**
	 * @param params the params to set
	 */
	public void setParams(int params) {
		this.params = params;
	}

	public File getFile() {
		return null; //empty
	}

	/**
	 * Is Hooks?
	 * @return the hook
	 */
	public Event getHook() {
		return hook;
	}

	/**
	 * Is Hooks?
	 * @param hook the hook to set
	 */
	public void setHook(Event hook) {
		this.hook = hook;
	}

	/**
	 * @return the optParams
	 */
	public int getOptParams() {
		return optParams;
	}

	/**
	 * @param optParams the optParams to set
	 */
	public void setOptParams(int optParams) {
		this.optParams = optParams;
	}
}