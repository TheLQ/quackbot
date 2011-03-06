

package org.quackbot.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.Event;
import org.quackbot.Controller;
import org.quackbot.hook.HookManager;

/**
 * Created very early just before the bot is fully initialized. Only created
 * once
 * <p>
 * <b>Note 1:</b> This event is created before the plugin system is initialized. This
 * means that anyone wanting to use this event must write it in Java and add it
 * to the {@link HookManager} before starting Quackbot. 
 * <p>
 * <b>Note 2:</b> {@link #getBot() } will return null since there is no bot yet
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InitEvent extends Event {
	private final Controller controller;
	
	public <T extends PircBotX> InitEvent(Controller ctrl) {
		super(null);
		this.controller = ctrl;
	}
}
