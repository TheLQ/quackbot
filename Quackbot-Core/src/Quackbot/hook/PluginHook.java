/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Quackbot.hook;

import Quackbot.Bot;
import Quackbot.info.BotEvent;

/**
 * The PluginHook interface is what all Hooks must implement to be added to the stack.
 *
 * This interface has one method, {@link #run(Quackbot.hook.HookList, Quackbot.Bot, Quackbot.info.BotEvent)  },
 * that is called during stack execution.
 *
 * See {@link HookManager} for an explanation on what a hook is and how hooks are
 * treated and executed.
 * @author LordQuackstar
 */
public interface PluginHook<A,B> {
	public void run(HookList hookStack, Bot bot, BotEvent<A,B> msgInfo);
}
