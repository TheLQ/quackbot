/**
 * @(#)JavaTest.java
 *
 * This file is part of Quackbot
 */
package Quackbot.plugins.core;

import Quackbot.Bot;
import Quackbot.LogFactory;
import Quackbot.info.BotMessage;
import Quackbot.info.Hooks;
import Quackbot.info.BotEvent;
import Quackbot.plugins.java.Hook;
import Quackbot.plugins.java.JavaBase;
import org.apache.log4j.Logger;

/**
 * Simple Java cmd test
 * @author Lord.Quackstar
 */

@Hook(Hooks.onMessage)
public class HookTest implements JavaBase {
	String something;
	String somethins;
	String optional ,optional2, optional3;
	private static Logger log = LogFactory.getLogger(HookTest.class);

	public void invoke(Bot bot, BotEvent msgInfo) {
		log.trace("In log");
		log.trace("Using "+msgInfo.getRawmsg());
		bot.sendMsg(new BotMessage(msgInfo,new StringBuilder().append("You said: ").append(msgInfo.getRawmsg()).toString()));
	}
}
