/**
 * Copyright (C) 2011 Leon Blakey <lord.quackstar at gmail.com>
 *
 * This file is part of Quackbot.
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

package org.quackbot.hooks.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.Event;
import org.quackbot.Controller;
import org.quackbot.hooks.HookManager;

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
public class InitEvent extends QuackbotEvent {
	public InitEvent(Controller controller) {
		super(controller);
	}
	
	/**
	 * Does NOT respond to the server! This will throw an {@link UnsupportedOperationException} 
	 * since there is no bot.
	 * @param response The response to send 
	 */
	@Override
	public void respond(String response) {
		throw new UnsupportedOperationException("Attempting to respond to an Init");
	}
}
