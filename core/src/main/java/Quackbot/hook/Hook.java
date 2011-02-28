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
package Quackbot.hook;

import Quackbot.Bot;
import Quackbot.Controller;
import org.pircbotx.hooks.Listener;

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
public abstract class Hook implements Listener {
	private String name;

	public Hook(String name) {
		this.name = name;
	}

	public Hook() {
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
}
