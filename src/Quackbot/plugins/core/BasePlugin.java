/**
 * @(#)BasePlugin.java
 *
 * This file is part of Quackbot
 */
package Quackbot.plugins.core;

import Quackbot.Bot;
import Quackbot.annotations.HelpDoc;
import Quackbot.err.NumArgException;

import Quackbot.info.UserMessage;
import Quackbot.info.BotMessage;
import Quackbot.info.JavaPlugin;

/**
 * Java command interface. All Java commands MUST implement this
 * @author Lord.Quackstar
 */
@HelpDoc("This is BasePlugin Help")
public abstract class BasePlugin extends JavaPlugin {

	/**
	 * Called when spefic command is requested
	 *
	 * Commands MUST override this in order to work
	 * @param bot     Bot instance
	 * @param msgInfo UserMessage bean
	 */
	public void invoke(Bot bot, UserMessage msgInfo) throws Exception {
		bot.sendMsg(new BotMessage(msgInfo,"Command is empty"));
	}

	public void checkParams(int num, UserMessage msgInfo) throws NumArgException {
		int userArgs = msgInfo.getArgs().length;
		if(num != userArgs)
			throw new NumArgException(num, userArgs);
	}
}
