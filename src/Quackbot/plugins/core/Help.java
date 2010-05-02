/**
 * @(#)Help.java
 *
 * This file is part of Quackbot
 */
package Quackbot.plugins.core;

import Quackbot.plugins.java.JavaBase;
import Quackbot.Bot;
import Quackbot.Controller;
import Quackbot.InstanceTracker;
import Quackbot.PluginType;
import Quackbot.Utils;
import Quackbot.plugins.java.HelpDoc;
import Quackbot.plugins.java.ParamConfig;
import Quackbot.err.InvalidCMDException;
import Quackbot.info.BotMessage;
import Quackbot.plugins.JSPlugin;
import Quackbot.plugins.JavaPlugin;
import Quackbot.info.UserMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Core plugin that provides help for a command
 *
 * @author Lord.Quackstar
 */
@ParamConfig(optional = {"pluginName"})
@HelpDoc("Provides list of commands or help for specific command. Syntax: ?help <OPTIONAL:command>")
public class Help implements JavaBase {
	private static Logger log = Logger.getLogger(Help.class);
	Controller ctrl = InstanceTracker.getController();
	String pluginName;

	public void invoke(Bot qb, UserMessage msgInfo) throws Exception {
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
			PluginType result = Utils.findPlugin(pluginName);
			if (result != null)
				qb.sendMsg(new BotMessage(msgInfo, result.getHelp()));
			else
				throw new InvalidCMDException(pluginName);
		}
	}
}
