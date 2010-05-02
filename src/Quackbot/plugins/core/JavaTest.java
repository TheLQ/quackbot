/**
 * @(#)JavaTest.java
 *
 * This file is part of Quackbot
 */
package Quackbot.plugins.core;

import Quackbot.Bot;
import Quackbot.plugins.java.HelpDoc;
import Quackbot.plugins.java.ParamConfig;
import Quackbot.info.BotMessage;
import Quackbot.info.UserMessage;
import Quackbot.plugins.java.JavaBase;
import org.apache.commons.lang.StringUtils;

/**
 * Simple Java cmd test
 * @author Lord.Quackstar
 */
@ParamConfig(optional={"optional","optional2","optional3"})
//@ParamNum(5)
@HelpDoc("This is JavaTest Help")
public class JavaTest implements JavaBase {
	String something;
	String somethins;
	String optional ,optional2, optional3;

	public void invoke(Bot bot, UserMessage msgInfo) {
		bot.sendMsg(new BotMessage(msgInfo,"You said "+StringUtils.join(msgInfo.getArgs()," ")+" with length "+msgInfo.getArgs().length));

		/*bot.sendMsg(new BotMessage(msgInfo,"hehe, you said "+something+" and "+somethins));
		if(optional != null)
			bot.sendMsg(new BotMessage(msgInfo," Hmm, you also said "+optional));
		if(optional2 != null)
			bot.sendMsg(new BotMessage(msgInfo," Hmm, you also said "+optional2));
		if(optional3 != null)
			bot.sendMsg(new BotMessage(msgInfo," Hmm, you also said "+optional3));*/
	}
}
