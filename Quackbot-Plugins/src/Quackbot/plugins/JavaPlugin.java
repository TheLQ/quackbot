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

import Quackbot.plugins.java.JavaBase;
import Quackbot.Bot;
import Quackbot.Controller;
import Quackbot.InitHook;

import Quackbot.PluginType;
import Quackbot.plugins.java.HelpDoc;
import Quackbot.plugins.java.ParamConfig;
import Quackbot.plugins.java.ParamNum;
import Quackbot.err.NumArgException;
import Quackbot.err.QuackbotException;
import Quackbot.info.Hooks;
import Quackbot.info.BotEvent;
import Quackbot.plugins.core.Help;
import Quackbot.plugins.java.AdminOnly;
import Quackbot.plugins.java.Ignore;
import Quackbot.plugins.java.Hook;
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
		Controller.initHooks.add(new InitHook() {
			public void run(Controller ctrl) {
				ctrl.addPluginType("java", JavaPlugin.class);
				ctrl.addPluginType("js", JSPlugin.class);
				ctrl.addPlugin(new JavaPlugin(Help.class.getName()));
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
	 * Is Hook?
	 */
	private Hooks hook;
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
	private int params;
	/**
	 * Optional params
	 */
	private int optParams;
	/**
	 * Param class
	 */
	private ParamField paramField;
	/**
	 * Log4j logger
	 */
	private static Logger log = LoggerFactory.getLogger(JavaPlugin.class);

	public JavaPlugin(String className) {
		setFqcn(className);
		String[] fqcna = StringUtils.split(className, ".");
		setName(fqcna[fqcna.length - 1]);
		log.info("New Java Plugin " + getName());
		try {
			Class<?> javaBase = this.getClass().getClassLoader().loadClass(getFqcn());

			//Set all fields acessable
			Field[] fields = javaBase.getDeclaredFields();
			for (Field curField : fields)
				curField.setAccessible(true);
			//Plugin info creation
			if (javaBase.isAnnotationPresent(ReqArg.class))
				setReqArg(true);
			if (javaBase.isAnnotationPresent(AdminOnly.class))
				setAdmin(true);
			if (javaBase.isAnnotationPresent(Ignore.class))
				setIgnore(true);
			if (javaBase.isAnnotationPresent(Hook.class))
				setHook(javaBase.getAnnotation(Hook.class).value());
			if (javaBase.isAnnotationPresent(Service.class))
				setService(true);

			//Param and syntax generation
			if (javaBase.isAnnotationPresent(ParamNum.class) && javaBase.isAnnotationPresent(ParamConfig.class))
				throw new QuackbotException("Class " + name + " cannot use both parameter annotations, please remove one and restarts");

			StringBuilder syntax = new StringBuilder();
			if (javaBase.isAnnotationPresent(ParamNum.class))
				setParamField(new ParamField(javaBase.getAnnotation(ParamNum.class).value()));
			else if (javaBase.isAnnotationPresent(ParamConfig.class))
				setParamField(new ParamField(javaBase));
			else
				setParamField(new ParamField(0));

			//Help generation
			if (javaBase.isAnnotationPresent(HelpDoc.class))
				setHelp(javaBase.getAnnotation(HelpDoc.class).value() + syntax);
			else if (StringUtils.isNotEmpty(syntax.toString()))
				setHelp(syntax.toString());
			else
				setHelp("No help avalible");
		} catch (Exception e) {
			log.error("Cannot load help of command " + name, e);
		}
	}

	public void load(File file) {
		//Empty, commands must be added manually using constructor
	}

	public JavaBase newInstance() throws Exception {
		JavaBase javaCmd = null;
		try {
			javaCmd = (JavaBase) this.getClass().getClassLoader().loadClass(getFqcn()).newInstance();

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

		log.info("Running Java Plugin " + msgInfo.getCommand());
		getParamField().fillFields(javaCmd, msgInfo.getArgs());
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
	 * Param class
	 * @return the paramField
	 */
	public ParamField getParamField() {
		return paramField;
	}

	/**
	 * Param class
	 * @param paramField the paramField to set
	 */
	public void setParamField(ParamField paramField) {
		this.paramField = paramField;
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
	 * Is Hook?
	 * @return the hook
	 */
	public Hooks getHook() {
		return hook;
	}

	/**
	 * Is Hook?
	 * @param hook the hook to set
	 */
	public void setHook(Hooks hook) {
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

	public class ParamField {
		/**
		 * Number of total params?
		 */
		private int paramNum = 0;
		/**
		 * Number of required params?
		 */
		private int reqParamNum = 0;
		private List<Field> reqFields = new ArrayList<Field>();
		private List<Field> optFields = new ArrayList<Field>();
		private Logger logging = LoggerFactory.getLogger(ParamField.class);

		/**
		 * Used for @ParamConfig annotation
		 * @param clazz
		 * @throws NoSuchFieldException
		 */
		public ParamField(Class<?> clazz) throws NoSuchFieldException {
			String[] pcArr = clazz.getAnnotation(ParamConfig.class).value();
			//log.trace("Length of ParamConfig "+pcArr+" | Containing "+StringUtils.join(pcArr,","));
			for (String curName : pcArr) {
				Field field = clazz.getDeclaredField(curName);
				paramNum++;
				reqParamNum++;
				reqFields.add(field);
			}

			//Loop over optionals
			pcArr = clazz.getAnnotation(ParamConfig.class).optional();
			for (String curName : pcArr) {
				Field field = clazz.getDeclaredField(curName);
				paramNum++;
				optFields.add(field);
			}
			setParams(reqParamNum);
			setOptParams(paramNum);
		}

		/**
		 * Used for @ParamNum annotation
		 * @param num
		 */
		public ParamField(int num) {
			paramNum = num;
			reqParamNum = num;
			JavaPlugin.this.params = paramNum;
		}

		public void fillFields(Object inst, String[] params) throws IllegalAccessException, NoSuchFieldException, NumArgException {
			int paramLen = params.length;
			//First check if there are enough fields
			logging.trace("Required Java params: " + reqParamNum + " user params: " + paramLen);


			//Well there are enough fields, continue
			int paramPos = -1;
			log.info("User params: " + paramLen + " Feilds: " + reqFields.size());
			for (Field curField : reqFields) {
				paramPos++;
				curField.set(inst, params[paramPos]);
			}

			//If it dosen't match, then we have optional params to fill
			if (paramPos != (paramLen - 1))
				for (Field curField : optFields) {
					paramPos++;
					if (paramPos > (paramLen - 1))
						break;
					curField.set(inst, params[paramPos]);
				}
		}
	}
}
