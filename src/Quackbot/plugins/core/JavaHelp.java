/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Quackbot.plugins.core;

import Quackbot.Bot;
import Quackbot.InstanceTracker;
import Quackbot.info.BotMessage;
import Quackbot.info.UserMessage;

/**
 *
 * @author admins
 */
public class JavaHelp extends BasePlugin {
	public void invoke(Bot bot, UserMessage msgInfo) throws Exception {
		String command = InstanceTracker.getCtrlInst().javaPlugins.get(1).getName();
		BasePlugin javaCmd = (BasePlugin)this.getClass().getClassLoader().loadClass(command).newInstance();
		bot.sendMsg(new BotMessage(msgInfo,javaCmd.help()));
	}
}
