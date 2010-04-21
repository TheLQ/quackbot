/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Quackbot.plugins.core;

import Quackbot.Bot;
import Quackbot.Controller;
import Quackbot.InstanceTracker;
import Quackbot.Utils;
import Quackbot.info.BotMessage;
import Quackbot.info.JSPlugin;
import Quackbot.info.JavaPlugin;
import Quackbot.info.UserMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Lord.Quackstar
 */
public class Help {

	Controller ctrl = InstanceTracker.getCtrlInst();

	public String help() {
		return "Displays all commands or help for specific command. Syntax ?help <OPTIONAL:Command>";
	}

	public void invoke(Bot qb, UserMessage msgInfo) throws Exception {
		//Does user want command list
		if (msgInfo.getArgs().length == 0) {
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
			String command = msgInfo.getArgs()[0];
			JavaPlugin javaResult  = Utils.findCI(ctrl.javaPlugins, "Quackbot.plugins.java." + command);
			if (ctrl.JSplugins.keySet().contains(command))
				qb.sendMsg(new BotMessage(msgInfo,ctrl.JSplugins.get(command).getHelp()));
			else if (javaResult != null)
				qb.sendMsg(new BotMessage(msgInfo,javaResult.newInstance().help()));
		}
	}
}
