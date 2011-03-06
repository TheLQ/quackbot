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

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * First, some definitions:
 * <ul>
 * <li>Event - An operation that is requested, originating from the user.
 * <li>Event - Class carrying out an operation in place of or with default code
 * <li>Event List - List of hooks that are executed in sequential order upon an
 *                 event
 * </ul>
 *
 * A hook is a special type of plugin that is called during the execution of
 * an event. The advantage of a hook based system is that the flow
 * and execution of various parts of the system are changeable by you.
 * They can be added anywhere in the List and can halt the execution of the Event
 * List, make hooks be ignored, and rearrange and delete hooks.
 * <p>
 * In Quackbot, everything that deals with the execution of user events is kept
 * on the list, even the core methods of Quackbot for executing plugins. Because
 * of this, you could, for example, use your own custom parsing method instead
 * of the provided one or handle PM and various IRC information commands in your
 * own way.
 * <p>
 * Internally, Event types are kept on an enum map while hooks are kept on their
 * corresponding {@link HookMap}. Please see {@link HookMap} for more
 * information. The the avalible events are defined in the enum.
 * <p>
 * HookManager is a singleton handling all Event operations. It holds the above
 * mentioned map and various methods for executing tasks.
 * @author LordQuackstar
 */
public class HookManager {
	/**
	 * String - Name of Hook Method
	 * ArrayList - All Hooks for that method
	 *		BaseHook - Hook
	 */
	private static final Set<Hook> hooks = Collections.synchronizedSet(new HashSet());
	/**
	 * TODO
	 */
	private static Logger log = LoggerFactory.getLogger(HookManager.class);

	/**
	 * This class is a Singleton and should not be initialized
	 */
	private HookManager() {
	}

	public static void addHook(Hook hook) {
		log.debug("Adding hook " + hook.getName());
		hooks.add(hook);
	}

	public static void removeHook(String hookName) {
		log.debug("Removing hook " + hookName);
		synchronized (hooks) {
			Iterator<Hook> i = hooks.iterator();
			while(i.hasNext()) {
				Hook curHook = i.next();
				if(curHook.getName().equals(hookName))
					i.remove();
			}
		}
	}

	public static void removeHook(Hook hook) {
		log.debug("Removing command " + hook.getName());
		hooks.remove(hook);
	}
}
