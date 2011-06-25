
package org.quackbot.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.Event;

/**
 * Event that represents a command being sent.
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CommandEvent extends Event {
	protected final Channel channel;
	protected final User user;
	protected final Event parentEvent;

	public CommandEvent(PircBotX bot, Channel channel, User user, Event parentEvent) {
		super(bot);
		this.channel = channel;
		this.user = user;
		this.parentEvent = parentEvent;
	}

	@Override
	public void respond(String response) {
		parentEvent.respond(response);
	}
}
