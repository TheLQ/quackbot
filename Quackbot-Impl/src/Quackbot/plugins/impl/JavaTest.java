/**
 * @(#)JavaTest.java
 *
 * This file is part of Quackbot
 */
package Quackbot.plugins.impl;

import Quackbot.Bot;
import Quackbot.plugins.java.HelpDoc;
import Quackbot.info.BotMessage;
import Quackbot.info.BotEvent;
import Quackbot.plugins.java.JavaBase;
import Quackbot.plugins.java.ParamNum;
import org.apache.commons.lang.StringUtils;

/**
 * Simple Java cmd test
 * @author Lord.Quackstar
 */
//@ParamConfig(optional={"optional","optional2","optional3"})
@ParamNum(5)
@HelpDoc("This is JavaTest Help")
public class JavaTest implements JavaBase {
	String something;
	String somethins;
	String optional, optional2, optional3;

	public void invoke(Bot bot, BotEvent msgInfo) {
		bot.sendMsg(new BotMessage(msgInfo, "You said " + StringUtils.join(msgInfo.getArgs(), " ") + " with length " + msgInfo.getArgs().length));
	}
}