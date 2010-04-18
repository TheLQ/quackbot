/**
 * @(#)JavaCmdTpl.java
 *
 * This file is part of Quackbot
 */
package Quackbot.plugins.java;

import Quackbot.Bot;
import Quackbot.info.UserMessage;

/**
 * Java command interface. All Java commands MUST implement this
 * @author Lord.Quackstar
 */
public interface JavaCmdTpl {

	/**
	 * Called when spefic command is requested
	 * @param bot     Bot instance
	 * @param msgInfo UserMessage bean
	 */
	public void invoke(Bot bot, UserMessage msgInfo);
}
