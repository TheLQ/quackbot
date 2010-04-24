/**
 * @(#)JavaTest.java
 *
 * This file is part of Quackbot
 */
package Quackbot.plugins.core;

import Quackbot.Bot;
import Quackbot.annotations.HelpDoc;
import Quackbot.annotations.Param;
import Quackbot.annotations.ParamNum;
import Quackbot.info.BotMessage;
import Quackbot.info.UserMessage;

/**
 * Simple Java cmd test
 * @author Lord.Quackstar
 */
@HelpDoc("This is JavaTest Help")
@ParamNum(1)
public class JavaTest implements BasePlugin {
	int something;
	public void invoke(Bot bot, UserMessage msgInfo) {
		bot.sendMsg(new BotMessage(msgInfo,"hehe"));
	}
}
