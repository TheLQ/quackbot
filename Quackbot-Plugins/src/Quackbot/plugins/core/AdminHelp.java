/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Quackbot.plugins.core;

import Quackbot.Bot;
import Quackbot.Controller;
import Quackbot.PluginType;
import Quackbot.err.InvalidCMDException;
import Quackbot.info.BotEvent;
import Quackbot.info.BotMessage;
import Quackbot.plugins.java.AdminOnly;
import Quackbot.plugins.java.HelpDoc;
import Quackbot.plugins.java.JavaBase;
import Quackbot.plugins.java.ParamConfig;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author LordQuackstar
 */
@ParamConfig(optional = {"pluginName"})
@HelpDoc("Provides list of Admin-only commands or help for specific command. Syntax: ?help <OPTIONAL:command>")
@AdminOnly
public class AdminHelp implements JavaBase {
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
			if (result != null && Controller.throwIsPluginUsable(result, false, qb, msgInfo)) {
				if(StringUtils.isBlank(result.getHelp()))
					qb.sendMsg(new BotMessage(msgInfo, "No help avalible"));
				else
					qb.sendMsg(new BotMessage(msgInfo, result.getHelp()));
			}
			else
				throw new InvalidCMDException(pluginName);
		}
	}
}
