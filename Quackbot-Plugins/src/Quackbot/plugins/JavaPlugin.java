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

import Quackbot.ParameterConfig;
import Quackbot.hook.HookList;
import Quackbot.plugins.java.JavaBase;
import Quackbot.Bot;
import Quackbot.Controller;

import Quackbot.PluginType;
import Quackbot.plugins.java.HelpDoc;
import Quackbot.hook.Event;
import Quackbot.hook.HookManager;
import Quackbot.hook.PluginHook;
import Quackbot.info.BotEvent;
import Quackbot.plugins.core.AdminHelp;
import Quackbot.plugins.core.Help;
import Quackbot.plugins.java.AdminOnly;
import Quackbot.plugins.java.Disabled;
import Quackbot.plugins.java.Hooks;
import Quackbot.plugins.java.Parameters;
import Quackbot.plugins.java.Service;
import java.io.File;
import java.lang.reflect.Field;
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
		HookManager.addHook(Event.onInit, "QBPluginInit",new PluginHook() {
			public void run(HookList hookStack, Bot bot, BotEvent msgInfo) {
				Controller ctrl = Controller.instance;
				ctrl.addPluginType("java", JavaPlugin.class);
				ctrl.addPluginType("js", JSPlugin.class);
				ctrl.addPlugin(new JavaPlugin(Help.class), true);
				ctrl.addPlugin(new JavaPlugin(AdminHelp.class), true);
			}
		});
	}
	private String name = "";
	private String help = "";
	private boolean admin = false;
	private boolean enabled = true;
	private boolean service = false;
	private boolean util = false;
	/**
	 * Log4j logger
	 */
	private static Logger log = LoggerFactory.getLogger(JavaPlugin.class);
	protected Class<?> clazz;
	protected JavaBase instance;
	ParameterConfig<Field> paramConfig = new ParameterConfig<Field>() {
		public void fillByObject(Field object, String argument) throws Exception {
			if(instance == null)
				return;
			object.set(instance, argument);
		}
	};

	public JavaPlugin(Class<?> clazz) {
		this(clazz.getName());
	}

	public JavaPlugin(String className) {
		String fqcn = className;
		String[] fqcna = StringUtils.split(className, ".");
		name = fqcna[fqcna.length - 1];

		log.info("New Java Plugin " + name);
		try {
			clazz = this.getClass().getClassLoader().loadClass(fqcn);

			//Set all fields acessable
			Field[] fields = clazz.getDeclaredFields();
			for (Field curField : fields)
				curField.setAccessible(true);

			//Plugin info creation
			if (clazz.isAnnotationPresent(AdminOnly.class))
				admin = true;
			if (clazz.isAnnotationPresent(Disabled.class))
				enabled = false;
			if (clazz.isAnnotationPresent(Hooks.class)) //TODO
				HookManager.addPluginHooks(clazz.getAnnotation(Hooks.class).value(), this);
			if (clazz.isAnnotationPresent(Service.class))
				service = true;

			//Param and syntax generation
			if (clazz.isAnnotationPresent(Parameters.class)) {
				Parameters paramAnnot = clazz.getAnnotation(Parameters.class);
				if (paramAnnot.optionalCount() != -1)
					paramConfig.setOptionalCount(paramAnnot.optionalCount());
				if (paramAnnot.requiredCount() != -1)
					paramConfig.setRequiredCount(paramAnnot.requiredCount());
				if (paramAnnot.optional().length != 0)
					for (String curFieldName : paramAnnot.optional())
						paramConfig.addOptionalObject(clazz.getDeclaredField(curFieldName));
				if (paramAnnot.value().length != 0)
					for (String curFieldName : paramAnnot.value())
						paramConfig.addRequiredObject(clazz.getDeclaredField(curFieldName));
			}

			//Help generation
			if (clazz.isAnnotationPresent(HelpDoc.class))
				help = clazz.getAnnotation(HelpDoc.class).value();
		} catch (Exception e) {
			log.error("Cannot load help of command " + name, e);
		}
	}

	public boolean load(File file) throws Exception {
		throw new UnsupportedOperationException("Java plugins cannot be loaded. Attempted to load " + file.getAbsolutePath());
	}

	/**
	 * {@inheritDoc}
	 * @param bot
	 * @param msgInfo
	 * @throws Exception
	 */
	public void invoke(Bot bot, BotEvent msgInfo) throws Exception {
		log.debug("Running Java Plugin " + msgInfo.getCommand());
		try {
			instance = (JavaBase) clazz.newInstance();
		} catch (ClassCastException e) {
			if (StringUtils.contains(e.getMessage(), "JavaBase"))
				throw new ClassCastException("Can't cast java plugin to JavaBase (maybe class isn't exentding it?)");
			else
				throw e;
		} catch (Exception e) {
			throw e;
		}

		//Because the instance isn't set yet, no parameters have been filled
		paramConfig.fillParameters(msgInfo.getArgs());

		//Call the invoke method of the plugin
		instance.invoke(bot, msgInfo);
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

	public File getFile() {
		return null; //empty
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public ParameterConfig getParamConfig() {
		return paramConfig;
	}
}
