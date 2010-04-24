/**
 * @(#)BasePlugin.java
 *
 * This file is part of Quackbot
 */
package Quackbot.plugins.core;

import Quackbot.Bot;

import Quackbot.info.UserMessage;

/**
 * Java command interface. All Java commands MUST implement this
 * @author Lord.Quackstar
 */
public interface BasePlugin {

	/**
	 * Called when spefic command is requested
	 *
	 * Commands MUST override this in order to work
	 * @param bot     Bot instance
	 * @param msgInfo UserMessage bean
	 */
	public void invoke(Bot bot, UserMessage msgInfo) throws Exception;
}
