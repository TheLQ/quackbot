/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Quackbot.javacmd;

import Quackbot.Bot;
import Quackbot.info.UserMessage;

/**
 *
 * @author lordquackstar
 */
public class JavaTest implements CMDSuper {
	public void invoke(Bot bot, UserMessage msgInfo) {
		bot.sendMessage(msgInfo.channel,"hehe");
	}
}
