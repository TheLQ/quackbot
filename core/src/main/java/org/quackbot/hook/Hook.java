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

import java.io.File;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.Listener;
import org.quackbot.Bot;
import org.quackbot.Controller;
import org.pircbotx.hooks.ListenerAdapter;

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
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class Hook extends ListenerAdapter {
	private final String name;
	private final File file;
	private final Listener listener;

	/**
	 * Create a hook with the given name. File is null
	 * @param name The name to use for this Hook
	 */
	public Hook(String name) {
		this.name = name;
		this.file = null;
		this.listener = null;
	}

	/**
	 * Create an hook with the given file and name
	 * @param file The file that this Hook came from
	 * @param name The name to use for this Hook
	 */
	public Hook(File file, String name) {
		this.name = name;
		this.file = file;
		this.listener = null;
	}

	/**
	 * Wrap an {@link Listener}, allowing it to be easily used in Quackbot
	 * @param listener The listener to wrap
	 */
	public Hook(Listener listener) {
		this.listener = listener;
		this.name = listener.getClass().getSimpleName();
		this.file = null;
	}

	public Bot getBot() {
		return Bot.getPoolLocal();
	}

	public Controller getController() {
		return getBot().controller;
	}

	@Override
	public void onEvent(Event event) throws Exception {
		//Capture and redirect to Listener if we are wrapping one
		if (listener != null)
			listener.onEvent(event);
		else
			super.onEvent(event);
	}
}
