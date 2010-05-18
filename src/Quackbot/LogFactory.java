/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Quackbot;

import Quackbot.log.BotAppender;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.helpers.NullEnumeration;

/**
 *
 * @author lordquackstar
 */
public class LogFactory {
	public static String[] botClasses  = new String[]{"Quackbot.Bot", "org.jibble.", "Quackbot.PluginExecutor","Quackbot.plugins.core.","Quackbot.plugins.java."};

	public static synchronized Logger getLogger(Class<?> clazz) {
		return getLogger(clazz.getName());
	}

	public static synchronized  Logger getLogger(String className) {
		Logger reqLogger = Logger.getLogger(className);
		//First make sure that this is comming from the right class
		for (String search : botClasses)
			if (StringUtils.startsWithIgnoreCase(className, search)) {
				//Does this already have a BotAppender?
				if (reqLogger.getAppender("BotAppender") == null) {
					reqLogger.removeAllAppenders();
					reqLogger.setAdditivity(false);
					reqLogger.addAppender(new BotAppender());
				}
			}
		return reqLogger;
	}
}
