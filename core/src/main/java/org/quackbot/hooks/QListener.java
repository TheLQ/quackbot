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
package org.quackbot.hooks;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pircbotx.hooks.Event;
import org.quackbot.Bot;
import org.pircbotx.hooks.ListenerAdapter;
import org.quackbot.hooks.events.InitEvent;

/**
 * The Hook interface is what all Hooks must implement to be added to the stack.
 *
 * This interface has one method, {@link #run(Quackbot.hook.HookList, Quackbot.Bot, Quackbot.info.BotEvent)  },
 * that is called during stack execution.
 *
 * See {@link HookManager} for an explanation on what a hook is and how hooks are
 * treated and executed.
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class QListener extends ListenerAdapter<Bot> {
	@Override
	public void onEvent(Event<Bot> event) throws Exception {
		super.onEvent(event);
		if (event instanceof InitEvent)
			onInit((InitEvent) event);
	}

	//public void onHookLoadEnd(HookLoadEndEvent event) {
	//}

	//public void onHookLoadStart(HookLoadStartEvent event) {
	//}

	//public void onHookLoad(HookLoadEvent event) {
	//}

	public void onInit(InitEvent event) {
	}
}
