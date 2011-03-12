

package org.quackbot.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pircbotx.hooks.Event;
import org.quackbot.Controller;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class HookLoadEndEvent extends Event {
	private final Controller controller;
	
	public HookLoadEndEvent(Controller ctrl) {
		super(null);
		this.controller = ctrl;
	}
}