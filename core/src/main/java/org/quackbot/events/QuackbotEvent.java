package org.quackbot.events;

import lombok.Getter;
import org.pircbotx.hooks.Event;
import org.quackbot.Bot;
import org.quackbot.Controller;

/**
 *
 * @author lordquackstar
 */
@Getter
public abstract class QuackbotEvent extends Event<Bot> {
	protected Controller controller;

	public QuackbotEvent(Controller controller) {
		super(null);
		this.controller = controller;
	}

	public QuackbotEvent(Bot bot) {
		super(bot);
		this.controller = bot.getController();
	}
}
