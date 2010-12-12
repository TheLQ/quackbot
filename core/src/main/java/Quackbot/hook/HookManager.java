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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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
	private static final Map<String, HookMap> hooks = Collections.synchronizedMap(new HashMap<String, HookMap>() {
		{
			for (Method curMethod : Hook.class.getDeclaredMethods())
				if (curMethod.getName().startsWith("on"))
					put(curMethod.getName(), new HookMap(curMethod.getName()));
		}
	});
	/**
	 * TODO
	 */
	private static Logger log = LoggerFactory.getLogger(HookManager.class);

	/**
	 * This class is a Singleton and should not be initialized
	 */
	private HookManager() {
	}

	public static void addPluginHook(Hook hook) {
		log.debug("Adding hook " + hook.getName());
		for (Method curMethod : hook.getClass().getDeclaredMethods())
			if (hooks.containsKey(curMethod.getName())) {
				curMethod.setAccessible(true);
				HookMap curHookMap = getHookMap(curMethod.getName());
				synchronized (curHookMap) {
					curHookMap.put(hook.getName(), hook);
				}
			}
	}

	public static void removePluginHook(String hookName) {
		log.debug("Removing hook " + hookName);
		for (HookMap curList : hooks.values())
			synchronized (curList) {
				curList.remove(hookName);
			}
	}

	public static void removePluginHook(Hook hook) {
		for (HookMap curList : hooks.values())
			for (Map.Entry<String, Hook> curEntry : curList.entrySet())
				if (curEntry.getValue() == hook) {
					synchronized (curList) {
						log.debug("Removing command " + hook.getName());
						curList.remove(curEntry.getKey());
					}
					break;
				}
	}

	public static HookMap getHookMap(String event) {
		return hooks.get(event);
	}

	public static Collection<Hook> getList(String name) {
		return hooks.get(name).values();
	}

	public static ArrayList<String> getNames() {
		return new ArrayList<String>(hooks.keySet());
	}
}
