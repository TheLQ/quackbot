/**
 * @(#)JavaTest.java
 *
 * This file is part of Quackbot
 */
package Quackbot.plugins.java;

import Quackbot.Bot;
import Quackbot.info.BotMessage;
import Quackbot.info.UserMessage;

/**
 * Simple Java cmd test
 * @author Lord.Quackstar
 */
public class JavaTest implements JavaCmdTpl {
	public void invoke(Bot bot, UserMessage msgInfo) {
		bot.sendMsg(new BotMessage(msgInfo,"hehe"));
	}
}
