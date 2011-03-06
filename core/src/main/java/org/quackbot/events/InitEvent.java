

package org.quackbot.events;

import org.pircbotx.PircBotX;
import org.pircbotx.hooks.Event;
import org.quackbot.hook.HookManager;

/**
 * Created very early just before the bot is fully initialized. Only created
 * once
 * <p>
 * <b>Note:</b> This event is created before the plugin system is initialized. This
 * means that anyone wanting to use this event must write it in Java and add it
 * to the {@link HookManager} before starting the bot. 
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public class InitEvent extends Event {
	public <T extends PircBotX> InitEvent(T bot) {
		super(bot);
	}
}
