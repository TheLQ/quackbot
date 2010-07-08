/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Quackbot.hook;

import Quackbot.Bot;
import Quackbot.PluginType;
import Quackbot.info.BotEvent;
import java.util.EnumMap;
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
 * corresponding {@link HookList}. Please see {@link HookList} for more
 * information. The the avalible events are defined in the {@link Event} enum.
 * <p>
 * HookManager is a singleton handling all Event operations. It holds the above
 * mentioned map and various methods for executing tasks.
 * @author LordQuackstar
 */
public class HookManager {
	protected static final EnumMap<Event, HookList<?, ?>> hookMap = new EnumMap<Event, HookList<?, ?>>(Event.class);
	private static Logger log = LoggerFactory.getLogger(HookManager.class);

	static {
		//Setup hookMap with values for all the avalible hooks
		for (Event curHook : Event.values())
			hookMap.put(curHook, new HookList());
	}

	/**
	 * This class is a Singleton and should not be initialized
	 */
	private HookManager() {
	}

	public static void addHook(Event hookType, PluginHook hook) {
		hookMap.get(hookType).add(hook);
	}

	public static void addPluginHook(Event[] events, final PluginType plugin) {
		for (Event curEvent : events)
			addHook(curEvent, new PluginHook() {
				public void run(HookList hookStack, Bot bot, BotEvent msgInfo) throws Exception {
					plugin.invoke(bot, msgInfo);
				}
			});
	}

	public static void removeHook(Event hookType, PluginHook hook) {
		hookMap.get(hookType).remove(hook);
	}

	public static HookList getEvent(Event hookType) {
		return hookMap.get(hookType);
	}

	public static <A,B> void executeEvent(Bot bot, BotEvent msgInfo) {
		if (msgInfo == null)
			throw new NullPointerException("msgInfo is null, must be set");
		try {
			hookMap.get(msgInfo.getEvent()).startStack(bot, msgInfo);
		} catch (Exception e) {
			log.error("Can't finish executing event " + msgInfo.getEvent().toString(), e);
		}
	}
}
