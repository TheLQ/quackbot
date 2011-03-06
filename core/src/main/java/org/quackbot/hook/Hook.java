/*
 * Copyright (C) 2009-2010 Leon Blakey
 *
 * Quackedbot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Quackedbot  is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.quackbot.hook;

import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.Listener;
import org.quackbot.Bot;
import org.quackbot.Controller;
import org.pircbotx.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Hook interface is what all Hooks must implement to be added to the stack.
 *
 * This interface has one method, {@link #run(Quackbot.hook.HookList, Quackbot.Bot, Quackbot.info.BotEvent)  },
 * that is called during stack execution.
 *
 * See {@link HookManager} for an explanation on what a hook is and how hooks are
 * treated and executed.
 * @author LordQuackstar
 */
public abstract class Hook extends ListenerAdapter {
	private String name;
	protected Listener listener = null;

	public Hook(String name) {
		this.name = name;
	}

	public Hook() {
	}

	public Hook(Listener listener) {
		this.listener = listener;
		this.name = listener.getClass().getSimpleName();
	}

	public Bot getBot() {
		return Bot.getPoolLocal();
	}

	public String getName() {
		return name;
	}

	public Controller getController() {
		return getBot().controller;
	}

	public Hook setup(String name) {
		this.name = name;
		return this;
	}

	@Override
	public void onEvent(Event event) throws Exception {
		if (listener != null)
			listener.onEvent(event);
		else
			super.onEvent(event);
	}
}
