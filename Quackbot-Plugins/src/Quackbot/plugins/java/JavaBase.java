/**
 * @(#)JavaBase.java
 *
 * Copyright Leon Blakey/Lord.Quackstar, 2009-2010
 *
 * This file is part of Quackbot
 *
 * Quackbot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Quackbot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Quackbot.  If not, see <http://www.gnu.org/licenses/>.
 */
package Quackbot.plugins.java;

import Quackbot.Bot;

import Quackbot.info.BotEvent;

/**
 * This is the Java Plugin interface that all Java Plugins <u>must</u> implement.
 * 
 * @author Lord.Quackstar
 */
public interface JavaBase {
	/**
	 * Called when spefic command is requested
	 *
	 * @param bot     Bot instance
	 * @param msgInfo BotEvent bean
         *
         * @throws Exception Any exception encoutered in execution that prevents
         *                   plugin from continuing to execute
	 */
	public void invoke(Bot bot, BotEvent msgInfo) throws Exception;
}
