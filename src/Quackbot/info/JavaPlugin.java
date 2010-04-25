/**
 * @(#)JavaPlugin.java
 *
 * This file is part of Quackbot
 */
package Quackbot.info;

import Quackbot.annotations.HelpDoc;
import Quackbot.annotations.ParamConfig;
import Quackbot.annotations.ParamNum;
import Quackbot.err.NumArgException;
import Quackbot.err.QuackbotException;
import Quackbot.plugins.core.BasePlugin;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * This is the global JavaBean/Utility for all Java written plugins
 *
 * @author Lord.Quackstar
 */
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
	 * Param class
	 */
	private ParamField paramField;
	/**
	 * Log4j logger
	 */
	private static Logger log = Logger.getLogger(JavaPlugin.class);

	/**
	 * Creates empty JavaPlugin with class name
	 * @param FQCN Fully Qualified Class Name of Java plugin
	 */
	public JavaPlugin(String className) {
		this.fqcn = className;
		String[] fqcna = StringUtils.split(className, ".");
		this.name = fqcna[fqcna.length - 1];
		try {
			Class<?> javaBase = newInstance().getClass();

			//Set all fields acessable
			Field[] fields = javaBase.getDeclaredFields();
			for (Field curField : fields)
				curField.setAccessible(true);

			//Param and syntax generation
			if(javaBase.isAnnotationPresent(ParamNum.class) && javaBase.isAnnotationPresent(ParamConfig.class))
				throw new QuackbotException("Class "+name+" cannot use both parameter annotations, please remove one and restarts");

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
			plugin = (BasePlugin) this.getClass().getClassLoader().loadClass(getFqcn()).newInstance();

		} catch (ClassCastException e) {
			if (StringUtils.contains(e.getMessage(), "BasePlugin"))
				throw new ClassCastException("Can't cast java plugin to BasePlugin (maybe class isn't exentding it?)");
			else
				throw e;
		} catch (Exception e) {
			log.error("Unable to create instance of " + getName(), e);
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
		private Logger logging = Logger.getLogger(ParamField.class);

		/**
		 * Used for @ParamConfig annotation
		 * @param clazz
		 * @throws NoSuchFieldException
		 */
		public ParamField(Class<?> clazz) throws NoSuchFieldException {
			logging.trace("Currently at @ParamConfig handling");
			String[] pcArr = clazz.getAnnotation(ParamConfig.class).value();
			logging.trace("ParamConfig len: " + pcArr.length);
			for (String curName : pcArr) {
				Field field = clazz.getDeclaredField(curName);
				paramNum++;
				reqParamNum++;
				reqFields.add(field);
				logging.trace("Adding " + field + " to required");
			}

			//Loop over optionals
			pcArr = clazz.getAnnotation(ParamConfig.class).optional();
			for (String curName : pcArr) {
				Field field = clazz.getDeclaredField(curName);
				paramNum++;
				optFields.add(field);
				logging.trace("Adding " + field + " to optional");
			}
		}

		/**
		 * Used for @ParamNum annotation
		 * @param num
		 */
		public ParamField(int num) {
			logging.trace("Currently at @ParamNum handling, passing " + num);
			paramNum = num;
			reqParamNum = num;
		}

		public void fillFields(Object inst, String[] params) throws IllegalAccessException, NoSuchFieldException, NumArgException {
			int paramLen = params.length;
			//First check if there are enough fields
			logging.trace("Required Java params: " + reqParamNum + " user params: " + paramLen);
			if (paramLen > paramNum) //Do we have too many?
				throw new NumArgException(paramLen,reqParamNum,paramNum-reqParamNum );
			else if (paramLen < reqParamNum) //Do we not have enough?
				throw new NumArgException(paramLen, reqParamNum);

			//Well there are enough fields, continue
			int paramPos = -1;
			log.info("User params: " + paramLen + " Feilds: " + reqFields.size());
			log.trace("Req: " + reqParamNum + ", Optional: " + paramNum);
			for (Field curField : reqFields) {
				paramPos++;
				curField.set(inst, params[paramPos]);
				log.trace("Param Pos: " + paramPos);
			}

			//If it dosen't match, then we have optional params to fill
			if (paramPos != (paramLen - 1))
				for (Field curField : optFields) {
					paramPos++;
					if (paramPos > (paramLen - 1))
						break;
					curField.set(inst, params[paramPos]);
					log.trace("Param Pos - 0: " + paramPos);
				}
		}
	}
}
