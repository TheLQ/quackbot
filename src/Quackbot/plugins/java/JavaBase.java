/**
 * @(#)JavaBase.java
 *
 * This file is part of Quackbot
 */
package Quackbot.plugins.java;

import Quackbot.Bot;

import Quackbot.info.BotEvent;

/**
 * Java command interface. All Java commands MUST implement this
 * @author Lord.Quackstar
 */
public interface JavaBase {

	/**
	 * Called when spefic command is requested
	 *
	 * Commands MUST override this in order to work
	 * @param bot     Bot instance
	 * @param msgInfo BotEvent bean
	 */
	public void invoke(Bot bot, BotEvent msgInfo) throws Exception;
}
