/**
 * @(#)Help.java
 *
 * This file is part of Quackbot
 */
package Quackbot.plugins.core;

import Quackbot.plugins.java.JavaBase;
import Quackbot.Bot;
import Quackbot.Controller;

import Quackbot.PluginType;
import Quackbot.plugins.java.HelpDoc;
import Quackbot.plugins.java.ParamConfig;
import Quackbot.err.InvalidCMDException;
import Quackbot.info.BotMessage;
import Quackbot.info.BotEvent;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Core plugin that provides help for a command
 *
 * @author Lord.Quackstar
 */
@ParamConfig(optional = {"pluginName"})
@HelpDoc("Provides list of commands or help for specific command. Syntax: ?help <OPTIONAL:command>")
public class Help implements JavaBase {
	private static Logger log = LoggerFactory.getLogger(Help.class);
	Controller ctrl = Controller.instance;
	String pluginName;

	public void invoke(Bot qb, BotEvent msgInfo) throws Exception {
		//Does user want command list
		if (pluginName == null) {
			List<String> cmdList = new ArrayList<String>();

			//Add Java Plugins
			for (PluginType curPlugin : ctrl.plugins)
				if (!curPlugin.isAdmin())
					cmdList.add(curPlugin.getName());

			//Send to user
			qb.sendMsg(new BotMessage(msgInfo, "Possible commands: " + StringUtils.join(cmdList.toArray(), ", ")));
		} else {
			PluginType result = ctrl.findPlugin(pluginName);
			if (result != null)
				qb.sendMsg(new BotMessage(msgInfo, result.getHelp()));
			else
				throw new InvalidCMDException(pluginName);
		}
	}
}