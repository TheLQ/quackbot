/**
 * @(#)JavaTest.java
 *
 * This file is part of Quackbot
 */
package Quackbot.plugins.impl;

import Quackbot.Bot;

import Quackbot.info.BotMessage;
import Quackbot.info.Hooks;
import Quackbot.info.BotEvent;
import Quackbot.plugins.java.Hook;
import Quackbot.plugins.java.JavaBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple Java cmd test
 * @author Lord.Quackstar
 */
@Hook(Hooks.onMessage)
public class HookTest implements JavaBase {
	String something;
	String somethins;
	String optional, optional2, optional3;
	private static Logger log = LoggerFactory.getLogger(HookTest.class);

	public void invoke(Bot bot, BotEvent msgInfo) {
		log.trace("In log");
		log.trace("Using " + msgInfo.getRawmsg());
		bot.sendMsg(new BotMessage(msgInfo, new StringBuilder().append("You said: ").append(msgInfo.getRawmsg()).toString()));
	}
}
