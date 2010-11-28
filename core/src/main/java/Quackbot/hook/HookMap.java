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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HookMap extends HashMap<String,Hook> {
	public boolean interupted, needsSuper;
	public List<Hook> ignored = new ArrayList<Hook>(), skipped;
	private Logger log = LoggerFactory.getLogger(HookMap.class);
	public String event;

	public HookMap(String event) {
		this.event = event;
	}

	public void execute(final Object... args) {
		execute(true, args);
	}

	public void execute(boolean runInThread, final Object... args) {
		if (size() == 0)
			//log.trace("Nothing to run, just exit");
			return;
		Runnable run = new Runnable() {
			@Override
			public void run() {
				executeThread(args);
			}
		};
		if (runInThread && Bot.getPoolLocal() != null)
			//log.trace("Going to submit event "+event+" to bot pool | Args: "+StringUtils.join(args,","));
			Bot.getPoolLocal().threadPool.submit(run);
		else if (runInThread)
			//log.trace("Going to submit  event "+event+ " to main pool | Args: "+StringUtils.join(args,","));
			Controller.mainPool.submit(run);
		else
			//log.trace("Simply running event "+event+" | Args: "+StringUtils.join(args,","));
			run.run();
	}

	public void executeThread(Object... args) {
		//Reset all class vars
		interupted = needsSuper = false;
		skipped = new ArrayList<Hook>();

		//Execute stack
		for (Hook hook : values())
			if (!ignored.contains(hook) && !skipped.contains(hook)) {

				Method curMethod = null;
				try {
					for (Method curMeth : hook.getClass().getMethods()) {
						curMethod = curMeth;
						if (curMethod.getName().equals(event)) {
							curMethod.setAccessible(true);
							curMethod.invoke(hook, args);
						}
					}
				} catch (IllegalArgumentException e) {
					log.error("Wrong number of params (" + event + ", " + hook.getName() + ") Method: " + curMethod.toString() + " | Args: " + StringUtils.join(args, ", "), e);
				} catch (Exception e) {
					log.error("Error encountered while executing hook " + hook.getName() + " for event " + event, e);
				}
			} else if (interupted)
				break;
	}

	public int getPosition(Hook myHook) {
		for (int i = 0; i <= size(); i++)
			if (values().toArray()[i].equals(myHook))
				return i;
		return -1;
	}

	public Hook getHookAt(int position) {
		return values().toArray(new Hook[0])[position];
	}

	public void skip(int position) {
		skip(getHookAt(position));
	}

	public void skip(Hook hook) {
		skipped.add(hook);
	}

	public void ignore(int position) {
		ignore(getHookAt(position));
	}

	public void ignore(Hook hook) {
		ignored.add(hook);
	}

	public void stopStack() {
		interupted = true;
	}
}
