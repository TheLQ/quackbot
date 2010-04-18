/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Quackbot.plugins.java;

import Quackbot.Bot;
import Quackbot.info.BotMessage;
import Quackbot.info.UserMessage;

/**
 *
 * @author lordquackstar
 */
public class JavaTest implements JavaCmdTpl {
	public void invoke(Bot bot, UserMessage msgInfo) {
		bot.sendMsg(new BotMessage(msgInfo,"hehe"));
	}
}
