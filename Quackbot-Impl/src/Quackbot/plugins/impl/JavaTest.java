/**
 * @(#)JavaTest.java
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
package Quackbot.plugins.impl;

import Quackbot.Bot;
import Quackbot.plugins.java.HelpDoc;
import Quackbot.info.BotMessage;
import Quackbot.info.BotEvent;
import Quackbot.plugins.java.JavaBase;
import Quackbot.plugins.java.ParamCount;
import org.apache.commons.lang.StringUtils;

/**
 * Simple Java cmd test
 * @author Lord.Quackstar
 */
//@ParamConfig(optional={"optional","optional2","optional3"})
@ParamCount(5)
@HelpDoc("This is JavaTest Help")
public class JavaTest implements JavaBase {
	String something;
	String somethins;
	String optional, optional2, optional3;

	public void invoke(Bot bot, BotEvent msgInfo) {
		bot.sendMsg(new BotMessage(msgInfo, "You said " + StringUtils.join(msgInfo.getArgs(), " ") + " with length " + msgInfo.getArgs().length));
	}
}
