/**
 * @(#)Help.java
 *
 * This file is part of Quackbot
 */
package Quackbot.plugins.core;

import Quackbot.Bot;
import Quackbot.Controller;
import Quackbot.InstanceTracker;
import Quackbot.Utils;
import Quackbot.annotations.HelpDoc;
import Quackbot.annotations.ParamConfig;
import Quackbot.err.InvalidCMDException;
import Quackbot.info.BotMessage;
import Quackbot.info.JSPlugin;
import Quackbot.info.JavaPlugin;
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
@ParamConfig(optional={"pluginName"})
@HelpDoc("Provides list of commands or help for specific command. Syntax: ?help <OPTIONAL:command>")
public class Help implements BasePlugin {
	private static Logger log = Logger.getLogger(Help.class);
	Controller ctrl = InstanceTracker.getController();
	String pluginName;
	public void invoke(Bot qb, UserMessage msgInfo) throws Exception {
		//Does user want command list
		if (pluginName == null) {
			List<String> cmdList = new ArrayList<String>();

			//Add Java Plugins
			for (JavaPlugin curPlugin : ctrl.javaPlugins)
				if (!curPlugin.isAdmin()) {
					String[] fqn = StringUtils.split(curPlugin.getName(), ".");
					cmdList.add(fqn[fqn.length-1]);
				}

			//Add JS plugins
			Set<Map.Entry<String, JSPlugin>> jsSet = ctrl.JSplugins.entrySet();
			for (Map.Entry<String, JSPlugin> curJS : jsSet)
				if (curJS.getValue().isAdmin())
					cmdList.add(curJS.getValue().getName());

			//Send to user
			qb.sendMsg(new BotMessage(msgInfo, "Possible commands: " + StringUtils.join(cmdList.toArray(), ", ")));
		} else {
			JavaPlugin javaResult  = Utils.findJavaPlugin(pluginName);
			if (ctrl.JSplugins.keySet().contains(pluginName))
				qb.sendMsg(new BotMessage(msgInfo,ctrl.JSplugins.get(pluginName).getHelp()));
			else if (javaResult != null)
				qb.sendMsg(new BotMessage(msgInfo,javaResult.getHelp()));
			else
				throw new InvalidCMDException(pluginName);
		}
	}
}
